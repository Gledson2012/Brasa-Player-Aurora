package com.example.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.example.data.model.AppThemeType
import com.example.data.model.CustomThemeConfig
import com.example.data.model.ThemeConfig
import com.example.data.model.ThemeMode

fun buildColorSchemeForPreset(theme: AppThemeType, forceLight: Boolean = false): ColorScheme {
    if (forceLight || !theme.isDarkPreset) {
        return buildLightPresetScheme(theme)
    }

    return when (theme) {
        AppThemeType.MIDNIGHT_OLED -> darkColorScheme(
            primary = theme.primaryColor,
            onPrimary = Color.White,
            primaryContainer = Color(0xFF2A1448),
            onPrimaryContainer = Color(0xFFE9D5FF),
            secondary = theme.secondaryColor,
            onSecondary = Color.Black,
            secondaryContainer = Color(0xFF00444D),
            onSecondaryContainer = Color(0xFFB4F9FF),
            tertiary = theme.tertiaryColor,
            background = theme.surfaceDark,
            onBackground = Color(0xFFF1F0F5),
            surface = DarkOledSurface,
            onSurface = Color(0xFFF1F0F5),
            surfaceVariant = DarkOledSurfaceVariant,
            onSurfaceVariant = Color(0xFFC7C2D6),
            outline = Color(0xFF382F52)
        )
        AppThemeType.CYBERPUNK_NEON -> darkColorScheme(
            primary = theme.primaryColor,
            onPrimary = Color.White,
            primaryContainer = Color(0xFF4C0028),
            onPrimaryContainer = Color(0xFFFFD6EA),
            secondary = theme.secondaryColor,
            onSecondary = Color.Black,
            secondaryContainer = Color(0xFF00434C),
            onSecondaryContainer = Color(0xFFB3F7FF),
            tertiary = theme.tertiaryColor,
            background = theme.surfaceDark,
            onBackground = Color(0xFFF7F3FF),
            surface = CyberSurface,
            onSurface = Color(0xFFF7F3FF),
            surfaceVariant = CyberSurfaceVariant,
            onSurfaceVariant = Color(0xFFCBC4E6),
            outline = Color(0xFF452B80)
        )
        AppThemeType.SUNSET_GLOW -> darkColorScheme(
            primary = theme.primaryColor,
            onPrimary = Color.White,
            primaryContainer = Color(0xFF571D0A),
            onPrimaryContainer = Color(0xFFFFDBD0),
            secondary = theme.secondaryColor,
            onSecondary = Color.Black,
            secondaryContainer = Color(0xFF523D00),
            onSecondaryContainer = Color(0xFFFFEFA6),
            tertiary = theme.tertiaryColor,
            background = theme.surfaceDark,
            onBackground = Color(0xFFFDF0ED),
            surface = SunsetSurface,
            onSurface = Color(0xFFFDF0ED),
            surfaceVariant = SunsetSurfaceVariant,
            onSurfaceVariant = Color(0xFFD8C1BD),
            outline = Color(0xFF5C3B43)
        )
        AppThemeType.EMERALD_FOREST -> darkColorScheme(
            primary = theme.primaryColor,
            onPrimary = Color.Black,
            primaryContainer = Color(0xFF004D25),
            onPrimaryContainer = Color(0xFFA6FFCE),
            secondary = theme.secondaryColor,
            onSecondary = Color.Black,
            secondaryContainer = Color(0xFF004D3F),
            onSecondaryContainer = Color(0xFF9CF8E1),
            tertiary = theme.tertiaryColor,
            background = theme.surfaceDark,
            onBackground = Color(0xFFEDF8F4),
            surface = EmeraldSurface,
            onSurface = Color(0xFFEDF8F4),
            surfaceVariant = EmeraldSurfaceVariant,
            onSurfaceVariant = Color(0xFFBFDDD4),
            outline = Color(0xFF285444)
        )
        AppThemeType.SAPPHIRE_BLUE -> darkColorScheme(
            primary = theme.primaryColor,
            onPrimary = Color.White,
            primaryContainer = Color(0xFF0D2D7A),
            onPrimaryContainer = Color(0xFFDBE6FF),
            secondary = theme.secondaryColor,
            onSecondary = Color.Black,
            secondaryContainer = Color(0xFF003859),
            onSecondaryContainer = Color(0xFFBCE7FF),
            tertiary = theme.tertiaryColor,
            background = theme.surfaceDark,
            onBackground = Color(0xFFEEF3FC),
            surface = SapphireSurface,
            onSurface = Color(0xFFEEF3FC),
            surfaceVariant = SapphireSurfaceVariant,
            onSurfaceVariant = Color(0xFFBAC5DD),
            outline = Color(0xFF233E72)
        )
        AppThemeType.RETRO_SYNTHWAVE -> darkColorScheme(
            primary = theme.primaryColor,
            onPrimary = Color.White,
            primaryContainer = Color(0xFF4A0059),
            onPrimaryContainer = Color(0xFFFFD4FF),
            secondary = theme.secondaryColor,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFF5D0000),
            onSecondaryContainer = Color(0xFFFFDAD6),
            tertiary = theme.tertiaryColor,
            background = theme.surfaceDark,
            onBackground = Color(0xFFFBEFFF),
            surface = RetroSurface,
            onSurface = Color(0xFFFBEFFF),
            surfaceVariant = RetroSurfaceVariant,
            onSurfaceVariant = Color(0xFFDCC2EB),
            outline = Color(0xFF5F2783)
        )
        AppThemeType.AMOLED_PITCH_BLACK -> darkColorScheme(
            primary = theme.primaryColor,
            onPrimary = Color.Black,
            primaryContainer = Color(0xFF003D34),
            onPrimaryContainer = Color(0xFFB0FFF2),
            secondary = theme.secondaryColor,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFF380D61),
            onSecondaryContainer = Color(0xFFE8C8FF),
            tertiary = theme.tertiaryColor,
            background = PitchBlackBackground,
            onBackground = Color(0xFFF0F0F0),
            surface = PitchBlackSurface,
            onSurface = Color(0xFFF0F0F0),
            surfaceVariant = PitchBlackSurfaceVariant,
            onSurfaceVariant = Color(0xFFC0C0C0),
            outline = Color(0xFF333333)
        )
        AppThemeType.ROSE_GOLD -> darkColorScheme(
            primary = theme.primaryColor,
            onPrimary = Color.White,
            primaryContainer = Color(0xFF4E0326),
            onPrimaryContainer = Color(0xFFFFD9E8),
            secondary = theme.secondaryColor,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFF3D0034),
            onSecondaryContainer = Color(0xFFFFD7F3),
            tertiary = theme.tertiaryColor,
            background = RoseGoldBackground,
            onBackground = Color(0xFFFDF0F6),
            surface = RoseGoldSurface,
            onSurface = Color(0xFFFDF0F6),
            surfaceVariant = RoseGoldSurfaceVariant,
            onSurfaceVariant = Color(0xFFE2C4D3),
            outline = Color(0xFF572545)
        )
        AppThemeType.NORDIC_FROST -> darkColorScheme(
            primary = theme.primaryColor,
            onPrimary = Color(0xFF001E2B),
            primaryContainer = Color(0xFF004D6B),
            onPrimaryContainer = Color(0xFFC2E8FF),
            secondary = theme.secondaryColor,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFF1E2966),
            onSecondaryContainer = Color(0xFFDFE2FF),
            tertiary = theme.tertiaryColor,
            background = NordicFrostBackground,
            onBackground = Color(0xFFEDF2F7),
            surface = NordicFrostSurface,
            onSurface = Color(0xFFEDF2F7),
            surfaceVariant = NordicFrostSurfaceVariant,
            onSurfaceVariant = Color(0xFFB8C5D6),
            outline = Color(0xFF2E4061)
        )
        AppThemeType.AURORA_MINT -> darkColorScheme(
            primary = theme.primaryColor,
            onPrimary = Color(0xFF003731),
            primaryContainer = Color(0xFF005047),
            onPrimaryContainer = Color(0xFF8AF7E7),
            secondary = theme.secondaryColor,
            onSecondary = Color(0xFF263500),
            secondaryContainer = Color(0xFF354F00),
            onSecondaryContainer = Color(0xFFD2FF8B),
            tertiary = theme.tertiaryColor,
            background = theme.surfaceDark,
            onBackground = Color(0xFFE6F5F1),
            surface = Color(0xFF10221F),
            onSurface = Color(0xFFE6F5F1),
            surfaceVariant = Color(0xFF19352F),
            onSurfaceVariant = Color(0xFFB5D3CA),
            outline = Color(0xFF35675B)
        )
        AppThemeType.VOLCANIC_RED -> darkColorScheme(
            primary = theme.primaryColor,
            onPrimary = Color.White,
            primaryContainer = Color(0xFF680022),
            onPrimaryContainer = Color(0xFFFFD9DF),
            secondary = theme.secondaryColor,
            onSecondary = Color(0xFF3D2E00),
            secondaryContainer = Color(0xFF5A4300),
            onSecondaryContainer = Color(0xFFFFE08A),
            tertiary = theme.tertiaryColor,
            background = theme.surfaceDark,
            onBackground = Color(0xFFFFF0F1),
            surface = Color(0xFF2A1217),
            onSurface = Color(0xFFFFF0F1),
            surfaceVariant = Color(0xFF411D25),
            onSurfaceVariant = Color(0xFFE4BBC2),
            outline = Color(0xFF74404C)
        )
        AppThemeType.OBSIDIAN_GOLD -> darkColorScheme(
            primary = theme.primaryColor,
            onPrimary = Color(0xFF2B1A00),
            primaryContainer = Color(0xFF5A3A00),
            onPrimaryContainer = Color(0xFFFFE8B3),
            secondary = theme.secondaryColor,
            onSecondary = Color(0xFF2B1600),
            secondaryContainer = Color(0xFF4D2800),
            onSecondaryContainer = Color(0xFFFFDC9A),
            tertiary = theme.tertiaryColor,
            background = theme.surfaceDark,
            onBackground = Color(0xFFF6F2EA),
            surface = Color(0xFF18191F),
            onSurface = Color(0xFFF6F2EA),
            surfaceVariant = Color(0xFF282A33),
            onSurfaceVariant = Color(0xFFD0C8B7),
            outline = Color(0xFF5B5140)
        )
        AppThemeType.OCEANIC_TEAL -> darkColorScheme(
            primary = theme.primaryColor,
            onPrimary = Color(0xFF002A2E),
            primaryContainer = Color(0xFF004B52),
            onPrimaryContainer = Color(0xFFA5F3FC),
            secondary = theme.secondaryColor,
            onSecondary = Color(0xFF002A27),
            secondaryContainer = Color(0xFF064E4A),
            onSecondaryContainer = Color(0xFF99F6E4),
            tertiary = theme.tertiaryColor,
            background = theme.surfaceDark,
            onBackground = Color(0xFFE4F7F7),
            surface = Color(0xFF0C2226),
            onSurface = Color(0xFFE4F7F7),
            surfaceVariant = Color(0xFF12343A),
            onSurfaceVariant = Color(0xFFB5D8D9),
            outline = Color(0xFF2B6269)
        )
        AppThemeType.MINIMALIST_LIGHT, AppThemeType.PAPER_CREAM, AppThemeType.LAVENDER_STUDIO -> lightColorScheme(
            primary = theme.primaryColor,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE0E7FF),
            onPrimaryContainer = Color(0xFF1E1B4B),
            secondary = theme.secondaryColor,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFCFFAFE),
            onSecondaryContainer = Color(0xFF164E63),
            tertiary = theme.tertiaryColor,
            background = StudioLightBackground,
            onBackground = StudioLightTextPrimary,
            surface = StudioLightSurface,
            onSurface = StudioLightTextPrimary,
            surfaceVariant = StudioLightSurfaceVariant,
            onSurfaceVariant = StudioLightTextSecondary,
            outline = Color(0xFFCBD5E1)
        )
    }
}

