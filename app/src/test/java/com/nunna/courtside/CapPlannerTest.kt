package com.nunna.courtside

import com.nunna.courtside.engine.CapPlanner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CapPlannerTest {
    private val mon = LocalDate.of(2026, 10, 26)
    private fun days(vararg c: Int) = c.mapIndexed { i, n -> CapPlanner.Day(mon.plusDays(i.toLong()), n) }

    @Test fun holdsAt24ThenGoesAllIn() {
        // Friday (index 4) is the best all-in night.
        val plan = CapPlanner.plan(0, days(7, 7, 7, 9, 9, 4, 3))
        assertEquals(33, plan.projected)
        assertEquals(mon.plusDays(4), plan.overflowDate)
        assertEquals(24, plan.days.take(4).sumOf { it.starts })
        assertEquals(9, plan.days[4].starts)
        assertEquals(30, plan.careless)
    }

    @Test fun carelessManagerStopsNear25() {
        assertEquals(27, CapPlanner.careless(0, listOf(9, 9, 9, 9)))
    }

    @Test fun neverMoreThanNineStartsADay() {
        val plan = CapPlanner.plan(20, days(14, 3))
        assertTrue(plan.days.all { it.starts <= 9 })
    }

    @Test fun lightWeekHasNoOverflow() {
        val plan = CapPlanner.plan(0, days(2, 2, 2))
        assertEquals(6, plan.projected)
        assertNull(plan.overflowDate)
    }

    @Test fun alreadyCappedPlansNothing() {
        val plan = CapPlanner.plan(26, days(5, 5))
        assertEquals(26, plan.projected)
        assertEquals(0, plan.days.sumOf { it.starts })
    }
}
