package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.Ingredient
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.usecase.recipe.CreateRecipe
import com.ourcookbook.domain.usecase.recipe.GetRecipeById
import com.ourcookbook.domain.usecase.recipe.UpdateRecipe
import com.ourcookbook.domain.usecase.ingredient.CreateIngredient
import com.ourcookbook.domain.usecase.ingredient.UpdateIngredient
import com.ourcookbook.domain.usecase.ingredient.DeleteIngredient
import com.ourcookbook.domain.usecase.ingredient.GetIngredientsByRecipe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * State for RecipeEditScreen (Create/Edit Recipe)
 */
data class RecipeEditState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val recipe: Recipe? = null,
    val ingredients: List<Ingredient> = emptyList(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
) {
    // Helper properties for UI
    val title: String get() = recipe?.title ?: ""
    val description: String? get() = recipe?.description
    val category: String get() = recipe?.category ?: ""
    val servingSize: Int? get() = recipe?.servingSize
    val prepTime: Int? get() = recipe?.prepTime
    val cookTime: Int? get() = recipe?.cookTime
    val notes: String? get() = recipe?.notes
    val source: String? get() = recipe?.source
    val tags: List<String> get() = recipe?.tags ?: emptyList()
    val instructions: List<String> get() = recipe?.instructions ?: emptyList()
    val imageUrl: String? get() = recipe?.imageUrl
    val isFavorite: Boolean get() = recipe?.isFavorite ?: false
    val isNewRecipe: Boolean get() = recipe?.id.isNullOrBlank()
}

/**
 * Event for RecipeEditScreen
 */
sealed class RecipeEditEvent {
    data class LoadRecipe(val recipeId: String?) : RecipeEditEvent()
    data class UpdateTitle(val title: String) : RecipeEditEvent()
    data class UpdateDescription(val description: String?) : RecipeEditEvent()
    data class UpdateCategory(val category: String) : RecipeEditEvent()
    data class UpdateServingSize(val servingSize: Int?) : RecipeEditEvent()
    data class UpdatePrepTime(val prepTime: Int?) : RecipeEditEvent()
    data class UpdateCookTime(val cookTime: Int?) : RecipeEditEvent()
    data class UpdateNotes(val notes: String?) : RecipeEditEvent()
    data class UpdateSource(val source: String?) : RecipeEditEvent()
    data class UpdateTags(val tags: List<String>) : RecipeEditEvent()
    data class UpdateInstructions(val instructions: List<String>) : RecipeEditEvent()
    data class UpdateImageUrl(val imageUrl: String?) : RecipeEditEvent()
    data class UpdateFavorite(val isFavorite: Boolean) : RecipeEditEvent()
    
    // Ingredient events
    data class AddIngredient(val ingredient: Ingredient) : RecipeEditEvent()
    data class UpdateIngredient(val ingredient: Ingredient) : RecipeEditEvent()
    data class DeleteIngredient(val ingredientId: String) : RecipeEditEvent()
    
    object SaveRecipe : RecipeEditEvent()
    object ValidateRecipe : RecipeEditEvent()
    object ClearError : RecipeEditEvent()
}

/**
 * Action for RecipeEditScreen
 */
sealed class RecipeEditAction {
    data class ShowValidationError(val errors: List<String>) : RecipeEditAction()
    data class ShowError(val message: String) : RecipeEditAction()
    data class NavigateToRecipeDetail(val recipeId: String) : RecipeEditAction()
    object NavigateBack : RecipeEditAction()
}

/**
 * ViewModel for RecipeEditScreen
 * Handles recipe creation and editing with ingredient management
 */
