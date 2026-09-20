package com.nikpapajohn.moviedb.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Teal700,
    onPrimary = OnPrimaryLight,
    primaryContainer = TealContainer,
    onPrimaryContainer = Teal700,
    secondary = Teal700,
    onSecondary = OnPrimaryLight,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceLight,
    outline = OutlineLight,
    error = ErrorLight,
    onError = OnPrimaryLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    tertiary = Teal700,
    onTertiary = OnPrimaryLight,
)

private val DarkColors = darkColorScheme(
    primary = Teal200Dark,
    onPrimary = OnPrimaryDark,
    primaryContainer = TealContainerDark,
    onPrimaryContainer = TealContainer,
    secondary = Teal200Dark,
    onSecondary = OnPrimaryDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceDark,
    outline = OutlineDark,
    error = ErrorDark,
    onError = OnPrimaryDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    tertiary = Teal200Dark,
    onTertiary = OnPrimaryDark,
)

/**
 * Dynamic color is deliberately off: the assessment mockup specifies a brand palette,
 * and Material You would replace it with the device wallpaper colors on Android 12+.
 */
@Composable
fun MovieDbTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MovieDbTypography,
        content = content,
    )
}
