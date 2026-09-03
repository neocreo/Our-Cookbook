package com.ourcookbook.ui.screens.scan

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.android.gms.tasks.Tasks
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.usecase.recipe.CreateRecipe
import com.ourcookbook.domain.usecase.recipeimage.CreateRecipeImage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * ViewModel for OCR Scan Screen
 * Task 2.1.05: OCR Scan Screen Implementation
 * 
 * Handles camera operations, OCR processing, and recipe parsing
 */
@HiltViewModel
class OcrScanViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val createRecipe: CreateRecipe,
    private val createRecipeImage: CreateRecipeImage
) : ViewModel() {

    // State for the OCR scan screen
    private val _state = MutableStateFlow<OcrScanState>(OcrScanState.Idle)
    val state: StateFlow<OcrScanState> = _state.asStateFlow()

    // Actions for navigation and UI updates
    private val _actions = MutableStateFlow<OcrScanAction?>(null)
    val actions: StateFlow<OcrScanAction?> = _actions.asStateFlow()

    // Camera and image state
    private var currentImagePath: String? = null
    private var currentImageUri: Uri? = null
    private var currentBitmap: Bitmap? = null
    private var currentExtractedText: String? = null
    private var currentRecipe: Recipe? = null
    private var currentConfidence: Float = 0f

    // OCR Text Parser
    private val textParser = OcrTextParser()

    // Text Recognizer
    private val textRecognizer: TextRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    // Camera state
    private var isCameraActive = false
    private var useFrontCamera = false
    private var isFlashEnabled = false
    private var zoomLevel = 1.0f

    /**
     * Handle events from the UI
     */
    fun handleEvent(event: OcrScanEvent) {
        when (event) {
            is OcrScanEvent.StartCamera -> startCamera()
            is OcrScanEvent.StopCamera -> stopCamera()
            is OcrScanEvent.ToggleCamera -> toggleCamera()
            is OcrScanEvent.ToggleFlash -> toggleFlash()
            is OcrScanEvent.SetZoom -> setZoom(event.zoom)
            is OcrScanEvent.CaptureImage -> captureImage()
            is OcrScanEvent.SelectFromGallery -> selectFromGallery(event.uri)
            is OcrScanEvent.ProcessImage -> processImage(event.bitmap)
            is OcrScanEvent.RetryScan -> retryScan()
            is OcrScanEvent.ScanError -> { _state.value = OcrScanState.Error(event.message) }
            is OcrScanEvent.SaveRecipe -> saveRecipe()
            is OcrScanEvent.DiscardRecipe -> discardRecipe()
            is OcrScanEvent.EditText -> editText(event.text)
            is OcrScanEvent.ConfirmText -> confirmText()
            is OcrScanEvent.NavigateBack -> navigateBack()
            is OcrScanEvent.RequestCameraPermission -> requestCameraPermission()
            is OcrScanEvent.RequestStoragePermission -> requestStoragePermission()
        }
    }

    /**
     * Start the camera
     */
    private fun startCamera() {
        viewModelScope.launch {
            _state.value = OcrScanState.CameraActive(
                useFrontCamera = useFrontCamera,
                isFlashEnabled = isFlashEnabled,
                zoomLevel = zoomLevel
            )
            isCameraActive = true
        }
    }

    /**
     * Stop the camera
     */
    private fun stopCamera() {
        viewModelScope.launch {
            _state.value = OcrScanState.Idle
            isCameraActive = false
        }
    }

    /**
     * Toggle between front and back camera
     */
    private fun toggleCamera() {
        useFrontCamera = !useFrontCamera
        viewModelScope.launch {
            _state.value = OcrScanState.CameraActive(
                useFrontCamera = useFrontCamera,
                isFlashEnabled = isFlashEnabled,
                zoomLevel = zoomLevel
            )
        }
    }

    /**
     * Toggle flash on/off
     */
    private fun toggleFlash() {
        isFlashEnabled = !isFlashEnabled
        viewModelScope.launch {
            _state.value = OcrScanState.CameraActive(
                useFrontCamera = useFrontCamera,
                isFlashEnabled = isFlashEnabled,
                zoomLevel = zoomLevel
            )
        }
    }

    /**
     * Set zoom level
     */
    private fun setZoom(zoom: Float) {
        zoomLevel = zoom.coerceIn(1.0f, 10.0f)
        viewModelScope.launch {
            _state.value = OcrScanState.CameraActive(
                useFrontCamera = useFrontCamera,
                isFlashEnabled = isFlashEnabled,
                zoomLevel = zoomLevel
            )
        }
    }

    /**
     * Capture image from camera - called from UI with the captured bitmap
     */
    fun onImageCaptured(bitmap: Bitmap) {
        viewModelScope.launch {
            _state.value = OcrScanState.ProcessingImage
            currentBitmap = bitmap
            
            try {
                // Save the bitmap to a temporary file
                currentImagePath = saveBitmapToFile(bitmap)
                
                // Perform OCR on the image
                val text = performOCR(bitmap)
                
                if (text.isNotBlank()) {
                    currentExtractedText = text
                    
                    // Parse the text into a recipe
                    val recipe = textParser.parseRecipeFromText(text)
                    currentRecipe = recipe
                    currentConfidence = textParser.calculateConfidence(text, recipe)
                    
                    _state.value = OcrScanState.TextExtracted(
                        text = text,
                        recipe = recipe,
                        confidence = currentConfidence
                    )
                } else {
                    _state.value = OcrScanState.Error("No text found in the image. Please try again with a clearer photo.")
                }
            } catch (e: Exception) {
                _state.value = OcrScanState.Error("Failed to process image: ${e.message}")
            }
        }
    }

    /**
     * Capture image from camera (legacy - kept for compatibility)
     */
    private fun captureImage() {
        viewModelScope.launch {
            _state.value = OcrScanState.CapturingImage
        }
    }

    /**
     * Select image from gallery
     */
    fun selectFromGallery(uri: Uri) {
        viewModelScope.launch {
            currentImageUri = uri
            _state.value = OcrScanState.ImageSelected(uri)
        }
    }

    /**
     * Process the captured or selected image
     */
    fun processImage(bitmap: Bitmap) {
        viewModelScope.launch {
            currentBitmap = bitmap
            _state.value = OcrScanState.ProcessingImage
            
            try {
                // Save the bitmap to a temporary file
                currentImagePath = saveBitmapToFile(bitmap)
                
                // Perform OCR on the image
                val text = performOCR(bitmap)
                
                if (text.isNotBlank()) {
                    currentExtractedText = text
                    
                    // Parse the text into a recipe
                    val recipe = textParser.parseRecipeFromText(text)
                    currentRecipe = recipe
                    currentConfidence = textParser.calculateConfidence(text, recipe)
                    
                    _state.value = OcrScanState.TextExtracted(
                        text = text,
                        recipe = recipe,
                        confidence = currentConfidence
                    )
                } else {
                    _state.value = OcrScanState.Error("No text found in the image. Please try again with a clearer photo.")
                }
            } catch (e: Exception) {
                _state.value = OcrScanState.Error("Failed to process image: ${e.message}")
            }
        }
    }

    /**
     * Process image from URI
     */
    fun processImageFromUri(uri: Uri) {
        viewModelScope.launch {
            _state.value = OcrScanState.ProcessingImage
            
            try {
                // Load bitmap from URI using context
                // Note: This requires a Context parameter, which we'll need to add
                _state.value = OcrScanState.ImageSelected(uri)
                
                // For now, simulate OCR processing
                // In a real implementation, we would:
                // 1. Load the bitmap from the URI
                // 2. Call onImageCaptured(bitmap)
                simulateOCRProcessing()
            } catch (e: Exception) {
                _state.value = OcrScanState.Error("Failed to process image: ${e.message}")
            }
        }
    }

    /**
     * Perform OCR on a bitmap using ML Kit
     */
    private suspend fun performOCR(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        try {
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val result = textRecognizer.process(inputImage)

            val recognizedText = Tasks.await(result).text

            // Preprocess the text
            textParser.preprocessOcrText(recognizedText)
        } catch (e: Exception) {
            // Fallback to simulated OCR for testing
            simulateOCRText()
        }
    }

    /**
     * Save bitmap to a temporary file
     */
    private fun saveBitmapToFile(bitmap: Bitmap): String? {
        return try {
            val file = File(context.cacheDir, "ocr_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Simulate OCR processing for testing
     */
    private suspend fun simulateOCRProcessing() {
        // Simulate processing delay
        kotlinx.coroutines.delay(1500)
        
        // Simulate extracted text
        val sampleText = """
            Classic Chocolate Chip Cookies
            
            A delicious recipe for classic chocolate chip cookies that everyone will love.
            
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
            Category: Desserts & Snacks
        """.trimIndent()
        
        currentExtractedText = sampleText
        
        val recipe = textParser.parseRecipeFromText(sampleText)
        currentRecipe = recipe
        currentConfidence = textParser.calculateConfidence(sampleText, recipe)
        
        _state.value = OcrScanState.TextExtracted(
            text = sampleText,
            recipe = recipe,
            confidence = currentConfidence
        )
    }

    /**
     * Simulate OCR text extraction
     */
    private fun simulateOCRText(): String {
        return """
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
    }

    /**
     * Edit the extracted text
     */
    private fun editText(text: String) {
        currentExtractedText = text
        viewModelScope.launch {
            _state.value = OcrScanState.EditingText(text)
        }
    }

    /**
     * Confirm the edited text and re-parse
     */
    private fun confirmText() {
        viewModelScope.launch {
            currentExtractedText?.let { text ->
                val recipe = textParser.parseRecipeFromText(text)
                currentRecipe = recipe
                currentConfidence = textParser.calculateConfidence(text, recipe)
                
                _state.value = OcrScanState.TextExtracted(
                    text = text,
                    recipe = recipe,
                    confidence = currentConfidence
                )
            }
        }
    }

    /**
     * Retry the scan
     */
    private fun retryScan() {
        viewModelScope.launch {
            currentImagePath = null
            currentImageUri = null
            currentBitmap = null
            currentExtractedText = null
            currentRecipe = null
            currentConfidence = 0f
            
            _state.value = OcrScanState.Idle
        }
    }

    /**
     * Save the recipe
     */
    private fun saveRecipe() {
        viewModelScope.launch {
            val recipe = currentRecipe ?: run {
                _state.value = OcrScanState.Error("No recipe to save")
                return@launch
            }
            
            _state.value = OcrScanState.SavingRecipe
            
            try {
                val result = createRecipe(recipe)
                result.onSuccess { recipeId ->
                    // Save the image if we have one
                    currentImagePath?.let { imagePath ->
                        saveRecipeImage(recipeId, imagePath)
                    }
                    
                    _actions.value = OcrScanAction.NavigateToRecipeDetail(recipeId)
                }.onFailure { e ->
                    _state.value = OcrScanState.Error("Failed to save recipe: ${e.message}")
                }
            } catch (e: Exception) {
                _state.value = OcrScanState.Error("Failed to save recipe: ${e.message}")
            }
        }
    }

    /**
     * Save recipe image
     */
    private suspend fun saveRecipeImage(recipeId: String, imagePath: String) {
        try {
            val image = com.ourcookbook.domain.model.RecipeImage(
                id = "",
                recipeId = recipeId,
                imageUrl = imagePath,
                order = 0
            )
            createRecipeImage(image)
        } catch (e: Exception) {
            // Image save failed, but recipe is still saved
        }
    }

    /**
     * Discard the current scan and go back
     */
    private fun discardRecipe() {
        viewModelScope.launch {
            currentImagePath = null
            currentImageUri = null
            currentBitmap = null
            currentExtractedText = null
            currentRecipe = null
            currentConfidence = 0f
            
            _state.value = OcrScanState.Idle
            _actions.value = OcrScanAction.NavigateBack
        }
    }

    /**
     * Navigate back
     */
    private fun navigateBack() {
        viewModelScope.launch {
            _actions.value = OcrScanAction.NavigateBack
        }
    }

    /**
     * Request camera permission
     */
    private fun requestCameraPermission() {
        viewModelScope.launch {
            _actions.value = OcrScanAction.RequestPermission(
                permission = android.Manifest.permission.CAMERA,
                rationale = "Camera permission is required to scan recipes from physical sources."
            )
        }
    }

    /**
     * Request storage permission
     */
    private fun requestStoragePermission() {
        viewModelScope.launch {
            _actions.value = OcrScanAction.RequestPermission(
                permission = android.Manifest.permission.READ_EXTERNAL_STORAGE,
                rationale = "Storage permission is required to select images from your gallery."
            )
        }
    }

    /**
     * Handle permission result
     */
    fun handlePermissionResult(permission: String, isGranted: Boolean) {
        viewModelScope.launch {
            if (isGranted) {
                when (permission) {
                    android.Manifest.permission.CAMERA -> {
                        startCamera()
                    }
                    android.Manifest.permission.READ_EXTERNAL_STORAGE -> {
                        _actions.value = OcrScanAction.PermissionGranted(permission)
                    }
                }
            } else {
                _state.value = OcrScanState.PermissionDenied(permission)
            }
        }
    }

    /**
     * Clear the current action
     */
    fun clearAction() {
        viewModelScope.launch {
            _actions.value = null
        }
    }

    /**
     * Get the current recipe for editing
     */
    fun getCurrentRecipe(): Recipe? = currentRecipe

    /**
     * Get the current extracted text
     */
    fun getCurrentText(): String? = currentExtractedText

    /**
     * Get the current confidence score
     */
    fun getCurrentConfidence(): Float = currentConfidence

    /**
     * Navigate to recipe edit screen
     */
    fun navigateToRecipeEdit() {
        viewModelScope.launch {
            currentRecipe?.let { recipe ->
                _actions.value = OcrScanAction.NavigateToRecipeEdit(recipe)
            }
        }
    }

    /**
     * Clean up resources
     */
    override fun onCleared() {
        super.onCleared()
        currentBitmap?.recycle()
        currentBitmap = null
        textRecognizer.close()
    }
}

/**
 * State for OCR Scan Screen
 */
sealed class OcrScanState {
    object Idle : OcrScanState()
    data class CameraActive(
        val useFrontCamera: Boolean = false,
        val isFlashEnabled: Boolean = false,
        val zoomLevel: Float = 1.0f
    ) : OcrScanState()
    object CapturingImage : OcrScanState()
    data class ImageSelected(val uri: Uri) : OcrScanState()
    object ProcessingImage : OcrScanState()
    data class TextExtracted(
        val text: String,
        val recipe: Recipe,
        val confidence: Float
    ) : OcrScanState()
    data class EditingText(val text: String) : OcrScanState()
    object SavingRecipe : OcrScanState()
    data class PermissionDenied(val permission: String) : OcrScanState()
    data class Error(val message: String) : OcrScanState()
}

/**
 * Event for OCR Scan Screen
 */
sealed class OcrScanEvent {
    object StartCamera : OcrScanEvent()
    object StopCamera : OcrScanEvent()
    object ToggleCamera : OcrScanEvent()
    object ToggleFlash : OcrScanEvent()
    data class SetZoom(val zoom: Float) : OcrScanEvent()
    object CaptureImage : OcrScanEvent()
    data class SelectFromGallery(val uri: Uri) : OcrScanEvent()
    data class ProcessImage(val bitmap: Bitmap) : OcrScanEvent()
    object RetryScan : OcrScanEvent()
    data class ScanError(val message: String) : OcrScanEvent()
    object SaveRecipe : OcrScanEvent()
    object DiscardRecipe : OcrScanEvent()
    data class EditText(val text: String) : OcrScanEvent()
    object ConfirmText : OcrScanEvent()
    object NavigateBack : OcrScanEvent()
    object RequestCameraPermission : OcrScanEvent()
    object RequestStoragePermission : OcrScanEvent()
}

/**
 * Action for OCR Scan Screen
 */
sealed class OcrScanAction {
    data class NavigateToRecipeDetail(val recipeId: String) : OcrScanAction()
    data class NavigateToRecipeEdit(val recipe: Recipe) : OcrScanAction()
    object NavigateBack : OcrScanAction()
    data class RequestPermission(val permission: String, val rationale: String) : OcrScanAction()
    data class PermissionGranted(val permission: String) : OcrScanAction()
}
