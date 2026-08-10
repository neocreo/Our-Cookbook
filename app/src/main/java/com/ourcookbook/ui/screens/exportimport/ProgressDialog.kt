package com.ourcookbook.ui.screens.exportimport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ourcookbook.ui.components.*
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.CookbookTypography

/**
 * Progress Dialog for Export/Import Operations
 * Task 2.1.09: Export/Import Screen Implementation
 */

@Composable
fun ProgressDialog(
    title: String,
    message: String,
    progress: Float,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator(
                    progress = { if (progress >= 0) progress else null },
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(CookbookSpacing.small))
                
                Text(
                    text = title,
                    style = CookbookTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Linear progress indicator
                LinearProgressIndicator(
                    progress = { if (progress >= 0) progress else null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.medium))
                
                // Progress percentage
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = CookbookTypography.displayMedium.copy(
                        fontSize = MaterialTheme.typography.displaySmall.fontSize
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.small))
                
                // Status message
                Text(
                    text = message,
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.medium))
                
                // Progress details
                ProgressDetails(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text(
                    text = "Cancel",
                    style = CookbookTypography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
    )
}

@Composable
private fun ProgressDetails(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val processedItems = (progress * 100).toInt()
    val totalItems = 100
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        ProgressDetailItem(
            label = "Processed",
            value = processedItems.toString(),
            color = MaterialTheme.colorScheme.primary
        )
        
        ProgressDetailItem(
            label = "Total",
            value = totalItems.toString(),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        ProgressDetailItem(
            label = "Remaining",
            value = (totalItems - processedItems).toString(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ProgressDetailItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(CookbookSpacing.xSmall)
    ) {
        Text(
            text = value,
            style = CookbookTypography.titleSmall,
            color = color
        )
        
        Text(
            text = label,
            style = CookbookTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

// ==================== CIRCULAR PROGRESS DIALOG ====================

@Composable
fun CircularProgressDialog(
    title: String,
    message: String? = null,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = title,
                style = CookbookTypography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Large circular progress indicator
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.medium))
                
                // Message
                message?.let {
                    Text(
                        text = it,
                        style = CookbookTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text(
                    text = "Cancel",
                    style = CookbookTypography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
    )
}

// ==================== DETERMINATE PROGRESS DIALOG ====================

@Composable
fun DeterminateProgressDialog(
    title: String,
    current: Int,
    total: Int,
    message: String? = null,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (total > 0) current.toFloat() / total else 0f
    
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = title,
                style = CookbookTypography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Progress text
                Text(
                    text = "$current / $total",
                    style = CookbookTypography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.small))
                
                // Progress bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.medium))
                
                // Message
                message?.let {
                    Text(
                        text = it,
                        style = CookbookTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text(
                    text = "Cancel",
                    style = CookbookTypography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
    )
}

// ==================== MULTI-STEP PROGRESS DIALOG ====================

@Composable
fun MultiStepProgressDialog(
    title: String,
    currentStep: Int,
    totalSteps: Int,
    stepLabels: List<String>,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = title,
                style = CookbookTypography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Step progress
                Text(
                    text = "Step $currentStep of $totalSteps",
                    style = CookbookTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.medium))
                
                // Step indicators
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    stepLabels.forEachIndexed { index, label ->
                        StepIndicator(
                            stepNumber = index + 1,
                            label = label,
                            isActive = index + 1 == currentStep,
                            isCompleted = index + 1 < currentStep
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(CookbookSpacing.medium))
                
                // Progress bar
                LinearProgressIndicator(
                    progress = { if (totalSteps > 0) (currentStep - 1).toFloat() / (totalSteps - 1) else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text(
                    text = "Cancel",
                    style = CookbookTypography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
    )
}

@Composable
private fun StepIndicator(
    stepNumber: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(CookbookSpacing.xSmall)
    ) {
        // Step circle
        val circleColor = when {
            isActive -> MaterialTheme.colorScheme.primary
            isCompleted -> MaterialTheme.colorScheme.success
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .background(circleColor, CircleShape)
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.onSuccess,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = stepNumber.toString(),
                    style = CookbookTypography.labelLarge,
                    color = if (isActive) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
        
        // Step label
        Text(
            text = label,
            style = CookbookTypography.bodySmall,
            color = if (isActive || isCompleted) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            },
            textAlign = TextAlign.Center
        )
    }
}

// ==================== PREVIEW ====================

@Preview(showBackground = true)
@Composable
fun ProgressDialogPreview() {
    MaterialTheme {
        ProgressDialog(
            title = "Exporting Recipes",
            message = "Processing your recipes...",
            progress = 0.65f,
            onCancel = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CircularProgressDialogPreview() {
    MaterialTheme {
        CircularProgressDialog(
            title = "Processing",
            message = "Please wait while we process your request",
            onCancel = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DeterminateProgressDialogPreview() {
    MaterialTheme {
        DeterminateProgressDialog(
            title = "Importing Files",
            current = 7,
            total = 10,
            message = "Importing recipe 7 of 10",
            onCancel = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MultiStepProgressDialogPreview() {
    MaterialTheme {
        MultiStepProgressDialog(
            title = "Batch Export",
            currentStep = 2,
            totalSteps = 4,
            stepLabels = listOf("Preparing", "Exporting", "Saving", "Finalizing"),
            onCancel = {}
        )
    }
}
