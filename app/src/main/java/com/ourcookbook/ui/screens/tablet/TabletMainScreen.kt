@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ourcookbook.ui.screens.tablet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.ui.components.ResponsiveAppBar
import com.ourcookbook.ui.components.ResponsiveNavigation
import com.ourcookbook.ui.components.ResponsiveNavItem
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.DeviceType
import com.ourcookbook.ui.theme.ScreenSize
import com.ourcookbook.ui.theme.responsiveNavigationType
import com.ourcookbook.ui.viewmodel.MainViewModel

/**
 * Tablet-optimized Main Screen
 * Task 2.2.10: Responsive design for tablets
 *
 * Features:
 * - Navigation rail for tablet navigation
 * - Split-pane layout for large screens
 * - Adaptive content based on screen size
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabletMainScreen(
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val navigationItems = listOf(
        ResponsiveNavItem(
            route = "home",
            label = "Home",
            icon = Icons.Default.Home
        ),
        ResponsiveNavItem(
            route = "recipes",
            label = "Recipes",
            icon = Icons.Default.Home
        ),
        ResponsiveNavItem(
            route = "categories",
            label = "Categories",
            icon = Icons.Default.Menu
        ),
        ResponsiveNavItem(
            route = "favorites",
            label = "Favorites",
            icon = Icons.Default.Favorite
        ),
        ResponsiveNavItem(
            route = "search",
            label = "Search",
            icon = Icons.Default.Search
        ),
        ResponsiveNavItem(
            route = "settings",
            label = "Settings",
            icon = Icons.Default.Settings
        )
    )
    
    // Determine navigation type based on screen size
    val navigationType = responsiveNavigationType()
    
    // For tablets, use a layout with navigation rail and main content
    when (navigationType) {
        com.ourcookbook.ui.components.ResponsiveNavigationType.NAVIGATION_RAIL -> {
            TabletLayoutWithRail(
                navController = navController,
                navigationItems = navigationItems,
                snackbarHostState = snackbarHostState,
                viewModel = viewModel
            )
        }
        com.ourcookbook.ui.components.ResponsiveNavigationType.NAVIGATION_DRAWER -> {
            TabletLayoutWithDrawer(
                navController = navController,
                navigationItems = navigationItems,
                snackbarHostState = snackbarHostState,
                viewModel = viewModel
            )
        }
        else -> {
            // Fallback to standard layout
            StandardTabletLayout(
                navController = navController,
                navigationItems = navigationItems,
                snackbarHostState = snackbarHostState,
                viewModel = viewModel
            )
        }
    }
}

/**
 * Tablet layout with navigation rail
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabletLayoutWithRail(
    navController: NavController,
    navigationItems: List<ResponsiveNavItem>,
    snackbarHostState: SnackbarHostState,
    viewModel: MainViewModel
) {
    Row(
        modifier = Modifier.fillMaxSize()
    ) {
        // Navigation rail
        NavigationRail(
            modifier = Modifier.width(80.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            // Header
            ElevatedCard(
                modifier = Modifier
                    .padding(CookbookSpacing.small)
                    .height(64.dp)
                    .width(64.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Menu",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(CookbookSpacing.medium))
            
            // Navigation items
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry.value?.destination
            
            navigationItems.forEach { item ->
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
                    label = { Text(item.label) },
                    modifier = Modifier.padding(vertical = CookbookSpacing.small)
                )
            }
            
            Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        }
        
        // Main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = CookbookSpacing.small)
        ) {
            // Content based on current destination
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            
            when (currentRoute) {
                "recipes" -> TabletRecipeListScreen(navController)
                "categories" -> TabletCategoryScreen(navController)
                "favorites" -> TabletFavoritesScreen(navController)
                "search" -> TabletSearchScreen(navController)
                "settings" -> TabletSettingsScreen(navController)
                else -> TabletHomeScreen(navController)
            }
        }
    }
}

/**
 * Tablet layout with navigation drawer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabletLayoutWithDrawer(
    navController: NavController,
    navigationItems: List<ResponsiveNavItem>,
    snackbarHostState: SnackbarHostState,
    viewModel: MainViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                navController = navController,
                items = navigationItems
            )
        }
    ) {
        Scaffold(
            topBar = {
                TabletTopAppBar(
                    onMenuClick = { /* TODO */ },
                    navController = navController
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier.padding(CookbookSpacing.medium)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                
                when (currentRoute) {
                    "recipes" -> TabletRecipeListScreen(navController)
                    "categories" -> TabletCategoryScreen(navController)
                    "favorites" -> TabletFavoritesScreen(navController)
                    "search" -> TabletSearchScreen(navController)
                    "settings" -> TabletSettingsScreen(navController)
                    else -> TabletHomeScreen(navController)
                }
            }
        }
    }
}

