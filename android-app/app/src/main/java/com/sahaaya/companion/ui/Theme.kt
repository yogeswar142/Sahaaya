package com.sahaaya.companion.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PrimaryTeal = Color(0xFF00695C)
val PrimaryTealDark = Color(0xFF004D40)
val SecondaryNavy = Color(0xFF1F4E8C)
val DangerRed = Color(0xFFD32F2F)
val DangerRedLight = Color(0xFFFFEBEE)
val WarningAmber = Color(0xFFEF6C00)
val WarningAmberLight = Color(0xFFFFF3E0)
val SuccessGreen = Color(0xFF2E7D32)
val SuccessGreenLight = Color(0xFFE8F5E9)
val BackgroundLight = Color(0xFFF4F6F9)
val SurfaceWhite = Color(0xFFFFFFFF)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    secondary = SecondaryNavy,
    tertiary = WarningAmber,
    background = BackgroundLight,
    surface = SurfaceWhite,
    error = DangerRed
)

@Composable
fun SahaayaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
