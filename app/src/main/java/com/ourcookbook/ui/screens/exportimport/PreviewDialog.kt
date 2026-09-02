package com.ourcookbook.ui.screens.exportimport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ourcookbook.domain.model.*
import com.ourcookbook.ui.components.*
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.CookbookTypography
import com.ourcookbook.ui.theme.getStatusColor

/**
 * Preview Dialog for Import
 * Task 2.1.09: Export/Import Screen Implementation
 */

@Composable
fun PreviewDialog(
    preview: ExportImportPreview,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveRedEye,
                    contentDescription = "Preview",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(CookbookSpacing.small))
                
                Text(
                    text = "Import Preview",
                    style = CookbookTypography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Summary
                PreviewSummary(
                    preview = preview,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.medium))
                
                // Items list
                Text(
                    text = "Items to Import:",
                    style = CookbookTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.small))
                
                // Scrollable list of items
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    contentPadding = PaddingValues(vertical = CookbookSpacing.small)
                ) {
                    items(preview.items) { item ->
                        PreviewItemCard(
                            item = item,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
                    }
                }
                
                Spacer(modifier = Modifier.height(CookbookSpacing.medium))
                
                // Metadata
                PreviewMetadata(
                    preview = preview,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Confirm Import",
                    style = CookbookTypography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = "Cancel",
                    style = CookbookTypography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun PreviewSummary(
    preview: ExportImportPreview,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(CookbookSpacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PreviewStat(
                    label = "Total Items",
                    value = preview.totalCount.toString(),
                    icon = Icons.Default.Inventory2
                )
                
                PreviewStat(
                    label = "Format",
                    value = preview.format,
                    icon = when (preview.format) {
                        "JSON" -> Icons.Default.Code
                        "MARKDOWN" -> Icons.Default.TextSnippet
                        "PDF" -> Icons.Default.PictureAsPdf
                        "DOCX" -> Icons.Default.Description
                        else -> Icons.Default.InsertDriveFile
                    }
                )
                
                PreviewStat(
                    label = "Est. Size",
                    value = formatFileSize(preview.estimatedSize),
                    icon = Icons.Default.Storage
                )
            }
        }
    }
}

@Composable
private fun PreviewStat(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(CookbookSpacing.small)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
        
        Text(
            text = value,
            style = CookbookTypography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = label,
            style = CookbookTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun PreviewItemCard(
    item: PreviewItem,
    modifier: Modifier = Modifier
) {
    val statusColor = when (item.status) {
        PreviewStatus.READY -> MaterialTheme.colorScheme.primary
        PreviewStatus.PROCESSING -> MaterialTheme.colorScheme.error
        PreviewStatus.ERROR -> MaterialTheme.colorScheme.error
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(CookbookSpacing.small)
        ) {
            // Item icon based on type
            val icon = when (item.type.lowercase()) {
                "recipe" -> Icons.Default.RestaurantMenu
                "cookbook" -> Icons.Default.Book
                "category" -> Icons.Default.Category
                "ingredient" -> Icons.Default.SetMeal
                else -> Icons.Default.InsertDriveFile
            }
            
            Icon(
                imageVector = icon,
                contentDescription = item.type,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(CookbookSpacing.small))
            
            // Item info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = CookbookTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${item.type} • ${formatFileSize(item.size)}",
                    style = CookbookTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            // Status indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, CircleShape)
            )
        }
    }
}

@Composable
private fun PreviewMetadata(
    preview: ExportImportPreview,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(CookbookSpacing.medium)
        ) {
            Text(
                text = "Metadata",
                style = CookbookTypography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetadataItem(
                    label = "Operation ID",
                    value = preview.operationId.take(8) + "..."
                )
                
                MetadataItem(
                    label = "Format",
                    value = preview.format
                )
                
                MetadataItem(
                    label = "Item Count",
                    value = preview.totalCount.toString()
                )
            }
        }
    }
}

@Composable
private fun MetadataItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(CookbookSpacing.small)
    ) {
        Text(
            text = label,
            style = CookbookTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Text(
            text = value,
            style = CookbookTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ==================== UTILITY FUNCTIONS ====================

@Composable
fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 * 1024 -> "%.2f GB".format(bytes.toDouble() / (1024 * 1024 * 1024))
        bytes >= 1024 * 1024 -> "%.2f MB".format(bytes.toDouble() / (1024 * 1024))
        bytes >= 1024 -> "%.2f KB".format(bytes.toDouble() / 1024)
        else -> "$bytes B"
    }
}

// ==================== PREVIEW ====================

@Preview(showBackground = true)
@Composable
fun PreviewDialogPreview() {
    val previewItems = listOf(
        PreviewItem(
            id = "1",
            name = "Spaghetti Carbonara",
            type = "recipe",
            size = 2048,
            status = PreviewStatus.READY
        ),
        PreviewItem(
            id = "2",
            name = "Chocolate Cake",
            type = "recipe",
            size = 1536,
            status = PreviewStatus.READY
        ),
        PreviewItem(
            id = "3",
            name = "Italian Cookbook",
            type = "cookbook",
            size = 5120,
            status = PreviewStatus.READY
        )
    )
    
    val preview = ExportImportPreview(
        operationId = "op-12345678",
        items = previewItems,
        totalCount = 3,
        format = "JSON",
        estimatedSize = 8704
    )
    
    MaterialTheme {
        PreviewDialog(
            preview = preview,
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSummaryPreview() {
    val preview = ExportImportPreview(
        operationId = "op-12345678",
        items = emptyList(),
        totalCount = 5,
        format = "JSON",
        estimatedSize = 10240
    )
    
    MaterialTheme {
        PreviewSummary(
            preview = preview,
            modifier = Modifier.padding(CookbookSpacing.medium)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewItemCardPreview() {
    val item = PreviewItem(
        id = "1",
        name = "Spaghetti Carbonara",
        type = "recipe",
        size = 2048,
        status = PreviewStatus.READY
    )
    
    MaterialTheme {
        PreviewItemCard(
            item = item,
            modifier = Modifier.padding(CookbookSpacing.medium)
        )
    }
}
