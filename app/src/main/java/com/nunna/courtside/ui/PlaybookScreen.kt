package com.nunna.courtside.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nunna.courtside.BuildConfig
import com.nunna.courtside.data.Store
import kotlinx.serialization.Serializable

@Serializable
data class PlaybookSection(val title: String, val bullets: List<String>)

@Serializable
data class Playbook(val sections: List<PlaybookSection>)

@Composable
fun PlaybookScreen(state: AppState) {
    val ctx = LocalContext.current
    val book = remember {
        runCatching {
            val text = ctx.assets.open("playbook.json").bufferedReader().use { it.readText() }
            Store.json.decodeFromString(Playbook.serializer(), text)
        }.getOrDefault(Playbook(emptyList()))
    }
    var open by remember { mutableStateOf(setOf(0)) }

    LazyColumn(Modifier.fillMaxWidth()) {
        items(book.sections.size) { i ->
            val s = book.sections[i]
            SectionCard {
                Row(Modifier.fillMaxWidth().clickable { open = if (i in open) open - i else open + i }) {
                    Text(s.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Orange, modifier = Modifier.weight(1f))
                    Text(if (i in open) "−" else "+", color = Muted, fontSize = 18.sp)
                }
                if (i in open) s.bullets.forEach { Bullet(it) }
            }
        }
        item {
            SectionCard("ℹ️ About") {
                Text("Courtside AI ${BuildConfig.VERSION_NAME} · player data ${state.dataVersion}", fontSize = 13.sp)
                Text("Updates arrive automatically through Obtainium (GitHub Releases).", fontSize = 13.sp, color = Muted)
                Text(
                    "Live scores and injuries come from ESPN's public data feed. Player values are research estimates. " +
                        "Not affiliated with the NBA or ESPN. For fun and information only.",
                    fontSize = 12.sp, color = Muted,
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
