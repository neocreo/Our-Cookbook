@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ourcookbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.DeviceType
import com.ourcookbook.ui.theme.FoldableSupport
import com.ourcookbook.ui.theme.ResponsiveDimensions
import com.ourcookbook.ui.theme.ResponsiveGrid
import com.ourcookbook.ui.theme.ScreenSize
import com.ourcookbook.ui.theme.isLandscape
import com.ourcookbook.ui.theme.isPortrait
import com.ourcookbook.ui.theme.paddingValues
import com.ourcookbook.ui.theme.responsiveAppBarConfiguration
import com.ourcookbook.ui.theme.responsiveNavigationType
import com.ourcookbook.ui.theme.screenWidthDp

/**
 * Responsive UI Components for Cookbook App
 * Task 2.2.10: Responsive design for tablets
 *
 * Provides responsive versions of common UI components that adapt to different screen sizes
 */

// ============================================================================
// RESPONSIVE NAVIGATION TYPES
// ============================================================================

/**
 * Navigation types for different screen sizes
 */
enum class ResponsiveNavigationType {
    BOTTOM_NAVIGATION,    // Bottom navigation bar (phones)
    NAVIGATION_RAIL,      // Navigation rail (tablets, landscape)
    NAVIGATION_DRAWER,    // Navigation drawer (tablets, desktop)
    PERMANENT_DRAWER      // Permanent navigation drawer (large screens)
}

// ============================================================================
// RESPONSIVE APP BAR
// ============================================================================

/**
 * Configuration for responsive app bar
 */
data class ResponsiveAppBarConfiguration(
    val showTitle: Boolean = true,
    val showNavigationIcon: Boolean = true,
    val showActions: Boolean = true,
    val showSubtitle: Boolean = false
) {
    companion object {
        val Default = ResponsiveAppBarConfiguration()
    }
}

/**
 * Responsive top app bar that adapts to screen size
 */
@Composable
fun ResponsiveAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = Icons.Default.Menu,
    onNavigationClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    configuration: ResponsiveAppBarConfiguration = responsiveAppBarConfiguration(),
    subtitle: String? = null
) {
    when (ScreenSize.current()) {
        ScreenSize.COMPACT -> {
            CompactAppBar(
                title = title,
                modifier = modifier,
                navigationIcon = navigationIcon,
                onNavigationClick = onNavigationClick,
                actions = actions,
                configuration = configuration,
                subtitle = subtitle
            )
        }
        else -> {
            ExpandedAppBar(
                title = title,
                modifier = modifier,
                navigationIcon = navigationIcon,
                onNavigationClick = onNavigationClick,
                actions = actions,
                configuration = configuration,
                subtitle = subtitle
            )
        }
    }
}

/**
 * Compact app bar for small screens
 */
@Composable
private fun CompactAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    configuration: ResponsiveAppBarConfiguration = ResponsiveAppBarConfiguration.Default,
    subtitle: String? = null
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                if (configuration.showSubtitle && subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        navigationIcon = if (configuration.showNavigationIcon && navigationIcon != null) {
            {
                IconButton(onClick = onNavigationClick) {
                    Icon(navigationIcon, contentDescription = "Menu")
                }
            }
        } else null,
        actions = if (configuration.showActions) actions else {},
        colors = TopAppBarDefaults.mediumTopAppBarColors()
    )
}

/**
 * Expanded app bar for larger screens
 */
@Composable
private fun ExpandedAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    configuration: ResponsiveAppBarConfiguration = ResponsiveAppBarConfiguration.Default,
    subtitle: String? = null
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                if (configuration.showNavigationIcon && navigationIcon != null) {
                    IconButton(onClick = onNavigationClick) {
                        Icon(navigationIcon, contentDescription = "Menu")
                    }
                    Spacer(modifier = Modifier.width(CookbookSpacing.small))
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (configuration.showSubtitle && subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        actions = if (configuration.showActions) actions else {},
        colors = TopAppBarDefaults.largeTopAppBarColors()
    )
}

// ============================================================================
// RESPONSIVE NAVIGATION
// ============================================================================

/**
 * Responsive navigation component that adapts to screen size
 */
