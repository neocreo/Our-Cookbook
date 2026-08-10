package com.ourcookbook.ui.screens.exportimport

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ourcookbook.domain.model.*
import com.ourcookbook.ui.screens.exportimport.ExportImportEvent
import com.ourcookbook.ui.screens.exportimport.ExportImportMode
import com.ourcookbook.ui.screens.exportimport.ExportImportState
import com.ourcookbook.ui.theme.CookbookTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit Tests for Export/Import Screen
 * Task 2.1.09: Export/Import Screen Implementation
 */

@RunWith(AndroidJUnit4::class)
class ExportImportScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    private val mockViewModel: ExportImportViewModel = mock()
    
    @Test
    fun testExportImportScreen_InitialState() {
        // Given
        val initialState = ExportImportState()
        whenever(mockViewModel.state).thenReturn(java.util.concurrent.Flows.just(initialState))
        
        // When
        composeTestRule.setContent {
            CookbookTheme {
                ExportImportScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Export Your Data").assertIsDisplayed()
        composeTestRule.onNodeWithText("Import Data").assertDoesNotExist()
        composeTestRule.onNodeWithText("Export").assertIsDisplayed()
        composeTestRule.onNodeWithText("Import").assertIsDisplayed()
    }
    
    @Test
    fun testExportImportScreen_ModeSwitch() {
        // Given
        val exportState = ExportImportState(currentMode = ExportImportMode.EXPORT)
        val importState = ExportImportState(currentMode = ExportImportMode.IMPORT)
        
        // When
        composeTestRule.setContent {
            CookbookTheme {
                ExportImportScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel
                )
            }
        }
        
        // Switch to Import mode
        composeTestRule.onNodeWithText("Import").performClick()
        
        // Then
        verify(mockViewModel).handleEvent(ExportImportEvent.SwitchMode(ExportImportMode.IMPORT))
    }
    
    @Test
    fun testExportImportScreen_ExportMode_ShowsExportContent() {
        // Given
        val exportState = ExportImportState(currentMode = ExportImportMode.EXPORT)
        whenever(mockViewModel.state).thenReturn(java.util.concurrent.Flows.just(exportState))
        
        // When
        composeTestRule.setContent {
            CookbookTheme {
                ExportImportScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Export Your Data").assertIsDisplayed()
        composeTestRule.onNodeWithText("Select Recipes").assertDoesNotExist()
        composeTestRule.onNodeWithText("Select Cookbooks").assertDoesNotExist()
        composeTestRule.onNodeWithText("Select Files").assertDoesNotExist()
    }
    
    @Test
    fun testExportImportScreen_ImportMode_ShowsImportContent() {
        // Given
        val importState = ExportImportState(currentMode = ExportImportMode.IMPORT)
        whenever(mockViewModel.state).thenReturn(java.util.concurrent.Flows.just(importState))
        
        // When
        composeTestRule.setContent {
            CookbookTheme {
                ExportImportScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Import Data").assertIsDisplayed()
        composeTestRule.onNodeWithText("Select Files").assertIsDisplayed()
        composeTestRule.onNodeWithText("Export Your Data").assertDoesNotExist()
    }
    
    @Test
    fun testExportImportScreen_ExportTargetSelection() {
        // Given
        val state = ExportImportState(
            currentMode = ExportImportMode.EXPORT,
            exportTarget = ExportTarget.INDIVIDUAL_RECIPE
        )
        whenever(mockViewModel.state).thenReturn(java.util.concurrent.Flows.just(state))
        
        // When
        composeTestRule.setContent {
            CookbookTheme {
                ExportImportScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Recipes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cookbooks").assertIsDisplayed()
        composeTestRule.onNodeWithText("All Recipes").assertIsDisplayed()
    }
    
    @Test
    fun testExportImportScreen_FormatSelection() {
        // Given
        val state = ExportImportState(
            currentMode = ExportImportMode.EXPORT,
            exportSettings = ExportSettings(format = ExportFormat.JSON)
        )
        whenever(mockViewModel.state).thenReturn(java.util.concurrent.Flows.just(state))
        
        // When
        composeTestRule.setContent {
            CookbookTheme {
                ExportImportScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel
                )
            }
        }
        
        // Click on format selection
        composeTestRule.onNodeWithText("Export Format").performClick()
        
        // Then
        verify(mockViewModel).handleEvent(ExportImportEvent.ShowFormatSelection)
    }
    
    @Test
    fun testExportImportScreen_ExportButton_EnabledWhenItemsSelected() {
        // Given
        val stateWithSelection = ExportImportState(
            currentMode = ExportImportMode.EXPORT,
            exportTarget = ExportTarget.INDIVIDUAL_RECIPE,
            selectedRecipeIds = listOf("recipe1", "recipe2")
        )
        whenever(mockViewModel.state).thenReturn(java.util.concurrent.Flows.just(stateWithSelection))
        
        // When
        composeTestRule.setContent {
            CookbookTheme {
                ExportImportScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Export").assertIsEnabled()
    }
    
    @Test
    fun testExportImportScreen_ExportButton_DisabledWhenNoItemsSelected() {
        // Given
        val stateWithoutSelection = ExportImportState(
            currentMode = ExportImportMode.EXPORT,
            exportTarget = ExportTarget.INDIVIDUAL_RECIPE,
            selectedRecipeIds = emptyList()
        )
        whenever(mockViewModel.state).thenReturn(java.util.concurrent.Flows.just(stateWithoutSelection))
        
        // When
        composeTestRule.setContent {
            CookbookTheme {
                ExportImportScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Export").assertIsNotEnabled()
    }
    
    @Test
    fun testExportImportScreen_ImportButton_EnabledWhenFilesSelected() {
        // Given
        val stateWithFiles = ExportImportState(
            currentMode = ExportImportMode.IMPORT,
            selectedFilePaths = listOf("/path/to/file1.json", "/path/to/file2.json")
        )
        whenever(mockViewModel.state).thenReturn(java.util.concurrent.Flows.just(stateWithFiles))
        
        // When
        composeTestRule.setContent {
            CookbookTheme {
                ExportImportScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Import").assertIsEnabled()
    }
    
    @Test
    fun testExportImportScreen_ImportButton_DisabledWhenNoFilesSelected() {
        // Given
        val stateWithoutFiles = ExportImportState(
            currentMode = ExportImportMode.IMPORT,
            selectedFilePaths = emptyList()
        )
        whenever(mockViewModel.state).thenReturn(java.util.concurrent.Flows.just(stateWithoutFiles))
        
        // When
        composeTestRule.setContent {
            CookbookTheme {
                ExportImportScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Import").assertIsNotEnabled()
    }
    
    @Test
    fun testExportImportScreen_BackNavigation() {
        // Given
        val state = ExportImportState()
        whenever(mockViewModel.state).thenReturn(java.util.concurrent.Flows.just(state))
        
        // When
        composeTestRule.setContent {
            CookbookTheme {
                ExportImportScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel
                )
            }
        }
        
        // Click back button
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        
        // Then
        verify(mockViewModel).handleEvent(ExportImportEvent.NavigateBack)
    }
    
    @Test
    fun testExportImportScreen_CloudExportOptions() {
        // Given
        val state = ExportImportState(
            currentMode = ExportImportMode.EXPORT,
            isDriveConnected = true,
            selectedRecipeIds = listOf("recipe1")
        )
        whenever(mockViewModel.state).thenReturn(java.util.concurrent.Flows.just(state))
        
        // When
        composeTestRule.setContent {
            CookbookTheme {
                ExportImportScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Export to Google Drive").assertIsDisplayed()
        composeTestRule.onNodeWithText("Disconnect from Google Drive").assertDoesNotExist()
    }
    
    @Test
    fun testExportImportScreen_CloudImportOptions() {
        // Given
        val state = ExportImportState(
            currentMode = ExportImportMode.IMPORT,
            isDriveConnected = true
        )
        whenever(mockViewModel.state).thenReturn(java.util.concurrent.Flows.just(state))
        
        // When
        composeTestRule.setContent {
            CookbookTheme {
                ExportImportScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Import from Google Drive").assertIsDisplayed()
        composeTestRule.onNodeWithText("Connect to Google Drive").assertDoesNotExist()
    }
    
    @Test
    fun testExportImportScreen_ConnectToDrive() {
        // Given
        val state = ExportImportState(
            currentMode = ExportImportMode.EXPORT,
            isDriveConnected = false
        )
        whenever(mockViewModel.state).thenReturn(java.util.concurrent.Flows.just(state))
        
        // When
        composeTestRule.setContent {
            CookbookTheme {
                ExportImportScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel
                )
            }
        }
        
        // Click connect button
        composeTestRule.onNodeWithText("Connect to Google Drive").performClick()
        
        // Then
        verify(mockViewModel).handleEvent(ExportImportEvent.ConnectToDrive)
    }
    
    @Test
    fun testExportImportScreen_BatchExportOption() {
        // Given
        val state = ExportImportState(
            currentMode = ExportImportMode.EXPORT,
            exportTarget = ExportTarget.INDIVIDUAL_RECIPE,
            selectedRecipeIds = listOf("recipe1", "recipe2", "recipe3")
        )
        whenever(mockViewModel.state).thenReturn(java.util.concurrent.Flows.just(state))
        
        // When
        composeTestRule.setContent {
            CookbookTheme {
                ExportImportScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Batch Export").assertIsDisplayed()
        composeTestRule.onNodeWithText("Batch Export").assertIsEnabled()
    }
    
    @Test
    fun testExportImportScreen_BatchImportOption() {
        // Given
        val state = ExportImportState(
            currentMode = ExportImportMode.IMPORT,
            selectedFilePaths = listOf("/path/to/file1.json", "/path/to/file2.json")
        )
        whenever(mockViewModel.state).thenReturn(java.util.concurrent.Flows.just(state))
        
        // When
        composeTestRule.setContent {
            CookbookTheme {
                ExportImportScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Batch Import").assertIsDisplayed()
        composeTestRule.onNodeWithText("Batch Import").assertIsEnabled()
    }
    
    @Test
    fun testExportImportScreen_ProgressDialog() {
        // Given
        val state = ExportImportState(
            currentMode = ExportImportMode.EXPORT,
            showProgressDialog = true,
            currentProgress = 0.5f,
            isOperationInProgress = true
        )
        whenever(mockViewModel.state).thenReturn(java.util.concurrent.Flows.just(state))
        
        // When
        composeTestRule.setContent {
            CookbookTheme {
                ExportImportScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Exporting...").assertIsDisplayed()
        composeTestRule.onNodeWithText("Processing 50%").assertIsDisplayed()
    }
    
    @Test
    fun testExportImportScreen_ErrorMessage() {
        // Given
        val state = ExportImportState(
            errorMessage = "Failed to export: Invalid format"
        )
        whenever(mockViewModel.state).thenReturn(java.util.concurrent.Flows.just(state))
        
        // When
        composeTestRule.setContent {
            CookbookTheme {
                ExportImportScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Failed to export: Invalid format").assertIsDisplayed()
    }
    
    @Test
    fun testExportImportScreen_SuccessMessage() {
        // Given
        val state = ExportImportState(
            successMessage = "Export completed successfully"
        )
        whenever(mockViewModel.state).thenReturn(java.util.concurrent.Flows.just(state))
        
        // When
        composeTestRule.setContent {
            CookbookTheme {
                ExportImportScreen(
                    navController = rememberNavController(),
                    viewModel = mockViewModel
                )
            }
        }
        
        // Then
        composeTestRule.onNodeWithText("Success").assertIsDisplayed()
        composeTestRule.onNodeWithText("Export completed successfully").assertIsDisplayed()
    }
}

// ==================== VIEWMODEL TESTS ====================

class ExportImportViewModelTest {
    
    @Test
    fun testViewModel_InitialState() {
        // Given
        val viewModel = ExportImportViewModel(
            exportUseCases = mock(),
            importUseCases = mock()
        )
        
        // When
        val state = viewModel.state.value
        
        // Then
        assert(state.currentMode == ExportImportMode.EXPORT)
        assert(state.exportSettings.format == ExportFormat.JSON)
        assert(state.importSettings.format == ImportFormat.JSON)
        assert(state.selectedRecipeIds.isEmpty())
        assert(state.selectedCookbookIds.isEmpty())
        assert(state.selectedFilePaths.isEmpty())
        assert(!state.isOperationInProgress)
        assert(!state.showProgressDialog)
    }
    
    @Test
    fun testViewModel_SwitchMode() {
        // Given
        val viewModel = ExportImportViewModel(
            exportUseCases = mock(),
            importUseCases = mock()
        )
        
        // When
        viewModel.handleEvent(ExportImportEvent.SwitchMode(ExportImportMode.IMPORT))
        
        // Then
        val state = viewModel.state.value
        assert(state.currentMode == ExportImportMode.IMPORT)
    }
    
    @Test
    fun testViewModel_SelectExportFormat() {
        // Given
        val viewModel = ExportImportViewModel(
            exportUseCases = mock(),
            importUseCases = mock()
        )
        
        // When
        viewModel.handleEvent(ExportImportEvent.SelectExportFormat(ExportFormat.PDF))
        
        // Then
        val state = viewModel.state.value
        assert(state.exportSettings.format == ExportFormat.PDF)
    }
    
    @Test
    fun testViewModel_ToggleRecipeSelection() {
        // Given
        val viewModel = ExportImportViewModel(
            exportUseCases = mock(),
            importUseCases = mock()
        )
        
        // When
        viewModel.handleEvent(ExportImportEvent.ToggleRecipeSelection("recipe1"))
        
        // Then
        val state = viewModel.state.value
        assert(state.selectedRecipeIds.contains("recipe1"))
        
        // When - toggle again
        viewModel.handleEvent(ExportImportEvent.ToggleRecipeSelection("recipe1"))
        
        // Then - should be removed
        val updatedState = viewModel.state.value
        assert(!updatedState.selectedRecipeIds.contains("recipe1"))
    }
    
    @Test
    fun testViewModel_AddFilesForImport() {
        // Given
        val viewModel = ExportImportViewModel(
            exportUseCases = mock(),
            importUseCases = mock()
        )
        
        // When
        viewModel.handleEvent(ExportImportEvent.AddFilesForImport(
            listOf("/path/to/file1.json", "/path/to/file2.json")
        ))
        
        // Then
        val state = viewModel.state.value
        assert(state.selectedFilePaths.size == 2)
        assert(state.selectedFilePaths.contains("/path/to/file1.json"))
        assert(state.selectedFilePaths.contains("/path/to/file2.json"))
    }
    
    @Test
    fun testViewModel_RemoveFileFromImport() {
        // Given
        val viewModel = ExportImportViewModel(
            exportUseCases = mock(),
            importUseCases = mock()
        )
        
        // Add files first
        viewModel.handleEvent(ExportImportEvent.AddFilesForImport(
            listOf("/path/to/file1.json", "/path/to/file2.json")
        ))
        
        // When
        viewModel.handleEvent(ExportImportEvent.RemoveFileFromImport("/path/to/file1.json"))
        
        // Then
        val state = viewModel.state.value
        assert(state.selectedFilePaths.size == 1)
        assert(!state.selectedFilePaths.contains("/path/to/file1.json"))
        assert(state.selectedFilePaths.contains("/path/to/file2.json"))
    }
    
    @Test
    fun testViewModel_CanExport_WithIndividualRecipes() {
        // Given
        val viewModel = ExportImportViewModel(
            exportUseCases = mock(),
            importUseCases = mock()
        )
        
        // Add recipe selection
        viewModel.handleEvent(ExportImportEvent.ToggleRecipeSelection("recipe1"))
        viewModel.handleEvent(ExportImportEvent.SelectExportTarget(ExportTarget.INDIVIDUAL_RECIPE))
        
        // When
        val state = viewModel.state.value
        
        // Then
        assert(state.canExport)
    }
    
    @Test
    fun testViewModel_CannotExport_WithNoSelection() {
        // Given
        val viewModel = ExportImportViewModel(
            exportUseCases = mock(),
            importUseCases = mock()
        )
        
        // When
        val state = viewModel.state.value
        
        // Then
        assert(!state.canExport)
    }
    
    @Test
    fun testViewModel_CanImport_WithFiles() {
        // Given
        val viewModel = ExportImportViewModel(
            exportUseCases = mock(),
            importUseCases = mock()
        )
        
        // Add files
        viewModel.handleEvent(ExportImportEvent.AddFilesForImport(
            listOf("/path/to/file1.json")
        ))
        
        // When
        val state = viewModel.state.value
        
        // Then
        assert(state.canImport)
    }
    
    @Test
    fun testViewModel_CannotImport_WithNoFiles() {
        // Given
        val viewModel = ExportImportViewModel(
            exportUseCases = mock(),
            importUseCases = mock()
        )
        
        // When
        val state = viewModel.state.value
        
        // Then
        assert(!state.canImport)
    }
}
