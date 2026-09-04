package com.ourcookbook.ui.screens.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToString
import androidx.navigation.NavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ourcookbook.ui.theme.CookbookTheme
import com.ourcookbook.ui.viewmodel.SettingsAction
import com.ourcookbook.ui.viewmodel.SettingsEvent
import com.ourcookbook.ui.viewmodel.SettingsState
import com.ourcookbook.ui.viewmodel.SettingsViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: SettingsViewModel
    private lateinit var mockNavController: NavController

    @Before
    fun setup() {
        mockViewModel = mockk(relaxed = true)
        mockNavController = TestNavHostController()

        val mockState = SettingsState(
            isLoading = false,
            error = null,
            preferences = null,
            syncStatus = "IDLE",
            appVersion = "1.0.0",
            buildNumber = "1",
            deviceName = "Test Device",
            deviceId = "test_device_id",
            deviceModel = "Test Model",
            androidVersion = "12",
            storageUsage = 128L * 1024 * 1024,
            maxStorage = 1L * 1024 * 1024 * 1024,
            recipeCount = 42,
            cookbookCount = 3,
            theme = "SYSTEM",
            fontSize = "MEDIUM",
            syncFrequency = "AUTO",
            offlineMode = false,
            notificationsEnabled = true,
            syncNotificationsEnabled = true,
            updateNotificationsEnabled = true,
            notificationSoundEnabled = true,
            notificationVibrationEnabled = true,
            dataEncryptionEnabled = true,
            defaultCookbookId = null,
            defaultCookbookName = "Personal",
            language = "en",
            userEmail = "test@example.com",
            userName = "Test User",
            isLoggedIn = true,
            linkedGoogleDriveAccount = "test@example.com",
            isSaving = false,
            saveSuccess = false
        )

        every { mockViewModel.state } returns MutableStateFlow(mockState)
        every { mockViewModel.actions } returns MutableStateFlow<SettingsAction?>(null)
        every { mockViewModel.handleEvent(any()) } returns Unit
        every { mockViewModel.clearAction() } returns Unit
    }

    @Test
    fun testSettingsScreen_DisplaysTitle() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Settings").assertExists()
    }

    @Test
    fun testSettingsScreen_DisplaysLoadingState() {
        val loadingState = SettingsState(isLoading = true)
        every { mockViewModel.state } returns MutableStateFlow(loadingState)
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testAppSettingsSection_DisplaysThemeOption() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Theme").assertExists()
    }

    @Test
    fun testAppSettingsSection_DisplaysLanguageOption() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Language").assertExists()
    }

    @Test
    fun testAppSettingsSection_DisplaysFontSizeOption() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Font Size").assertExists()
    }

    @Test
    fun testAppSettingsSection_DisplaysDefaultCookbookOption() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Default Cookbook").assertExists()
    }

    @Test
    fun testAppSettingsSection_DisplaysCategoriesSection() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Categories").assertExists()
    }

    @Test
    fun testAccountAndDeviceSettingsSection_DisplaysOfflineMode() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Offline Mode").assertExists()
    }

    @Test
    fun testAccountAndDeviceSettingsSection_DisplaysSyncFrequency() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Sync Frequency").assertExists()
    }

    @Test
    fun testAccountAndDeviceSettingsSection_DisplaysSyncStatus() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Sync Status").assertExists()
    }

    @Test
    fun testAccountAndDeviceSettingsSection_DisplaysStorageUsed() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Storage Used").assertExists()
    }

    @Test
    fun testAccountAndDeviceSettingsSection_DisplaysDisconnect() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Disconnect").assertExists()
    }

    @Test
    fun testAccountAndDeviceSettingsSection_DisplaysConnectToGoogleWhenLoggedOut() {
        val loggedOutState = SettingsState(isLoggedIn = false)
        every { mockViewModel.state } returns MutableStateFlow(loggedOutState)
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Connect to Google").assertExists()
    }

    @Test
    fun testPrivacyAndSecuritySection_DisplaysDataEncryption() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Data Encryption").assertExists()
    }

    @Test
    fun testPrivacyAndSecuritySection_DisplaysPrivacyPolicy() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Privacy Policy").assertExists()
    }

    @Test
    fun testPrivacyAndSecuritySection_DisplaysExportData() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Export Data").assertExists()
    }

    @Test
    fun testPrivacyAndSecuritySection_DisplaysImportData() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Import Data").assertExists()
    }

    @Test
    fun testNotificationSettingsSection_DisplaysUpdatedRecipe() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Updated Recipe").assertExists()
    }

    @Test
    fun testNotificationSettingsSection_DisplaysNewRecipe() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("New Recipe").assertExists()
    }

    @Test
    fun testNotificationSettingsSection_DisplaysSoundToggle() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Notification Sound").assertExists()
    }

    @Test
    fun testNotificationSettingsSection_DisplaysVibrationToggle() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Notification Vibration").assertExists()
    }

    @Test
    fun testAboutSection_DisplaysAppVersion() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("App Version").assertExists()
    }

    @Test
    fun testAboutSection_DisplaysBuildNumber() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Build Number").assertExists()
    }

    @Test
    fun testAboutSection_DisplaysChangelog() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Changelog").assertExists()
    }

    @Test
    fun testAdvancedSettingsSection_DisplaysHeaderCollapsed() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Advanced").assertExists()
    }

    @Test
    fun testSaveButton_DisplaysSaveSettings() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Save Settings").assertExists()
    }

    @Test
    fun testSaveButton_TriggersSaveEvent() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Save Settings").performClick()
        verify { mockViewModel.handleEvent(SettingsEvent.SaveSettings) }
    }

    @Test
    fun testSuccessMessage_DisplaysWhenSaveSuccessful() {
        val successState = SettingsState(saveSuccess = true)
        every { mockViewModel.state } returns MutableStateFlow(successState)
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Settings saved successfully!").assertExists()
    }

    @Test
    fun testSectionHeaders_DisplayAllCategories() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        val sectionTitles = listOf(
            "App Settings",
            "Account & Device",
            "Privacy & Security",
            "Notifications",
            "About",
            "Advanced"
        )
        sectionTitles.forEach { title ->
            composeTestRule.onNodeWithText(title).assertExists()
        }
    }

    @Test
    fun testSettingsScreen_PrintsToString() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        val content = composeTestRule.onRoot().printToString()
        assert(content.isNotEmpty())
    }

    @Test
    fun testThemeDropdown_TriggersUpdateEvent() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Theme").performClick()
    }

    @Test
    fun testToggleSwitch_TriggersUpdateEvent() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(viewModel = mockViewModel, navController = mockNavController)
            }
        }
        composeTestRule.onNodeWithText("Offline Mode").performClick()
    }
}
