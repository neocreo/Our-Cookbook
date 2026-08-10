package com.ourcookbook.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ourcookbook.ui.components.CookbookPrimaryButton
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.theme.CookbookTheme

/**
 * Google Drive Authentication Screen
 * Task 1.9: Complete Navigation Setup
 * 
 * Handles Google Drive authentication for cloud sync
 */

@Composable
fun DriveAuthScreen(
    onAuthSuccess: () -> Unit,
    onAuthFailed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Connect to Google Drive",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "To enable cloud sync and backup your recipes, please connect to Google Drive",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // In production, this would trigger the actual Google Sign-In flow
        CookbookPrimaryButton(
            text = "Connect with Google",
            onClick = { 
                // Simulate successful authentication
                // In production: startActivityForResult(googleSignInIntent)
                onAuthSuccess()
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CookbookPrimaryButton(
            text = "Skip for now",
            onClick = onAuthFailed,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}

@Composable
fun DriveAuthLoadingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoadingState()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Authenticating with Google...",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DriveAuthScreenPreview() {
    CookbookTheme {
        DriveAuthScreen(
            onAuthSuccess = {},
            onAuthFailed = {}
        )
    }
}
