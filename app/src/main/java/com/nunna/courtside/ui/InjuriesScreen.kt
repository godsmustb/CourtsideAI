@file:OptIn(ExperimentalMaterial3Api::class)

package com.nunna.courtside.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nunna.courtside.engine.Names

private enum class InjFilter(val label: String) { MINE("👥 My players"), TARGETS("🎯 Draft targets"), ALL("All NBA") }

@Composable
fun InjuriesScreen(state: AppState) {
    var filter by remember { mutableStateOf(if (state.roster.isEmpty()) InjFilter.TARGETS else InjFilter.MINE) }
    var query by remember { mutableStateOf("") }
    val rosterKeys = state.roster.map { Names.key(it.name) }.toSet()
    val targetKeys = state.players.filter { it.tier != null && it.tier != "DRAIN" }.map { Names.key(it.name) }.toSet()

    val list = state.injuries.filter { i ->
        val k = Names.key(i.player)
        (query.isBlank() || i.player.contains(query, true) || i.team.contains(query, true)) && when (filter) {
            InjFilter.MINE -> k in rosterKeys
            InjFilter.TARGETS -> k in targetKeys
            InjFilter.ALL -> true
        }
    }.sortedWith(compareBy<com.nunna.courtside.net.Injury>({ if (statusColor(it.status) == Bad) 0 else 1 }, { it.team }))

    LazyColumn(Modifier.fillMaxWidth()) {
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp)) {
                InjFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f, onClick = { filter = f },
                        label = { Text(f.label) }, modifier = Modifier.padding(end = 6.dp),
                    )
                }
            }
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                label = { Text("Search player or team") }, singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            )
            ErrorNote(state.injuriesError)
            if (filter == InjFilter.MINE) {
                val healthy = state.roster.filter { Names.key(it.name) !in state.injuries.map { i -> Names.key(i.player) }.toSet() }
                if (state.roster.isEmpty()) {
                    Text("Your roster is empty. Buy players in the Draft tab or add them on My Team.", color = Muted, modifier = Modifier.padding(16.dp))
                } else if (healthy.isNotEmpty()) {
                    Text("✅ Not on the injury report: " + healthy.joinToString { it.name }, color = Good, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                }
            }
            Text(
                "Out = won't play. Day-To-Day = might sit; check before tip-off. Alerts for your own players arrive automatically.",
                fontSize = 12.sp, color = Muted, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        if (list.isEmpty() && state.injuriesError == null && !(filter == InjFilter.MINE && state.roster.isEmpty())) {
            item { Text(if (state.loading) "Loading…" else "No injuries in this list. 🎉", color = Muted, modifier = Modifier.padding(16.dp)) }
        }
        items(list, key = { it.player + it.team + it.date }) { i ->
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(i.player, fontWeight = FontWeight.SemiBold)
                        Text(i.team + (if (i.date.length >= 10) " · updated ${i.date.take(10)}" else ""), fontSize = 12.sp, color = Muted)
                    }
                    Tag(i.status, statusColor(i.status))
                }
                if (i.comment.isNotBlank()) Text(i.comment, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
