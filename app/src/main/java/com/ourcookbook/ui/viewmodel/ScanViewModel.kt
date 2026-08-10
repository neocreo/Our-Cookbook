package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.usecase.recipe.CreateRecipe
import com.ourcookbook.domain.usecase.recipeimage.CreateRecipeImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * State for OCR Scanner Screen
 */
sealed class ScanState {
    object Idle : ScanState()
    object Scanning : ScanState()
    object Processing : ScanState()
    data class ScannedText(val text: String) : ScanState()
    data class ExtractedRecipe(val recipe: Recipe) : ScanState()
    data class Error(val message: String) : ScanState()
    data class ImageCaptured(val imagePath: String) : ScanState()
}

/**
 * Event for OCR Scanner Screen
 */
sealed class ScanEvent {
    object StartScan : ScanEvent()
    object StopScan : ScanEvent()
    data class ImageCaptured(val imagePath: String) : ScanEvent()
    data class TextExtracted(val text: String) : ScanEvent()
    object RetryScan : ScanEvent()
    object SaveRecipe : ScanEvent()
    object DiscardRecipe : ScanEvent()
    data class UpdateExtractedRecipe(val recipe: Recipe) : ScanEvent()
}

/**
 * Action for OCR Scanner Screen
 */
sealed class ScanAction {
    data class ShowCamera(val shouldStart: Boolean) : ScanAction()
    data class ShowExtractedText(val text: String) : ScanAction()
    data class ShowRecipePreview(val recipe: Recipe) : ScanAction()
    data class ShowRecipeEdit(val recipe: Recipe) : ScanAction()
    data class ShowError(val message: String) : ScanAction()
    data class NavigateToRecipeDetail(val recipeId: String) : ScanAction()
    object NavigateBack : ScanAction()
}

/**
 * ViewModel for OCR Scanner Screen
 * Handles OCR scanning, text extraction, and recipe creation from scanned text
 */
