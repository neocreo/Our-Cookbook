package com.ourcookbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.ui.theme.CookbookColors
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.CookbookTypography
import java.time.format.DateTimeFormatter

/**
 * Cookbook primary button component
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
        shape = RoundedCornerShape(8.dp),
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
 * Cookbook secondary button component
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
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.border.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = text.uppercase(),
            style = CookbookTypography.labelLarge
        )
    }
}

/**
 * Cookbook icon button component
 */
@Composable
fun CookbookIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = tint
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = tint
        )
    }
}

/**
 * Cookbook card component
 */
@Composable
fun CookbookCard(
    modifier: Modifier = Modifier,
    elevation: Dp = CookbookSpacing.medium,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardModifier = modifier
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    Card(
        modifier = cardModifier,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = if (onClick != null) 
            androidx.compose.foundation.border.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) 
            else null
    ) {
        content()
    }
}

/**
 * Recipe card component
 */
@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CookbookCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CookbookSpacing.medium,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(CookbookSpacing.medium)) {
            // Recipe image
            if (!recipe.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(recipe.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = recipe.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                    error = painterResource(id = android.R.drawable.ic_menu_gallery)
                )
                Spacer(modifier = Modifier.height(CookbookSpacing.small))
            }

            // Recipe title
            Text(
                text = recipe.title,
                style = CookbookTypography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))

            // Recipe metadata
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                // Category badge
                CategoryBadge(category = recipe.category)

                Spacer(modifier = Modifier.width(CookbookSpacing.small))

                // Rating
                RatingDisplay(rating = recipe.rating)

                Spacer(modifier = Modifier.width(CookbookSpacing.small))

                // Cook time
                if (recipe.cookTime != null) {
                    CookTimeDisplay(minutes = recipe.cookTime)
                }
            }

            Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))

            // Description preview
            if (!recipe.description.isNullOrEmpty()) {
                Text(
                    text = recipe.description,
                    style = CookbookTypography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Category badge component
 */
@Composable
fun CategoryBadge(category: String) {
    val categoryColor = CookbookColors.categoryColors.getOrDefault(category, MaterialTheme.colorScheme.primary)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(
                color = categoryColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = CookbookSpacing.small, vertical = CookbookSpacing.xSmall)
    ) {
        Text(
            text = category,
            style = CookbookTypography.labelSmall,
            color = categoryColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Rating display component
 */
@Composable
fun RatingDisplay(rating: Float?) {
    if (rating != null && rating > 0) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Rating",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "%.1f".format(rating),
                style = CookbookTypography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Cook time display component
 */
@Composable
fun CookTimeDisplay(minutes: Int?) {
    if (minutes != null && minutes > 0) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Cook time",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${minutes}min",
                style = CookbookTypography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Serving size display component
 */
@Composable
fun ServingSizeDisplay(servings: Int?) {
    if (servings != null && servings > 0) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Servings",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${servings} servings",
                style = CookbookTypography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Quick action button component
 */
@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(CookbookSpacing.small)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
        Text(
            text = label,
            style = CookbookTypography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Section header component
 */
@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = CookbookTypography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    style = CookbookTypography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Empty state component
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(CookbookSpacing.xxLarge)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        Text(
            text = title,
            style = CookbookTypography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        if (description != null) {
            Spacer(modifier = Modifier.height(CookbookSpacing.small))
            Text(
                text = description,
                style = CookbookTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Loading state component
 */
@Composable
fun LoadingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(CookbookSpacing.xxLarge)
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        Text(
            text = "Loading...",
            style = CookbookTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * Error state component
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(CookbookSpacing.xxLarge)
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        Text(
            text = "Error",
            style = CookbookTypography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.small))
        Text(
            text = message,
            style = CookbookTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(CookbookSpacing.large))
            CookbookPrimaryButton(
                text = "Retry",
                onClick = onRetry
            )
        }
    }
}

/**
 * Sync status icon component
 */
@Composable
fun SyncStatusIcon(status: String) {
    val (icon, color) = when (status) {
        "SYNCING" -> Icons.Default.Sync to MaterialTheme.colorScheme.primary
        "SUCCESS" -> Icons.Default.Check to MaterialTheme.colorScheme.secondary
        "ERROR" -> Icons.Default.Error to MaterialTheme.colorScheme.error
        "CONFLICT" -> Icons.Default.Warning to MaterialTheme.colorScheme.warning
        else -> Icons.Default.Sync to MaterialTheme.colorScheme.outline
    }

    Icon(
        imageVector = icon,
        contentDescription = "Sync status",
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

/**
 * Favorite button component
 */
@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Preview for Cookbook components
 */
@Preview(showBackground = true)
@Composable
fun CookbookComponentsPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(CookbookSpacing.medium)
                .verticalScroll(rememberScrollState())
        ) {
            // Preview buttons
            CookbookPrimaryButton(text = "Primary Button", onClick = {})
            Spacer(modifier = Modifier.height(CookbookSpacing.medium))
            CookbookSecondaryButton(text = "Secondary Button", onClick = {})
            Spacer(modifier = Modifier.height(CookbookSpacing.medium))

            // Preview card
            val sampleRecipe = Recipe(
                title = "Spaghetti Carbonara",
                description = "Classic Italian pasta dish with eggs, cheese, pancetta, and black pepper",
                category = "Mains",
                rating = 4.5f,
                cookTime = 30
            )
            RecipeCard(recipe = sampleRecipe, onClick = {})
            Spacer(modifier = Modifier.height(CookbookSpacing.medium))

            // Preview components
            Row(
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickActionButton(
                    icon = Icons.Default.Add,
                    label = "New",
                    onClick = {}
                )
                QuickActionButton(
                    icon = Icons.Default.Camera,
                    label = "Scan",
                    onClick = {}
                )
                QuickActionButton(
                    icon = Icons.Default.Search,
                    label = "Search",
                    onClick = {}
                )
                QuickActionButton(
                    icon = Icons.Default.Sync,
                    label = "Sync",
                    onClick = {}
                )
            }
        }
    }
}