/**
 * Standard tablet layout (fallback)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardTabletLayout(
    navController: NavController,
    navigationItems: List<ResponsiveNavItem>,
    snackbarHostState: SnackbarHostState,
    viewModel: MainViewModel
) {
    Scaffold(
        topBar = {
            TabletTopAppBar(
                onMenuClick = { /* TODO */ },
                navController = navController
            )
        },
        bottomBar = {
            ResponsiveNavigation(
                navController = navController,
                items = navigationItems
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO */ },
                modifier = Modifier.padding(CookbookSpacing.medium)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            
            when (currentRoute) {
                "recipes" -> TabletRecipeListScreen(navController)
                "categories" -> TabletCategoryScreen(navController)
                "favorites" -> TabletFavoritesScreen(navController)
                "search" -> TabletSearchScreen(navController)
                "settings" -> TabletSettingsScreen(navController)
                else -> TabletHomeScreen(navController)
            }
        }
    }
}

/**
 * Tablet top app bar
 */
@Composable
fun TabletTopAppBar(
    onMenuClick: () -> Unit,
    navController: NavController
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val title = when (currentRoute) {
        "recipes" -> "Recipes"
        "categories" -> "Categories"
        "favorites" -> "Favorites"
        "search" -> "Search"
        "settings" -> "Settings"
        else -> "Our Cookbook"
    }
    
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        colors = TopAppBarDefaults.largeTopAppBarColors()
    )
}

/**
 * Drawer content for tablet
 */
@Composable
fun DrawerContent(
    navController: NavController,
    items: List<ResponsiveNavItem>
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination
    
    ModalDrawerSheet(
        modifier = Modifier.width(280.dp)
    ) {
        // Header
        Text(
            text = "Our Cookbook",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(CookbookSpacing.medium)
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.small))
        
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

/**
 * Tablet home screen
 */
@Composable
fun TabletHomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CookbookSpacing.medium)
    ) {
        Text(
            text = "Welcome to Our Cookbook",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        Text(
            text = "Select a section from the navigation to get started",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Tablet category screen
 */
@Composable
fun TabletCategoryScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CookbookSpacing.medium)
    ) {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        Text(
            text = "Browse recipes by category",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Tablet favorites screen
 */
@Composable
fun TabletFavoritesScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CookbookSpacing.medium)
    ) {
        Text(
            text = "Your Favorites",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        Text(
            text = "View your saved recipes",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Tablet search screen
 */
@Composable
fun TabletSearchScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CookbookSpacing.medium)
    ) {
        Text(
            text = "Search Recipes",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        Text(
            text = "Find recipes by name, ingredient, or category",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Tablet settings screen
 */
@Composable
fun TabletSettingsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CookbookSpacing.medium)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(CookbookSpacing.medium))
        
        Text(
            text = "Configure app settings",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Preview for tablet main screen
 */
@Preview(showBackground = true)
@Composable
fun TabletMainScreenPreview() {
    MaterialTheme {
        Surface {
            TabletMainScreen(
                navController = rememberNavController()
            )
        }
    }
}