@Composable
fun ResponsiveNavigation(
    navController: NavController,
    items: List<ResponsiveNavItem>,
    modifier: Modifier = Modifier,
    drawerContent: @Composable (ColumnScope.() -> Unit)? = null
) {
    val navigationType = responsiveNavigationType()
    
    when (navigationType) {
        ResponsiveNavigationType.BOTTOM_NAVIGATION -> {
            BottomNavigationBar(
                navController = navController,
                items = items,
                modifier = modifier
            )
        }
        ResponsiveNavigationType.NAVIGATION_RAIL -> {
            NavigationRailComponent(
                navController = navController,
                items = items,
                modifier = modifier
            )
        }
        ResponsiveNavigationType.NAVIGATION_DRAWER,
        ResponsiveNavigationType.PERMANENT_DRAWER -> {
            NavigationDrawerComponent(
                navController = navController,
                items = items,
                modifier = modifier,
                drawerContent = drawerContent,
                isPermanent = navigationType == ResponsiveNavigationType.PERMANENT_DRAWER
            )
        }
    }
}

/**
 * Navigation item data
 */
data class ResponsiveNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int? = null
)

/**
 * Bottom navigation bar for compact screens
 */
@Composable
fun BottomNavigationBar(
    navController: NavController,
    items: List<ResponsiveNavItem>,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination
    
    NavigationBar(
        modifier = modifier
    ) {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                alwaysShowLabel = false
            )
        }
    }
}

/**
 * Navigation rail for medium and expanded screens
 */
@Composable
fun NavigationRailComponent(
    navController: NavController,
    items: List<ResponsiveNavItem>,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination
    
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            
            NavigationRailItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
    }
}

/**
 * Navigation drawer for large screens
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerComponent(
    navController: NavController,
    items: List<ResponsiveNavItem>,
    modifier: Modifier = Modifier,
    drawerContent: @Composable (ColumnScope.() -> Unit)? = null,
    isPermanent: Boolean = false
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    if (isPermanent) {
        PermanentNavigationDrawer(
            modifier = modifier,
            drawerContent = {
                DrawerContent(
                    navController = navController,
                    items = items,
                    drawerContent = drawerContent
                )
            }
        ) {
            // Main content would go here
        }
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            modifier = modifier
        ) {
            DrawerContent(
                navController = navController,
                items = items,
                drawerContent = drawerContent
            )
        }
    }
}

/**
 * Drawer content
 */
@Composable
private fun DrawerContent(
    navController: NavController,
    items: List<ResponsiveNavItem>,
    drawerContent: @Composable (ColumnScope.() -> Unit)?
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination
    
    ModalDrawerSheet {
        // Header
        Text(
            text = "Our Cookbook",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(CookbookSpacing.medium)
        )
        
        Divider()
        
        // Custom drawer content
        drawerContent?.invoke(this)
        
        // Navigation items
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            
            NavigationDrawerItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                modifier = Modifier.padding(horizontal = CookbookSpacing.small)
            )
        }
    }
}

// ============================================================================
// RESPONSIVE LAYOUT COMPONENTS
// ============================================================================

/**
 * Responsive scaffold that adapts to screen size
 */
@Composable
fun ResponsiveScaffold(
    title: String,
    navController: NavController,
    navigationItems: List<ResponsiveNavItem>? = null,
    modifier: Modifier = Modifier,
    floatingActionButton: @Composable () -> Unit = {},
    topBar: @Composable () -> Unit = {
        ResponsiveAppBar(
            title = title,
            onNavigationClick = { /* TODO */ }
        )
    },
    bottomBar: @Composable () -> Unit = {
        if (navigationItems != null) {
            ResponsiveNavigation(
                navController = navController,
                items = navigationItems
            )
        }
    },
    drawerContent: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val navigationType = responsiveNavigationType()
    
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        floatingActionButton = floatingActionButton,
        bottomBar = if (navigationType == ResponsiveNavigationType.BOTTOM_NAVIGATION) bottomBar else null,
        content = { paddingValues ->
            content(paddingValues)
        }
    )
}

/**
 * Responsive card that adapts its size based on screen
 */
@Composable
fun ResponsiveCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val cardModifier = when (ScreenSize.current()) {
        ScreenSize.COMPACT -> modifier
            .fillMaxWidth()
            .padding(CookbookSpacing.small)
        ScreenSize.MEDIUM -> modifier
            .width(300.dp)
            .padding(CookbookSpacing.small)
        ScreenSize.EXPANDED -> modifier
            .width(350.dp)
            .padding(CookbookSpacing.medium)
    }
    
    ElevatedCard(
        modifier = cardModifier,
        onClick = onClick
    ) {
        content()
    }
}

/**
 * Responsive grid for recipe cards
 */
