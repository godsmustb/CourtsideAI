@file:OptIn(ExperimentalMaterial3Api::class)

package com.nunna.courtside.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nunna.courtside.engine.Dates
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class Tab(val label: String, val emoji: String) {
    DRAFT("Draft", "💰"),
    TODAY("Today", "📅"),
    INJURIES("Injuries", "🚑"),
    TEAM("My Team", "👥"),
    PLAYBOOK("Playbook", "📘"),
}

private val DRAFT_DAY: LocalDate = LocalDate.of(2026, 10, 11)

@Composable
fun CourtsideApp() {
    val ctx = LocalContext.current
    val state = remember { AppState(ctx.applicationContext) }
    var tab by rememberSaveable { mutableStateOf(if (Dates.today().isAfter(DRAFT_DAY)) Tab.TEAM else Tab.DRAFT) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { state.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Courtside AI", fontWeight = FontWeight.Bold)
                        Text(
                            "2026-27 NBA Tracker" + (state.updatedAt?.let { " · live $it" } ?: ""),
                            fontSize = 11.sp, color = Muted,
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { scope.launch { state.refresh() } }, enabled = !state.loading) {
                        Text(if (state.loading) "Loading…" else "⟳ Refresh")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = { Text(t.emoji, fontSize = 20.sp) },
                        label = { Text(t.label, maxLines = 1, fontSize = 11.sp) },
                    )
                }
            }
        },
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (tab) {
                Tab.DRAFT -> DraftScreen(state)
                Tab.TODAY -> TodayScreen(state)
                Tab.INJURIES -> InjuriesScreen(state)
                Tab.TEAM -> TeamScreen(state)
                Tab.PLAYBOOK -> PlaybookScreen(state)
            }
        }
    }
}
