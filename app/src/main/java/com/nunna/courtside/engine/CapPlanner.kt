package com.nunna.courtside.engine

import java.time.LocalDate

/**
 * Plans starts against ESPN's 25-games-per-week limit.
 *
 * ESPN checks the limit at the START of each day: if you begin a day under 25, every game that day
 * counts. So we hold at 24 until the best "overflow" day, then start everyone who plays.
 */
object CapPlanner {
    const val CAP = 25
    const val SLOTS = 9

    data class Day(val date: LocalDate, val playable: Int)
    data class DayPlan(val date: LocalDate, val playable: Int, val starts: Int, val overflow: Boolean)
    data class Plan(val days: List<DayPlan>, val projected: Int, val overflowDate: LocalDate?, val careless: Int)

    /** Games counted if we hold at 24 before [overflowIdx] and play everyone from then on. */
    fun simulate(used: Int, counts: List<Int>, overflowIdx: Int): Pair<Int, List<Int>> {
        var u = used
        val starts = MutableList(counts.size) { 0 }
        for (i in counts.indices) {
            if (u >= CAP) break
            val c = counts[i].coerceIn(0, SLOTS)
            val s = if (i < overflowIdx) minOf(c, (CAP - 1 - u).coerceAtLeast(0)) else c
            starts[i] = s
            u += s
        }
        return u to starts
    }

    /** What a manager who starts everyone every day gets. */
    fun careless(used: Int, counts: List<Int>): Int = simulate(used, counts, 0).first

    fun plan(used: Int, days: List<Day>): Plan {
        if (days.isEmpty()) return Plan(emptyList(), used, null, used)
        val counts = days.map { it.playable }
        var bestIdx = 0
        var bestTotal = -1
        var bestStarts: List<Int> = emptyList()
        for (k in days.indices) {
            val (total, starts) = simulate(used, counts, k)
            if (total > bestTotal) {
                bestTotal = total; bestIdx = k; bestStarts = starts
            }
        }
        val useOverflow = bestTotal > CAP && used < CAP
        val plans = days.mapIndexed { i, d ->
            DayPlan(d.date, d.playable.coerceIn(0, SLOTS), bestStarts.getOrElse(i) { 0 }, useOverflow && i == bestIdx)
        }
        return Plan(plans, bestTotal, if (useOverflow) days[bestIdx].date else null, careless(used, counts))
    }
}
