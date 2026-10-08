package com.nunna.courtside

import com.nunna.courtside.data.PlayerFile
import com.nunna.courtside.data.Store
import com.nunna.courtside.ui.Playbook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** The bundled data files must always parse, or the app would open empty. */
class AssetsTest {
    private fun asset(name: String) = File("src/main/assets/$name").readText()

    @Test fun playersParseAndPlanFitsBudget() {
        val file = Store.json.decodeFromString(PlayerFile.serializer(), asset("players.json"))
        assertTrue(file.players.size >= 120)
        assertEquals(file.players.size, file.players.map { it.name }.toSet().size)
        assertTrue(file.players.any { it.name == "Victor Wembanyama" && it.tier == "ANCHOR" })
        assertTrue(file.players.filter { it.tier == "DRAIN" }.all { it.maxBid == 0 })
    }

    @Test fun playbookParses() {
        val book = Store.json.decodeFromString(Playbook.serializer(), asset("playbook.json"))
        assertTrue(book.sections.size >= 5)
    }
}
