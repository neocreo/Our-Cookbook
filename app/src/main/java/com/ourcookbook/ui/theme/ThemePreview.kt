package com.ourcookbook.ui.theme

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Theme Preview Components
 * 
 * Preview composables to showcase the complete theme system including:
 * - Color palette
 * - Typography
 * - Shapes
 * - Elevation
 * - Component styling
 */

// ============================================================================
// COLOR PALETTE PREVIEW
// ============================================================================

@Composable
fun ColorPalettePreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(CookbookSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
    ) {
        Text(
            text = "Color Palette",
            style = CookbookTypography.headlineMedium
        )
        
        // Primary colors
        ColorSection(
            title = "Primary Colors",
            colors = listOf(
                "Primary" to CookbookColors.primary,
                "Primary Variant" to CookbookColors.primaryVariant,
                "Primary Light" to CookbookColors.primaryLight,
                "On Primary" to CookbookColors.onPrimary
            )
        )
        
        // Secondary colors
        ColorSection(
            title = "Secondary Colors",
            colors = listOf(
                "Secondary" to CookbookColors.secondary,
                "Secondary Variant" to CookbookColors.secondaryVariant,
                "Secondary Light" to CookbookColors.secondaryLight,
                "On Secondary" to CookbookColors.onSecondary
            )
        )
        
        // Tertiary colors
        ColorSection(
            title = "Tertiary Colors",
            colors = listOf(
                "Tertiary" to CookbookColors.tertiary,
                "Tertiary Variant" to CookbookColors.tertiaryVariant,
                "Tertiary Light" to CookbookColors.tertiaryLight,
                "On Tertiary" to CookbookColors.onTertiary
            )
        )
        
        // Surface colors
        ColorSection(
            title = "Surface Colors",
            colors = listOf(
                "Surface" to MaterialTheme.colorScheme.surface,
                "Surface Variant" to MaterialTheme.colorScheme.surfaceVariant,
                "Background" to MaterialTheme.colorScheme.background,
                "On Surface" to MaterialTheme.colorScheme.onSurface,
                "On Background" to MaterialTheme.colorScheme.onBackground
            )
        )
        
        // Status colors
        ColorSection(
            title = "Status Colors",
            colors = listOf(
                "Success" to CookbookColors.success,
                "Warning" to CookbookColors.warning,
                "Error" to CookbookColors.error,
                "Info" to CookbookColors.info
            )
        )
        
        // Category colors
        ColorSection(
            title = "Category Colors",
            colors = CookbookColors.categoryColors.map { (name, color) ->
                name to color
            }.toList()
        )
    }
}

