package com.nunna.courtside

import com.nunna.courtside.net.Espn
import org.junit.Assert.assertEquals
import org.junit.Test

class EspnParseTest {
    private val scoreboard = """
        {"events":[{"id":"401","date":"2026-10-20T23:30Z","name":"Boston Celtics at Detroit Pistons",
          "competitions":[{"status":{"type":{"state":"post","shortDetail":"Final"}},
            "competitors":[
              {"homeAway":"home","score":"112","team":{"abbreviation":"DET","shortDisplayName":"Pistons"}},
              {"homeAway":"away","score":"108","team":{"abbreviation":"BOS","shortDisplayName":"Celtics"}}]}]},
          {"id":"402","date":"2026-10-21T02:00Z",
          "competitions":[{"status":{"type":{"state":"pre","shortDetail":"10:00 PM ET"}},
            "competitors":[
              {"homeAway":"home","score":"0","team":{"abbreviation":"GS","shortDisplayName":"Warriors"}},
              {"homeAway":"away","score":"0","team":{"abbreviation":"SA","shortDisplayName":"Spurs"}}]}]}]}
    """.trimIndent()

    private val injuries = """
        {"injuries":[{"displayName":"Golden State Warriors","injuries":[
          {"status":"Out","shortComment":"Butler (knee) is out.","date":"2026-10-07T18:00Z",
           "athlete":{"displayName":"Jimmy Butler III"}}]},
          {"displayName":"Los Angeles Lakers","injuries":[
          {"status":"Day-To-Day","shortComment":"Reaves (ankle) is day-to-day.","date":"2026-10-06T18:00Z",
           "athlete":{"displayName":"Austin Reaves","team":{"abbreviation":"LAL"}}}]}]}
    """.trimIndent()

    @Test fun parsesScoreboard() {
        val games = Espn.parseScoreboard(scoreboard)
        assertEquals(2, games.size)
        assertEquals("DET", games[0].home.abbr)
        assertEquals("108", games[0].away.score)
        assertEquals("post", games[0].state)
        assertEquals(listOf("GS", "SA"), games[1].teams)
    }

    @Test fun parsesInjuries() {
        val list = Espn.parseInjuries(injuries)
        assertEquals(2, list.size)
        assertEquals("GS", list[0].team)
        assertEquals("Out", list[0].status)
        assertEquals("LAL", list[1].team)
    }

    @Test fun garbageGivesEmptyList() {
        assertEquals(0, Espn.parseScoreboard("{}").size)
        assertEquals(0, Espn.parseInjuries("{\"injuries\":[]}").size)
    }
}
