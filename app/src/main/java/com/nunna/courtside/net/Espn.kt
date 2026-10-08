package com.nunna.courtside.net

import com.nunna.courtside.engine.Names
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class TeamScore(val abbr: String, val name: String, val score: String)

data class Game(
    val id: String,
    val start: String,
    /** pre, in or post */
    val state: String,
    val detail: String,
    val home: TeamScore,
    val away: TeamScore,
) {
    val teams: List<String> get() = listOf(home.abbr, away.abbr)
}

data class Injury(
    val player: String,
    val team: String,
    val status: String,
    val comment: String,
    val date: String,
)

/**
 * ESPN's public (unofficial, free, no key) NBA feeds. Can change without notice, so every parse is
 * defensive and the app shows a friendly message on failure.
 */
object Espn {
    private const val BASE = "https://site.api.espn.com/apis/site/v2/sports/basketball/nba"
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun get(url: String): String {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 15_000
        c.readTimeout = 20_000
        c.setRequestProperty("User-Agent", "CourtsideAI/1.0 (Android)")
        c.setRequestProperty("Accept", "application/json")
        try {
            val code = c.responseCode
            if (code !in 200..299) throw IOException("ESPN answered HTTP $code")
            return c.inputStream.bufferedReader().use { it.readText() }
        } finally {
            c.disconnect()
        }
    }

    fun scoreboard(date: LocalDate): List<Game> =
        parseScoreboard(get("$BASE/scoreboard?dates=${date.format(DateTimeFormatter.BASIC_ISO_DATE)}"))

    fun injuries(): List<Injury> = parseInjuries(get("$BASE/injuries"))

    // ---- parsing (pure, unit-tested) ----

    private fun JsonElement?.obj(): JsonObject? = this as? JsonObject
    private fun JsonElement?.arr(): JsonArray? = this as? JsonArray
    private fun JsonElement?.str(): String? = (this as? JsonPrimitive)?.contentOrNull

    fun parseScoreboard(body: String): List<Game> {
        val root = json.parseToJsonElement(body).obj() ?: return emptyList()
        val events = root["events"].arr() ?: return emptyList()
        return events.mapNotNull { ev ->
            val e = ev.obj() ?: return@mapNotNull null
            val comp = e["competitions"].arr()?.firstOrNull().obj()
            val status = (comp?.get("status") ?: e["status"]).obj()
            val type = status?.get("type").obj()
            val competitors = comp?.get("competitors").arr().orEmpty().mapNotNull { it.obj() }
            fun side(which: String): TeamScore {
                val c = competitors.firstOrNull { it["homeAway"].str() == which }
                val t = c?.get("team").obj()
                return TeamScore(
                    abbr = Names.team(t?.get("abbreviation").str() ?: "?"),
                    name = t?.get("shortDisplayName").str() ?: t?.get("displayName").str() ?: "?",
                    score = c?.get("score").str() ?: "",
                )
            }
            Game(
                id = e["id"].str() ?: "",
                start = e["date"].str() ?: "",
                state = type?.get("state").str() ?: "pre",
                detail = type?.get("shortDetail").str() ?: type?.get("detail").str() ?: "",
                home = side("home"),
                away = side("away"),
            )
        }
    }

    fun parseInjuries(body: String): List<Injury> {
        val root = json.parseToJsonElement(body).obj() ?: return emptyList()
        val teams = root["injuries"].arr() ?: return emptyList()
        val out = mutableListOf<Injury>()
        for (t in teams) {
            val team = t.obj() ?: continue
            val teamAbbr = team["abbreviation"].str()?.let { Names.team(it) }
                ?: team["displayName"].str()?.let { Names.teamFromName(it) }
            for (i in team["injuries"].arr().orEmpty()) {
                val inj = i.obj() ?: continue
                val athlete = inj["athlete"].obj()
                val name = athlete?.get("displayName").str() ?: continue
                val abbr = athlete?.get("team").obj()?.get("abbreviation").str()?.let { Names.team(it) }
                    ?: teamAbbr ?: "?"
                out += Injury(
                    player = name,
                    team = abbr,
                    status = inj["status"].str() ?: inj["type"].obj()?.get("description").str() ?: "Unknown",
                    comment = inj["shortComment"].str() ?: inj["longComment"].str() ?: "",
                    date = inj["date"].str() ?: "",
                )
            }
        }
        return out
    }
}