@Composable
fun ColorSection(title: String, colors: List<Pair<String, Color>>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
    ) {
        Text(
            text = title,
            style = CookbookTypography.titleMedium
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
        ) {
            colors.forEach { (name, color) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(color, CircleShape)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
                    Text(
                        text = name,
                        style = CookbookTypography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "#${color.toHex()}",
                        style = CookbookTypography.caption,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

fun Color.toHex(): String {
    val red = (this.value shr 16) and 0xFF
    val green = (this.value shr 8) and 0xFF
    val blue = this.value and 0xFF
    return "%02X%02X%02X".format(red, green, blue)
}

// ============================================================================
// TYPOGRAPHY PREVIEW
// ============================================================================

@Composable
fun TypographyPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(CookbookSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
    ) {
        Text(
            text = "Typography",
            style = CookbookTypography.headlineMedium
        )
        
        // Display styles
        TypographySection(
            title = "Display Styles",
            styles = listOf(
                "Display Large" to CookbookTypography.displayLarge,
                "Display Medium" to CookbookTypography.displayMedium,
                "Display Small" to CookbookTypography.displaySmall
            )
        )
        
        // Headline styles
        TypographySection(
            title = "Headline Styles",
            styles = listOf(
                "Headline Large" to CookbookTypography.headlineLarge,
                "Headline Medium" to CookbookTypography.headlineMedium,
                "Headline Small" to CookbookTypography.headlineSmall
            )
        )
        
        // Title styles
        TypographySection(
            title = "Title Styles",
            styles = listOf(
                "Title Large" to CookbookTypography.titleLarge,
                "Title Medium" to CookbookTypography.titleMedium,
                "Title Small" to CookbookTypography.titleSmall
            )
        )
        
        // Body styles
        TypographySection(
            title = "Body Styles",
            styles = listOf(
                "Body Large" to CookbookTypography.bodyLarge,
                "Body Medium" to CookbookTypography.bodyMedium,
                "Body Small" to CookbookTypography.bodySmall
            )
        )
        
        // Label styles
        TypographySection(
            title = "Label Styles",
            styles = listOf(
                "Label Large" to CookbookTypography.labelLarge,
                "Label Medium" to CookbookTypography.labelMedium,
                "Label Small" to CookbookTypography.labelSmall
            )
        )
        
        // Semantic styles
        TypographySection(
            title = "Semantic Styles",
            styles = listOf(
                "Recipe Title" to CookbookTextStyles.recipeTitle,
                "Recipe Ingredient" to CookbookTextStyles.recipeIngredient,
                "Button Primary" to CookbookTextStyles.buttonPrimary,
                "Category Chip" to CookbookTextStyles.categoryChip
            )
        )
    }
}

@Composable
fun TypographySection(title: String, styles: List<Pair<String, TextStyle>>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
    ) {
        Text(
            text = title,
            style = CookbookTypography.titleMedium
        )
        
        styles.forEach { (name, style) ->
            Column(
                verticalArrangement = Arrangement.spacedBy(CookbookSpacing.xSmall)
            ) {
                Text(
                    text = "The quick brown fox jumps over the lazy dog",
                    style = style
                )
                Text(
                    text = name,
                    style = CookbookTypography.caption,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Divider()
            }
        }
    }
}

// ============================================================================
// SHAPES PREVIEW
// ============================================================================

@Composable
fun ShapesPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(CookbookSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
    ) {
        Text(
            text = "Shapes",
            style = CookbookTypography.headlineMedium
        )
        
        Text(
            text = "Corner Radius Examples",
            style = CookbookTypography.titleMedium
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
        ) {
            ShapeExample(
                name = "Extra Small (4dp)",
                shape = CookbookShapes.extraSmall
            )
            ShapeExample(
                name = "Small (8dp)",
                shape = CookbookShapes.small
            )
            ShapeExample(
                name = "Medium (12dp)",
                shape = CookbookShapes.medium
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
        ) {
            ShapeExample(
                name = "Large (16dp)",
                shape = CookbookShapes.large
            )
            ShapeExample(
                name = "Extra Large (24dp)",
                shape = CookbookShapes.extraLarge
            )
            ShapeExample(
                name = "Circle",
                shape = CircleShape
            )
        }
        
        Text(
            text = "Component Shapes",
            style = CookbookTypography.titleMedium
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
        ) {
            ShapeExample(
                name = "Recipe Card",
                shape = ComponentShapes.recipeCard
            )
            ShapeExample(
                name = "Button",
                shape = ComponentShapes.primaryButton
            )
            ShapeExample(
                name = "Text Field",
                shape = ComponentShapes.textField
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
        ) {
            ShapeExample(
                name = "Chip",
                shape = ComponentShapes.filterChip
            )
            ShapeExample(
                name = "Dialog",
                shape = ComponentShapes.dialog
            )
            ShapeExample(
                name = "FAB",
                shape = ComponentShapes.floatingActionButton
            )
        }
    }
}

@Composable
fun ShapeExample(name: String, shape: androidx.compose.ui.graphics.Shape) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(MaterialTheme.colorScheme.primary, shape)
                .clip(shape)
        )
        Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
        Text(
            text = name,
            style = CookbookTypography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

// ============================================================================
// ELEVATION PREVIEW
// ============================================================================

@Composable
fun ElevationPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(CookbookSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
    ) {
        Text(
            text = "Elevation",
            style = CookbookTypography.headlineMedium
        )
        
        Text(
            text = "Elevation Levels",
            style = CookbookTypography.titleMedium
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
        ) {
            ElevationExample(
                name = "None (0dp)",
                elevation = CookbookElevation.none
            )
            ElevationExample(
                name = "Small (2dp)",
                elevation = CookbookElevation.small
            )
            ElevationExample(
                name = "Medium (4dp)",
                elevation = CookbookElevation.medium
            )
            ElevationExample(
                name = "Large (8dp)",
                elevation = CookbookElevation.large
            )
            ElevationExample(
                name = "Extra Large (12dp)",
                elevation = CookbookElevation.xLarge
            )
        }
        
        Text(
            text = "Component Elevations",
            style = CookbookTypography.titleMedium
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
        ) {
            ElevationExample(
                name = "Card",
                elevation = ComponentElevation.recipeCard
            )
            ElevationExample(
                name = "Dialog",
                elevation = ComponentElevation.dialog
            )
            ElevationExample(
                name = "FAB",
                elevation = ComponentElevation.floatingActionButton
            )
            ElevationExample(
                name = "Snackbar",
                elevation = ComponentElevation.snackbar
            )
        }
    }
}

@Composable
fun ElevationExample(name: String, elevation: Dp) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CookbookSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(CookbookSpacing.xSmall)
        ) {
            Text(
                text = name,
                style = CookbookTypography.bodyMedium
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            )
        }
    }
}

