package com.nunna.courtside.data

import kotlinx.serialization.Serializable

/** One NBA player from assets/players.json (researched values for a 10-team $200 9-cat auction). */
@Serializable
data class Player(
    val name: String,
    val team: String,
    val pos: String,
    val rank: Int,
    val value: Int,
    val espnAdp: Int? = null,
    val strengths: String = "",
    val weaknesses: String = "",
    val risk: String = "",
    val rookie: Boolean = false,
    /** ANCHOR, STAR2, CORE, VALUE, ENDGAME, DRAIN (nominate-to-drain) or null = not on our list. */
    val tier: String? = null,
    val maxBid: Int? = null,
    val role: String? = null,
    val why: String? = null,
    val caution: String? = null,
)

@Serializable
data class PlayerFile(val version: String, val players: List<Player>)

/** A player sold in the auction. mine = true when we bought him. */
@Serializable
data class Pick(val player: String, val price: Int, val mine: Boolean)

/** A player added to our roster after the draft (waiver pickup or trade). */
@Serializable
data class RosterEntry(val name: String, val team: String, val pos: String = "")

/** Everything the app remembers on the phone. */
@Serializable
data class AppData(
    val budget: Int = 200,
    val rosterSize: Int = 12,
    val leagueTeams: Int = 10,
    val picks: List<Pick> = emptyList(),
    val extraRoster: List<RosterEntry> = emptyList(),
    val dropped: List<String> = emptyList(),
    /** ISO date (Monday) of the week the games-used counter belongs to. */
    val capWeekStart: String = "",
    val capUsed: Int = 0,
)