private fun buildLightPresetScheme(theme: AppThemeType): ColorScheme {
    return when (theme) {
        AppThemeType.PAPER_CREAM -> lightColorScheme(
            primary = theme.primaryColor,
            onPrimary = readableContentColor(theme.primaryColor),
            primaryContainer = Color(0xFFFEF3C7),
            onPrimaryContainer = Color(0xFF451A03),
            secondary = theme.secondaryColor,
            onSecondary = readableContentColor(theme.secondaryColor),
            secondaryContainer = Color(0xFFCCFBF1),
            onSecondaryContainer = Color(0xFF134E4A),
            tertiary = theme.tertiaryColor,
            background = WarmLightBackground,
            onBackground = WarmLightTextPrimary,
            surface = WarmLightSurface,
            onSurface = WarmLightTextPrimary,
            surfaceVariant = WarmLightSurfaceVariant,
            onSurfaceVariant = WarmLightTextSecondary,
            outline = Color(0xFFD6D3D1)
        )

        AppThemeType.LAVENDER_STUDIO -> lightColorScheme(
            primary = theme.primaryColor,
            onPrimary = readableContentColor(theme.primaryColor),
            primaryContainer = Color(0xFFEDE9FE),
            onPrimaryContainer = Color(0xFF2E1065),
            secondary = theme.secondaryColor,
            onSecondary = readableContentColor(theme.secondaryColor),
            secondaryContainer = Color(0xFFDBEAFE),
            onSecondaryContainer = Color(0xFF172554),
            tertiary = theme.tertiaryColor,
            background = Color(0xFFF8F7FF),
            onBackground = Color(0xFF17152A),
            surface = Color.White,
            onSurface = Color(0xFF17152A),
            surfaceVariant = Color(0xFFF0EEFF),
            onSurfaceVariant = Color(0xFF68647A),
            outline = Color(0xFFD8D4FE)
        )

        else -> lightColorScheme(
            primary = theme.primaryColor,
            onPrimary = readableContentColor(theme.primaryColor),
            primaryContainer = Color(0xFFE0E7FF),
            onPrimaryContainer = Color(0xFF1E1B4B),
            secondary = theme.secondaryColor,
            onSecondary = readableContentColor(theme.secondaryColor),
            secondaryContainer = Color(0xFFCFFAFE),
            onSecondaryContainer = Color(0xFF164E63),
            tertiary = theme.tertiaryColor,
            background = StudioLightBackground,
            onBackground = StudioLightTextPrimary,
            surface = StudioLightSurface,
            onSurface = StudioLightTextPrimary,
            surfaceVariant = StudioLightSurfaceVariant,
            onSurfaceVariant = StudioLightTextSecondary,
            outline = Color(0xFFCBD5E1)
        )
    }
}

