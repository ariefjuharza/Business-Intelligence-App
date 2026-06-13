package studio.bonodigital.businessintelligence.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = TextPrimary,
    secondary = DarkSecondary,
    onSecondary = TextPrimary,
    tertiary = DarkTertiary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    error = BearishRed,
    onError = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = DarkPrimary,
    onPrimary = TextPrimary,
    secondary = DarkSecondary,
    onSecondary = TextPrimary,
    tertiary = DarkTertiary,
    background = DarkBackground,  // We force Dark Background for both light/dark for a consistent premium look, or we can make a lighter variation
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary
)

@Composable
fun BusinessIntelligenceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}