// ============================================================================
// COMPONENTS PREVIEW
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentsPreview() {
    var checked by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    var selectedIndex by remember { mutableStateOf(0) }
    var sliderValue by remember { mutableStateOf(0.5f) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(CookbookSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
    ) {
        Text(
            text = "UI Components",
            style = CookbookTypography.headlineMedium
        )
        
        // Buttons
        Text(
            text = "Buttons",
            style = CookbookTypography.titleMedium
        )
        
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Primary Button")
        }
        
        ElevatedButton(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Elevated Button")
        }
        
        FilledTonalButton(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Filled Tonal Button")
        }
        
        OutlinedButton(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Outlined Button")
        }
        
        TextButton(
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Text Button")
        }
        
        // Icon Buttons
        Text(
            text = "Icon Buttons",
            style = CookbookTypography.titleMedium
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
        ) {
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.Home, contentDescription = "Home")
            }
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.Favorite, contentDescription = "Favorite")
            }
        }
        
        // Input Fields
        Text(
            text = "Input Fields",
            style = CookbookTypography.titleMedium
        )
        
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Text Field") },
            placeholder = { Text("Enter text...") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Selection Controls
        Text(
            text = "Selection Controls",
            style = CookbookTypography.titleMedium
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { checked = it }
            )
            Spacer(modifier = Modifier.width(CookbookSpacing.small))
            Text("Checkbox")
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            RadioButton(
                selected = checked,
                onClick = { checked = !checked }
            )
            Spacer(modifier = Modifier.width(CookbookSpacing.small))
            Text("Radio Button")
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Switch(
                checked = checked,
                onCheckedChange = { checked = it }
            )
            Spacer(modifier = Modifier.width(CookbookSpacing.small))
            Text("Switch")
        }
        
        // Slider
        Text(
            text = "Slider",
            style = CookbookTypography.titleMedium
        )
        
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Segmented Button
        Text(
            text = "Segmented Button",
            style = CookbookTypography.titleMedium
        )
        
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("Option 1", "Option 2", "Option 3").forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                    onClick = { selectedIndex = index },
                    selected = index == selectedIndex
                ) {
                    Text(label)
                }
            }
        }
        
        // Cards
        Text(
            text = "Cards",
            style = CookbookTypography.titleMedium
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = CookbookElevation.card)
        ) {
            Column(
                modifier = Modifier.padding(CookbookSpacing.medium)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                )
                Spacer(modifier = Modifier.height(CookbookSpacing.small))
                Text(
                    text = "Recipe Card",
                    style = CookbookTypography.titleLarge
                )
                Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
                Text(
                    text = "This is a sample recipe card showing the elevation and shape system.",
                    style = CookbookTypography.bodyMedium
                )
            }
        }
    }
}

