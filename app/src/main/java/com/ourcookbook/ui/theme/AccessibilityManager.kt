package com.ourcookbook.ui.theme

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager as SystemAccessibilityManager
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ourcookbook.domain.model.DevicePreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Accessibility Manager for WCAG 2.1 AA Compliance
 * 
 * Manages accessibility features to ensure the app meets WCAG 2.1 AA standards:
 * - Minimum color contrast ratios (4.5:1 for normal text, 3:1 for large text)
 * - Text scaling support
 * - Reduced motion support
 * - High contrast mode
 * - Screen reader compatibility
 * - Focus management
 * - Touch target sizes
 */

// WCAG contrast ratio thresholds
const val WCAG_AA_NORMAL_TEXT = 4.5f
const val WCAG_AA_LARGE_TEXT = 3.0f
const val WCAG_AAA_NORMAL_TEXT = 7.0f
const val WCAG_AAA_LARGE_TEXT = 4.5f

// Minimum touch target size (48x48 dp per WCAG)
val MIN_TOUCH_TARGET_SIZE = 48.dp

// Accessibility preference keys
const val PREF_REDUCED_MOTION = "pref_reduced_motion"
const val PREF_HIGH_CONTRAST = "pref_high_contrast"
const val PREF_TEXT_SCALING = "pref_text_scaling"
const val PREF_COLOR_BLINDNESS = "pref_color_blindness"

/**
 * Accessibility settings data class
 */
data class AccessibilitySettings(
    val reducedMotion: Boolean = false,
    val highContrast: Boolean = false,
    val textScaling: Float = 1.0f,
    val colorBlindnessMode: ColorBlindnessMode = ColorBlindnessMode.NONE,
    val screenReaderEnabled: Boolean = false,
    val minimumTouchTarget: Boolean = true,
    val boldText: Boolean = false
)

/**
 * Color blindness modes
 */
enum class ColorBlindnessMode {
    NONE,
    DEUTERANOPIA,  // Red-Green (most common, ~6% of men)
    PROTANOPIA,   // Red-Green (less common)
    TRITANOPIA,   // Blue-Yellow (rare)
    ACHROMATOPSIA // Complete color blindness (very rare)
}

/**
 * Manages accessibility settings
 */