@HiltViewModel
class ScanViewModel @Inject constructor(
    private val createRecipe: CreateRecipe,
    private val createRecipeImage: CreateRecipeImage
) : ViewModel() {

    private val _state = MutableStateFlow<ScanState>(ScanState.Idle)
    val state: StateFlow<ScanState> = _state.asStateFlow()

    private val _actions = MutableStateFlow<ScanAction?>(null)
    val actions: StateFlow<ScanAction?> = _actions.asStateFlow()

    private var currentImagePath: String? = null
    private var currentExtractedText: String? = null
    private var currentRecipe: Recipe? = null

    fun handleEvent(event: ScanEvent) {
        when (event) {
            is ScanEvent.StartScan -> startScan()
            is ScanEvent.StopScan -> stopScan()
            is ScanEvent.ImageCaptured -> imageCaptured(event.imagePath)
            is ScanEvent.TextExtracted -> textExtracted(event.text)
            is ScanEvent.RetryScan -> retryScan()
            is ScanEvent.SaveRecipe -> saveRecipe()
            is ScanEvent.DiscardRecipe -> discardRecipe()
            is ScanEvent.UpdateExtractedRecipe -> updateExtractedRecipe(event.recipe)
        }
    }

    private fun startScan() {
        viewModelScope.launch {
            _state.value = ScanState.Scanning
            _actions.value = ScanAction.ShowCamera(shouldStart = true)
        }
    }

    private fun stopScan() {
        viewModelScope.launch {
            _state.value = ScanState.Idle
            _actions.value = ScanAction.ShowCamera(shouldStart = false)
        }
    }

    private fun imageCaptured(imagePath: String) {
        viewModelScope.launch {
            currentImagePath = imagePath
            _state.value = ScanState.ImageCaptured(imagePath)
            _state.value = ScanState.Processing
            
            // Simulate OCR processing
            // In production, this would call the actual OCR service
            simulateOCRProcessing(imagePath)
        }
    }

    private suspend fun simulateOCRProcessing(imagePath: String) {
        // Simulate OCR processing delay
        kotlinx.coroutines.delay(2000)
        
        // Simulate extracted text from a recipe card
        val sampleText = """
            Classic Chocolate Chip Cookies
            
            Ingredients:
            - 2 1/4 cups all-purpose flour
            - 1 tsp baking soda
            - 1 tsp salt
            - 1 cup butter, softened
            - 3/4 cup granulated sugar
            - 3/4 cup packed brown sugar
            - 1 tsp vanilla extract
            - 2 large eggs
            - 2 cups chocolate chips
            
            Instructions:
            1. Preheat oven to 375°F (190°C)
            2. Combine flour, baking soda and salt
            3. Beat butter, sugars, and vanilla until creamy
            4. Add eggs one at a time
            5. Gradually beat in flour mixture
            6. Stir in chocolate chips
            7. Drop by tablespoon onto baking sheets
            8. Bake for 9-11 minutes
            
            Makes about 3 dozen cookies
            Prep Time: 15 minutes
            Cook Time: 10 minutes
        """.trimIndent()
        
        textExtracted(sampleText)
    }

    private fun textExtracted(text: String) {
        viewModelScope.launch {
            currentExtractedText = text
            _state.value = ScanState.ScannedText(text)
            _actions.value = ScanAction.ShowExtractedText(text)
            
            // Parse the text into a recipe structure
            parseTextToRecipe(text)
        }
    }

    private suspend fun parseTextToRecipe(text: String) {
        // Simple parsing logic - in production this would be more sophisticated
        val recipe = try {
            val lines = text.split("\n")
            var title = ""
            val ingredients = mutableListOf<String>()
            val instructions = mutableListOf<String>()
            var category = "Desserts & Snacks"
            var servingSize: Int? = null
            var prepTime: Int? = null
            var cookTime: Int? = null
            
            var currentSection = ""
            
            for (line in lines) {
                val trimmedLine = line.trim()
                
                when {
                    trimmedLine.equals("Ingredients:", ignoreCase = true) -> {
                        currentSection = "ingredients"
                        continue
                    }
                    trimmedLine.equals("Instructions:", ignoreCase = true) -> {
                        currentSection = "instructions"
                        continue
                    }
                    trimmedLine.startsWith("Makes") || trimmedLine.startsWith("Serves") -> {
                        // Parse serving size
                        val match = Regex("(\d+)").find(trimmedLine)
                        match?.let { servingSize = it.value.toInt() }
                    }
                    trimmedLine.startsWith("Prep Time:") -> {
                        val match = Regex("(\d+)").find(trimmedLine)
                        match?.let { prepTime = it.value.toInt() }
                    }
                    trimmedLine.startsWith("Cook Time:") -> {
                        val match = Regex("(\d+)").find(trimmedLine)
                        match?.let { cookTime = it.value.toInt() }
                    }
                    currentSection == "ingredients" && trimmedLine.isNotBlank() && trimmedLine.firstOrNull() == '-' -> {
                        ingredients.add(trimmedLine.substring(1).trim())
                    }
                    currentSection == "instructions" && trimmedLine.isNotBlank() && trimmedLine.firstOrNull()?.isDigit() == true -> {
                        instructions.add(trimmedLine)
                    }
                }
                
                // Extract title from first non-empty line
                if (title.isBlank() && trimmedLine.isNotBlank() && 
                    !trimmedLine.equals("Ingredients:", ignoreCase = true) &&
                    !trimmedLine.equals("Instructions:", ignoreCase = true)) {
                    title = trimmedLine
                }
            }
            
            // Convert ingredients to Ingredient objects
            val ingredientObjects = ingredients.mapIndexed { index, ingredient ->
                com.ourcookbook.domain.model.Ingredient(
                    id = "",
                    name = ingredient,
                    quantity = null,
                    unit = null,
                    recipeId = "",
                    order = index
                )
            }
            
            Recipe.create(
                title = title.ifBlank { "Untitled Recipe" },
                category = category,
                ingredients = ingredientObjects,
                instructions = instructions.ifEmpty { listOf("Add instructions") },
                servingSize = servingSize,
                prepTime = prepTime,
                cookTime = cookTime,
                deviceId = "current_device_id"
            )
        } catch (e: Exception) {
            // If parsing fails, create a basic recipe with the text as description
            Recipe.create(
                title = "Scanned Recipe",
                category = "Mains",
                description = text,
                deviceId = "current_device_id"
            )
        }
        
        currentRecipe = recipe
        _state.value = ScanState.ExtractedRecipe(recipe)
        _actions.value = ScanAction.ShowRecipePreview(recipe)
    }

    private fun retryScan() {
        viewModelScope.launch {
            currentImagePath = null
            currentExtractedText = null
            currentRecipe = null
            _state.value = ScanState.Idle
            _actions.value = ScanAction.ShowCamera(shouldStart = true)
        }
    }

    private fun saveRecipe() {
        viewModelScope.launch {
            val recipe = currentRecipe ?: return@launch
            _state.value = ScanState.Processing
            
            try {
                val result = createRecipe(recipe)
                result.onSuccess { recipeId ->
                    // Save the image if we have one
                    currentImagePath?.let { imagePath ->
                        saveRecipeImage(recipeId, imagePath)
                    }
                    
                    _actions.value = ScanAction.NavigateToRecipeDetail(recipeId)
                }.onFailure { e ->
                    _state.value = ScanState.Error("Failed to save recipe: ${e.message}")
                    _actions.value = ScanAction.ShowError("Failed to save recipe: ${e.message}")
                }
            } catch (e: Exception) {
                _state.value = ScanState.Error("Failed to save recipe: ${e.message}")
                _actions.value = ScanAction.ShowError("Failed to save recipe: ${e.message}")
            }
        }
    }

    private suspend fun saveRecipeImage(recipeId: String, imagePath: String) {
        try {
            // In production, this would read the image file and upload it
            // For now, just create a placeholder
            val image = com.ourcookbook.domain.model.RecipeImage(
                id = "",
                recipeId = recipeId,
                imageUrl = imagePath,
                order = 0,
                checksum = ""
            )
            createRecipeImage(image)
        } catch (e: Exception) {
            // Image save failed, but recipe is still saved
        }
    }

    private fun discardRecipe() {
        viewModelScope.launch {
            currentImagePath = null
            currentExtractedText = null
            currentRecipe = null
            _state.value = ScanState.Idle
            _actions.value = ScanAction.NavigateBack
        }
    }

    private fun updateExtractedRecipe(recipe: Recipe) {
        viewModelScope.launch {
            currentRecipe = recipe
            _state.value = ScanState.ExtractedRecipe(recipe)
        }
    }

    fun clearAction() {
        viewModelScope.launch {
            _actions.value = null
        }
    }

    fun navigateToRecipeEdit() {
        viewModelScope.launch {
            currentRecipe?.let { recipe ->
                _actions.value = ScanAction.ShowRecipeEdit(recipe)
            }
        }
    }
}