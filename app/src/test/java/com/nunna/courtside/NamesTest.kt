package com.nunna.courtside

import com.nunna.courtside.engine.Names
import org.junit.Assert.assertEquals
import org.junit.Test

class NamesTest {
    @Test fun suffixesAndPunctuationIgnored() {
        assertEquals(Names.key("Jimmy Butler"), Names.key("Jimmy Butler III"))
        assertEquals(Names.key("Jaren Jackson Jr."), Names.key("Jaren Jackson"))
        assertEquals(Names.key("De'Aaron Fox"), Names.key("DeAaron Fox"))
    }

    @Test fun teamAliases() {
        assertEquals("GS", Names.team("GSW"))
        assertEquals("NY", Names.team("nyk"))
        assertEquals("BOS", Names.team("BOS"))
        assertEquals("UTAH", Names.teamFromName("Utah Jazz"))
    }
}
