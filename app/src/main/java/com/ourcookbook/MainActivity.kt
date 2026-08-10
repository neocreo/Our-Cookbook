package com.ourcookbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.ui.navigation.CookbookNavHost
import com.ourcookbook.ui.theme.CookbookTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for Cookbook Android App
 * Task 1.9: Complete Navigation Setup
 * 
 * Entry point for the application with Compose navigation
 * Handles authentication flow and main app navigation
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            CookbookTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Start with authentication flow
                    // The navController will handle navigation between auth and main app
                    CookbookNavHost(
                        navController = navController,
                        startDestination = com.ourcookbook.ui.navigation.Route.AUTH
                    )
                }
            }
        }
    }
}