@Singleton
class AccessibilityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val accessibilityManager by lazy {
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as SystemAccessibilityManager
    }
    
    private val _settings = mutableStateOf(AccessibilitySettings())
    val settings: AccessibilitySettings get() = _settings.value
    
    private val _settingsFlow = Channel<AccessibilitySettings>(Channel.CONFLATED)
    val settingsFlow: Flow<AccessibilitySettings> = _settingsFlow.receiveAsFlow()
    
    init {
        updateSettings()
    }
    
    /**
     * Update accessibility settings from system and preferences
     */
    fun updateSettings() {
        val newSettings = AccessibilitySettings(
            reducedMotion = isReducedMotionEnabled(),
            highContrast = getHighContrastPreference(),
            textScaling = getTextScaling(),
            colorBlindnessMode = getColorBlindnessMode(),
            screenReaderEnabled = isScreenReaderEnabled(),
            minimumTouchTarget = true, // Always enforce minimum touch targets
            boldText = getBoldTextPreference()
        )
        
        _settings.value = newSettings
        _settingsFlow.trySend(newSettings)
    }
    
    /**
     * Check if reduced motion is enabled (system preference)
     */
    fun isReducedMotionEnabled(): Boolean {
        return try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if screen reader is enabled
     */
    fun isScreenReaderEnabled(): Boolean {
        return accessibilityManager.isEnabled
    }
    
    /**
     * Get high contrast preference
     */
    fun getHighContrastPreference(): Boolean {
        // Check if high contrast is enabled in system settings
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                Settings.Secure.getInt(
                    context.contentResolver,
                    "high_text_contrast_enabled",
                    0
                ) == 1
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }
    
    /**
     * Get text scaling factor
     */
    fun getTextScaling(): Float {
        val configuration = context.resources.configuration
        return configuration.fontScale.coerceAtLeast(0.85f).coerceAtMost(2.0f)
    }
    
    /**
     * Get color blindness mode from preferences
     */
    fun getColorBlindnessMode(): ColorBlindnessMode {
        // This would be stored in user preferences
        // For now, return NONE
        return ColorBlindnessMode.NONE
    }
    
    /**
     * Get bold text preference
     */
    fun getBoldTextPreference(): Boolean {
        // This would be stored in user preferences
        return false
    }
    
    /**
     * Set high contrast mode
     */
    fun setHighContrast(enabled: Boolean) {
        // Update preference
        // This would be implemented with SharedPreferences or DataStore
        _settings.value = _settings.value.copy(highContrast = enabled)
        _settingsFlow.trySend(_settings.value)
    }
    
    /**
     * Set color blindness mode
     */
    fun setColorBlindnessMode(mode: ColorBlindnessMode) {
        _settings.value = _settings.value.copy(colorBlindnessMode = mode)
        _settingsFlow.trySend(_settings.value)
    }
    
    /**
     * Set text scaling
     */
    fun setTextScaling(scale: Float) {
        val clampedScale = scale.coerceIn(0.85f, 2.0f)
        _settings.value = _settings.value.copy(textScaling = clampedScale)
        _settingsFlow.trySend(_settings.value)
    }
    
    /**
     * Set bold text
     */
    fun setBoldText(enabled: Boolean) {
        _settings.value = _settings.value.copy(boldText = enabled)
        _settingsFlow.trySend(_settings.value)
    }
    
    /**
     * Check if current color scheme meets WCAG AA contrast requirements
     */
    fun meetsWcagAaContrast(colorScheme: ColorScheme): Boolean {
        // Check primary on surface contrast
        val primaryContrast = calculateContrast(colorScheme.primary, colorScheme.surface)
        if (primaryContrast < WCAG_AA_NORMAL_TEXT) return false
        
        // Check secondary on surface contrast
        val secondaryContrast = calculateContrast(colorScheme.secondary, colorScheme.surface)
        if (secondaryContrast < WCAG_AA_NORMAL_TEXT) return false
        
        // Check onSurface on surface contrast
        val onSurfaceContrast = calculateContrast(colorScheme.onSurface, colorScheme.surface)
        if (onSurfaceContrast < WCAG_AA_NORMAL_TEXT) return false
        
        // Check error on surface contrast
        val errorContrast = calculateContrast(colorScheme.error, colorScheme.surface)
        if (errorContrast < WCAG_AA_NORMAL_TEXT) return false
        
        return true
    }
    
    /**
     * Calculate contrast ratio between two colors
     * Formula: (L1 + 0.05) / (L2 + 0.05) where L1 is lighter, L2 is darker
     */
    fun calculateContrast(color1: Color, color2: Color): Float {
        val l1 = color1.luminance()
        val l2 = color2.luminance()
        
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        
        return (lighter + 0.05f) / (darker + 0.05f)
    }
    
    /**
     * Adjust color for better contrast
     */
    fun adjustForContrast(color: Color, background: Color, targetRatio: Float = WCAG_AA_NORMAL_TEXT): Color {
        var adjustedColor = color
        var currentRatio = calculateContrast(color, background)
        
        // If contrast is already sufficient, return original
        if (currentRatio >= targetRatio) return color
        
        // Try to improve contrast by adjusting luminance
        val backgroundLuminance = background.luminance()
        val colorLuminance = color.luminance()
        
        // Determine if we need to make the color lighter or darker
        val needsLighter = colorLuminance < backgroundLuminance
        
        // Adjust in steps
        val step = 0.05f
        var luminance = colorLuminance
        
        while (calculateContrast(adjustedColor, background) < targetRatio) {
            luminance = if (needsLighter) {
                minOf(luminance + step, 1.0f)
            } else {
                maxOf(luminance - step, 0.0f)
            }
            
            adjustedColor = adjustLuminance(color, luminance)
            
            // Prevent infinite loop
            if (luminance == 0f || luminance == 1f) break
        }
        
        return adjustedColor
    }
    
    /**
     * Adjust a color's luminance
     */
    private fun adjustLuminance(color: Color, newLuminance: Float): Color {
        // Convert to HSL, adjust L, convert back to RGB
        val hsl = rgbToHsl(color.red, color.green, color.blue)
        val adjustedHsl = hsl.copy(l = newLuminance)
        val rgb = hslToRgb(adjustedHsl)
        
        return Color(rgb.first, rgb.second, rgb.second)
    }
    
    /**
     * Convert RGB to HSL
     */
    private fun rgbToHsl(r: Float, g: Float, b: Float): HSL {
        val rf = r.coerceIn(0f, 1f)
        val gf = g.coerceIn(0f, 1f)
        val bf = b.coerceIn(0f, 1f)
        
        val max = maxOf(rf, gf, bf)
        val min = minOf(rf, gf, bf)
        val delta = max - min
        
        var h = 0f
        var s = 0f
        val l = (max + min) / 2f
        
        if (delta > 0) {
            s = delta / (1 - abs(2 * l - 1))
            
            h = when (max) {
                rf -> (gf - bf) / delta + (if (gf < bf) 6 else 0)
                gf -> (bf - rf) / delta + 2
                else -> (rf - gf) / delta + 4
            }
            h /= 6f
        }
        
        return HSL(h, s, l)
    }
    
    /**
     * Convert HSL to RGB
     */
    private fun hslToRgb(hsl: HSL): Triple<Float, Float, Float> {
        val h = hsl.h
        val s = hsl.s
        val l = hsl.l
        
        var r: Float
        var g: Float
        var b: Float
        
        if (s == 0f) {
            r = l
            g = l
            b = l
        } else {
            fun hue2rgb(p: Float, q: Float, t: Float): Float {
                return when {
                    t < 0 -> hue2rgb(p, q, t + 1)
                    t > 1 -> hue2rgb(p, q, t - 1)
                    t < 1f/6 -> p + (q - p) * 6 * t
                    t < 1f/2 -> q
                    t < 2f/3 -> p + (q - p) * (2f/3 - t) * 6
                    else -> p
                }
            }
            
            val q = if (l < 0.5f) l * (1 + s) else l + s - l * s
            val p = 2 * l - q
            
            r = hue2rgb(p, q, h + 1f/3)
            g = hue2rgb(p, q, h)
            b = hue2rgb(p, q, h - 1f/3)
        }
        
        return Triple(r, g, b)
    }
    
    /**
     * Get text style with accessibility adjustments
     */
    fun getAccessibleTextStyle(
        baseStyle: TextStyle,
        isLargeText: Boolean = false
    ): TextStyle {
        val settings = _settings.value
        val scale = if (isLargeText) {
            // Large text gets additional scaling
            settings.textScaling * 1.2f
        } else {
            settings.textScaling
        }
        
        val fontWeight = if (settings.boldText) {
            FontWeight.Bold
        } else {
            baseStyle.fontWeight
        }
        
        val fontSize = baseStyle.fontSize.times(scale)
        
        return baseStyle.copy(
            fontSize = fontSize,
            fontWeight = fontWeight
        )
    }
    
    /**
     * Get colors adjusted for high contrast mode
     */
    fun getHighContrastColors(colorScheme: ColorScheme): ColorScheme {
        if (!settings.highContrast) return colorScheme
        
        return colorScheme.copy(
            primary = adjustForContrast(colorScheme.primary, colorScheme.surface, WCAG_AAA_NORMAL_TEXT),
            primaryContainer = adjustForContrast(colorScheme.primaryContainer, colorScheme.surface, WCAG_AAA_NORMAL_TEXT),
            onPrimary = adjustForContrast(colorScheme.onPrimary, colorScheme.primary, WCAG_AAA_NORMAL_TEXT),
            onPrimaryContainer = adjustForContrast(colorScheme.onPrimaryContainer, colorScheme.primaryContainer, WCAG_AAA_NORMAL_TEXT),
            
            secondary = adjustForContrast(colorScheme.secondary, colorScheme.surface, WCAG_AAA_NORMAL_TEXT),
            secondaryContainer = adjustForContrast(colorScheme.secondaryContainer, colorScheme.surface, WCAG_AAA_NORMAL_TEXT),
            onSecondary = adjustForContrast(colorScheme.onSecondary, colorScheme.secondary, WCAG_AAA_NORMAL_TEXT),
            onSecondaryContainer = adjustForContrast(colorScheme.onSecondaryContainer, colorScheme.secondaryContainer, WCAG_AAA_NORMAL_TEXT),
            
            tertiary = adjustForContrast(colorScheme.tertiary, colorScheme.surface, WCAG_AAA_NORMAL_TEXT),
            onTertiary = adjustForContrast(colorScheme.onTertiary, colorScheme.tertiary, WCAG_AAA_NORMAL_TEXT),
            
            onSurface = adjustForContrast(colorScheme.onSurface, colorScheme.surface, WCAG_AAA_NORMAL_TEXT),
            onSurfaceVariant = adjustForContrast(colorScheme.onSurfaceVariant, colorScheme.surfaceVariant, WCAG_AA_NORMAL_TEXT),
            
            error = adjustForContrast(colorScheme.error, colorScheme.surface, WCAG_AAA_NORMAL_TEXT),
            onError = adjustForContrast(colorScheme.onError, colorScheme.error, WCAG_AAA_NORMAL_TEXT)
        )
    }
    
    /**
     * Get text selection colors with accessibility adjustments
     */
    fun getTextSelectionColors(colorScheme: ColorScheme): TextSelectionColors {
        return TextSelectionColors(
            handleColor = colorScheme.primary,
            backgroundColor = colorScheme.primary.copy(alpha = 0.4f)
        )
    }
    
    /**
     * Simulate color for color blindness
     */
    fun simulateColorBlindness(color: Color, mode: ColorBlindnessMode): Color {
        return when (mode) {
            ColorBlindnessMode.NONE -> color
            ColorBlindnessMode.DEUTERANOPIA -> simulateDeuteranopia(color)
            ColorBlindnessMode.PROTANOPIA -> simulateProtanopia(color)
            ColorBlindnessMode.TRITANOPIA -> simulateTritanopia(color)
            ColorBlindnessMode.ACHROMATOPSIA -> simulateAchromatopsia(color)
        }
    }
    
    /**
     * Simulate Deuteranopia (Red-Green, most common)
     */
    private fun simulateDeuteranopia(color: Color): Color {
        // Deuteranopia: red and green cones are missing
        // Transform: R = 0.625 * R + 0.375 * G, G = 0.7 * R + 0.3 * G, B = B
        val r = 0.625f * color.red + 0.375f * color.green
        val g = 0.7f * color.red + 0.3f * color.green
        val b = color.blue
        return Color(r, g, b, color.alpha)
    }
    
    /**
     * Simulate Protanopia (Red-Green, less common)
     */
    private fun simulateProtanopia(color: Color): Color {
        // Protanopia: red cones are missing
        // Transform: R = 0.567 * R + 0.433 * G, G = 0.558 * R + 0.442 * G, B = B
        val r = 0.567f * color.red + 0.433f * color.green
        val g = 0.558f * color.red + 0.442f * color.green
        val b = color.blue
        return Color(r, g, b, color.alpha)
    }
    
    /**
     * Simulate Tritanopia (Blue-Yellow)
     */
    private fun simulateTritanopia(color: Color): Color {
        // Tritanopia: blue cones are missing
        // Transform: R = R, G = 0.95 * G + 0.05 * B, B = 0.933 * G + 0.067 * B
        val r = color.red
        val g = 0.95f * color.green + 0.05f * color.blue
        val b = 0.933f * color.green + 0.067f * color.blue
        return Color(r, g, b, color.alpha)
    }
    
    /**
     * Simulate Achromatopsia (Complete color blindness)
     */
    private fun simulateAchromatopsia(color: Color): Color {
        // Achromatopsia: only luminance is perceived
        val luminance = color.luminance()
        return Color(luminance, luminance, luminance, color.alpha)
    }
    
    /**
     * Get accessible touch target size
     */
    fun getTouchTargetSize(): Dp {
        return if (settings.minimumTouchTarget) {
            MIN_TOUCH_TARGET_SIZE
        } else {
            40.dp
        }
    }
    
    /**
     * Check if animation should be reduced or disabled
     */
    fun shouldReduceMotion(): Boolean {
        return settings.reducedMotion
    }
    
    /**
     * Check if screen reader is active
     */
    fun isScreenReaderActive(): Boolean {
        return settings.screenReaderEnabled
    }
    
    /**
     * Get accessibility settings from device preferences
     */
    suspend fun getSettingsFromDevice(
        deviceId: String,
        devicePreferencesRepository: com.ourcookbook.domain.repository.DevicePreferencesRepository
    ): AccessibilitySettings {
        return devicePreferencesRepository.getDevicePreferencesByDevice(deviceId)
            .getOrDefault(DevicePreferences.createDefault(deviceId))
            .let { prefs ->
                AccessibilitySettings(
                    reducedMotion = prefs.reduceMotion,
                    highContrast = prefs.highContrastMode,
                    textScaling = prefs.textScaling,
                    colorBlindnessMode = when (prefs.colorBlindnessMode) {
                        "DEUTERANOPIA" -> ColorBlindnessMode.DEUTERANOPIA
                        "PROTANOPIA" -> ColorBlindnessMode.PROTANOPIA
                        "TRITANOPIA" -> ColorBlindnessMode.TRITANOPIA
                        "ACHROMATOPSIA" -> ColorBlindnessMode.ACHROMATOPSIA
                        else -> ColorBlindnessMode.NONE
                    },
                    screenReaderEnabled = prefs.screenReaderCompatibility,
                    minimumTouchTarget = true,
                    boldText = false
                )
            }
    }
    
    /**
     * Save accessibility settings to device preferences
     */
    suspend fun saveSettingsToDevice(
        deviceId: String,
        settings: AccessibilitySettings,
        devicePreferencesRepository: com.ourcookbook.domain.repository.DevicePreferencesRepository
    ) {
        val currentPrefs = devicePreferencesRepository.getDevicePreferencesByDevice(deviceId)
            .getOrDefault(DevicePreferences.createDefault(deviceId))
        val updatedPrefs = currentPrefs.copy(
            reduceMotion = settings.reducedMotion,
            highContrastMode = settings.highContrast,
            textScaling = settings.textScaling,
            colorBlindnessMode = settings.colorBlindnessMode.name,
            screenReaderCompatibility = settings.screenReaderEnabled
        )
        devicePreferencesRepository.updateDevicePreferences(updatedPrefs)
    }
}

