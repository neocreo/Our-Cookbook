package com.ourcookbook.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.CookbookTypography

/**
 * Confirmation dialog with title, message, and action buttons
 */
@Composable
fun CookbookConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmButtonColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    dismissButtonColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    icon: ImageVector? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(CookbookSpacing.small))
                }
                Text(
                    text = title,
                    style = CookbookTypography.headlineSmall
                )
            }
        },
        text = {
            Text(
                text = message,
                style = CookbookTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = confirmButtonColor
                )
            ) {
                Text(
                    text = confirmText.uppercase(),
                    style = CookbookTypography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = dismissButtonColor
                )
            ) {
                Text(
                    text = dismissText.uppercase(),
                    style = CookbookTypography.labelLarge
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        shape = MaterialTheme.shapes.large
    )
}

/**
 * Delete confirmation dialog with warning styling
 */
@Composable
fun CookbookDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    CookbookConfirmationDialog(
        title = title,
        message = message,
        confirmText = "Delete",
        dismissText = "Cancel",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        confirmButtonColor = MaterialTheme.colorScheme.error,
        dismissButtonColor = MaterialTheme.colorScheme.onSurface,
        icon = Icons.Default.Delete
    )
}

/**
 * Info dialog for displaying information messages
 */
@Composable
fun CookbookInfoDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    icon: ImageVector = Icons.Default.Info
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(CookbookSpacing.small))
                Text(
                    text = title,
                    style = CookbookTypography.headlineSmall
                )
            }
        },
        text = {
            Text(
                text = message,
                style = CookbookTypography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "OK".uppercase(),
                    style = CookbookTypography.labelLarge
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    )
}

/**
 * Error dialog for displaying error messages
 */
@Composable
fun CookbookErrorDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(CookbookSpacing.small))
                Text(
                    text = title,
                    style = CookbookTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        text = {
            Text(
                text = message,
                style = CookbookTypography.bodyMedium
            )
        },
        confirmButton = {
            if (onRetry != null) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "CANCEL".uppercase(),
                            style = CookbookTypography.labelLarge
                        )
                    }
                    TextButton(
                        onClick = onRetry,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "RETRY".uppercase(),
                            style = CookbookTypography.labelLarge
                        )
                    }
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "OK".uppercase(),
                        style = CookbookTypography.labelLarge
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    )
}

/**
 * Loading dialog for long-running operations
 */
@Composable
fun CookbookLoadingDialog(
    title: String = "Loading",
    message: String = "Please wait...",
    onDismiss: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = { onDismiss?.invoke() },
        properties = DialogProperties(
            dismissOnBackPress = onDismiss != null,
            dismissOnClickOutside = onDismiss != null
        )
    ) {
        ElevatedCard(
            modifier = Modifier
                .padding(CookbookSpacing.medium)
                .fillMaxWidth(0.8f),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(CookbookSpacing.large)
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.medium))
                
                Text(
                    text = title,
                    style = CookbookTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.small))
                
                Text(
                    text = message,
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Custom dialog with full control over content
 */
@Composable
fun CookbookCustomDialog(
    onDismiss: () -> Unit,
    title: String? = null,
    content: @Composable () -> Unit,
    confirmText: String? = null,
    onConfirm: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        ElevatedCard(
            modifier = Modifier
                .padding(CookbookSpacing.medium)
                .fillMaxWidth(0.9f),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(CookbookSpacing.large)
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        style = CookbookTypography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(CookbookSpacing.medium))
                }
                
                content()
                
                if (confirmText != null && onConfirm != null) {
                    Spacer(modifier = Modifier.height(CookbookSpacing.medium))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = "CANCEL".uppercase(),
                                style = CookbookTypography.labelLarge
                            )
                        }
                        TextButton(
                            onClick = onConfirm,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = confirmText.uppercase(),
                                style = CookbookTypography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bottom sheet dialog for actions
 */
@Composable
fun CookbookActionSheet(
    title: String,
    onDismiss: () -> Unit,
    actions: List<ActionItem>
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        ElevatedCard(
            modifier = Modifier
                .padding(CookbookSpacing.medium)
                .fillMaxWidth(0.9f),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(CookbookSpacing.medium)
            ) {
                Text(
                    text = title,
                    style = CookbookTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.medium))
                
                actions.forEach { action ->
                    TextButton(
                        onClick = {
                            action.onClick()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (action.destructive) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            action.icon?.let {
                                Icon(
                                    imageVector = it,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(CookbookSpacing.small))
                            }
                            Text(
                                text = action.text,
                                style = CookbookTypography.bodyLarge,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
                }
            }
        }
    }
}

/**
 * Action item for action sheet
 */
data class ActionItem(
    val text: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val destructive: Boolean = false
)

/**
 * Preview for dialog components
 */
@Preview(showBackground = true)
@Composable
fun DialogsPreview() {
    MaterialTheme {
        // This preview shows the structure but dialogs need to be shown in context
        Column(
            modifier = Modifier.padding(CookbookSpacing.medium)
        ) {
            Text(
                text = "Dialog Previews",
                style = CookbookTypography.headlineMedium
            )
            Spacer(modifier = Modifier.height(CookbookSpacing.medium))
            Text(
                text = "Dialogs are best previewed in actual usage context",
                style = CookbookTypography.bodyMedium
            )
        }
    }
}