@Composable
fun ResponsiveRecipeGrid(
    items: List<Any>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = CookbookSpacing.medium.toPaddingValues(),
    itemContent: @Composable (Any) -> Unit
) {
    val columns = ResponsiveGrid.recipeColumns()
    val spacing = ResponsiveGrid.gridSpacing()
    
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(columns),
        state = rememberLazyGridState(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        items(items.size) { index ->
            itemContent(items[index])
        }
    }
}

/**
 * Responsive list for recipes
 */
@Composable
fun ResponsiveRecipeList(
    items: List<Any>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = CookbookSpacing.medium.toPaddingValues(),
    itemContent: @Composable (Any) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(CookbookSpacing.small)
    ) {
        items(items.size) { index ->
            itemContent(items[index])
        }
    }
}

/**
 * Responsive image component
 */
@Composable
fun ResponsiveImage(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val imageModifier = when (ScreenSize.current()) {
        ScreenSize.COMPACT -> modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
        ScreenSize.MEDIUM -> modifier
            .fillMaxWidth(0.8f)
            .aspectRatio(16f / 9f)
        ScreenSize.EXPANDED -> modifier
            .size(ResponsiveDimensions.imageSize())
            .aspectRatio(1f)
    }
    
    Box(
        modifier = imageModifier,
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// ============================================================================
// RESPONSIVE DIALOG
// ============================================================================

/**
 * Responsive dialog that adapts to screen size
 */
@Composable
fun ResponsiveDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val maxWidth = when (ScreenSize.current()) {
        ScreenSize.COMPACT -> 300.dp
        ScreenSize.MEDIUM -> 400.dp
        ScreenSize.EXPANDED -> 500.dp
    }
    
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            modifier = modifier
                .widthIn(min = 280.dp, max = maxWidth)
                .padding(ResponsiveDimensions.horizontalPadding())
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
        ) {
            content()
        }
    }
}

// ============================================================================
// RESPONSIVE FORM COMPONENTS
// ============================================================================

/**
 * Responsive form layout
 */
@Composable
fun ResponsiveForm(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ResponsiveDimensions.padding()),
        verticalArrangement = Arrangement.spacedBy(ResponsiveDimensions.formFieldSpacing()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

/**
 * Responsive button row
 */
@Composable
fun ResponsiveButtonRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val arrangement = when (ScreenSize.current()) {
        ScreenSize.COMPACT -> Arrangement.spacedBy(CookbookSpacing.small, Alignment.End)
        else -> Arrangement.spacedBy(CookbookSpacing.medium, Alignment.End)
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = CookbookSpacing.medium),
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

// ============================================================================
// RESPONSIVE TEXT COMPONENTS
// ============================================================================

/**
 * Responsive text component
 */
@Composable
fun ResponsiveText(
    text: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium
) {
    val responsiveStyle = when (ScreenSize.current()) {
        ScreenSize.COMPACT -> MaterialTheme.typography.bodySmall
        ScreenSize.MEDIUM -> MaterialTheme.typography.bodyMedium
        ScreenSize.EXPANDED -> MaterialTheme.typography.bodyLarge
    }
    
    Text(
        text = text,
        modifier = modifier,
        style = style.merge(responsiveStyle)
    )
}

/**
 * Responsive title component
 */
@Composable
fun ResponsiveTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    val style = when (ScreenSize.current()) {
        ScreenSize.COMPACT -> MaterialTheme.typography.titleMedium
        ScreenSize.MEDIUM -> MaterialTheme.typography.titleLarge
        ScreenSize.EXPANDED -> MaterialTheme.typography.headlineMedium
    }
    
    Text(
        text = text,
        modifier = modifier,
        style = style
    )
}

// ============================================================================
// FOLDABLE DEVICE SUPPORT
// ============================================================================

/**
 * Responsive foldable layout
 */
@Composable
fun ResponsiveFoldableLayout(
    displayFeatures: List<androidx.window.layout.DisplayFeature>,
    modifier: Modifier = Modifier,
    unfoldedContent: @Composable () -> Unit,
    foldedContent: @Composable () -> Unit
) {
    val isFolded = remember(displayFeatures) {
        FoldableSupport.isFolded(FoldableSupport.getFoldingFeatures(displayFeatures))
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        if (isFolded) {
            foldedContent()
        } else {
            unfoldedContent()
        }
    }
}

/**
 * Check if current device is foldable
 */
@Composable
fun isFoldableDevice(displayFeatures: List<androidx.window.layout.DisplayFeature>): Boolean {
    return FoldableSupport.hasFoldingFeature(displayFeatures)
}
