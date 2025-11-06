package com.example.wardrobe.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.core.view.WindowCompat
import com.example.wardrobe.R

@Composable
fun WardrobeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Color schemes are defined inside the Composable function
    // so they can use the composable colorResource() function.
    val LightColors = lightColorScheme(
        primary = colorResource(R.color.md_theme_light_primary),
        onPrimary = colorResource(R.color.md_theme_light_onPrimary),
        primaryContainer = colorResource(R.color.md_theme_light_primaryContainer),
        onPrimaryContainer = colorResource(R.color.md_theme_light_onPrimaryContainer),
        secondary = colorResource(R.color.md_theme_light_secondary),
        onSecondary = colorResource(R.color.md_theme_light_onSecondary),
        background = colorResource(R.color.md_theme_light_background),
        onBackground = colorResource(R.color.md_theme_light_onBackground),
        surface = colorResource(R.color.md_theme_light_surface),
        onSurface = colorResource(R.color.md_theme_light_onSurface),
        outline = colorResource(R.color.md_theme_light_outline),
    )

    val DarkColors = darkColorScheme(
        primary = colorResource(R.color.md_theme_dark_primary),
        onPrimary = colorResource(R.color.md_theme_dark_onPrimary),
        primaryContainer = colorResource(R.color.md_theme_dark_primaryContainer),
        onPrimaryContainer = colorResource(R.color.md_theme_dark_onPrimaryContainer),
        secondary = colorResource(R.color.md_theme_dark_secondary),
        onSecondary = colorResource(R.color.md_theme_dark_onSecondary),
        background = colorResource(R.color.md_theme_dark_background),
        onBackground = colorResource(R.color.md_theme_dark_onBackground),
        surface = colorResource(R.color.md_theme_dark_surface),
        onSurface = colorResource(R.color.md_theme_dark_onSurface),
        outline = colorResource(R.color.md_theme_dark_outline),
    )

    val colorScheme = if (darkTheme) DarkColors else LightColors

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
        typography = Typography, // Use the Typography object from Type.kt (no parentheses)
        content = content
    )
}