package com.nunna.courtside.engine

import com.nunna.courtside.data.AppData
import com.nunna.courtside.data.Player
import com.nunna.courtside.data.RosterEntry

/** Our current roster = players we bought in the draft + later adds, minus drops. */
object Roster {
    fun of(data: AppData, players: List<Player>): List<RosterEntry> {
        val byKey = players.associateBy { Names.key(it.name) }
        val dropped = data.dropped.map { Names.key(it) }.toSet()
        val drafted = data.picks.filter { it.mine }.map { pick ->
            val p = byKey[Names.key(pick.player)]
            RosterEntry(pick.player, p?.team ?: "?", p?.pos ?: "")
        }
        return (drafted + data.extraRoster)
            .filter { Names.key(it.name) !in dropped }
            .distinctBy { Names.key(it.name) }
    }
}
