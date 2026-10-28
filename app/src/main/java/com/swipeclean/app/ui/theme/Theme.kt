package com.swipeclean.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.swipeclean.app.R
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.swipeclean.app.data.ThemeMode

/**
 * "Keep" green is a custom M3 extended color (primary/on/container/on-container roles),
 * alongside the scheme's built-in error roles used for "delete".
 */
@Immutable
data class ExtendedColors(
    val keep: Color,
    val onKeep: Color,
    val keepContainer: Color,
    val onKeepContainer: Color,
)

private val LightExtended = ExtendedColors(
    keep = Color(0xFF2E6A44),
    onKeep = Color(0xFFFFFFFF),
    keepContainer = Color(0xFFB1F1C1),
    onKeepContainer = Color(0xFF002110),
)

private val DarkExtended = ExtendedColors(
    keep = Color(0xFF96D5A6),
    onKeep = Color(0xFF00391D),
    keepContainer = Color(0xFF12512E),
    onKeepContainer = Color(0xFFB1F1C1),
)

private val LocalExtendedColors = staticCompositionLocalOf { LightExtended }

object AppTheme {
    val extended: ExtendedColors
        @Composable get() = LocalExtendedColors.current
}

/** Inter (SIL OFL, bundled as a variable font) at the weights the M3 type scale uses. */
@OptIn(ExperimentalTextApi::class)
private fun inter(weight: FontWeight) = Font(
    resId = R.font.inter,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

private val Inter = FontFamily(
    inter(FontWeight.Normal),
    inter(FontWeight.Medium),
    inter(FontWeight.SemiBold),
    inter(FontWeight.Bold),
)

/** The default M3 type scale, set in Inter everywhere. */
private val AppTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = Inter),
        displayMedium = displayMedium.copy(fontFamily = Inter),
        displaySmall = displaySmall.copy(fontFamily = Inter),
        headlineLarge = headlineLarge.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        headlineMedium = headlineMedium.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        headlineSmall = headlineSmall.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.copy(fontFamily = Inter),
        bodyLarge = bodyLarge.copy(fontFamily = Inter),
        bodyMedium = bodyMedium.copy(fontFamily = Inter),
        bodySmall = bodySmall.copy(fontFamily = Inter),
        labelLarge = labelLarge.copy(fontFamily = Inter),
        labelMedium = labelMedium.copy(fontFamily = Inter),
        labelSmall = labelSmall.copy(fontFamily = Inter),
    )
}

/** Material 3 baseline scheme (seed #6750A4). Used when dynamic color is off or unavailable. */
private val BaselineLight = lightColorScheme()
private val BaselineDark = darkColorScheme()

@Composable
fun SwipeCleanTheme(mode: ThemeMode, dynamicColor: Boolean, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> BaselineDark
        else -> BaselineLight
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides if (dark) DarkExtended else LightExtended) {
        // M3 type scale (in Inter) and the default M3 shape scale.
        MaterialTheme(colorScheme = colorScheme, typography = AppTypography, content = content)
    }
}