// ============================================================================
// MAIN THEME PREVIEW
// ============================================================================

@Preview(showBackground = true, name = "Light Theme Preview")
@Composable
fun CookbookThemePreviewLight() {
    CookbookTheme(darkTheme = false, dynamicColor = false) {
        MainThemePreviewContent()
    }
}

@Preview(showBackground = true, name = "Dark Theme Preview")
@Composable
fun CookbookThemePreviewDark() {
    CookbookTheme(darkTheme = true, dynamicColor = false) {
        MainThemePreviewContent()
    }
}

@Composable
fun MainThemePreviewContent() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(CookbookSpacing.medium),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "Cookbook Theme Preview",
                style = CookbookTypography.titleLarge
            )
        }
        
        // Tab Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = CookbookSpacing.medium, vertical = CookbookSpacing.small),
            horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
        ) {
            TextButton(onClick = { /* Colors */ }) {
                Text("Colors")
            }
            TextButton(onClick = { /* Typography */ }) {
                Text("Typography")
            }
            TextButton(onClick = { /* Shapes */ }) {
                Text("Shapes")
            }
            TextButton(onClick = { /* Elevation */ }) {
                Text("Elevation")
            }
            TextButton(onClick = { /* Components */ }) {
                Text("Components")
            }
        }
        
        Divider()
        
        // Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(CookbookSpacing.medium),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CookbookSpacing.large)
            ) {
                // Theme Info Card
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = CookbookElevation.card),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(CookbookSpacing.large),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
                    ) {
                        Text(
                            text = "🎨 Cookbook Theme System",
                            style = CookbookTypography.headlineSmall
                        )
                        
                        Text(
                            text = "Material Design 3 Implementation",
                            style = CookbookTypography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        Divider()
                        
                        Text(
                            text = "This theme system includes:",
                            style = CookbookTypography.bodyMedium
                        )
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(CookbookSpacing.xSmall)
                        ) {
                            Text(
                                text = "• Complete color palette with food-inspired colors",
                                style = CookbookTypography.bodySmall
                            )
                            Text(
                                text = "• Full typography system with Material Design 3 scale",
                                style = CookbookTypography.bodySmall
                            )
                            Text(
                                text = "• Shape system with consistent corner radii",
                                style = CookbookTypography.bodySmall
                            )
                            Text(
                                text = "• Elevation system for depth hierarchy",
                                style = CookbookTypography.bodySmall
                            )
                            Text(
                                text = "• Light and dark theme support",
                                style = CookbookTypography.bodySmall
                            )
                            Text(
                                text = "• Dynamic color support for Android 12+",
                                style = CookbookTypography.bodySmall
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
                        
                        Button(
                            onClick = { /* Show full preview */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View Full Theme Preview")
                        }
                    }
                }
                
                // Sample Components
                Text(
                    text = "Sample Components",
                    style = CookbookTypography.titleLarge
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.medium)
                ) {
                    Button(
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Primary")
                    }
                    OutlinedButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Secondary")
                    }
                }
                
                // Category Colors Preview
                Text(
                    text = "Category Colors",
                    style = CookbookTypography.titleLarge
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
                ) {
                    CookbookColors.categoryColors.take(5).forEach { (name, color) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(color, CircleShape)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.height(CookbookSpacing.xSmall))
                            Text(
                                text = name.split(" ")[0],
                                style = CookbookTypography.caption,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
        
        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.fillMaxWidth()
        )
    }
}