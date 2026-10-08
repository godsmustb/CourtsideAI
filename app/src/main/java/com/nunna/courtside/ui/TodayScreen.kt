@file:OptIn(ExperimentalMaterial3Api::class)

package com.nunna.courtside.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nunna.courtside.engine.Dates
import com.nunna.courtside.net.Game
import java.time.LocalDate

@Composable
fun TodayScreen(state: AppState) {
    val days = Dates.weekDays()
    var date by remember { mutableStateOf(Dates.today()) }
    LaunchedEffect(date) { state.loadDay(date) }
    val games: List<Game>? = state.week[date]
    val roster = state.roster
    val busiest = days.maxByOrNull { state.week[it]?.size ?: 0 }

    LazyColumn(Modifier.fillMaxWidth()) {
        item {
            Text(
                "NBA games per day this week (busiest day = best night to use up your weekly games)",
                fontSize = 12.sp, color = Muted, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
            LazyRow(Modifier.padding(horizontal = 12.dp)) {
                items(days) { d ->
                    val n = state.week[d]?.size
                    FilterChip(
                        selected = d == date,
                        onClick = { date = d },
                        label = {
                            Text(
                                Dates.short(d) + " · " + (n?.toString() ?: "–") + (if (d == busiest && n != null && n > 0) " 🔥" else ""),
                            )
                        },
                        modifier = Modifier.padding(end = 6.dp),
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                androidx.compose.material3.TextButton(onClick = { date = date.minusDays(1) }) { Text("◀ Prev day") }
                androidx.compose.material3.TextButton(onClick = { date = Dates.today() }) { Text("Today") }
                androidx.compose.material3.TextButton(onClick = { date = date.plusDays(1) }) { Text("Next day ▶") }
            }
            Text(
                Dates.label(date) + when {
                    games == null -> " · loading…"
                    games.isEmpty() -> " · no games"
                    else -> " · ${games.size} games"
                },
                fontWeight = FontWeight.Bold, fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            ErrorNote(state.weekError)
        }
        items(games.orEmpty(), key = { it.id + it.start }) { g ->
            val mine = roster.filter { it.team in g.teams }
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        TeamLine(g.away.name, g.away.abbr, g.away.score, g.state != "pre")
                        TeamLine(g.home.name, g.home.abbr, g.home.score, g.state != "pre")
                    }
                    Text(
                        when (g.state) {
                            "pre" -> Dates.tipTime(g.start).ifBlank { g.detail }
                            else -> g.detail
                        },
                        color = if (g.state == "in") Good else Muted,
                        fontWeight = if (g.state == "in") FontWeight.Bold else FontWeight.Normal,
                    )
                }
                if (mine.isNotEmpty()) {
                    Text("Your players: " + mine.joinToString { it.name }, color = Orange, fontSize = 13.sp)
                }
            }
        }
        item { androidx.compose.foundation.layout.Spacer(Modifier.padding(12.dp)) }
    }
}

@Composable
private fun TeamLine(name: String, abbr: String, score: String, showScore: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text("$abbr  $name", modifier = Modifier.weight(1f))
        if (showScore) Text(score, fontWeight = FontWeight.Bold)
    }
}

@Suppress("unused")
private fun LocalDate.isWeekend() = dayOfWeek.value >= 6
