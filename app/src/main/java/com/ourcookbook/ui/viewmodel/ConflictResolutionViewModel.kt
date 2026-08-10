package com.ourcookbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ourcookbook.domain.model.ConflictResolution
import com.ourcookbook.domain.model.SyncConflict
import com.ourcookbook.domain.usecase.sync.GetConflictById
import com.ourcookbook.domain.usecase.sync.ResolveSyncConflict
import com.ourcookbook.domain.usecase.sync.GetConflictsByRecipe
import com.ourcookbook.domain.usecase.sync.UpdateConflict
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State for ConflictResolutionScreen
 */
sealed class ConflictResolutionState {
    object Loading : ConflictResolutionState()
    data class Success(
        val conflict: SyncConflict,
        val localRecipe: com.ourcookbook.domain.model.Recipe? = null,
        val remoteRecipe: com.ourcookbook.domain.model.Recipe? = null,
        val resolution: ConflictResolution? = null,
        val isResolving: Boolean = false
    ) : ConflictResolutionState()
    data class Error(val message: String) : ConflictResolutionState()
    object NotFound : ConflictResolutionState()
}

/**
 * Event for ConflictResolutionScreen
 */
sealed class ConflictResolutionEvent {
    data class LoadConflict(val conflictId: String) : ConflictResolutionEvent()
    data class SelectResolution(val resolution: ConflictResolution) : ConflictResolutionEvent()
    object ApplyResolution : ConflictResolutionEvent()
    object SkipConflict : ConflictResolutionEvent()
    object LoadNextConflict : ConflictResolutionEvent()
}

/**
 * Action for ConflictResolutionScreen
 */
sealed class ConflictResolutionAction {
    data class ShowResolutionConfirmation(val resolution: ConflictResolution) : ConflictResolutionAction()
    data class ShowError(val message: String) : ConflictResolutionAction()
    data class NavigateToNextConflict(val conflictId: String?) : ConflictResolutionAction()
    object NavigateBack : ConflictResolutionAction()
    object ShowSuccess : ConflictResolutionAction()
}

/**
 * ViewModel for ConflictResolutionScreen
 * Handles conflict detection, display, and resolution
 */
@HiltViewModel
class ConflictResolutionViewModel @Inject constructor(
    private val getConflictById: GetConflictById,
    private val resolveSyncConflict: ResolveSyncConflict,
    private val getConflictsByRecipe: GetConflictsByRecipe,
    private val updateConflict: UpdateConflict
) : ViewModel() {

    private val _state = MutableStateFlow<ConflictResolutionState>(ConflictResolutionState.Loading)
    val state: StateFlow<ConflictResolutionState> = _state.asStateFlow()

    private val _actions = MutableStateFlow<ConflictResolutionAction?>(null)
    val actions: StateFlow<ConflictResolutionAction?> = _actions.asStateFlow()

    private var currentConflictId: String = ""
    private var conflictQueue: List<String> = emptyList()
    private var currentIndex: Int = 0

    fun handleEvent(event: ConflictResolutionEvent) {
        when (event) {
            is ConflictResolutionEvent.LoadConflict -> loadConflict(event.conflictId)
            is ConflictResolutionEvent.SelectResolution -> selectResolution(event.resolution)
            is ConflictResolutionEvent.ApplyResolution -> applyResolution()
            is ConflictResolutionEvent.SkipConflict -> skipConflict()
            is ConflictResolutionEvent.LoadNextConflict -> loadNextConflict()
        }
    }

    private fun loadConflict(conflictId: String) {
        viewModelScope.launch {
            currentConflictId = conflictId
            _state.value = ConflictResolutionState.Loading
            
            try {
                val result = getConflictById(conflictId)
                result.onSuccess { conflict ->
                    if (conflict != null) {
                        // In production, we would load the actual local and remote recipes
                        // For now, create placeholder recipes based on conflict data
                        val localRecipe = createPlaceholderRecipe("Local Version", conflict.localRecipeId)
                        val remoteRecipe = createPlaceholderRecipe("Remote Version", conflict.remoteRecipeId)
                        
                        _state.value = ConflictResolutionState.Success(
                            conflict = conflict,
                            localRecipe = localRecipe,
                            remoteRecipe = remoteRecipe
                        )
                    } else {
                        _state.value = ConflictResolutionState.NotFound
                    }
                }.onFailure { e ->
                    _state.value = ConflictResolutionState.Error("Failed to load conflict: ${e.message}")
                }
                
            } catch (e: Exception) {
                _state.value = ConflictResolutionState.Error("Failed to load conflict: ${e.message}")
            }
        }
    }

    private fun createPlaceholderRecipe(title: String, recipeId: String): com.ourcookbook.domain.model.Recipe {
        return com.ourcookbook.domain.model.Recipe.create(
            title = title,
            category = "Mains",
            deviceId = "device_$recipeId"
        ).copy(id = recipeId)
    }

    private fun selectResolution(resolution: ConflictResolution) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is ConflictResolutionState.Success) {
                _state.value = currentState.copy(resolution = resolution)
                _actions.value = ConflictResolutionAction.ShowResolutionConfirmation(resolution)
            }
        }
    }

    private fun applyResolution() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is ConflictResolutionState.Success && currentState.resolution != null) {
                _state.value = currentState.copy(isResolving = true)
                
                try {
                    val result = resolveSyncConflict(currentState.conflict.id, currentState.resolution)
                    result.onSuccess {
                        _state.value = currentState.copy(isResolving = false)
                        _actions.value = ConflictResolutionAction.ShowSuccess
                        
                        // Move to next conflict after a delay
                        kotlinx.coroutines.delay(1500)
                        loadNextConflict()
                        
                    }.onFailure { e ->
                        _state.value = currentState.copy(isResolving = false)
                        _actions.value = ConflictResolutionAction.ShowError("Failed to apply resolution: ${e.message}")
                    }
                    
                } catch (e: Exception) {
                    _state.value = currentState.copy(isResolving = false)
                    _actions.value = ConflictResolutionAction.ShowError("Failed to apply resolution: ${e.message}")
                }
            }
        }
    }

    private fun skipConflict() {
        viewModelScope.launch {
            loadNextConflict()
        }
    }

    private fun loadNextConflict() {
        viewModelScope.launch {
            // In production, this would load the next conflict from the queue
            // For now, just navigate back
            _actions.value = ConflictResolutionAction.NavigateBack
        }
    }

    fun setConflictQueue(conflictIds: List<String>) {
        viewModelScope.launch {
            conflictQueue = conflictIds
            currentIndex = 0
            
            if (conflictIds.isNotEmpty()) {
                loadConflict(conflictIds.first())
            } else {
                _actions.value = ConflictResolutionAction.NavigateBack
            }
        }
    }

    fun loadNextInQueue() {
        viewModelScope.launch {
            if (currentIndex < conflictQueue.size - 1) {
                currentIndex++
                loadConflict(conflictQueue[currentIndex])
            } else {
                _actions.value = ConflictResolutionAction.NavigateToNextConflict(null)
            }
        }
    }

    fun clearAction() {
        viewModelScope.launch {
            _actions.value = null
        }
    }

    fun refresh() {
        if (currentConflictId.isNotBlank()) {
            loadConflict(currentConflictId)
        }
    }
}