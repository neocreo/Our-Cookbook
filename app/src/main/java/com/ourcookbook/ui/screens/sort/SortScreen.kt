package com.ourcookbook.ui.screens.sort

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.viewmodel.SortViewModel
import com.ourcookbook.ui.viewmodel.SearchSortOption

/**
 * Sort Screen
 * Task 2.2.03: Advanced Sorting Options
 * Allows users to select sorting options for recipe lists
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortScreen(
    viewModel: SortViewModel = hiltViewModel(),
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadCurrentSortOption()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sort Options") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Current selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Sort,
                    contentDescription = "Current sort",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.size(16.dp))
                Column {
                    Text(
                        text = "Current Sort",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = state.selectedSortOption.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Sort options
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(SearchSortOption.values()) { sortOption ->
                    SortOptionCard(
                        sortOption = sortOption,
                        isSelected = state.selectedSortOption == sortOption,
                        onClick = { viewModel.selectSortOption(sortOption) }
                    )
                    Spacer(Modifier.size(8.dp))
                }
            }
        }
    }
}

@Composable
fun SortOptionCard(
    sortOption: SearchSortOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = sortOption.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = sortOption.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Extension properties for SearchSortOption
val SearchSortOption.displayName: String
    get() = when (this) {
        SearchSortOption.RELEVANCE -> "Relevance"
        SearchSortOption.TITLE_ASC -> "Title A-Z"
        SearchSortOption.TITLE_DESC -> "Title Z-A"
        SearchSortOption.DATE_NEWEST -> "Newest First"
        SearchSortOption.DATE_OLDEST -> "Oldest First"
        SearchSortOption.RATING_HIGH -> "Highest Rated"
        SearchSortOption.RATING_LOW -> "Lowest Rated"
        SearchSortOption.TIME_SHORTEST -> "Shortest Cook Time"
        SearchSortOption.TIME_LONGEST -> "Longest Cook Time"
    }

val SearchSortOption.description: String
    get() = when (this) {
        SearchSortOption.RELEVANCE -> "Most relevant results first"
        SearchSortOption.TITLE_ASC -> "Sort alphabetically A-Z"
        SearchSortOption.TITLE_DESC -> "Sort alphabetically Z-A"
        SearchSortOption.DATE_NEWEST -> "Newest recipes first"
        SearchSortOption.DATE_OLDEST -> "Oldest recipes first"
        SearchSortOption.RATING_HIGH -> "Highest rated recipes first"
        SearchSortOption.RATING_LOW -> "Lowest rated recipes first"
        SearchSortOption.TIME_SHORTEST -> "Shortest cook time first"
        SearchSortOption.TIME_LONGEST -> "Longest cook time first"
    }

@Preview(showBackground = true)
@Composable
fun SortScreenPreview() {
    CookbookTheme {
        SortScreen(navController = rememberNavController())
    }
}
