package com.ourcookbook.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.CookbookTypography

/**
 * Primary button component following Material Design 3 guidelines
 * Used for main actions like "Save", "Create", "Submit"
 */
@Composable
fun CookbookPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .fillMaxWidth(),
        enabled = enabled && !loading,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = CookbookSpacing.small,
            pressedElevation = CookbookSpacing.medium
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = text.uppercase(),
                style = CookbookTypography.labelLarge
            )
        }
    }
}

/**
 * Secondary button component for less prominent actions
 * Used for actions like "Cancel", "Back", "Secondary options"
 */
@Composable
fun CookbookSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .fillMaxWidth(),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = text.uppercase(),
            style = CookbookTypography.labelLarge
        )
    }
}

/**
 * Tertiary button for minimal emphasis actions
 * Used for actions like "Learn more", "Skip", etc.
 */
@Composable
fun CookbookTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = text.uppercase(),
            style = CookbookTypography.labelLarge
        )
    }
}

/**
 * Icon button for actions represented by icons
 * Used in toolbars, cards, and inline actions
 */
@Composable
fun CookbookIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = tint,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = if (enabled) tint else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}

/**
 * Floating Action Button for primary actions
 * Used for "Add Recipe", "Scan", etc.
 */
@Composable
fun CookbookFloatingActionButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    extended: Boolean = false,
    text: String? = null
) {
    if (extended && text != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = CookbookSpacing.large,
                pressedElevation = CookbookSpacing.xLarge
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = text,
                modifier = Modifier.padding(start = CookbookSpacing.small),
                style = CookbookTypography.labelLarge
            )
        }
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = CookbookSpacing.large,
                pressedElevation = CookbookSpacing.xLarge
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Button with icon and text for important actions
 * Used in dialogs, cards, and toolbars
 */
@Composable
fun CookbookIconTextButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconPosition: IconPosition = IconPosition.START
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        when (iconPosition) {
            IconPosition.START -> {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = text,
                    modifier = Modifier.padding(start = CookbookSpacing.small),
                    style = CookbookTypography.labelLarge
                )
            }
            IconPosition.END -> {
                Text(
                    text = text,
                    modifier = Modifier.padding(end = CookbookSpacing.small),
                    style = CookbookTypography.labelLarge
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Favorite toggle button with animation support
 */
@Composable
fun FavoriteToggleButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Icon position for IconTextButton
 */
enum class IconPosition {
    START, END
}

/**
 * Preview for all button components
 */
@Preview(showBackground = true)
@Composable
fun ButtonsPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(CookbookSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
        ) {
            CookbookPrimaryButton(text = "Primary Action", onClick = {})
            CookbookSecondaryButton(text = "Secondary Action", onClick = {})
            CookbookTextButton(text = "Text Button", onClick = {})
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                CookbookIconButton(
                    icon = Icons.Default.Favorite,
                    onClick = {},
                    contentDescription = "Favorite"
                )
                CookbookIconButton(
                    icon = Icons.Default.Delete,
                    onClick = {},
                    contentDescription = "Delete"
                )
                CookbookIconButton(
                    icon = Icons.Default.Edit,
                    onClick = {},
                    contentDescription = "Edit"
                )
                CookbookIconButton(
                    icon = Icons.Default.Share,
                    onClick = {},
                    contentDescription = "Share"
                )
            }
            
            CookbookIconTextButton(
                text = "Save Recipe",
                icon = Icons.Default.Check,
                onClick = {}
            )
            
            CookbookFloatingActionButton(
                icon = Icons.Default.Add,
                onClick = {},
                contentDescription = "Add Recipe"
            )
        }
    }
}
