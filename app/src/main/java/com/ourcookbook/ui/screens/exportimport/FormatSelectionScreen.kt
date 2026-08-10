package com.ourcookbook.ui.screens.exportimport

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.domain.model.ExportFormat
import com.ourcookbook.domain.model.ImportFormat
import com.ourcookbook.ui.components.*
import com.ourcookbook.ui.screens.exportimport.ExportImportEvent
import com.ourcookbook.ui.screens.exportimport.ExportImportMode
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.CookbookTypography

/**
 * Format Selection Screen
 * Task 2.1.09: Export/Import Screen Implementation
 */

@Composable
fun FormatSelectionScreen(
    navController: NavController,
    isExport: Boolean = true,
    currentFormat: Any = if (isExport) ExportFormat.JSON else ImportFormat.JSON,
    onFormatSelected: (Any) -> Unit
) {
    val formats = if (isExport) {
        ExportFormat.entries
    } else {
        ImportFormat.entries
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(CookbookSpacing.medium)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.width(CookbookSpacing.small))
            
            Text(
                text = if (isExport) "Select Export Format" else "Select Import Format",
                style = CookbookTypography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        Text(
            text = "Choose the format for your ${if (isExport) "export" else "import"}:",
            style = CookbookTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.large))
        
        // Format list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = CookbookSpacing.small)
        ) {
            items(formats) { format ->
                val isSelected = when (currentFormat) {
                    is ExportFormat -> format == currentFormat
                    is ImportFormat -> format == currentFormat
                    else -> false
                }
                
                FormatItem(
                    format = format,
                    isSelected = isSelected,
                    isExport = isExport,
                    onClick = { onFormatSelected(format) }
                )
                
                Spacer(modifier = Modifier.height(CookbookSpacing.small))
            }
        }
    }
}

@Composable
private fun FormatItem(
    format: Any,
    isSelected: Boolean,
    isExport: Boolean,
    onClick: () -> Unit
) {
    val cardModifier = if (isSelected) {
        Modifier.background(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.shapes.medium
        )
    } else {
        Modifier.background(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.shapes.medium
        )
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(cardModifier),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(CookbookSpacing.medium)
        ) {
            // Format icon
            val icon = when (format) {
                ExportFormat.JSON -> Icons.Default.Code
                ExportFormat.MARKDOWN -> Icons.Default.TextSnippet
                ExportFormat.PDF -> Icons.Default.PictureAsPdf
                ExportFormat.DOCX -> Icons.Default.Description
                ImportFormat.JSON -> Icons.Default.Code
                ImportFormat.MARKDOWN -> Icons.Default.TextSnippet
                else -> Icons.Default.InsertDriveFile
            }
            
            Icon(
                imageVector = icon,
                contentDescription = format.name,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(CookbookSpacing.medium))
            
            // Format info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = format.name,
                    style = CookbookTypography.titleMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                Text(
                    text = getFormatDescription(format, isExport),
                    style = CookbookTypography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                )
            }
            
            // Selection indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun getFormatDescription(format: Any, isExport: Boolean): String {
    return when (format) {
        ExportFormat.JSON -> "Structured data format, preserves all metadata"
        ExportFormat.MARKDOWN -> "Human-readable format, easy to edit"
        ExportFormat.PDF -> "Print-ready format with styling and images"
        ExportFormat.DOCX -> "Microsoft Word format, fully editable"
        ImportFormat.JSON -> "Structured data format, supports full recipe data"
        ImportFormat.MARKDOWN -> "Human-readable format, supports basic recipe structure"
        else -> "Unknown format"
    }
}

@Composable
fun FormatFeatures(
    format: Any,
    isExport: Boolean
) {
    val features = when (format) {
        ExportFormat.JSON -> listOf(
            "✓ Preserves all metadata",
            "✓ Supports images (base64)",
            "✓ Human-readable structure",
            "✓ Easy to parse programmatically"
        )
        ExportFormat.MARKDOWN -> listOf(
            "✓ Human-readable",
            "✓ Easy to edit",
            "✓ Works with any text editor",
            "✓ Version control friendly"
        )
        ExportFormat.PDF -> listOf(
            "✓ Print-ready",
            "✓ Preserves formatting",
            "✓ Includes images",
            "✓ Professional appearance"
        )
        ExportFormat.DOCX -> listOf(
            "✓ Fully editable",
            "✓ Microsoft Word compatible",
            "✓ Preserves formatting",
            "✓ Includes images"
        )
        ImportFormat.JSON -> listOf(
            "✓ Supports full recipe data",
            "✓ Preserves metadata",
            "✓ Handles images (base64)",
            "✓ Validates structure"
        )
        ImportFormat.MARKDOWN -> listOf(
            "✓ Supports basic recipe structure",
            "✓ Human-readable",
            "✓ Easy to create manually",
            "✓ Works with any text file"
        )
        else -> emptyList()
    }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Features:",
            style = CookbookTypography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.small))
        
        features.forEach { feature ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Feature",
                    tint = MaterialTheme.colorScheme.success,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(CookbookSpacing.small))
                
                Text(
                    text = feature,
                    style = CookbookTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
        }
    }
}

// ==================== PREVIEW ====================

@Preview(showBackground = true)
@Composable
fun FormatSelectionScreenPreview() {
    MaterialTheme {
        FormatSelectionScreen(
            navController = rememberNavController(),
            isExport = true,
            currentFormat = ExportFormat.JSON,
            onFormatSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FormatItemPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(CookbookSpacing.medium)
        ) {
            FormatItem(
                format = ExportFormat.JSON,
                isSelected = true,
                isExport = true,
                onClick = {}
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            FormatItem(
                format = ExportFormat.MARKDOWN,
                isSelected = false,
                isExport = true,
                onClick = {}
            )
        }
    }
}
