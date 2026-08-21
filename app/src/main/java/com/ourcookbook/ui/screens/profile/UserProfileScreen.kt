package com.ourcookbook.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ourcookbook.ui.theme.CookbookTheme
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

/**
 * User Profile Screen
 * Task 2.1.10: User Profile Screen Implementation
 * 
 * Displays user profile information with device details and preferences
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel = hiltViewModel(),
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Handle error and success messages
    state.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(UserProfileEvent.ClearError)
        }
    }

    state.successMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(UserProfileEvent.ClearSuccess)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (state.isLoading && state.device == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Loading profile...")
            }
        } else {
            ProfileContent(
                state = state,
                onEvent = viewModel::onEvent,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun ProfileContent(
    state: UserProfileState,
    onEvent: (UserProfileEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header
        ProfileHeader(state = state, onEvent = onEvent)

        // Device Information Card
        DeviceInfoCard(state = state)

        // Preferences Card
        PreferencesCard(state = state)

        // Actions
        if (state.isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { onEvent(UserProfileEvent.CancelEditing) }
                ) {
                    Text("Cancel")
                }
                Spacer(Modifier.size(8.dp))
                Button(
                    onClick = { onEvent(UserProfileEvent.SaveProfile) },
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Save")
                    }
                }
            }
        } else {
            Button(
                onClick = { onEvent(UserProfileEvent.StartEditing) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
                Spacer(Modifier.size(8.dp))
                Text("Edit Profile")
            }
        }
    }
}

@Composable
fun ProfileHeader(
    state: UserProfileState,
    onEvent: (UserProfileEvent) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = "Profile",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.size(16.dp))
        Column {
            if (state.isEditing) {
                OutlinedTextField(
                    value = state.editedDeviceName,
                    onValueChange = { onEvent(UserProfileEvent.UpdateDeviceName(it)) },
                    label = { Text("Device Name") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true
                )
            } else {
                Text(
                    text = state.device?.name ?: "Unknown Device",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Device ID: ${state.device?.deviceId ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun DeviceInfoCard(state: UserProfileState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Device Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            InfoRow("Device ID", state.device?.deviceId ?: "N/A")
            InfoRow("Created", state.device?.createdAt?.let { 
                DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(it) 
            } ?: "N/A")
            InfoRow("Last Seen", state.device?.lastSeenAt?.let { 
                DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(it) 
            } ?: "N/A")
        }
    }
}

@Composable
fun PreferencesCard(state: UserProfileState) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            InfoRow("Theme", state.preferences?.themeDisplayName ?: "N/A")
            InfoRow("Font Size", state.preferences?.fontSizeDisplayName ?: "N/A")
            InfoRow("Sync Frequency", state.preferences?.syncFrequencyDisplayName ?: "N/A")
            InfoRow("Language", state.preferences?.languageDisplayName ?: "N/A")
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UserProfileScreenPreview() {
    CookbookTheme {
        UserProfileScreen(
            navController = rememberNavController()
        )
    }
}
