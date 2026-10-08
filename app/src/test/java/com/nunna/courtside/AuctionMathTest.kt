package com.nunna.courtside

import com.nunna.courtside.data.Pick
import com.nunna.courtside.data.Player
import com.nunna.courtside.engine.AuctionMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuctionMathTest {
    @Test fun openingMaxBidIs189() {
        assertEquals(189, AuctionMath.maxBid(200, 12, emptyList()))
    }

    @Test fun maxBidKeepsOneDollarPerEmptySpot() {
        val picks = listOf(Pick("A", 78, true), Pick("B", 52, true), Pick("X", 60, false))
        // 200 - 130 = 70 left, 10 open spots -> must keep 9 -> max 61
        assertEquals(70, AuctionMath.remaining(200, picks))
        assertEquals(10, AuctionMath.openSlots(12, picks))
        assertEquals(61, AuctionMath.maxBid(200, 12, picks))
    }

    @Test fun fullRosterMeansNoBid() {
        val picks = (1..12).map { Pick("P$it", 1, true) }
        assertEquals(0, AuctionMath.maxBid(200, 12, picks))
    }

    @Test fun inflationAboveOneWhenStarsSellCheap() {
        val players = (1..20).map { Player("P$it", "BOS", "C", it, 10) }
        val picks = listOf(Pick("P1", 1, false))
        val heat = AuctionMath.inflation(players, picks, teams = 2, budget = 100, rosterSize = 10)
        assertTrue("heat=$heat", heat > 1.0)
    }
}