private fun readableContentColor(background: Color): Color {
    return if (background.luminance() > 0.42f) Color(0xFF0F172A) else Color.White
}

fun buildColorSchemeForCustom(custom: CustomThemeConfig): ColorScheme {
    return if (custom.isDark) {
        darkColorScheme(
            primary = custom.primaryColor,
            onPrimary = Color.White,
            primaryContainer = custom.primaryColor.copy(alpha = 0.25f),
            onPrimaryContainer = Color(0xFFF1F0F5),
            secondary = custom.secondaryColor,
            onSecondary = Color.Black,
            secondaryContainer = custom.secondaryColor.copy(alpha = 0.25f),
            onSecondaryContainer = Color(0xFFF1F0F5),
            tertiary = custom.tertiaryColor,
            background = custom.backgroundColor,
            onBackground = Color(0xFFF1F0F5),
            surface = custom.surfaceColor,
            onSurface = Color(0xFFF1F0F5),
            surfaceVariant = custom.surfaceColor.copy(alpha = 0.85f),
            onSurfaceVariant = Color(0xFFCBC4E6),
            outline = custom.primaryColor.copy(alpha = 0.35f)
        )
    } else {
        lightColorScheme(
            primary = custom.primaryColor,
            onPrimary = Color.White,
            primaryContainer = custom.primaryColor.copy(alpha = 0.15f),
            onPrimaryContainer = Color(0xFF0F172A),
            secondary = custom.secondaryColor,
            onSecondary = Color.White,
            secondaryContainer = custom.secondaryColor.copy(alpha = 0.15f),
            onSecondaryContainer = Color(0xFF0F172A),
            tertiary = custom.tertiaryColor,
            background = custom.backgroundColor,
            onBackground = StudioLightTextPrimary,
            surface = custom.surfaceColor,
            onSurface = StudioLightTextPrimary,
            surfaceVariant = Color(0xFFEEF2F6),
            onSurfaceVariant = StudioLightTextSecondary,
            outline = Color(0xFFCBD5E1)
        )
    }
}

