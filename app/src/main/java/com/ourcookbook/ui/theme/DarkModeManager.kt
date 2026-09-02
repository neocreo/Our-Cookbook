package com.ourcookbook.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.hilt.navigation.compose.hiltViewModel
import com.ourcookbook.domain.model.DevicePreferences
import com.ourcookbook.domain.repository.DevicePreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dark Mode Manager
 * 
 * Manages dark mode preferences across the application
 * Supports:
 * - LIGHT: Always use light theme
 * - DARK: Always use dark theme
 * - SYSTEM: Follow system preference
 * - AUTO: Automatically switch based on time of day (battery saving)
 * 
 * Per-user preference support for multi-user scenarios
 */

// Theme preference keys
const val PREF_THEME = "pref_theme"
const val PREF_DARK_MODE = "pref_dark_mode"
const val PREF_DYNAMIC_COLORS = "pref_dynamic_colors"
const val PREF_AUTO_DARK_MODE = "pref_auto_dark_mode"
const val PREF_DARK_MODE_START = "pref_dark_mode_start"
const val PREF_DARK_MODE_END = "pref_dark_mode_end"

/**
 * Theme preference options
 */
enum class ThemePreference {
    LIGHT,
    DARK,
    SYSTEM,
    AUTO
}

/**
 * Manages theme preferences using DataStore
 */
@Singleton
class ThemePreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")
    
    private val themeKey = stringPreferencesKey(PREF_THEME)
    private val dynamicColorsKey = booleanPreferencesKey(PREF_DYNAMIC_COLORS)
    private val autoDarkModeKey = booleanPreferencesKey(PREF_AUTO_DARK_MODE)
    private val darkModeStartKey = stringPreferencesKey(PREF_DARK_MODE_START)
    private val darkModeEndKey = stringPreferencesKey(PREF_DARK_MODE_END)
    
    // Default values
    private val defaultTheme = ThemePreference.SYSTEM
    private val defaultDynamicColors = true
    private val defaultAutoDarkMode = false
    private val defaultDarkModeStart = "20:00" // 8 PM
    private val defaultDarkModeEnd = "07:00" // 7 AM
    
    /**
     * Get theme preference flow
     */
    val themePreference: Flow<ThemePreference> = context.dataStore.data
        .map { preferences ->
            preferences[themeKey]?.let { ThemePreference.valueOf(it) } ?: defaultTheme
        }
    
    /**
     * Get dynamic colors preference flow
     */
    val dynamicColorsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[dynamicColorsKey] ?: defaultDynamicColors
        }
    
    /**
     * Get auto dark mode preference flow
     */
    val autoDarkModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[autoDarkModeKey] ?: defaultAutoDarkMode
        }
    
    /**
     * Get dark mode start time flow
     */
    val darkModeStartTime: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[darkModeStartKey] ?: defaultDarkModeStart
        }
    
    /**
     * Get dark mode end time flow
     */
    val darkModeEndTime: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[darkModeEndKey] ?: defaultDarkModeEnd
        }
    
    /**
     * Set theme preference
     */
    suspend fun setThemePreference(theme: ThemePreference) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = theme.name
        }
    }
    
    /**
     * Set dynamic colors preference
     */
    suspend fun setDynamicColorsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[dynamicColorsKey] = enabled
        }
    }
    
    /**
     * Set auto dark mode preference
     */
    suspend fun setAutoDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[autoDarkModeKey] = enabled
        }
    }
    
    /**
     * Set dark mode start time
     */
    suspend fun setDarkModeStartTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[darkModeStartKey] = time
        }
    }
    
    /**
     * Set dark mode end time
     */
    suspend fun setDarkModeEndTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[darkModeEndKey] = time
        }
    }
    
    /**
     * Get all theme preferences as a single flow
     */
    fun getThemeSettings(): Flow<ThemeSettings> {
        return combine(
            themePreference,
            dynamicColorsEnabled,
            autoDarkModeEnabled,
            darkModeStartTime,
            darkModeEndTime
        ) { theme, dynamic, auto, start, end ->
            ThemeSettings(
                themePreference = theme,
                dynamicColorsEnabled = dynamic,
                autoDarkModeEnabled = auto,
                darkModeStartTime = start,
                darkModeEndTime = end
            )
        }
    }
    
    /**
     * Reset to default settings
     */
    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

