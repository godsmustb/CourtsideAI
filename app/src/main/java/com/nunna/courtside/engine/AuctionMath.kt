package com.nunna.courtside.engine

import com.nunna.courtside.data.Pick
import com.nunna.courtside.data.Player
import kotlin.math.roundToInt

/** Pure auction arithmetic for ESPN salary-cap drafts. No Android imports. */
object AuctionMath {
    fun spent(picks: List<Pick>): Int = picks.filter { it.mine }.sumOf { it.price }

    fun remaining(budget: Int, picks: List<Pick>): Int = budget - spent(picks)

    fun openSlots(rosterSize: Int, picks: List<Pick>): Int =
        (rosterSize - picks.count { it.mine }).coerceAtLeast(0)

    /** ESPN rule: you must keep $1 for every other empty roster spot. */
    fun maxBid(budget: Int, rosterSize: Int, picks: List<Pick>): Int {
        val open = openSlots(rosterSize, picks)
        if (open == 0) return 0
        return (remaining(budget, picks) - (open - 1)).coerceAtLeast(0)
    }

    /**
     * Room inflation: money left in the whole room divided by the value of the best players still
     * available (as many as there are empty roster spots league-wide). Above 1.0 = prices will run hot.
     */
    fun inflation(players: List<Player>, picks: List<Pick>, teams: Int, budget: Int, rosterSize: Int): Double {
        val taken = picks.map { Names.key(it.player) }.toSet()
        val slotsLeft = teams * rosterSize - picks.size
        if (slotsLeft <= 0) return 1.0
        val moneyLeft = teams * budget - picks.sumOf { it.price }
        val valueLeft = players.asSequence()
            .filter { Names.key(it.name) !in taken }
            .sortedByDescending { it.value }
            .take(slotsLeft)
            .sumOf { it.value.coerceAtLeast(1) }
        if (valueLeft <= 0) return 1.0
        return moneyLeft.toDouble() / valueLeft
    }

    fun roomPrice(value: Int, inflation: Double): Int = (value * inflation).roundToInt().coerceAtLeast(1)
}
