package com.swipeclean.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
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
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.swipeclean.app.R
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
    keep = Color(0xFF1E6B45),
    onKeep = Color(0xFFFFFFFF),
    keepContainer = Color(0xFFB4F2C8),
    onKeepContainer = Color(0xFF002112),
)

private val DarkExtended = ExtendedColors(
    keep = Color(0xFF8FD9AC),
    onKeep = Color(0xFF00391F),
    keepContainer = Color(0xFF0E5233),
    onKeepContainer = Color(0xFFB4F2C8),
)

private val LocalExtendedColors = staticCompositionLocalOf { LightExtended }

object AppTheme {
    val extended: ExtendedColors
        @Composable get() = LocalExtendedColors.current
}

// Expressive brand scheme: a saturated violet primary with lilac containers and a pink accent.
private val ExpressiveLight = lightColorScheme(
    primary = Color(0xFF6A35D4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE9DCFF),
    onPrimaryContainer = Color(0xFF23005C),
    inversePrimary = Color(0xFFD2BCFF),
    secondary = Color(0xFF8A4BB8),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF4D9FF),
    onSecondaryContainer = Color(0xFF320047),
    tertiary = Color(0xFFB02A6C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD9E5),
    onTertiaryContainer = Color(0xFF3E0021),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFDF7FF),
    onBackground = Color(0xFF1D1A23),
    surface = Color(0xFFFDF7FF),
    onSurface = Color(0xFF1D1A23),
    surfaceVariant = Color(0xFFE8DEF4),
    onSurfaceVariant = Color(0xFF4A4458),
    surfaceTint = Color(0xFF6A35D4),
    outline = Color(0xFF7B7389),
    outlineVariant = Color(0xFFCCC2DA),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFFDF7FF),
    surfaceDim = Color(0xFFDED6E6),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F1FD),
    surfaceContainer = Color(0xFFF2EAFA),
    surfaceContainerHigh = Color(0xFFECE3F6),
    surfaceContainerHighest = Color(0xFFE6DCF2),
)

private val ExpressiveDark = darkColorScheme(
    primary = Color(0xFFD2BCFF),
    onPrimary = Color(0xFF3B0A92),
    primaryContainer = Color(0xFF5218BB),
    onPrimaryContainer = Color(0xFFE9DCFF),
    inversePrimary = Color(0xFF6A35D4),
    secondary = Color(0xFFE6B4FF),
    onSecondary = Color(0xFF4F1572),
    secondaryContainer = Color(0xFF6B2F96),
    onSecondaryContainer = Color(0xFFF4D9FF),
    tertiary = Color(0xFFFFB0CC),
    onTertiary = Color(0xFF650037),
    tertiaryContainer = Color(0xFF8E1154),
    onTertiaryContainer = Color(0xFFFFD9E5),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF14111A),
    onBackground = Color(0xFFE8E0EE),
    surface = Color(0xFF14111A),
    onSurface = Color(0xFFE8E0EE),
    surfaceVariant = Color(0xFF4A4458),
    onSurfaceVariant = Color(0xFFCCC2DA),
    surfaceTint = Color(0xFFD2BCFF),
    outline = Color(0xFF958CA3),
    outlineVariant = Color(0xFF4A4458),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF3B3741),
    surfaceDim = Color(0xFF14111A),
    surfaceContainerLowest = Color(0xFF0F0C15),
    surfaceContainerLow = Color(0xFF1D1A23),
    surfaceContainer = Color(0xFF221E28),
    surfaceContainerHigh = Color(0xFF2C2832),
    surfaceContainerHighest = Color(0xFF37323D),
)

/** Inter (SIL OFL, bundled as a variable font) at the weights the type scale uses. */
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
    inter(FontWeight.ExtraBold),
    inter(FontWeight.Black),
)

/** M3 type scale in Inter, with heavier, tighter display and headline styles for an expressive feel. */
private val AppTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = Inter, fontWeight = FontWeight.Black, letterSpacing = (-1.5).sp),
        displayMedium = displayMedium.copy(fontFamily = Inter, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1.2).sp),
        displaySmall = displaySmall.copy(fontFamily = Inter, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.8).sp),
        headlineLarge = headlineLarge.copy(fontFamily = Inter, fontWeight = FontWeight.Bold, letterSpacing = (-0.6).sp),
        headlineMedium = headlineMedium.copy(fontFamily = Inter, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp),
        headlineSmall = headlineSmall.copy(fontFamily = Inter, fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp),
        titleLarge = titleLarge.copy(fontFamily = Inter, fontWeight = FontWeight.Bold),
        titleMedium = titleMedium.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        bodyLarge = bodyLarge.copy(fontFamily = Inter),
        bodyMedium = bodyMedium.copy(fontFamily = Inter),
        bodySmall = bodySmall.copy(fontFamily = Inter),
        labelLarge = labelLarge.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        labelMedium = labelMedium.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        labelSmall = labelSmall.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
    )
}

/** Expressive shape scale: noticeably rounder than the baseline. */
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(40.dp),
)

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
        dark -> ExpressiveDark
        else -> ExpressiveLight
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
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = MotionScheme.expressive(),
            shapes = AppShapes,
            typography = AppTypography,
            content = content,
        )
    }
}
