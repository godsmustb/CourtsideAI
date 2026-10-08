package com.nunna.courtside.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Orange = Color(0xFFF5A623)
val Good = Color(0xFF4CAF50)
val Warn = Color(0xFFFFB300)
val Bad = Color(0xFFEF5350)
val Muted = Color(0xFF9AA3AD)

private val scheme = darkColorScheme(
    primary = Orange,
    onPrimary = Color(0xFF231400),
    secondary = Color(0xFF4FC3F7),
    background = Color(0xFF0F1115),
    surface = Color(0xFF171A21),
    surfaceVariant = Color(0xFF222632),
    onBackground = Color(0xFFE8E8E8),
    onSurface = Color(0xFFE8E8E8),
    onSurfaceVariant = Color(0xFFB0B7C3),
    error = Bad,
)

@Composable
fun CourtsideTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, content = content)
}

fun statusColor(status: String): Color {
    val s = status.lowercase()
    return when {
        "out" in s || "injured reserve" in s || s == "ir" -> Bad
        "day" in s || "question" in s || "doubt" in s || "game time" in s || "gtd" in s -> Warn
        else -> Muted
    }
}
