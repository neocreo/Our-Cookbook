@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ourcookbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ourcookbook.ui.theme.CookbookColors
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.CookbookTypography

/**
 * Filter chip for category selection
 */
@Composable
fun CookbookFilterChip(
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    category: String? = null
) {
    val chipColor = if (category != null) {
        CookbookColors.categoryColors.getOrDefault(category, MaterialTheme.colorScheme.primary)
    } else {
        MaterialTheme.colorScheme.primary
    }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(text = label, style = CookbookTypography.labelMedium) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = chipColor,
            selectedLabelColor = Color.White,
            containerColor = chipColor.copy(alpha = 0.2f),
            labelColor = chipColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = chipColor,
            selectedBorderColor = chipColor
        )
    )
}

/**
 * Suggestion chip for tags and suggestions
 */
@Composable
fun CookbookSuggestionChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SuggestionChip(
        onClick = onClick,
        label = { Text(text = label, style = CookbookTypography.labelMedium) },
        modifier = modifier,
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Assist chip for actions and information
 */
@Composable
fun CookbookAssistChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    AssistChip(
        onClick = onClick,
        label = {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(text = label, style = CookbookTypography.labelMedium)
        },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Elevated assist chip for important actions
 */
@Composable
fun CookbookElevatedAssistChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    ElevatedAssistChip(
        onClick = onClick,
        label = {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(text = label, style = CookbookTypography.labelMedium)
        },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Tag chip for displaying recipe tags
 */
@Composable
fun TagChip(
    tag: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val chipModifier = modifier
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    Box(
        contentAlignment = Alignment.Center,
        modifier = chipModifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = CookbookSpacing.small, vertical = CookbookSpacing.xSmall)
    ) {
        Text(
            text = tag,
            style = CookbookTypography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Tag input chip for adding new tags
 */
@Composable
fun TagInputChip(
    tag: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = CookbookSpacing.xSmall, vertical = CookbookSpacing.xxSmall)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = tag,
                style = CookbookTypography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove tag",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Tag input field for adding multiple tags
 */
@Composable
fun TagInputField(
    tags: List<String>,
    onTagsChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    // This would typically be implemented with a TextField and chip display
    // For now, showing the chip display part
    Row(
        horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.xSmall),
        modifier = modifier.fillMaxWidth()
    ) {
        tags.forEach { tag ->
            TagInputChip(
                tag = tag,
                onDelete = { 
                    onTagsChange(tags.filter { it != tag })
                }
            )
        }
        
        // Add button for new tags
        if (tags.isNotEmpty()) {
            Spacer(modifier = Modifier.width(CookbookSpacing.xSmall))
        }
        
        AssistChip(
            onClick = { onTagsChange(tags + "New Tag") },
            label = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add tag",
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

/**
 * Chip row for displaying multiple chips in a row with wrapping
 */
@Composable
fun ChipRow(
    chips: List<String>,
    selectedChips: Set<String> = emptySet(),
    onChipClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    chipType: ChipType = ChipType.FILTER
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.xSmall),
        modifier = modifier.fillMaxWidth()
    ) {
        chips.forEach { chip ->
            when (chipType) {
                ChipType.FILTER -> {
                    CookbookFilterChip(
                        label = chip,
                        isSelected = selectedChips.contains(chip),
                        onClick = { onChipClick(chip) }
                    )
                }
                ChipType.SUGGESTION -> {
                    CookbookSuggestionChip(
                        label = chip,
                        onClick = { onChipClick(chip) }
                    )
                }
                ChipType.TAG -> {
                    TagChip(
                        tag = chip,
                        onClick = { onChipClick(chip) }
                    )
                }
            }
        }
    }
}

/**
 * Chip type enum
 */
enum class ChipType {
    FILTER, SUGGESTION, TAG
}

/**
 * Preview for chip components
 */
@Preview(showBackground = true)
@Composable
fun ChipsPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(CookbookSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
        ) {
            // Filter chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                CookbookFilterChip(
                    label = "Mains",
                    isSelected = true,
                    onClick = {},
                    category = "Mains"
                )
                CookbookFilterChip(
                    label = "Desserts",
                    isSelected = false,
                    onClick = {},
                    category = "Desserts"
                )
                CookbookFilterChip(
                    label = "Breakfasts",
                    isSelected = false,
                    onClick = {}
                )
            }
            
            // Suggestion chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                CookbookSuggestionChip(
                    label = "Italian",
                    onClick = {}
                )
                CookbookSuggestionChip(
                    label = "Quick",
                    onClick = {}
                )
                CookbookSuggestionChip(
                    label = "Vegetarian",
                    onClick = {}
                )
            }
            
            // Assist chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                CookbookAssistChip(
                    label = "Add Tag",
                    onClick = {},
                    icon = Icons.Default.Add
                )
                CookbookAssistChip(
                    label = "Help",
                    onClick = {},
                    icon = Icons.Default.Info
                )
            }
            
            // Tag chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                TagChip(tag = "Pasta", onClick = {})
                TagChip(tag = "Italian", onClick = {})
                TagChip(tag = "Quick", onClick = {})
            }
            
            // Tag input field
            TagInputField(
                tags = listOf("Pasta", "Italian", "Quick"),
                onTagsChange = {}
            )
        }
    }
}