/**
 * Theme settings data class
 */
data class ThemeSettings(
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val dynamicColorsEnabled: Boolean = true,
    val autoDarkModeEnabled: Boolean = false,
    val darkModeStartTime: String = "20:00",
    val darkModeEndTime: String = "07:00"
)

/**
 * Converts DevicePreferences theme to ThemePreference
 */
fun DevicePreferences.toThemePreference(): ThemePreference {
    return when (theme.uppercase()) {
        "LIGHT" -> ThemePreference.LIGHT
        "DARK" -> ThemePreference.DARK
        "SYSTEM" -> ThemePreference.SYSTEM
        else -> ThemePreference.SYSTEM
    }
}

/**
 * Converts ThemePreference to DevicePreferences theme
 */
fun ThemePreference.toDeviceTheme(): String {
    return when (this) {
        ThemePreference.LIGHT -> "LIGHT"
        ThemePreference.DARK -> "DARK"
        ThemePreference.SYSTEM -> "SYSTEM"
        ThemePreference.AUTO -> "AUTO"
    }
}

/**
 * ViewModel for managing theme preferences in Compose
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themePreferencesManager: ThemePreferencesManager,
    private val devicePreferencesRepository: DevicePreferencesRepository?
) : androidx.lifecycle.ViewModel() {
    
    private val _themeSettings = MutableStateFlow(ThemeSettings())
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    init {
        scope.launch {
            themePreferencesManager.getThemeSettings()
                .collectLatest { settings ->
                    _themeSettings.value = settings
                }
        }
    }
    
    /**
     * Get whether dark theme should be used
     */
    fun shouldUseDarkTheme(): Boolean {
        val settings = _themeSettings.value
        
        return when (settings.themePreference) {
            ThemePreference.LIGHT -> false
            ThemePreference.DARK -> true
            ThemePreference.SYSTEM -> {
                // System preference is resolved at the Compose layer via isSystemInDarkTheme()
                false
            }
            ThemePreference.AUTO -> {
                // Auto mode based on time
                shouldUseAutoDarkMode(settings)
            }
        }
    }
    
    /**
     * Check if auto dark mode should be active based on time
     */
    private fun shouldUseAutoDarkMode(settings: ThemeSettings): Boolean {
        if (!settings.autoDarkModeEnabled) return false
        
        val currentHour = java.time.LocalTime.now().hour
        val startHour = settings.darkModeStartTime.substringBefore(':').toIntOrNull() ?: 20
        val endHour = settings.darkModeEndTime.substringBefore(':').toIntOrNull() ?: 7
        
        // Handle overnight period (e.g., 20:00 to 07:00)
        return if (startHour > endHour) {
            // Overnight period: dark mode between start and end
            currentHour >= startHour || currentHour < endHour
        } else {
            // Same day period: dark mode between start and end
            currentHour >= startHour && currentHour < endHour
        }
    }
    
    /**
     * Update theme preference
     */
    fun updateThemePreference(theme: ThemePreference) {
        scope.launch {
            themePreferencesManager.setThemePreference(theme)
        }
    }
    
    /**
     * Update dynamic colors preference
     */
    fun updateDynamicColorsEnabled(enabled: Boolean) {
        scope.launch {
            themePreferencesManager.setDynamicColorsEnabled(enabled)
        }
    }
    
    /**
     * Update auto dark mode preference
     */
    fun updateAutoDarkModeEnabled(enabled: Boolean) {
        scope.launch {
            themePreferencesManager.setAutoDarkModeEnabled(enabled)
        }
    }
    
    /**
     * Update dark mode start time
     */
    fun updateDarkModeStartTime(time: String) {
        scope.launch {
            themePreferencesManager.setDarkModeStartTime(time)
        }
    }
    
    /**
     * Update dark mode end time
     */
    fun updateDarkModeEndTime(time: String) {
        scope.launch {
            themePreferencesManager.setDarkModeEndTime(time)
        }
    }
    
    /**
     * Reset to default settings
     */
    fun resetToDefaults() {
        scope.launch {
            themePreferencesManager.resetToDefaults()
        }
    }
    
    override fun onCleared() {
        scope.cancel()
        super.onCleared()
    }
}

/**
 * Composable function to remember theme settings
 */
