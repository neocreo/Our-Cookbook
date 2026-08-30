@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.ourcookbook.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ourcookbook.ui.theme.CookbookSpacing
import com.ourcookbook.ui.theme.CookbookTypography

/**
 * Bottom navigation bar for main app navigation
 */
@Composable
fun CookbookBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(
            route = "home",
            icon = Icons.Default.Home,
            label = "Home",
            contentDescription = "Home"
        ),
        BottomNavItem(
            route = "recipes",
            icon = Icons.Default.List,
            label = "Recipes",
            contentDescription = "Recipes"
        ),
        BottomNavItem(
            route = "search",
            icon = Icons.Default.Search,
            label = "Search",
            contentDescription = "Search"
        ),
        BottomNavItem(
            route = "cookbooks",
            icon = Icons.Default.Folder,
            label = "Cookbooks",
            contentDescription = "Cookbooks"
        ),
        BottomNavItem(
            route = "settings",
            icon = Icons.Default.Settings,
            label = "Settings",
            contentDescription = "Settings"
        )
    )

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.contentDescription
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )
        }
    }
}

/**
 * Top app bar with title and actions
 */
@Composable
fun CookbookTopAppBar(
    title: String,
    navigationIcon: ImageVector? = null,
    onNavigationClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(title, style = CookbookTypography.headlineSmall) },
        navigationIcon = navigationIcon?.let {
            {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = it,
                        contentDescription = "Navigate back"
                    )
                }
            }
        },
        actions = actions,
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Top app bar with search functionality
 */
@Composable
fun CookbookSearchAppBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            CookbookSearchField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                onSearch = { onSearch() },
                onClear = { onSearchQueryChange("") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Navigation rail for tablet/desktop layouts
 */
@Composable
fun CookbookNavigationRail(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(
            route = "home",
            icon = Icons.Default.Home,
            label = "Home",
            contentDescription = "Home"
        ),
        BottomNavItem(
            route = "recipes",
            icon = Icons.Default.List,
            label = "Recipes",
            contentDescription = "Recipes"
        ),
        BottomNavItem(
            route = "search",
            icon = Icons.Default.Search,
            label = "Search",
            contentDescription = "Search"
        ),
        BottomNavItem(
            route = "cookbooks",
            icon = Icons.Default.Folder,
            label = "Cookbooks",
            contentDescription = "Cookbooks"
        ),
        BottomNavItem(
            route = "settings",
            icon = Icons.Default.Settings,
            label = "Settings",
            contentDescription = "Settings"
        )
    )

    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { item ->
            NavigationRailItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.contentDescription
                    )
                },
                label = { Text(item.label) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )
        }
    }
}

/**
 * Bottom app bar with floating action button
 */
@Composable
fun CookbookBottomAppBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onFabClick: () -> Unit,
    fabIcon: ImageVector = Icons.Default.Add,
    fabContentDescription: String = "Add",
    modifier: Modifier = Modifier
) {
    BottomAppBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        CookbookBottomNavigation(
            currentRoute = currentRoute,
            onNavigate = onNavigate
        )
    }
}

/**
 * Data class for bottom navigation items
 */
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String,
    val contentDescription: String
)

/**
 * Preview for navigation components
 */
@Preview(showBackground = true)
@Composable
fun NavigationPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(CookbookSpacing.medium)
        ) {
            CookbookTopAppBar(
                title = "Our Cookbook",
                navigationIcon = Icons.Default.Menu,
                onNavigationClick = {},
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync"
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(CookbookSpacing.medium))
            
            CookbookBottomNavigation(
                currentRoute = "home",
                onNavigate = {}
            )
        }
    }
}
