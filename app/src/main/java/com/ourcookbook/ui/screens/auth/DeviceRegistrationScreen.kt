package com.ourcookbook.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ourcookbook.ui.components.CookbookPrimaryButton
import com.ourcookbook.ui.components.LoadingState
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.viewmodel.DeviceRegistrationEvent
import com.ourcookbook.ui.viewmodel.DeviceRegistrationState

/**
 * Device Registration Screen
 * Task 1.9: Complete Navigation Setup
 * 
 * Handles device registration process for new users
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceRegistrationScreen(
    state: DeviceRegistrationState,
    onEvent: (DeviceRegistrationEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHome: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with back button
        if (state.deviceId == null) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
        
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                state.isLoading -> {
                    LoadingState()
                }
                state.registrationSuccess -> {
                    SuccessContent(
                        onNavigateToHome = { onNavigateToHome(state.deviceId ?: "") }
                    )
                }
                else -> {
                    RegistrationForm(
                        state = state,
                        onEvent = onEvent
                    )
                }
            }
            
            // Show error message if present
            state.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
            
            // Show loading indicator during registration
            if (state.isRegistering) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun RegistrationForm(
    state: DeviceRegistrationState,
    onEvent: (DeviceRegistrationEvent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Register Your Device",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Give your device a name to identify it",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = state.deviceName,
            onValueChange = { 
                onEvent(DeviceRegistrationEvent.UpdateDeviceName(it)) 
            },
            label = { Text("Device Name") },
            placeholder = { Text("My Tablet, Kitchen Display, etc.") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        CookbookPrimaryButton(
            text = "Register Device",
            onClick = { onEvent(DeviceRegistrationEvent.RegisterDevice) },
            enabled = state.isFormValid && !state.isRegistering,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}

@Composable
fun SuccessContent(
    onNavigateToHome: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Device Registered Successfully!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "You're all set to start using Our Cookbook",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        CookbookPrimaryButton(
            text = "Continue to Home",
            onClick = onNavigateToHome,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceRegistrationScreenPreview() {
    CookbookTheme {
        DeviceRegistrationScreen(
            state = DeviceRegistrationState(),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToHome = {}
        )
    }
}
