package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.DarkAccent,
    secondary = AppColors.DarkAccentSecond,
    tertiary = AppColors.DarkAccentThird,
    background = AppColors.DarkBg,
    surface = AppColors.DarkSurface,
    onBackground = AppColors.DarkTextPrimary,
    onSurface = AppColors.DarkTextPrimary,
    onPrimary = AppColors.DarkTextPrimary,
    outline = AppColors.DarkBorder,
    error = AppColors.DarkDanger
)

private val LightColorScheme = lightColorScheme(
    primary = AppColors.LightAccent,
    secondary = AppColors.LightAccentSecond,
    tertiary = AppColors.LightAccentThird,
    background = AppColors.LightBg,
    surface = AppColors.LightSurface,
    onBackground = AppColors.LightTextPrimary,
    onSurface = AppColors.LightTextPrimary,
    onPrimary = AppColors.LightSurface,
    outline = AppColors.LightBorder,
    error = AppColors.LightDanger
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // SubTrackr standard is default dark
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
