package com.ourcookbook.ui.screens.cookbook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.ui.components.CookbookPrimaryButton
import com.ourcookbook.ui.navigation.Route
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.viewmodel.CookbookManagementViewModel

/**
 * Cookbook Edit Screen
 * Task 1.9: Complete Navigation Setup
 * 
 * Handles cookbook creation and editing
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookbookEditScreen(
    viewModel: CookbookManagementViewModel,
    cookbookId: String? = null,
    isCreating: Boolean,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(if (isCreating) "Create Cookbook" else "Edit Cookbook") 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isCreating) "Create a new cookbook" else "Edit cookbook",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
            )
            
            // Cookbook edit form would go here
            // For now, just show a message
            Text(
                text = "Cookbook editing functionality will be implemented in a future task.",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CookbookPrimaryButton(
                text = if (isCreating) "Create Cookbook" else "Save Changes",
                onClick = { 
                    if (isCreating) {
                        viewModel.showCreateCookbookDialog()
                    } else {
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            if (!isCreating) {
                Spacer(modifier = Modifier.height(8.dp))
                CookbookPrimaryButton(
                    text = "Cancel",
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CookbookEditScreenPreview() {
    CookbookTheme {
        CookbookEditScreen(
            viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
            isCreating = true,
            navController = rememberNavController()
        )
    }
}
