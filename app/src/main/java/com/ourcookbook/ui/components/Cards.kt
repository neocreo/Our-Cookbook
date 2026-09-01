package com.ourcookbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ourcookbook.domain.model.Recipe
import com.ourcookbook.ui.theme.CookbookColors
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.CookbookTypography
import java.time.format.DateTimeFormatter

/**
 * Base card component with customizable elevation and click behavior
 */
@Composable
fun CookbookCard(
    modifier: Modifier = Modifier,
    elevation: Dp = CookbookSpacing.medium,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val cardModifier = modifier
        .then(if (onClick != null) 
            Modifier.clickable(
                onClick = onClick,
                enabled = enabled
            ) 
        else Modifier)

    Card(
        modifier = cardModifier,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = if (onClick != null) 
            androidx.compose.foundation.border.BorderStroke(
                1.dp, 
                MaterialTheme.colorScheme.outlineVariant
            ) 
        else null
    ) {
        content()
    }
}

/**
 * Elevated card for important content that needs more visual weight
 */
@Composable
fun CookbookElevatedCard(
    modifier: Modifier = Modifier,
    elevation: Dp = CookbookSpacing.large,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .then(if (onClick != null) 
                Modifier.clickable(onClick = onClick) 
            else Modifier),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        content()
    }
}

/**
 * Recipe card component for displaying recipe previews
 */
@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showFavorite: Boolean = false,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null
) {
    CookbookCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CookbookSpacing.medium,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(CookbookSpacing.medium)) {
            // Recipe image
            if (!recipe.imageUrl.isNullOrEmpty()) {
                Box(modifier = Modifier.fillMaxWidth()) {
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
                    
                    // Favorite button overlay
                    if (showFavorite) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(CookbookSpacing.small)
                        ) {
                            FavoriteToggleButton(
                                isFavorite = isFavorite,
                                onClick = { onFavoriteClick?.invoke() }
                            )
                        }
                    }
                }
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
                
                // Serving size
                if (recipe.servingSize != null) {
                    Spacer(modifier = Modifier.width(CookbookSpacing.small))
                    ServingSizeDisplay(servings = recipe.servingSize)
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
 * Compact recipe card for grid layouts
 */
@Composable
fun CompactRecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CookbookCard(
        modifier = modifier,
        elevation = CookbookSpacing.small,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(CookbookSpacing.small)) {
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
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                    error = painterResource(id = android.R.drawable.ic_menu_gallery)
                )
                Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
            }

            // Recipe title
            Text(
                text = recipe.title,
                style = CookbookTypography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(CookbookSpacing.xxSmall))

            // Category badge
            CategoryBadge(category = recipe.category)
        }
    }
}

/**
 * Category badge component for displaying recipe categories
 */
@Composable
fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val categoryColor = CookbookColors.categoryColors.getOrDefault(
        category, 
        MaterialTheme.colorScheme.primary
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
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
fun RatingDisplay(
    rating: Float?,
    modifier: Modifier = Modifier
) {
    if (rating != null && rating > 0) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
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
fun CookTimeDisplay(
    minutes: Int?,
    modifier: Modifier = Modifier
) {
    if (minutes != null && minutes > 0) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
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
fun ServingSizeDisplay(
    servings: Int?,
    modifier: Modifier = Modifier
) {
    if (servings != null && servings > 0) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
        ) {
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
 * Stats card for displaying recipe statistics
 */
@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    CookbookCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(CookbookSpacing.medium)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
            Text(
                text = value,
                style = CookbookTypography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = CookbookTypography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Preview for card components
 */
@Preview(showBackground = true)
@Composable
fun CardsPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(CookbookSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
        ) {
            // Sample recipe for preview
            val sampleRecipe = Recipe(
                title = "Spaghetti Carbonara",
                description = "Classic Italian pasta dish with eggs, cheese, pancetta, and black pepper",
                category = "Mains",
                rating = 4.5f,
                cookTime = 30,
                servingSize = 4
            )
            
            RecipeCard(
                recipe = sampleRecipe,
                onClick = {},
                showFavorite = true,
                isFavorite = true
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                CompactRecipeCard(
                    recipe = sampleRecipe,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                CompactRecipeCard(
                    recipe = sampleRecipe.copy(title = "Pasta", category = "Sides"),
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Stats cards
            Row(
                horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatsCard(
                    title = "Total Recipes",
                    value = "42",
                    icon = Icons.Default.Info,
                    modifier = Modifier.weight(1f)
                )
                StatsCard(
                    title = "Favorites",
                    value = "12",
                    icon = Icons.Default.Favorite,
                    iconTint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