fun buildColorScheme(
    themeConfig: ThemeConfig,
    isSystemDark: Boolean,
    context: Context
): ColorScheme {
    // Dynamic Colors (Material You on Android 12+)
    if (themeConfig.dynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val isDark = when (themeConfig.themeMode) {
            ThemeMode.SYSTEM -> isSystemDark
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.CUSTOM -> themeConfig.customTheme.isDark
        }
        return try {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } catch (e: Exception) {
            // Fallback to current theme if dynamic colors fail
            buildColorSchemeForPreset(themeConfig.presetTheme, !isDark)
        }
    }

    return when (themeConfig.themeMode) {
        ThemeMode.SYSTEM -> {
            if (isSystemDark) {
                buildColorSchemeForPreset(themeConfig.presetTheme, forceLight = false)
            } else {
                buildColorSchemeForPreset(themeConfig.presetTheme, forceLight = true)
            }
        }
        ThemeMode.LIGHT -> {
            buildColorSchemeForPreset(themeConfig.presetTheme, forceLight = true)
        }
        ThemeMode.DARK -> {
            buildColorSchemeForPreset(themeConfig.presetTheme, forceLight = false)
        }
        ThemeMode.CUSTOM -> {
            buildColorSchemeForCustom(themeConfig.customTheme)
        }
    }
}

