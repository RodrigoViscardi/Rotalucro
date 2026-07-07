package com.lucronarota.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val VerdeCorrida = Color(0xFF4CAF50)
val AmareloCorrida = Color(0xFFFFC107)
val VermelhoCorrida = Color(0xFFF44336)

val Primary = Color(0xFF1B5E20)
val PrimaryVariant = Color(0xFF388E3C)
val OnPrimary = Color.White
val Secondary = Color(0xFFFF6F00)
val SecondaryVariant = Color(0xFFFF8F00)
val Background = Color(0xFFF5F5F5)
val Surface = Color.White
val OnSurface = Color(0xFF212121)
val OnBackground = Color(0xFF212121)
val Error = Color(0xFFD32F2F)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryVariant,
    secondary = Secondary,
    secondaryContainer = SecondaryVariant,
    background = Background,
    surface = Surface,
    onSurface = OnSurface,
    onBackground = OnBackground,
    error = Error,
    surfaceVariant = Color(0xFFE8F5E9)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF66BB6A),
    onPrimary = Color(0xFF003300),
    primaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFFFFB74D),
    secondaryContainer = Color(0xFFE65100),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    onBackground = Color(0xFFE0E0E0),
    error = Color(0xFFEF9A9A),
    surfaceVariant = Color(0xFF1B3B1B)
)

@Composable
fun LucroNaRotaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
