@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.CookbookTypography
import java.time.format.DateTimeFormatter

/**
 * Cookbook Card Component
 * Task 1.9: Complete Navigation Setup
 * 
 * Displays cookbook information in a card format
 */

@Composable
fun CookbookCard(
    cookbook: Cookbook,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = CookbookSpacing.medium),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CookbookSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
        ) {
            // Cookbook header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(CookbookSpacing.xxSmall)
                ) {
                    Text(
                        text = cookbook.name,
                        style = CookbookTypography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (!cookbook.description.isNullOrBlank()) {
                        Text(
                            text = cookbook.description,
                            style = CookbookTypography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View Cookbook",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            
            // Cookbook metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
            ) {
                CookbookMetadataItem(
                    icon = Icons.Default.Book,
                    text = "${cookbook.recipeCount} recipes",
                    modifier = Modifier.weight(1f)
                )
                
                CookbookMetadataItem(
                    icon = Icons.Default.People,
                    text = cookbook.ownerDeviceId.take(8),
                    modifier = Modifier.weight(1f)
                )
                
                CookbookMetadataItem(
                    icon = Icons.Default.DateRange,
                    text = formatDate(cookbook.createdAt),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CookbookMetadataItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.xxSmall)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Text(
            text = text,
            style = CookbookTypography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

private fun formatDate(instant: java.time.Instant): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    return formatter.format(java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()))
}

@Preview(showBackground = true)
@Composable
fun CookbookCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(CookbookSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
        ) {
            val sampleCookbook = Cookbook(
                name = "Family Recipes",
                description = "Collection of traditional family recipes passed down through generations",
                ownerDeviceId = "device_123",
                recipeCount = 42,
                createdAt = java.time.Instant.now(),
                updatedAt = java.time.Instant.now()
            )
            
            CookbookCard(
                cookbook = sampleCookbook,
                onClick = {}
            )
        }
    }
}
