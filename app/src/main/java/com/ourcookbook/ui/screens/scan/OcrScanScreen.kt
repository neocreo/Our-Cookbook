package com.ourcookbook.ui.screens.scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import java.nio.ByteBuffer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.ourcookbook.ui.components.CookbookIconButton
import com.ourcookbook.ui.components.CookbookPrimaryButton
import com.ourcookbook.ui.components.CookbookSecondaryButton
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookTheme
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executor
import android.graphics.BitmapFactory

/**
 * OCR Scan Screen
 * Task 2.1.05: OCR Scan Screen Implementation
 * 
 * Main screen for scanning recipes using camera or gallery images
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun OcrScanScreen(
    viewModel: OcrScanViewModel = viewModel(),
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val actions by viewModel.actions.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Permission states
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val storagePermissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE)

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.selectFromGallery(it) }
        }
    )

    // Handle navigation actions from ViewModel
    actions?.let { action ->
        when (action) {
            is OcrScanAction.NavigateToRecipeDetail -> {
                navController.navigate(Route.recipeDetail(action.recipeId)) {
                    popUpTo(Route.OCR_SCANNER) { inclusive = true }
                }
                viewModel.clearAction()
            }
            is OcrScanAction.NavigateToRecipeEdit -> {
                // Navigate to recipe edit with the parsed recipe
                // This would require a way to pass the recipe to the edit screen
                // For now, we'll just navigate to the create screen
                navController.navigate(Route.RECIPE_CREATE) {
                    popUpTo(Route.OCR_SCANNER) { inclusive = true }
                }
                viewModel.clearAction()
            }
            is OcrScanAction.NavigateBack -> {
                navController.popBackStack()
                viewModel.clearAction()
            }
            is OcrScanAction.RequestPermission -> {
                when (action.permission) {
                    Manifest.permission.CAMERA -> {
                        cameraPermissionState.launchPermissionRequest()
                    }
                    Manifest.permission.READ_EXTERNAL_STORAGE -> {
                        storagePermissionState.launchPermissionRequest()
                    }
                }
                viewModel.clearAction()
            }
            is OcrScanAction.PermissionGranted -> {
                // Permission granted, proceed with the action
                viewModel.clearAction()
            }
            else -> {}
        }
    }

    // Handle permission results
    LaunchedEffect(cameraPermissionState.status, storagePermissionState.status) {
        if (cameraPermissionState.status.isGranted) {
            viewModel.handlePermissionResult(Manifest.permission.CAMERA, true)
        } else if (cameraPermissionState.status.shouldShowRationale) {
            // Show rationale
        }

        if (storagePermissionState.status.isGranted) {
            viewModel.handlePermissionResult(Manifest.permission.READ_EXTERNAL_STORAGE, true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Recipe") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.handleEvent(OcrScanEvent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = state) {
                is OcrScanState.Idle -> {
                    ScannerIdleContent(
                        onStartCamera = {
                            if (cameraPermissionState.status.isGranted) {
                                viewModel.handleEvent(OcrScanEvent.StartCamera)
                            } else {
                                viewModel.handleEvent(OcrScanEvent.RequestCameraPermission)
                            }
                        },
                        onSelectFromGallery = {
                            if (storagePermissionState.status.isGranted) {
                                imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            } else {
                                viewModel.handleEvent(OcrScanEvent.RequestStoragePermission)
                            }
                        }
                    )
                }
                is OcrScanState.CameraActive -> {
                    CameraPreviewContent(
                        useFrontCamera = currentState.useFrontCamera,
                        isFlashEnabled = currentState.isFlashEnabled,
                        zoomLevel = currentState.zoomLevel,
                        onToggleCamera = { viewModel.handleEvent(OcrScanEvent.ToggleCamera) },
                        onToggleFlash = { viewModel.handleEvent(OcrScanEvent.ToggleFlash) },
                        onCapture = { viewModel.handleEvent(OcrScanEvent.CaptureImage) },
                        onSelectFromGallery = {
                            if (storagePermissionState.status.isGranted) {
                                imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            } else {
                                viewModel.handleEvent(OcrScanEvent.RequestStoragePermission)
                            }
                        },
                        onZoomChange = { zoom -> viewModel.handleEvent(OcrScanEvent.SetZoom(zoom)) }
                    )
                }
                is OcrScanState.CapturingImage -> {
                    CapturingImageContent()
                }
                is OcrScanState.ImageSelected -> {
                    ImagePreviewContent(
                        uri = currentState.uri,
                        onProcess = { viewModel.processImageFromUri(currentState.uri) },
                        onRetry = { viewModel.handleEvent(OcrScanEvent.RetryScan) }
                    )
                }
                is OcrScanState.ProcessingImage -> {
                    ProcessingContent()
                }
                is OcrScanState.TextExtracted -> {
                    TextExtractedContent(
                        text = currentState.text,
                        recipe = currentState.recipe,
                        confidence = currentState.confidence,
                        onEdit = { viewModel.handleEvent(OcrScanEvent.EditText(currentState.text)) },
                        onSave = { viewModel.handleEvent(OcrScanEvent.SaveRecipe) },
                        onDiscard = { viewModel.handleEvent(OcrScanEvent.DiscardRecipe) },
                        onRetry = { viewModel.handleEvent(OcrScanEvent.RetryScan) }
                    )
                }
                is OcrScanState.EditingText -> {
                    TextEditingContent(
                        text = currentState.text,
                        onTextChange = { newText -> viewModel.handleEvent(OcrScanEvent.EditText(newText)) },
                        onConfirm = { viewModel.handleEvent(OcrScanEvent.ConfirmText) },
                        onCancel = { viewModel.handleEvent(OcrScanEvent.RetryScan) }
                    )
                }
                is OcrScanState.SavingRecipe -> {
                    SavingContent()
                }
                is OcrScanState.PermissionDenied -> {
                    PermissionDeniedContent(
                        permission = currentState.permission,
                        onRetry = {
                            when (currentState.permission) {
                                Manifest.permission.CAMERA -> viewModel.handleEvent(OcrScanEvent.RequestCameraPermission)
                                Manifest.permission.READ_EXTERNAL_STORAGE -> viewModel.handleEvent(OcrScanEvent.RequestStoragePermission)
                                else -> viewModel.handleEvent(OcrScanEvent.RetryScan)
                            }
                        },
                        onBack = { viewModel.handleEvent(OcrScanEvent.NavigateBack) }
                    )
                }
                is OcrScanState.Error -> {
                    ErrorContent(
                        message = currentState.message,
                        onRetry = { viewModel.handleEvent(OcrScanEvent.RetryScan) },
                        onBack = { viewModel.handleEvent(OcrScanEvent.NavigateBack) }
                    )
                }
            }
        }
    }
}

/**
 * Camera Preview Content
 */
