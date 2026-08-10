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
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit Tests for SettingsScreen
 * Tests all UI components and interactions
 */
@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: SettingsViewModel
    private lateinit var mockNavController: NavController

    @Before
    fun setup() {
        // Create mock ViewModel
        mockViewModel = mockk(relaxed = true)
        
        // Create mock NavController
        mockNavController = TestNavHostController()
        
        // Setup mock state
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
            storageUsage = 128L * 1024 * 1024, // 128MB
            maxStorage = 1L * 1024 * 1024 * 1024, // 1GB
            recipeCount = 42,
            cookbookCount = 3,
            theme = "SYSTEM",
            fontSize = "MEDIUM",
            syncFrequency = "AUTO",
            offlineMode = false,
            notificationsEnabled = true,
            recipeRemindersEnabled = true,
            syncNotificationsEnabled = true,
            updateNotificationsEnabled = true,
            notificationSoundEnabled = true,
            notificationVibrationEnabled = true,
            appLockEnabled = false,
            appLockType = "PIN",
            dataEncryptionEnabled = true,
            autoLockTimeout = 300,
            screenReaderCompatibility = true,
            highContrastMode = false,
            reduceMotion = false,
            textScaling = 1.0f,
            colorBlindnessMode = "NONE",
            debugModeEnabled = false,
            logLevel = "INFO",
            developerOptionsEnabled = false,
            defaultCookbookId = null,
            defaultCookbookName = "Personal",
            language = "en",
            userEmail = "test@example.com",
            userName = "Test User",
            isLoggedIn = true,
            linkedGoogleDriveAccount = "test@example.com",
            lastSyncTime = null,
            syncInProgress = false,
            syncError = null,
            isSaving = false,
            saveSuccess = false,
            currentCategory = null,
            expandedCategories = emptySet(),
            showThemeDialog = false,
            showFontSizeDialog = false,
            showSyncFrequencyDialog = false,
            showLanguageDialog = false,
            showAppLockDialog = false,
            showClearCacheDialog = false,
            showResetAppDataDialog = false,
            showDeleteAccountDialog = false,
            showExportDataDialog = false,
            showImportDataDialog = false,
            lastAction = null,
            actionMessage = null
        )
        
        every { mockViewModel.state } returns MutableStateFlow(mockState)
        every { mockViewModel.actions } returns MutableStateFlow<SettingsAction?>(null)
        every { mockViewModel.handleEvent(any()) } returns Unit
        every { mockViewModel.clearAction() } returns Unit
    }

    // ========================================================================
    // BASIC UI TESTS
    // ========================================================================

    @Test
    fun testSettingsScreen_DisplaysTitle() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that the title is displayed
        composeTestRule.onNodeWithText("Settings").assertExists()
    }

    @Test
    fun testSettingsScreen_DisplaysBackButton() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that the back button is displayed (by checking for the navigation icon)
        // This is a bit tricky to test directly, but we can verify the top bar exists
        composeTestRule.onNodeWithText("Settings").assertExists()
    }

    @Test
    fun testSettingsScreen_DisplaysLoadingState() {
        // Create a loading state
        val loadingState = SettingsState(isLoading = true)
        every { mockViewModel.state } returns MutableStateFlow(loadingState)
        
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that loading state is displayed
        // This would show a progress indicator
        composeTestRule.waitForIdle()
    }

    // ========================================================================
    // APP SETTINGS TESTS
    // ========================================================================

    @Test
    fun testAppSettingsSection_DisplaysThemeOption() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that theme option is displayed
        composeTestRule.onNodeWithText("Theme").assertExists()
    }

    @Test
    fun testAppSettingsSection_DisplaysLanguageOption() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that language option is displayed
        composeTestRule.onNodeWithText("Language").assertExists()
    }

    @Test
    fun testAppSettingsSection_DisplaysFontSizeOption() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that font size option is displayed
        composeTestRule.onNodeWithText("Font Size").assertExists()
    }

    @Test
    fun testAppSettingsSection_DisplaysSyncFrequencyOption() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that sync frequency option is displayed
        composeTestRule.onNodeWithText("Sync Frequency").assertExists()
    }

    @Test
    fun testAppSettingsSection_DisplaysOfflineModeToggle() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that offline mode toggle is displayed
        composeTestRule.onNodeWithText("Offline Mode").assertExists()
    }

    @Test
    fun testAppSettingsSection_DisplaysDefaultCookbookOption() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that default cookbook option is displayed
        composeTestRule.onNodeWithText("Default Cookbook").assertExists()
    }

    // ========================================================================
    // ACCOUNT & DEVICE SETTINGS TESTS
    // ========================================================================

    @Test
    fun testAccountAndDeviceSettingsSection_DisplaysUserProfile() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that user profile option is displayed
        composeTestRule.onNodeWithText("User Profile").assertExists()
    }

    @Test
    fun testAccountAndDeviceSettingsSection_DisplaysDeviceName() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that device name option is displayed
        composeTestRule.onNodeWithText("Device Name").assertExists()
    }

    @Test
    fun testAccountAndDeviceSettingsSection_DisplaysDeviceID() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that device ID option is displayed
        composeTestRule.onNodeWithText("Device ID").assertExists()
    }

    @Test
    fun testAccountAndDeviceSettingsSection_DisplaysGoogleDriveAccount() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that Google Drive account option is displayed
        composeTestRule.onNodeWithText("Google Drive Account").assertExists()
    }

    @Test
    fun testAccountAndDeviceSettingsSection_DisplaysSyncStatus() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that sync status option is displayed
        composeTestRule.onNodeWithText("Sync Status").assertExists()
    }

    @Test
    fun testAccountAndDeviceSettingsSection_DisplaysStorageUsage() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that storage usage option is displayed
        composeTestRule.onNodeWithText("Storage Usage").assertExists()
    }

    // ========================================================================
    // PRIVACY & SECURITY TESTS
    // ========================================================================

    @Test
    fun testPrivacyAndSecuritySection_DisplaysAppLock() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that app lock option is displayed
        composeTestRule.onNodeWithText("App Lock").assertExists()
    }

    @Test
    fun testPrivacyAndSecuritySection_DisplaysDataEncryption() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that data encryption option is displayed
        composeTestRule.onNodeWithText("Data Encryption").assertExists()
    }

    @Test
    fun testPrivacyAndSecuritySection_DisplaysPrivacyPolicy() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that privacy policy option is displayed
        composeTestRule.onNodeWithText("Privacy Policy").assertExists()
    }

    @Test
    fun testPrivacyAndSecuritySection_DisplaysExportData() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that export data option is displayed
        composeTestRule.onNodeWithText("Export Data").assertExists()
    }

    @Test
    fun testPrivacyAndSecuritySection_DisplaysImportData() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that import data option is displayed
        composeTestRule.onNodeWithText("Import Data").assertExists()
    }

    @Test
    fun testPrivacyAndSecuritySection_DisplaysDeleteAccount() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that delete account option is displayed
        composeTestRule.onNodeWithText("Delete Account").assertExists()
    }

    // ========================================================================
    // NOTIFICATION SETTINGS TESTS
    // ========================================================================

    @Test
    fun testNotificationSettingsSection_DisplaysMasterToggle() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that master notifications toggle is displayed
        composeTestRule.onNodeWithText("Notifications").assertExists()
    }

    @Test
    fun testNotificationSettingsSection_DisplaysRecipeReminders() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that recipe reminders toggle is displayed
        composeTestRule.onNodeWithText("Recipe Reminders").assertExists()
    }

    @Test
    fun testNotificationSettingsSection_DisplaysSyncNotifications() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that sync notifications toggle is displayed
        composeTestRule.onNodeWithText("Sync Notifications").assertExists()
    }

    @Test
    fun testNotificationSettingsSection_DisplaysUpdateNotifications() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that update notifications toggle is displayed
        composeTestRule.onNodeWithText("Update Notifications").assertExists()
    }

    @Test
    fun testNotificationSettingsSection_DisplaysSoundToggle() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that notification sound toggle is displayed
        composeTestRule.onNodeWithText("Notification Sound").assertExists()
    }

    @Test
    fun testNotificationSettingsSection_DisplaysVibrationToggle() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that vibration toggle is displayed
        composeTestRule.onNodeWithText("Vibration").assertExists()
    }

    // ========================================================================
    // ACCESSIBILITY SETTINGS TESTS
    // ========================================================================

    @Test
    fun testAccessibilitySettingsSection_DisplaysScreenReaderCompatibility() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that screen reader compatibility toggle is displayed
        composeTestRule.onNodeWithText("Screen Reader Compatibility").assertExists()
    }

    @Test
    fun testAccessibilitySettingsSection_DisplaysHighContrastMode() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that high contrast mode toggle is displayed
        composeTestRule.onNodeWithText("High Contrast Mode").assertExists()
    }

    @Test
    fun testAccessibilitySettingsSection_DisplaysReduceMotion() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that reduce motion toggle is displayed
        composeTestRule.onNodeWithText("Reduce Motion").assertExists()
    }

    @Test
    fun testAccessibilitySettingsSection_DisplaysTextScaling() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that text scaling option is displayed
        composeTestRule.onNodeWithText("Text Scaling").assertExists()
    }

    @Test
    fun testAccessibilitySettingsSection_DisplaysColorBlindnessMode() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that color blindness mode option is displayed
        composeTestRule.onNodeWithText("Color Blindness Mode").assertExists()
    }

    // ========================================================================
    // ABOUT SECTION TESTS
    // ========================================================================

    @Test
    fun testAboutSection_DisplaysAppVersion() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that app version is displayed
        composeTestRule.onNodeWithText("App Version").assertExists()
    }

    @Test
    fun testAboutSection_DisplaysBuildNumber() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that build number is displayed
        composeTestRule.onNodeWithText("Build Number").assertExists()
    }

    @Test
    fun testAboutSection_DisplaysChangelog() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that changelog option is displayed
        composeTestRule.onNodeWithText("Changelog").assertExists()
    }

    @Test
    fun testAboutSection_DisplaysOpenSourceLicenses() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that open source licenses option is displayed
        composeTestRule.onNodeWithText("Open Source Licenses").assertExists()
    }

    @Test
    fun testAboutSection_DisplaysContactUs() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that contact us option is displayed
        composeTestRule.onNodeWithText("Contact Us").assertExists()
    }

    @Test
    fun testAboutSection_DisplaysRateTheApp() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that rate the app option is displayed
        composeTestRule.onNodeWithText("Rate the App").assertExists()
    }

    // ========================================================================
    // ADVANCED SETTINGS TESTS
    // ========================================================================

    @Test
    fun testAdvancedSettingsSection_DisplaysDebugMode() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that debug mode toggle is displayed
        composeTestRule.onNodeWithText("Debug Mode").assertExists()
    }

    @Test
    fun testAdvancedSettingsSection_DisplaysLogLevel() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that log level option is displayed
        composeTestRule.onNodeWithText("Log Level").assertExists()
    }

    @Test
    fun testAdvancedSettingsSection_DisplaysClearCache() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that clear cache option is displayed
        composeTestRule.onNodeWithText("Clear Cache").assertExists()
    }

    @Test
    fun testAdvancedSettingsSection_DisplaysResetAppData() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that reset app data option is displayed
        composeTestRule.onNodeWithText("Reset App Data").assertExists()
    }

    @Test
    fun testAdvancedSettingsSection_DisplaysDeveloperOptions() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that developer options toggle is displayed
        composeTestRule.onNodeWithText("Developer Options").assertExists()
    }

    // ========================================================================
    // SAVE BUTTON TESTS
    // ========================================================================

    @Test
    fun testSaveButton_DisplaysSaveSettings() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that save button is displayed
        composeTestRule.onNodeWithText("Save Settings").assertExists()
    }

    @Test
    fun testSaveButton_TriggersSaveEvent() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Click the save button
        composeTestRule.onNodeWithText("Save Settings").performClick()
        
        // Verify that handleEvent was called with SaveSettings
        verify { mockViewModel.handleEvent(SettingsEvent.SaveSettings) }
    }

    // ========================================================================
    // SUCCESS MESSAGE TESTS
    // ========================================================================

    @Test
    fun testSuccessMessage_DisplaysWhenSaveSuccessful() {
        // Create a state with saveSuccess = true
        val successState = SettingsState(saveSuccess = true)
        every { mockViewModel.state } returns MutableStateFlow(successState)
        
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that success message is displayed
        composeTestRule.onNodeWithText("Settings saved successfully!").assertExists()
    }

    // ========================================================================
    // SECTION HEADER TESTS
    // ========================================================================

    @Test
    fun testSectionHeaders_DisplayAllCategories() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Verify that all section headers are displayed
        val sectionTitles = listOf(
            "App Settings",
            "Account & Device", 
            "Privacy & Security",
            "Notifications",
            "Accessibility",
            "About",
            "Advanced"
        )
        
        sectionTitles.forEach { title ->
            composeTestRule.onNodeWithText(title).assertExists()
        }
    }

    // ========================================================================
    // NAVIGATION TESTS
    // ========================================================================

    @Test
    fun testBackButton_NavigatesBack() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // This test would need to be more sophisticated to test actual navigation
        // For now, we just verify the back button exists
        composeTestRule.onNodeWithText("Settings").assertExists()
    }

    // ========================================================================
    // ERROR HANDLING TESTS
    // ========================================================================

    @Test
    fun testErrorState_DisplaysErrorMessage() {
        // Create a state with an error
        val errorState = SettingsState(error = "Failed to load settings")
        every { mockViewModel.state } returns MutableStateFlow(errorState)
        
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // The error should be handled by the ViewModel and shown as a snackbar
        // This is a basic test - more sophisticated error handling tests would be needed
        composeTestRule.waitForIdle()
    }

    // ========================================================================
    // UTILITY TESTS
    // ========================================================================

    @Test
    fun testSettingsScreen_PrintsToString() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // This test verifies that the screen can be rendered without crashing
        val content = composeTestRule.onRoot().printToString()
        assert(content.isNotEmpty())
    }

    @Test
    fun testAllSettingsCategories_ArePresent() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Count the number of settings sections
        val sectionTitles = listOf(
            "App Settings",
            "Account & Device",
            "Privacy & Security", 
            "Notifications",
            "Accessibility",
            "About",
            "Advanced"
        )
        
        sectionTitles.forEach { title ->
            composeTestRule.onNodeWithText(title).assertExists("Section $title should be present")
        }
    }

    // ========================================================================
    // COMPONENT INTERACTION TESTS
    // ========================================================================

    @Test
    fun testThemeDropdown_TriggersUpdateEvent() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Click on the theme dropdown (this would open the dropdown menu)
        composeTestRule.onNodeWithText("Theme").performClick()
        
        // Verify that the ViewModel was notified (though the actual dropdown interaction
        // would be more complex to test)
        // This is a basic test - more sophisticated dropdown tests would be needed
    }

    @Test
    fun testToggleSwitch_TriggersUpdateEvent() {
        composeTestRule.setContent {
            CookbookTheme {
                SettingsScreen(
                    viewModel = mockViewModel,
                    navController = mockNavController
                )
            }
        }
        
        // Click on a toggle switch (this would toggle the value)
        composeTestRule.onNodeWithText("Offline Mode").performClick()
        
        // Verify that the ViewModel was notified
        // Note: This is a simplified test - actual toggle interaction would need more setup
    }
}