// Legacy helper for backward compatibility
fun buildColorSchemeForTheme(theme: AppThemeType): ColorScheme {
    return buildColorSchemeForPreset(theme)
}

/**
 * Extract dynamic color preview from the device wallpaper.
 * Returns a list of key colors (primary, secondary, tertiary, etc.) for preview.
 * Only works on Android 12+ (API 31+).
 */
fun extractDynamicColorPreview(context: Context): DynamicColorPreview? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null

    return try {
        val darkScheme = dynamicDarkColorScheme(context)
        val lightScheme = dynamicLightColorScheme(context)
        DynamicColorPreview(
            primary = darkScheme.primary,
            primaryContainer = darkScheme.primaryContainer,
            secondary = darkScheme.secondary,
            secondaryContainer = darkScheme.secondaryContainer,
            tertiary = darkScheme.tertiary,
            tertiaryContainer = darkScheme.tertiaryContainer,
            background = darkScheme.background,
            surface = darkScheme.surface,
            lightPrimary = lightScheme.primary,
            lightSecondary = lightScheme.secondary,
            lightTertiary = lightScheme.tertiary
        )
    } catch (e: Exception) {
        null
    }
}

data class DynamicColorPreview(
    val primary: Color,
    val primaryContainer: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val tertiary: Color,
    val tertiaryContainer: Color,
    val background: Color,
    val surface: Color,
    val lightPrimary: Color,
    val lightSecondary: Color,
    val lightTertiary: Color
)

@Composable
fun MusicPlayerTheme(
    themeConfig: ThemeConfig = ThemeConfig(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()
    val targetColorScheme = buildColorScheme(themeConfig, isSystemDark, context)

    // Smooth animated transition for key colors (600ms)
    val animSpec = tween<Color>(durationMillis = 600)
    val animatedPrimary by animateColorAsState(targetColorScheme.primary, animSpec)
    val animatedOnPrimary by animateColorAsState(targetColorScheme.onPrimary, animSpec)
    val animatedPrimaryContainer by animateColorAsState(targetColorScheme.primaryContainer, animSpec)
    val animatedSecondary by animateColorAsState(targetColorScheme.secondary, animSpec)
    val animatedSecondaryContainer by animateColorAsState(targetColorScheme.secondaryContainer, animSpec)
    val animatedTertiary by animateColorAsState(targetColorScheme.tertiary, animSpec)
    val animatedBackground by animateColorAsState(targetColorScheme.background, animSpec)
    val animatedOnBackground by animateColorAsState(targetColorScheme.onBackground, animSpec)
    val animatedSurface by animateColorAsState(targetColorScheme.surface, animSpec)
    val animatedOnSurface by animateColorAsState(targetColorScheme.onSurface, animSpec)
    val animatedSurfaceVariant by animateColorAsState(targetColorScheme.surfaceVariant, animSpec)
    val animatedOnSurfaceVariant by animateColorAsState(targetColorScheme.onSurfaceVariant, animSpec)
    val animatedOutline by animateColorAsState(targetColorScheme.outline, animSpec)

    val animatedScheme = targetColorScheme.copy(
        primary = animatedPrimary,
        onPrimary = animatedOnPrimary,
        primaryContainer = animatedPrimaryContainer,
        secondary = animatedSecondary,
        secondaryContainer = animatedSecondaryContainer,
        tertiary = animatedTertiary,
        background = animatedBackground,
        onBackground = animatedOnBackground,
        surface = animatedSurface,
        onSurface = animatedOnSurface,
        surfaceVariant = animatedSurfaceVariant,
        onSurfaceVariant = animatedOnSurfaceVariant,
        outline = animatedOutline
    )

    MaterialTheme(
        colorScheme = animatedScheme,
        typography = Typography,
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(18.dp),
            large = RoundedCornerShape(28.dp)
        ),
        content = content
    )
}