/**
 * HSL color representation
 */
private data class HSL(
    val h: Float,  // Hue (0-1)
    val s: Float,  // Saturation (0-1)
    val l: Float   // Lightness (0-1)
)

/**
 * Composable function to remember accessibility settings
 */
@Composable
fun rememberAccessibilitySettings(): AccessibilitySettings {
    val context = LocalContext.current
    val accessibilityManager = remember { AccessibilityManager(context) }
    var settings by remember { mutableStateOf(AccessibilitySettings()) }

    LaunchedEffect(accessibilityManager) {
        accessibilityManager.updateSettings()
        settings = accessibilityManager.settings
    }

    return settings
}

/**
 * Composable function to get accessible color scheme
 */
@Composable
fun accessibleColorScheme(): ColorScheme {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val accessibilityManager = remember { AccessibilityManager(context) }

    return if (accessibilityManager.settings.highContrast) {
        accessibilityManager.getHighContrastColors(colorScheme)
    } else {
        colorScheme
    }
}

/**
 * Composable function to check if reduced motion should be used
 */
@Composable
fun shouldReduceMotion(): Boolean {
    val context = LocalContext.current
    val accessibilityManager = remember { AccessibilityManager(context) }
    return accessibilityManager.shouldReduceMotion()
}

/**
 * Composable function to check if screen reader is active
 */
@Composable
fun isScreenReaderActive(): Boolean {
    val context = LocalContext.current
    val accessibilityManager = remember { AccessibilityManager(context) }
    return accessibilityManager.isScreenReaderActive()
}

/**
 * Composable function to get touch target size
 */
@Composable
fun touchTargetSize(): Dp {
    val context = LocalContext.current
    val accessibilityManager = remember { AccessibilityManager(context) }
    return accessibilityManager.getTouchTargetSize()
}

/**
 * Composable wrapper for accessible text
 */
@Composable
fun AccessibleText(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    isLargeText: Boolean = false,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val context = LocalContext.current
    val accessibilityManager = remember { AccessibilityManager(context) }
    val accessibleStyle = accessibilityManager.getAccessibleTextStyle(style, isLargeText)
    
    androidx.compose.material3.Text(
        text = text,
        style = accessibleStyle,
        modifier = modifier
    )
}
