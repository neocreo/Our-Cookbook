package com.ourcookbook.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.ourcookbook.ui.viewmodel.AuthEvent
import com.ourcookbook.ui.viewmodel.AuthState

/**
 * Authentication Screen
 * Task 1.9: Complete Navigation Setup
 * 
 * Handles user authentication and device registration flow
 */

@Composable
fun AuthScreen(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit,
    onNavigateToDeviceRegistration: (String?) -> Unit,
    onNavigateToHome: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (state) {
            is AuthState.Loading -> {
                LoadingState()
            }
            is AuthState.Idle -> {
                AuthContent(
                    onStartRegistration = { onEvent(AuthEvent.StartDeviceRegistration) },
                    onSkipRegistration = { onEvent(AuthEvent.SkipRegistration) }
                )
            }
            is AuthState.Authenticated -> {
                // Should be handled by navigation
            }
            is AuthState.DeviceRegistrationRequired -> {
                // Navigate to device registration
                onNavigateToDeviceRegistration(null)
            }
            is AuthState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = { onEvent(AuthEvent.RetryAuthentication) }
                )
            }
        }
    }
}

@Composable
fun AuthContent(
    onStartRegistration: () -> Unit,
    onSkipRegistration: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to Our Cookbook",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Your personal recipe manager",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "To get started, please register your device",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        CookbookPrimaryButton(
            text = "Register Device",
            onClick = onStartRegistration,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        
        Button(
            onClick = onSkipRegistration,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Skip for now")
        }
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Authentication Error",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        CookbookPrimaryButton(
            text = "Retry",
            onClick = onRetry
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    CookbookTheme {
        AuthScreen(
            state = AuthState.Idle,
            onEvent = {},
            onNavigateToDeviceRegistration = {},
            onNavigateToHome = {}
        )
    }
}
