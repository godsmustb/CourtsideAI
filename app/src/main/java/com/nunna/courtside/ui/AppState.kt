package com.nunna.courtside.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nunna.courtside.data.AppData
import com.nunna.courtside.data.Player
import com.nunna.courtside.data.PlayerRepo
import com.nunna.courtside.data.RosterEntry
import com.nunna.courtside.data.Store
import com.nunna.courtside.engine.Dates
import com.nunna.courtside.engine.Names
import com.nunna.courtside.engine.Roster
import com.nunna.courtside.net.Espn
import com.nunna.courtside.net.Game
import com.nunna.courtside.net.Injury
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** One place for everything the screens show. Saved data lives in [Store]; live feeds are refreshed. */
class AppState(private val ctx: Context) {
    private val file = PlayerRepo.load(ctx)
    val players: List<Player> = file.players
    val dataVersion: String = file.version

    var data by mutableStateOf(Store.load(ctx))
        private set

    fun update(change: (AppData) -> AppData) {
        data = change(data)
        Store.save(ctx, data)
    }

    var injuries by mutableStateOf<List<Injury>>(emptyList())
    var injuriesError by mutableStateOf<String?>(null)
    var week by mutableStateOf<Map<LocalDate, List<Game>>>(emptyMap())
    var weekError by mutableStateOf<String?>(null)
    var loading by mutableStateOf(false)
    var updatedAt by mutableStateOf<String?>(null)

    val roster: List<RosterEntry> get() = Roster.of(data, players)

    fun player(name: String): Player? = players.firstOrNull { Names.key(it.name) == Names.key(name) }

    fun injuryFor(name: String): Injury? = injuries.firstOrNull { Names.key(it.player) == Names.key(name) }

    fun isOut(name: String): Boolean = injuryFor(name)?.status?.lowercase()?.contains("out") == true

    /** Games used this week; resets automatically when a new Monday starts. */
    fun capUsed(): Int = if (data.capWeekStart == Dates.monday().toString()) data.capUsed else 0

    fun setCapUsed(n: Int) = update { it.copy(capWeekStart = Dates.monday().toString(), capUsed = n.coerceIn(0, 60)) }

    suspend fun refresh() {
        if (loading) return
        loading = true
        val inj = withContext(Dispatchers.IO) { runCatching { Espn.injuries() } }
        inj.onSuccess { injuries = it; injuriesError = null }
            .onFailure { injuriesError = friendly(it) }
        val days = Dates.weekDays()
        val results = withContext(Dispatchers.IO) {
            coroutineScope { days.map { d -> async { d to runCatching { Espn.scoreboard(d) } } }.awaitAll() }
        }
        val ok = results.mapNotNull { (d, r) -> r.getOrNull()?.let { d to it } }.toMap()
        week = week + ok
        weekError = if (ok.size < results.size) friendly(results.first { it.second.isFailure }.second.exceptionOrNull()) else null
        updatedAt = LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))
        loading = false
    }

    suspend fun loadDay(d: LocalDate) {
        if (week.containsKey(d)) return
        val r = withContext(Dispatchers.IO) { runCatching { Espn.scoreboard(d) } }
        r.onSuccess { week = week + (d to it) }
    }

    companion object {
        fun friendly(e: Throwable?): String =
            "Couldn't reach the live NBA feed (code NET-1: ${e?.message ?: "unknown"}). " +
                "Check your internet and tap Refresh. Your draft and team data are safe on the phone."
    }
}