@HiltViewModel
class RecipeEditViewModel @Inject constructor(
    private val getRecipeById: GetRecipeById,
    private val createRecipe: CreateRecipe,
    private val updateRecipe: UpdateRecipe,
    private val createIngredient: CreateIngredient,
    private val updateIngredientUseCase: UpdateIngredient,
    private val deleteIngredientUseCase: DeleteIngredient,
    private val getIngredientsByRecipe: GetIngredientsByRecipe
) : ViewModel() {

    private val _state = MutableStateFlow(RecipeEditState())
    val state: StateFlow<RecipeEditState> = _state.asStateFlow()

    private val _actions = MutableStateFlow<RecipeEditAction?>(null)
    val actions: StateFlow<RecipeEditAction?> = _actions.asStateFlow()

    private var currentRecipeId: String? = null

    fun handleEvent(event: RecipeEditEvent) {
        when (event) {
            is RecipeEditEvent.LoadRecipe -> loadRecipe(event.recipeId)
            is RecipeEditEvent.UpdateTitle -> updateTitle(event.title)
            is RecipeEditEvent.UpdateDescription -> updateDescription(event.description)
            is RecipeEditEvent.UpdateCategory -> updateCategory(event.category)
            is RecipeEditEvent.UpdateServingSize -> updateServingSize(event.servingSize)
            is RecipeEditEvent.UpdatePrepTime -> updatePrepTime(event.prepTime)
            is RecipeEditEvent.UpdateCookTime -> updateCookTime(event.cookTime)
            is RecipeEditEvent.UpdateNotes -> updateNotes(event.notes)
            is RecipeEditEvent.UpdateSource -> updateSource(event.source)
            is RecipeEditEvent.UpdateTags -> updateTags(event.tags)
            is RecipeEditEvent.UpdateInstructions -> updateInstructions(event.instructions)
            is RecipeEditEvent.UpdateImageUrl -> updateImageUrl(event.imageUrl)
            is RecipeEditEvent.UpdateFavorite -> updateFavorite(event.isFavorite)
            is RecipeEditEvent.AddIngredient -> addIngredient(event.ingredient)
            is RecipeEditEvent.UpdateIngredient -> updateIngredient(event.ingredient)
            is RecipeEditEvent.DeleteIngredient -> deleteIngredient(event.ingredientId)
            is RecipeEditEvent.SaveRecipe -> saveRecipe()
            is RecipeEditEvent.ValidateRecipe -> validateRecipe()
            is RecipeEditEvent.ClearError -> clearError()
        }
    }

    private fun loadRecipe(recipeId: String?) {
        viewModelScope.launch {
            currentRecipeId = recipeId
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            if (recipeId == null) {
                // New recipe
                _state.value = _state.value.copy(
                    isLoading = false,
                    recipe = Recipe.create(
                        title = "",
                        category = "",
                        deviceId = "" // Will be set when saving
                    )
                )
                return@launch
            }

            try {
                val result = getRecipeById(recipeId)
                result.onSuccess { recipe ->
                    if (recipe != null) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            recipe = recipe
                        )
                        // Load ingredients for this recipe
                        loadIngredients(recipeId)
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Recipe not found"
                        )
                    }
                }.onFailure { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Failed to load recipe: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load recipe: ${e.message}"
                )
            }
        }
    }

    private suspend fun loadIngredients(recipeId: String) {
        try {
            val result = getIngredientsByRecipe(recipeId)
            result.onSuccess { ingredients ->
                _state.value = _state.value.copy(ingredients = ingredients)
            }
        } catch (e: Exception) {
            // Ingredients loading failed, but don't fail the whole screen
        }
    }

    private fun updateTitle(title: String) {
        _state.value = _state.value.copy(
            recipe = _state.value.recipe?.copy(title = title)
        )
    }

    private fun updateDescription(description: String?) {
        _state.value = _state.value.copy(
            recipe = _state.value.recipe?.copy(description = description)
        )
    }

    private fun updateCategory(category: String) {
        _state.value = _state.value.copy(
            recipe = _state.value.recipe?.copy(category = category)
        )
    }

    private fun updateServingSize(servingSize: Int?) {
        _state.value = _state.value.copy(
            recipe = _state.value.recipe?.copy(servingSize = servingSize)
        )
    }

    private fun updatePrepTime(prepTime: Int?) {
        _state.value = _state.value.copy(
            recipe = _state.value.recipe?.copy(prepTime = prepTime)
        )
    }

    private fun updateCookTime(cookTime: Int?) {
        _state.value = _state.value.copy(
            recipe = _state.value.recipe?.copy(cookTime = cookTime)
        )
    }

    private fun updateNotes(notes: String?) {
        _state.value = _state.value.copy(
            recipe = _state.value.recipe?.copy(notes = notes)
        )
    }

    private fun updateSource(source: String?) {
        _state.value = _state.value.copy(
            recipe = _state.value.recipe?.copy(source = source)
        )
    }

    private fun updateTags(tags: List<String>) {
        _state.value = _state.value.copy(
            recipe = _state.value.recipe?.copy(tags = tags)
        )
    }

    private fun updateInstructions(instructions: List<String>) {
        _state.value = _state.value.copy(
            recipe = _state.value.recipe?.copy(instructions = instructions)
        )
    }

    private fun updateImageUrl(imageUrl: String?) {
        _state.value = _state.value.copy(
            recipe = _state.value.recipe?.copy(imageUrl = imageUrl)
        )
    }

    private fun updateFavorite(isFavorite: Boolean) {
        _state.value = _state.value.copy(
            recipe = _state.value.recipe?.copy(isFavorite = isFavorite)
        )
    }

    private fun addIngredient(ingredient: Ingredient) {
        // Keep ingredients in memory only; they are persisted as part of the
        // recipe (ingredientsJson) when the recipe is saved. Persisting them
        // as separate rows here would violate the recipe foreign key for a
        // new, not-yet-saved recipe (SQLite error 19).
        _state.value = _state.value.copy(
            ingredients = _state.value.ingredients + ingredient
        )
    }

    private fun updateIngredient(ingredient: Ingredient) {
        _state.value = _state.value.copy(
            ingredients = _state.value.ingredients.map {
                if (it.id == ingredient.id) ingredient else it
            }
        )
    }

    private fun deleteIngredient(ingredientId: String) {
        _state.value = _state.value.copy(
            ingredients = _state.value.ingredients.filter { it.id != ingredientId }
        )
    }

    private fun validateRecipe() {
        viewModelScope.launch {
            val currentState = _state.value
            val recipe = currentState.recipe ?: return@launch
            
            val errors = mutableListOf<String>()
            
            if (recipe.title.isBlank()) {
                errors.add("Title is required")
            }
            
            if (recipe.category.isBlank()) {
                errors.add("Category is required")
            }
            
            if (recipe.ingredients.isEmpty()) {
                errors.add("At least one ingredient is required")
            }
            
            if (recipe.instructions.isEmpty()) {
                errors.add("At least one instruction is required")
            }
            
            if (errors.isNotEmpty()) {
                _actions.value = RecipeEditAction.ShowValidationError(errors)
            }
        }
    }

    private fun saveRecipe() {
        viewModelScope.launch {
            val currentState = _state.value
            val recipe = currentState.recipe ?: return@launch
            
            // Validate first
            validateRecipe()
            
            if (currentState.error != null) {
                return@launch
            }

            _state.value = currentState.copy(isSaving = true, error = null)
            
            try {
                val recipeToSave = recipe.copy(
                    ingredients = currentState.ingredients,
                    deviceId = "current_device_id" // Will be set properly in production
                )
                
                val result = if (recipeToSave.id.isBlank()) {
                    // New recipe
                    createRecipe(recipeToSave)
                } else {
                    // Existing recipe
                    updateRecipe(recipeToSave)
                    Result.success(recipeToSave.id)
                }
                
                result.onSuccess { recipeId ->
                    _state.value = currentState.copy(
                        isSaving = false,
                        saveSuccess = true
                    )
                    _actions.value = RecipeEditAction.NavigateToRecipeDetail(recipeId)
                }.onFailure { e ->
                    _state.value = currentState.copy(
                        isSaving = false,
                        error = "Failed to save recipe: ${e.message}"
                    )
                    _actions.value = RecipeEditAction.ShowError("Failed to save recipe: ${e.message}")
                }
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isSaving = false,
                    error = "Failed to save recipe: ${e.message}"
                )
                _actions.value = RecipeEditAction.ShowError("Failed to save recipe: ${e.message}")
            }
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearAction() {
        viewModelScope.launch {
            _actions.value = null
        }
    }

    fun resetSaveSuccess() {
        _state.value = _state.value.copy(saveSuccess = false)
    }
}