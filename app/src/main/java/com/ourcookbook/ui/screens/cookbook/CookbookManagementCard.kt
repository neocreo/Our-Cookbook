@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ourcookbook.ui.screens.cookbook

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ourcookbook.R
import com.ourcookbook.domain.model.Cookbook
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.CookbookTypography
import java.time.format.DateTimeFormatter

/**
 * Enhanced Cookbook Card for Management Screen
 * Task 2.1.07: Cookbook Management Screen Implementation
 * 
 * Features:
 * - Cookbook image/thumbnail display
 * - Name, description, and metadata
 * - Sync status indicator
 * - Quick action buttons (edit, share, delete)
 * - Overflow menu for additional actions
 */
@Composable
fun CookbookManagementCard(
    cookbook: Cookbook,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onExport: () -> Unit,
    onSync: () -> Unit,
    isSelected: Boolean = false,
    syncStatus: SyncStatus = SyncStatus.SYNCED,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) CookbookSpacing.medium else CookbookSpacing.small,
            pressedElevation = CookbookSpacing.medium
        ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CookbookSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
        ) {
            // Header with image and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Cookbook image
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(CookbookSpacing.small))
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(CookbookSpacing.small)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (cookbook.imageUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(cookbook.imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Cookbook Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            placeholder = painterResource(R.drawable.ic_cookbook_placeholder),
                            error = painterResource(R.drawable.ic_cookbook_placeholder)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = "Cookbook",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(CookbookSpacing.medium))

                // Cookbook info
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

                    // Metadata row
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
                            icon = Icons.Default.DateRange,
                            text = formatDate(cookbook.updatedAt),
                            modifier = Modifier.weight(1f)
                        )

                        // Sync status indicator
                        SyncStatusIndicator(status = syncStatus)
                    }
                }

                // Action buttons
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(CookbookSpacing.xxSmall)
                ) {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Actions"
                        )
                    }
                }
            }

            // Quick actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalArrangement = Arrangement.spacedBy(CookbookSpacing.xSmall)
            ) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share"
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Dropdown menu for additional actions
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(
                    text = { Text("Export") },
                    onClick = {
                        showMenu = false
                        onExport()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export"
                        )
                    }
                )

                DropdownMenuItem(
                    text = { Text("Sync Now") },
                    onClick = {
                        showMenu = false
                        onSync()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync"
                        )
                    }
                )

                DropdownMenuItem(
                    text = { Text("View Details") },
                    onClick = {
                        showMenu = false
                        onClick()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = "Details"
                        )
                    }
                )
            }
        }
    }
}

/**
 * Sync status for cookbook cards
 */
enum class SyncStatus {
    SYNCED,       // Fully synced
    SYNCING,      // Currently syncing
    NOT_SYNCED,   // Not synced
    ERROR         // Sync error
}

/**
 * Sync status indicator
 */
@Composable
fun SyncStatusIndicator(status: SyncStatus) {
    val (icon, color, description) = when (status) {
        SyncStatus.SYNCED -> Triple(Icons.Default.Sync, MaterialTheme.colorScheme.success, "Synced")
        SyncStatus.SYNCING -> Triple(Icons.Default.Sync, MaterialTheme.colorScheme.primary, "Syncing")
        SyncStatus.NOT_SYNCED -> Triple(Icons.Default.SyncDisabled, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), "Not Synced")
        SyncStatus.ERROR -> Triple(Icons.Default.SyncDisabled, MaterialTheme.colorScheme.error, "Sync Error")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.xxSmall)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = description,
            style = CookbookTypography.labelSmall,
            color = color,
            maxLines = 1
        )
    }
}

/**
 * Cookbook metadata item
 */
@Composable
fun CookbookMetadataItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Date formatting helper
 */
private fun formatDate(instant: java.time.Instant): String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    return formatter.format(java.time.LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault()))
}

/**
 * Cookbook card for grid view
 */
@Composable
fun CookbookGridCard(
    cookbook: Cookbook,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    syncStatus: SyncStatus = SyncStatus.SYNCED,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = CookbookSpacing.small),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CookbookSpacing.small),
            verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
        ) {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(CookbookSpacing.small))
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(CookbookSpacing.small)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (cookbook.imageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(cookbook.imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Cookbook Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = painterResource(R.drawable.ic_cookbook_placeholder),
                        error = painterResource(R.drawable.ic_cookbook_placeholder)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "Cookbook",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CookbookSpacing.small),
                verticalArrangement = Arrangement.spacedBy(CookbookSpacing.xxSmall)
            ) {
                Text(
                    text = cookbook.name,
                    style = CookbookTypography.titleSmall,
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${cookbook.recipeCount} recipes",
                        style = CookbookTypography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    SyncStatusIndicator(status = syncStatus)
                }
            }

            // Action bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalArrangement = Arrangement.spacedBy(CookbookSpacing.xxSmall)
            ) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Preview for Cookbook Management Card
 */
@Preview(showBackground = true)
@Composable
fun CookbookManagementCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(CookbookSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
        ) {
            val sampleCookbook = Cookbook(
                name = "Family Recipes",
                description = "Collection of traditional family recipes passed down through generations",
                ownerDeviceId = "device_123",
                recipeIds = listOf("1", "2", "3", "4"),
                createdAt = java.time.Instant.now(),
                updatedAt = java.time.Instant.now()
            )

            CookbookManagementCard(
                cookbook = sampleCookbook,
                onClick = {},
                onEdit = {},
                onShare = {},
                onDelete = {},
                onExport = {},
                onSync = {},
                syncStatus = SyncStatus.SYNCED
            )

            CookbookGridCard(
                cookbook = sampleCookbook,
                onClick = {},
                onEdit = {},
                onShare = {},
                onDelete = {},
                syncStatus = SyncStatus.SYNCED
            )
        }
    }
}