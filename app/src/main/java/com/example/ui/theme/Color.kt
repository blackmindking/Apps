package com.example.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    // Dark theme values
    val DarkBg = Color(0xFF080B14)
    val DarkSurface = Color(0xFF0F1629)
    val DarkSurfaceHigh = Color(0xFF161D35)
    val DarkBorder = Color(0xFF1E2A45)
    val DarkAccent = Color(0xFF6C63FF)
    val DarkAccentSecond = Color(0xFFFF6584)
    val DarkAccentThird = Color(0xFF43E8B0)
    val DarkTextPrimary = Color(0xFFF0F4FF)
    val DarkTextSecondary = Color(0xFF8892B0)
    val DarkTextMuted = Color(0xFF4A5578)
    val DarkDanger = Color(0xFFFF4757)
    val DarkWarning = Color(0xFFFFB347)
    val DarkSuccess = Color(0xFF43E8B0)
    val DarkGold = Color(0xFFFFD700)

    // Light theme values
    val LightBg = Color(0xFFF0F4FF)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurfaceHigh = Color(0xFFF8FAFF)
    val LightBorder = Color(0xFFE2E8F8)
    val LightAccent = Color(0xFF6C63FF)
    val LightAccentSecond = Color(0xFFFF6584)
    val LightAccentThird = Color(0xFF059669)
    val LightTextPrimary = Color(0xFF0A0E1F)
    val LightTextSecondary = Color(0xFF4A5578)
    val LightTextMuted = Color(0xFF94A3B8)
    val LightDanger = Color(0xFFDC2626)
    val LightWarning = Color(0xFFD97706)
    val LightSuccess = Color(0xFF059669)
    val LightGold = Color(0xFFB45309)

    // Category colors (same in both themes)
    val CategoryColors = mapOf(
        "Entertainment" to Color(0xFFFF6B6B),
        "Music" to Color(0xFF6C63FF),
        "Fitness" to Color(0xFF43E8B0),
        "Food" to Color(0xFFFF9F43),
        "Productivity" to Color(0xFF5352ED),
        "Software" to Color(0xFF2ECC71),
        "News" to Color(0xFFA29BFE),
        "Finance" to Color(0xFFFDCB6E),
        "Shopping" to Color(0xFFFD79A8),
        "Health" to Color(0xFF00CEC9),
        "Education" to Color(0xFF74B9FF),
        "Other" to Color(0xFF636E72)
    )

    fun getCategoryColor(category: String): Color {
        return CategoryColors[category] ?: CategoryColors["Other"]!!
    }
}