@Composable
fun CameraPreviewContent(
    useFrontCamera: Boolean,
    isFlashEnabled: Boolean,
    zoomLevel: Float,
    onToggleCamera: () -> Unit,
    onToggleFlash: () -> Unit,
    onCapture: () -> Unit,
    onSelectFromGallery: () -> Unit,
    onZoomChange: (Float) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var preview by remember { mutableStateOf<CameraPreview?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }

    // Transformable state for pinch-to-zoom
    val zoomState = rememberTransformableState { zoomChange, _, _ ->
        val newZoom = (zoomLevel * zoomChange).coerceIn(1.0f, 10.0f)
        onZoomChange(newZoom)
    }

    LaunchedEffect(useFrontCamera) {
        cameraSelector = if (useFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    cameraProvider = cameraProviderFuture.get()

                    // Unbind all use cases
                    cameraProvider?.unbindAll()

                    // Create preview
                    preview = CameraPreview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    // Create image capture
                    imageCapture = ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                    // Bind to lifecycle
                    cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture

                    )
                }, ContextCompat.getMainExecutor(context))
                previewView
            },
            modifier = Modifier
                .fillMaxSize()
                .transformable(zoomState)
        )

        // Camera Controls Overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // Top controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Flash toggle
                CookbookIconButton(
                    icon = if (isFlashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    onClick = onToggleFlash,
                    contentDescription = if (isFlashEnabled) "Turn off flash" else "Turn on flash",
                    tint = if (isFlashEnabled) MaterialTheme.colorScheme.primary else Color.White
                )

                // Camera switch
                CookbookIconButton(
                    icon = Icons.Default.SwapHoriz,
                    onClick = onToggleCamera,
                    contentDescription = "Switch camera",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Center controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                // Gallery picker
                CookbookIconButton(
                    icon = Icons.Default.Image,
                    onClick = onSelectFromGallery,
                    contentDescription = "Select from gallery",
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.width(32.dp))

                // Capture button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .border(
                            width = 4.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            onCapture()
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Capture image",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(32.dp))

                // Placeholder for future controls
                Spacer(modifier = Modifier.width(64.dp))
            }
        }

        // Zoom indicator
        if (zoomLevel > 1.0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${"%.1f".format(zoomLevel)}x",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Scanner Idle Content
 */
@Composable
fun ScannerIdleContent(
    onStartCamera: () -> Unit,
    onSelectFromGallery: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = "Camera",
            modifier = Modifier.height(128.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Scan a Recipe",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Point your camera at a recipe card or printed recipe to automatically extract the text",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        CookbookPrimaryButton(
            text = "Start Scanning",
            onClick = onStartCamera,
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        CookbookSecondaryButton(
            text = "Select from Gallery",
            onClick = onSelectFromGallery,
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Or paste text manually",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Capturing Image Content
 */
@Composable
fun CapturingImageContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoadingState()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Capturing image...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Image Preview Content
 */
@Composable
fun ImagePreviewContent(
    uri: Uri,
    onProcess: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image preview
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Selected image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Image selected successfully!",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Processing the image to extract recipe text...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            CookbookSecondaryButton(
                text = "Retry",
                onClick = onRetry,
                modifier = Modifier.width(150.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            CookbookPrimaryButton(
                text = "Process",
                onClick = onProcess,
                modifier = Modifier.width(150.dp)
            )
        }
    }
}

/**
 * Processing Content
 */
@Composable
fun ProcessingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Processing image...",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "Extracting text from the captured image",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(0.8f),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Text Extracted Content
 */
@Composable
fun TextExtractedContent(
    text: String,
    recipe: com.ourcookbook.domain.model.Recipe,
    confidence: Float,
    onEdit: () -> Unit,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.TextFields,
                contentDescription = "Extracted Text",
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Recipe Extracted",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.weight(1f))

            // Confidence indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Confidence",
                    tint = when {
                        confidence > 0.8f -> MaterialTheme.colorScheme.primary
                        confidence > 0.6f -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "${(confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        confidence > 0.8f -> MaterialTheme.colorScheme.primary
                        confidence > 0.6f -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }

        Divider()

        // Recipe Preview
        RecipePreviewCard(recipe = recipe)

        // Confidence message
        when {
            confidence > 0.8f -> {
                Text(
                    text = "High confidence - Recipe looks complete!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            confidence > 0.6f -> {
                Text(
                    text = "Medium confidence - Please review the extracted information",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            else -> {
                Text(
                    text = "Low confidence - Please edit the text before saving",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit")
            }

            OutlinedButton(
                onClick = onDiscard,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Discard"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Discard")
            }
        }

        CookbookPrimaryButton(
            text = "Save Recipe",
            onClick = onSave,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Raw text section (collapsible)
        var showRawText by remember { mutableStateOf(false) }
        
        Column {
            TextButton(
                onClick = { showRawText = !showRawText }
            ) {
                Text(
                    text = if (showRawText) "Hide Raw Text" else "Show Raw Text",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (showRawText) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}

/**
 * Text Editing Content
 */
@Composable
fun TextEditingContent(
    text: String,
    onTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    var editedText by remember { mutableStateOf(text) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Edit Extracted Text",
            style = MaterialTheme.typography.headlineSmall
        )

        Divider()

        // Text editing area
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            BasicTextField(
                value = editedText,
                onValueChange = { newText ->
                    editedText = newText
                    onTextChange(newText)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp),
                textStyle = MaterialTheme.typography.bodyMedium,
                decorationBox = { innerTextField ->
                    if (editedText.isEmpty()) {
                        Text(
                            text = "Enter or edit the recipe text here...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CookbookSecondaryButton(
                text = "Cancel",
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            )

            CookbookPrimaryButton(
                text = "Confirm",
                onClick = onConfirm,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Recipe Preview Card
 */
@Composable
fun RecipePreviewCard(recipe: com.ourcookbook.domain.model.Recipe) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Category
            if (!recipe.category.isNullOrBlank()) {
                Text(
                    text = "Category: ${recipe.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Metadata
            val metadataParts = mutableListOf<String>()
            recipe.servingSize?.let { metadataParts.add("Serves: $it") }
            recipe.prepTime?.let { metadataParts.add("Prep: ${it}min") }
            recipe.cookTime?.let { metadataParts.add("Cook: ${it}min") }
            recipe.totalTime?.let { metadataParts.add("Total: ${it}min") }

            if (metadataParts.isNotEmpty()) {
                Text(
                    text = metadataParts.joinToString(" | "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Divider()

            // Description
            if (!recipe.description.isNullOrBlank()) {
                Text(
                    text = recipe.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Divider()
            }

            // Ingredients
            if (recipe.ingredients.isNotEmpty()) {
                Text(
                    text = "Ingredients:",
                    style = MaterialTheme.typography.titleSmall
                )

                recipe.ingredients.forEach { ingredient ->
                    Text(
                        text = "• ${ingredient.displayString}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Divider()
            }

            // Instructions
            if (recipe.instructions.isNotEmpty()) {
                Text(
                    text = "Instructions:",
                    style = MaterialTheme.typography.titleSmall
                )

                recipe.instructions.forEachIndexed { index, instruction ->
                    Text(
                        text = "${index + 1}. $instruction",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Saving Content
 */
@Composable
fun SavingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Saving recipe...",
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "Please wait while we save your recipe",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * Permission Denied Content
 */
@Composable
fun PermissionDeniedContent(
    permission: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Permission Denied",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (permission) {
                Manifest.permission.CAMERA -> "Camera permission is required to scan recipes."
                Manifest.permission.READ_EXTERNAL_STORAGE -> "Storage permission is required to select images from your gallery."
                else -> "Permission is required for this feature."
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        CookbookPrimaryButton(
            text = "Request Permission",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        CookbookSecondaryButton(
            text = "Back",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}

/**
 * Error Content
 */
@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Scanning Error",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        CookbookPrimaryButton(
            text = "Retry",
            onClick = onRetry
        )

        Spacer(modifier = Modifier.height(8.dp))

        CookbookSecondaryButton(
            text = "Back",
            onClick = onBack
        )
    }
}

/**
 * Text Button for the show/hide raw text
 */
@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        content()
    }
}

/**
 * Preview for OCR Scan Screen
 */
@Preview(showBackground = true)
@Composable
fun OcrScanScreenPreview() {
    CookbookTheme {
        OcrScanScreen(
            navController = rememberNavController()
        )
    }
}

/**
 * Preview for Camera Preview Content
 */
@Preview(showBackground = true)
@Composable
fun CameraPreviewContentPreview() {
    CookbookTheme {
        CameraPreviewContent(
            useFrontCamera = false,
            isFlashEnabled = false,
            zoomLevel = 1.0f,
            onToggleCamera = {},
            onToggleFlash = {},
            onCapture = {},
            onSelectFromGallery = {},
            onZoomChange = {}
        )
    }
}

/**
 * Preview for Text Extracted Content
 */
@Preview(showBackground = true)
@Composable
fun TextExtractedContentPreview() {
    val sampleRecipe = com.ourcookbook.domain.model.Recipe.create(
        title = "Classic Chocolate Chip Cookies",
        category = "Desserts & Snacks",
        ingredients = listOf(
            com.ourcookbook.domain.model.Ingredient.create(
                name = "All-purpose flour",
                amount = "2 1/4",
                unit = "cups"
            ),
            com.ourcookbook.domain.model.Ingredient.create(
                name = "Baking soda",
                amount = "1",
                unit = "tsp"
            ),
            com.ourcookbook.domain.model.Ingredient.create(
                name = "Butter",
                amount = "1",
                unit = "cup"
            )
        ),
        instructions = listOf(
            "Preheat oven to 375°F (190°C)",
            "Combine flour, baking soda and salt",
            "Beat butter, sugars, and vanilla until creamy"
        ),
        servingSize = 36,
        prepTime = 15,
        cookTime = 10
    )

    CookbookTheme {
        TextExtractedContent(
            text = "Classic Chocolate Chip Cookies...",
            recipe = sampleRecipe,
            confidence = 0.95f,
            onEdit = {},
            onSave = {},
            onDiscard = {},
            onRetry = {}
        )
    }
}

/**
 * Capture photo using CameraX and pass to ViewModel
 */
private fun capturePhoto(
    imageCapture: ImageCapture?,
    viewModel: OcrScanViewModel,
    context: Context,
    onCapture: () -> Unit
) {
    onCapture()
    imageCapture?.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                super.onCaptureSuccess(imageProxy)

                try {
                    // Convert ImageProxy to Bitmap — the built-in toBitmap()
                    // handles rotation correctly. Wrap in try/catch because
                    // decodeByteArray can return null for malformed data.
                    val bitmap = imageProxy.toBitmap()

                    if (bitmap != null) {
                        viewModel.onImageCaptured(bitmap)
                    } else {
                        viewModel.handleEvent(OcrScanEvent.ScanError("Failed to decode captured image"))
                    }
                } catch (e: Exception) {
                    viewModel.handleEvent(OcrScanEvent.ScanError("Camera capture failed: ${e.message}"))
                } finally {
                    imageProxy.close()
                }
            }
            
            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                // Handle error
            }
        }
    )
}

// Note: ImageProxy.toBitmap() is provided by CameraX 1.3.0 and handles
// rotation correctly. The custom extension that was here shadowed it
// and decoded raw JPEG bytes without rotation, which could crash on
// certain devices. Rely on the built-in extension instead.
