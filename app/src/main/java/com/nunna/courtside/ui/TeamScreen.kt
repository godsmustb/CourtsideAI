@file:OptIn(ExperimentalMaterial3Api::class)

package com.nunna.courtside.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.nunna.courtside.data.RosterEntry
import com.nunna.courtside.engine.CapPlanner
import com.nunna.courtside.engine.Dates
import com.nunna.courtside.engine.Names

@Composable
fun TeamScreen(state: AppState) {
    val roster = state.roster
    val today = Dates.today()
    val days = Dates.weekDays().filter { !it.isBefore(today) }
    val used = state.capUsed()
    var showAdd by remember { mutableStateOf(false) }
    var dropTarget by remember { mutableStateOf<RosterEntry?>(null) }

    fun playsOn(team: String, d: java.time.LocalDate): Boolean = state.week[d]?.any { team in it.teams } == true

    val healthy = roster.filter { !state.isOut(it.name) }
    val plan = CapPlanner.plan(used, days.map { d -> CapPlanner.Day(d, healthy.count { playsOn(it.team, d) }) })
    val scheduleLoaded = days.all { state.week.containsKey(it) }

    LazyColumn(Modifier.fillMaxWidth()) {
        item {
            SectionCard("🧮 25-game weekly limit") {
                Text(
                    "Week of ${Dates.label(Dates.monday())}. Only 25 starter games count per week, but every game on the day " +
                        "you pass 25 counts. So hold at 24, then start everyone on the busiest night.",
                    fontSize = 13.sp, color = Muted,
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Games used so far:", modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { state.setCapUsed(used - 1) }) { Text("−") }
                    Text("$used", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp))
                    OutlinedButton(onClick = { state.setCapUsed(used + 1) }) { Text("+") }
                }
                Text("Copy \"Games Played\" from your ESPN matchup page each morning.", fontSize = 11.sp, color = Muted)
                Spacer(Modifier.height(8.dp))
                when {
                    roster.isEmpty() -> Text("Add your players below to get a plan.", color = Muted)
                    !scheduleLoaded -> Text("Loading this week's schedule… tap Refresh if it doesn't load.", color = Muted)
                    used >= CapPlanner.CAP -> Text("✅ You've hit the limit for this week. Nothing else counts until Monday.", color = Good)
                    else -> {
                        plan.days.forEach { d ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                Text(Dates.label(d.date) + if (d.date == today) " (today)" else "", modifier = Modifier.weight(1f),
                                    fontWeight = if (d.overflow) FontWeight.Bold else FontWeight.Normal)
                                Text(
                                    "${d.playable} playing → start ${d.starts}" + if (d.overflow) "  ⭐ ALL-IN" else "",
                                    color = if (d.overflow) Orange else if (d.starts == 0) Muted else androidx.compose.ui.graphics.Color.Unspecified,
                                    fontWeight = if (d.overflow) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Projected counted games: ${plan.projected} (starting everyone every day: ${plan.careless}).",
                            color = if (plan.projected > plan.careless) Good else Muted, fontSize = 13.sp,
                        )
                        Text(
                            "\"Start N\" means how many of your playing guys to put in the lineup that day. Start your best ones first.",
                            fontSize = 11.sp, color = Muted,
                        )
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("My roster (${roster.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                Button(onClick = { showAdd = true }) { Text("+ Add player") }
            }
            if (roster.isEmpty()) {
                Text("Players you mark \"I bought\" in the Draft tab show up here automatically.", color = Muted, modifier = Modifier.padding(16.dp))
            }
        }
        items(roster, key = { it.name }) { r ->
            val inj = state.injuryFor(r.name)
            val left = days.count { playsOn(r.team, it) }
            val playsToday = playsOn(r.team, today)
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(r.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${r.team} · ${r.pos.ifBlank { "?" }} · $left games left this week" + if (playsToday) " · plays today" else "",
                            fontSize = 12.sp, color = if (playsToday) Orange else Muted,
                        )
                        inj?.let { Text(it.comment, fontSize = 12.sp, color = statusColor(it.status), maxLines = 2) }
                    }
                    if (inj != null) Tag(inj.status, statusColor(inj.status))
                    Spacer(Modifier.width(6.dp))
                    TextButton(onClick = { dropTarget = r }) { Text("Drop", color = Muted) }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }

    dropTarget?.let { r ->
        AlertDialog(
            onDismissRequest = { dropTarget = null },
            title = { Text("Remove ${r.name}?") },
            text = { Text("Only removes him from this app. Make the actual drop in the ESPN app.") },
            confirmButton = {
                TextButton(onClick = {
                    state.update { d ->
                        d.copy(
                            dropped = (d.dropped + r.name).distinct(),
                            extraRoster = d.extraRoster.filterNot { Names.key(it.name) == Names.key(r.name) },
                        )
                    }
                    dropTarget = null
                }) { Text("Remove", color = Bad) }
            },
            dismissButton = { TextButton(onClick = { dropTarget = null }) { Text("Cancel") } },
        )
    }

    if (showAdd) AddPlayerDialog(state) { showAdd = false }
}

@Composable
private fun AddPlayerDialog(state: AppState, onDone: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var team by remember { mutableStateOf("") }
    val matches = if (query.length < 2) emptyList() else state.players.filter { it.name.contains(query, true) }.take(8)

    fun add(entry: RosterEntry) {
        state.update { d ->
            d.copy(
                extraRoster = (d.extraRoster.filterNot { Names.key(it.name) == Names.key(entry.name) } + entry),
                dropped = d.dropped.filterNot { Names.key(it) == Names.key(entry.name) },
            )
        }
        onDone()
    }

    AlertDialog(
        onDismissRequest = onDone,
        title = { Text("Add a player") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Player name") }, singleLine = true)
                matches.forEach { p ->
                    Text(
                        "${p.name} · ${p.team} · ${p.pos}",
                        modifier = Modifier.fillMaxWidth().clickable { add(RosterEntry(p.name, p.team, p.pos)) }.padding(vertical = 8.dp),
                        color = Orange,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text("Not in the list (a waiver pickup)? Type his NBA team code, e.g. LAL, BOS, GS:", fontSize = 12.sp, color = Muted)
                OutlinedTextField(value = team, onValueChange = { team = it.take(4) }, label = { Text("Team code") }, singleLine = true)
                Button(
                    onClick = { add(RosterEntry(query.trim(), Names.team(team), "")) },
                    enabled = query.trim().length >= 3 && team.trim().length >= 2,
                ) { Text("Add \"${query.trim()}\"") }
            }
        },
        confirmButton = { TextButton(onClick = onDone) { Text("Cancel") } },
    )
}
