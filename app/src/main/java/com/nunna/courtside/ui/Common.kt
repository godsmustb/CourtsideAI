package com.nunna.courtside.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Stat(label: String, value: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column(modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color, maxLines = 1)
        Text(label, fontSize = 11.sp, color = Muted, maxLines = 1)
    }
}

@Composable
fun Tag(text: String, color: Color) {
    Text(
        text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF111111),
        modifier = Modifier
            .background(color, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
fun SectionCard(title: String? = null, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(12.dp)) {
            if (title != null) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Orange)
                Spacer(Modifier.width(4.dp))
            }
            content()
        }
    }
}

@Composable
fun Bullet(text: String) {
    Row(Modifier.padding(vertical = 2.dp)) {
        Text("•", color = Orange, modifier = Modifier.padding(end = 6.dp))
        Text(text, fontSize = 14.sp)
    }
}

@Composable
fun ErrorNote(text: String?) {
    if (text == null) return
    Text(text, color = Warn, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
}

fun tierLabel(tier: String?): String = when (tier) {
    "ANCHOR" -> "ANCHOR"
    "STAR2" -> "STAR"
    "CORE" -> "CORE"
    "VALUE" -> "VALUE"
    "ENDGAME" -> "$1-3"
    "DRAIN" -> "DRAIN"
    else -> ""
}

fun tierColor(tier: String?): Color = when (tier) {
    "ANCHOR" -> Color(0xFFFFD54F)
    "STAR2" -> Orange
    "CORE" -> Color(0xFF81C784)
    "VALUE" -> Color(0xFF4FC3F7)
    "ENDGAME" -> Color(0xFFB0BEC5)
    "DRAIN" -> Color(0xFFE57373)
    else -> Muted
}
