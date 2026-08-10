package com.ourcookbook.ui.screens.scan

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ourcookbook.domain.model.Ingredient
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.domain.usecase.recipe.CreateRecipe
import com.ourcookbook.domain.usecase.recipeimage.CreateRecipeImage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestWatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for OCR Scan Screen
 * Task 2.1.05: OCR Scan Screen Implementation
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class OcrScanScreenTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var viewModel: OcrScanViewModel
    private val mockCreateRecipe: CreateRecipe = mockk()
    private val mockCreateRecipeImage: CreateRecipeImage = mockk()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = OcrScanViewModel(mockCreateRecipe, mockCreateRecipeImage)
    }

    @Test
    fun `test initial state is Idle`() = runTest {
        val state = viewModel.state.value
        assertTrue(state is OcrScanState.Idle)
    }

    @Test
    fun `test start camera transitions to CameraActive state`() = runTest {
        viewModel.handleEvent(OcrScanEvent.StartCamera)
        
        val state = viewModel.state.value
        assertTrue(state is OcrScanState.CameraActive)
        val cameraState = state as OcrScanState.CameraActive
        assertEquals(false, cameraState.useFrontCamera)
        assertEquals(false, cameraState.isFlashEnabled)
        assertEquals(1.0f, cameraState.zoomLevel)
    }

    @Test
    fun `test toggle camera switches camera`() = runTest {
        viewModel.handleEvent(OcrScanEvent.StartCamera)
        viewModel.handleEvent(OcrScanEvent.ToggleCamera)
        
        val state = viewModel.state.value
        assertTrue(state is OcrScanState.CameraActive)
        val cameraState = state as OcrScanState.CameraActive
        assertEquals(true, cameraState.useFrontCamera)
    }

    @Test
    fun `test toggle flash switches flash`() = runTest {
        viewModel.handleEvent(OcrScanEvent.StartCamera)
        viewModel.handleEvent(OcrScanEvent.ToggleFlash)
        
        val state = viewModel.state.value
        assertTrue(state is OcrScanState.CameraActive)
        val cameraState = state as OcrScanState.CameraActive
        assertEquals(true, cameraState.isFlashEnabled)
    }

    @Test
    fun `test set zoom updates zoom level`() = runTest {
        viewModel.handleEvent(OcrScanEvent.StartCamera)
        viewModel.handleEvent(OcrScanEvent.SetZoom(2.5f))
        
        val state = viewModel.state.value
        assertTrue(state is OcrScanState.CameraActive)
        val cameraState = state as OcrScanState.CameraActive
        assertEquals(2.5f, cameraState.zoomLevel)
    }

    @Test
    fun `test zoom level is clamped between 1 and 10`() = runTest {
        viewModel.handleEvent(OcrScanEvent.StartCamera)
        
        // Test minimum zoom
        viewModel.handleEvent(OcrScanEvent.SetZoom(0.5f))
        var state = viewModel.state.value as OcrScanState.CameraActive
        assertEquals(1.0f, state.zoomLevel)
        
        // Test maximum zoom
        viewModel.handleEvent(OcrScanEvent.SetZoom(15f))
        state = viewModel.state.value as OcrScanState.CameraActive
        assertEquals(10.0f, state.zoomLevel)
    }

    @Test
    fun `test stop camera returns to Idle state`() = runTest {
        viewModel.handleEvent(OcrScanEvent.StartCamera)
        viewModel.handleEvent(OcrScanEvent.StopCamera)
        
        val state = viewModel.state.value
        assertTrue(state is OcrScanState.Idle)
    }

    @Test
    fun `test capture image transitions to CapturingImage state`() = runTest {
        viewModel.handleEvent(OcrScanEvent.StartCamera)
        viewModel.handleEvent(OcrScanEvent.CaptureImage)
        
        val state = viewModel.state.value
        assertTrue(state is OcrScanState.CapturingImage)
    }

    @Test
    fun `test select from gallery transitions to ImageSelected state`() = runTest {
        val mockUri: Uri = mockk()
        viewModel.handleEvent(OcrScanEvent.SelectFromGallery(mockUri))
        
        val state = viewModel.state.value
        assertTrue(state is OcrScanState.ImageSelected)
        val imageState = state as OcrScanState.ImageSelected
        assertEquals(mockUri, imageState.uri)
    }

    @Test
    fun `test process image from URI transitions to ProcessingImage state`() = runTest {
        val mockUri: Uri = mockk()
        viewModel.handleEvent(OcrScanEvent.SelectFromGallery(mockUri))
        viewModel.processImageFromUri(mockUri)
        
        // Wait for the simulated processing to complete
        coroutineTestRule.advanceTimeBy(2000)
        
        val state = viewModel.state.value
        // After processing, should be in TextExtracted state
        assertTrue(state is OcrScanState.TextExtracted)
    }

    @Test
    fun `test retry scan returns to Idle state`() = runTest {
        viewModel.handleEvent(OcrScanEvent.StartCamera)
        viewModel.handleEvent(OcrScanEvent.CaptureImage)
        viewModel.handleEvent(OcrScanEvent.RetryScan)
        
        val state = viewModel.state.value
        assertTrue(state is OcrScanState.Idle)
    }

    @Test
    fun `test navigate back action`() = runTest {
        viewModel.handleEvent(OcrScanEvent.NavigateBack)
        
        val action = viewModel.actions.value
        assertTrue(action is OcrScanAction.NavigateBack)
    }

    @Test
    fun `test request camera permission action`() = runTest {
        viewModel.handleEvent(OcrScanEvent.RequestCameraPermission)
        
        val action = viewModel.actions.value
        assertTrue(action is OcrScanAction.RequestPermission)
        val permissionAction = action as OcrScanAction.RequestPermission
        assertEquals(android.Manifest.permission.CAMERA, permissionAction.permission)
    }

    @Test
    fun `test request storage permission action`() = runTest {
        viewModel.handleEvent(OcrScanEvent.RequestStoragePermission)
        
        val action = viewModel.actions.value
        assertTrue(action is OcrScanAction.RequestPermission)
        val permissionAction = action as OcrScanAction.RequestPermission
        assertEquals(android.Manifest.permission.READ_EXTERNAL_STORAGE, permissionAction.permission)
    }

    @Test
    fun `test handle permission result granted`() = runTest {
        viewModel.handleEvent(OcrScanEvent.RequestCameraPermission)
        viewModel.handlePermissionResult(android.Manifest.permission.CAMERA, true)
        
        val state = viewModel.state.value
        assertTrue(state is OcrScanState.CameraActive)
    }

    @Test
    fun `test handle permission result denied`() = runTest {
        viewModel.handlePermissionResult(android.Manifest.permission.CAMERA, false)
        
        val state = viewModel.state.value
        assertTrue(state is OcrScanState.PermissionDenied)
        val permissionState = state as OcrScanState.PermissionDenied
        assertEquals(android.Manifest.permission.CAMERA, permissionState.permission)
    }

    @Test
    fun `test clear action sets action to null`() = runTest {
        viewModel.handleEvent(OcrScanEvent.NavigateBack)
        assertNotNull(viewModel.actions.value)
        
        viewModel.clearAction()
        assertEquals(null, viewModel.actions.value)
    }

    @Test
    fun `test OcrTextParser parses simple recipe`() {
        val parser = OcrTextParser()
        
        val text = """
            Chocolate Chip Cookies
            
            Ingredients:
            - 2 cups flour
            - 1 cup sugar
            
            Instructions:
            1. Mix ingredients
            2. Bake at 350°F
        """.trimIndent()
        
        val recipe = parser.parseRecipeFromText(text)
        
        assertEquals("Chocolate Chip Cookies", recipe.title)
        assertEquals(2, recipe.ingredients.size)
        assertEquals(2, recipe.instructions.size)
    }

    @Test
    fun `test OcrTextParser extracts metadata`() {
        val parser = OcrTextParser()
        
        val text = """
            Chocolate Chip Cookies
            
            Serves: 12
            Prep Time: 15 minutes
            Cook Time: 10 minutes
            
            Ingredients:
            - 2 cups flour
            
            Instructions:
            1. Mix ingredients
        """.trimIndent()
        
        val recipe = parser.parseRecipeFromText(text)
        
        assertEquals(12, recipe.servingSize)
        assertEquals(15, recipe.prepTime)
        assertEquals(10, recipe.cookTime)
        assertEquals(25, recipe.totalTime) // 15 + 10
    }

    @Test
    fun `test OcrTextParser handles various formats`() {
        val parser = OcrTextParser()
        
        val text = """
            Classic Lasagna
            Category: Mains
            
            Makes 8 servings
            Prep: 20 min
            Cook: 45 min
            
            Ingredient List:
            - 1 lb ground beef
            - 12 lasagna noodles
            - 2 cups cheese
            
            Directions:
            1. Brown the beef
            2. Layer ingredients
            3. Bake covered
        """.trimIndent()
        
        val recipe = parser.parseRecipeFromText(text)
        
        assertEquals("Classic Lasagna", recipe.title)
        assertEquals("Mains", recipe.category)
        assertEquals(8, recipe.servingSize)
        assertEquals(20, recipe.prepTime)
        assertEquals(45, recipe.cookTime)
        assertEquals(3, recipe.ingredients.size)
        assertEquals(3, recipe.instructions.size)
    }

    @Test
    fun `test OcrTextParser preprocesses text`() {
        val parser = OcrTextParser()
        
        val text = "Line 1\r\n\r\nLine 2\r\n\n\nLine 3"
        val processed = parser.preprocessOcrText(text)
        
        assertEquals("Line 1\n\nLine 2\n\nLine 3", processed)
    }

    @Test
    fun `test OcrTextParser calculates confidence`() {
        val parser = OcrTextParser()
        
        val text = """
            Test Recipe
            
            Ingredients:
            - 1 cup flour
            - 2 eggs
            
            Instructions:
            1. Mix
            2. Bake
        """.trimIndent()
        
        val recipe = parser.parseRecipeFromText(text)
        val confidence = parser.calculateConfidence(text, recipe)
        
        assertTrue(confidence > 0.5f)
    }

    @Test
    fun `test navigate to recipe edit`() = runTest {
        // First, simulate a successful scan
        val mockUri: Uri = mockk()
        viewModel.handleEvent(OcrScanEvent.SelectFromGallery(mockUri))
        viewModel.processImageFromUri(mockUri)
        
        // Wait for processing
        coroutineTestRule.advanceTimeBy(2000)
        
        // Now navigate to edit
        viewModel.navigateToRecipeEdit()
        
        val action = viewModel.actions.value
        assertTrue(action is OcrScanAction.NavigateToRecipeEdit)
    }

    @Test
    fun `test discard recipe clears state`() = runTest {
        // Simulate a scan
        val mockUri: Uri = mockk()
        viewModel.handleEvent(OcrScanEvent.SelectFromGallery(mockUri))
        viewModel.processImageFromUri(mockUri)
        
        // Wait for processing
        coroutineTestRule.advanceTimeBy(2000)
        
        // Discard
        viewModel.handleEvent(OcrScanEvent.DiscardRecipe)
        
        val state = viewModel.state.value
        assertTrue(state is OcrScanState.Idle)
        
        val action = viewModel.actions.value
        assertTrue(action is OcrScanAction.NavigateBack)
    }

    @Test
    fun `test edit text and confirm`() = runTest {
        // Simulate a scan
        val mockUri: Uri = mockk()
        viewModel.handleEvent(OcrScanEvent.SelectFromGallery(mockUri))
        viewModel.processImageFromUri(mockUri)
        
        // Wait for processing
        coroutineTestRule.advanceTimeBy(2000)
        
        // Get current text
        val currentText = viewModel.getCurrentText()
        assertNotNull(currentText)
        
        // Edit text
        val editedText = currentText + " Additional text"
        viewModel.handleEvent(OcrScanEvent.EditText(editedText))
        
        val state = viewModel.state.value
        assertTrue(state is OcrScanState.EditingText)
        
        // Confirm
        viewModel.handleEvent(OcrScanEvent.ConfirmText)
        
        // Should be back to TextExtracted state
        val newState = viewModel.state.value
        assertTrue(newState is OcrScanState.TextExtracted)
    }

    @Test
    fun `test save recipe with successful creation`() = runTest {
        // Mock successful recipe creation
        coEvery { mockCreateRecipe(any()) } returns Result.success("test-recipe-id")
        
        // Simulate a scan
        val mockUri: Uri = mockk()
        viewModel.handleEvent(OcrScanEvent.SelectFromGallery(mockUri))
        viewModel.processImageFromUri(mockUri)
        
        // Wait for processing
        coroutineTestRule.advanceTimeBy(2000)
        
        // Save recipe
        viewModel.handleEvent(OcrScanEvent.SaveRecipe)
        
        // Wait for save to complete
        coroutineTestRule.advanceTimeBy(1000)
        
        // Verify the action
        val action = viewModel.actions.value
        assertTrue(action is OcrScanAction.NavigateToRecipeDetail)
        
        // Verify the use case was called
        coVerify { mockCreateRecipe(any()) }
    }

    @Test
    fun `test save recipe with failed creation`() = runTest {
        // Mock failed recipe creation
        coEvery { mockCreateRecipe(any()) } returns Result.failure(Exception("Save failed"))
        
        // Simulate a scan
        val mockUri: Uri = mockk()
        viewModel.handleEvent(OcrScanEvent.SelectFromGallery(mockUri))
        viewModel.processImageFromUri(mockUri)
        
        // Wait for processing
        coroutineTestRule.advanceTimeBy(2000)
        
        // Save recipe
        viewModel.handleEvent(OcrScanEvent.SaveRecipe)
        
        // Wait for save to complete
        coroutineTestRule.advanceTimeBy(1000)
        
        // Should be in error state
        val state = viewModel.state.value
        assertTrue(state is OcrScanState.Error)
    }
}

/**
 * Test rule for coroutines
 */
@ExperimentalCoroutinesApi
class CoroutineTestRule : TestWatcher() {
    val testDispatcher = StandardTestDispatcher()

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }

    fun advanceTimeBy(millis: Long) {
        testDispatcher.advanceTimeBy(millis)
    }
}

/**
 * Helper function for creating test recipes
 */
fun createTestRecipe(
    title: String = "Test Recipe",
    category: String = "Mains",
    ingredients: List<Ingredient> = emptyList(),
    instructions: List<String> = emptyList()
): Recipe {
    return Recipe.create(
        title = title,
        category = category,
        ingredients = ingredients,
        instructions = instructions
    )
}