@Composable
fun rememberThemeSettings(
    viewModel: ThemeViewModel = hiltViewModel()
): ThemeSettings {
    val settings by viewModel.themeSettings.collectAsState()
    return settings
}

/**
 * Composable function to determine if dark theme should be used
 */
@Composable
fun shouldUseDarkTheme(
    viewModel: ThemeViewModel = hiltViewModel()
): Boolean {
    val settings by viewModel.themeSettings.collectAsState()
    
    return when (settings.themePreference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> {
            // Follow system preference
            androidx.compose.foundation.isSystemInDarkTheme()
        }
        ThemePreference.AUTO -> {
            // Auto mode based on time
            shouldUseAutoDarkMode(settings)
        }
    }
}

/**
 * Composable function to check if auto dark mode should be active
 */
@Composable
fun shouldUseAutoDarkMode(settings: ThemeSettings): Boolean {
    if (!settings.autoDarkModeEnabled) return false
    
    val currentHour = java.time.LocalTime.now().hour
    val startHour = settings.darkModeStartTime.substringBefore(':').toIntOrNull() ?: 20
    val endHour = settings.darkModeEndTime.substringBefore(':').toIntOrNull() ?: 7
    
    // Handle overnight period (e.g., 20:00 to 07:00)
    return if (startHour > endHour) {
        // Overnight period: dark mode between start and end
        currentHour >= startHour || currentHour < endHour
    } else {
        // Same day period: dark mode between start and end
        currentHour >= startHour && currentHour < endHour
    }
}

/**
 * Composable wrapper for CookbookTheme with theme preferences
 */
@Composable
fun CookbookThemeWithPreferences(
    themeViewModel: ThemeViewModel = hiltViewModel(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = shouldUseDarkTheme(themeViewModel)
    val settings by themeViewModel.themeSettings.collectAsState()
    
    CookbookTheme(
        darkTheme = darkTheme,
        dynamicColor = settings.dynamicColorsEnabled && dynamicColor,
        content = content
    )
}

/**
 * SharedPreferences-based theme manager for simpler use cases
 */
class SharedPrefsThemeManager(private val sharedPreferences: SharedPreferences) {
    
    fun getThemePreference(): ThemePreference {
        return sharedPreferences.getString(PREF_THEME, ThemePreference.SYSTEM.name)
            ?.let { ThemePreference.valueOf(it) }
            ?: ThemePreference.SYSTEM
    }
    
    fun setThemePreference(theme: ThemePreference) {
        sharedPreferences.edit().putString(PREF_THEME, theme.name).apply()
    }
    
    fun isDarkThemeEnabled(): Boolean {
        return when (getThemePreference()) {
            ThemePreference.LIGHT -> false
            ThemePreference.DARK -> true
            ThemePreference.SYSTEM -> {
                // System preference is resolved at the Compose layer via isSystemInDarkTheme()
                false
            }
            ThemePreference.AUTO -> {
                val currentHour = java.time.LocalTime.now().hour
                val startHour = sharedPreferences.getString(PREF_DARK_MODE_START, "20:00")
                    ?.substringBefore(':')?.toIntOrNull() ?: 20
                val endHour = sharedPreferences.getString(PREF_DARK_MODE_END, "07:00")
                    ?.substringBefore(':')?.toIntOrNull() ?: 7
                
                if (startHour > endHour) {
                    currentHour >= startHour || currentHour < endHour
                } else {
                    currentHour >= startHour && currentHour < endHour
                }
            }
        }
    }
}

/**
 * Extension functions for easy theme management
 */

fun Context.getThemePreferencesManager(): ThemePreferencesManager {
    return ThemePreferencesManager(this)
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

/**
 * Get theme settings from device preferences
 */
suspend fun getThemeSettingsFromDevice(
    deviceId: String,
    devicePreferencesRepository: DevicePreferencesRepository
): ThemeSettings {
    val prefs = devicePreferencesRepository.getDevicePreferencesByDevice(deviceId)
        .getOrDefault(DevicePreferences.createDefault(deviceId))
    return ThemeSettings(
        themePreference = prefs.toThemePreference(),
        dynamicColorsEnabled = true, // Default for now
        autoDarkModeEnabled = false, // Default for now
        darkModeStartTime = "20:00",
        darkModeEndTime = "07:00"
    )
}
