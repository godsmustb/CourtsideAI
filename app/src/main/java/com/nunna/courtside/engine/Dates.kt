package com.nunna.courtside.engine

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

/** NBA days and fantasy weeks run on US Eastern time; weeks are Monday to Sunday. */
object Dates {
    val zone: ZoneId = ZoneId.of("America/New_York")
    private val dayFmt = DateTimeFormatter.ofPattern("EEE MMM d")
    private val shortFmt = DateTimeFormatter.ofPattern("EEE d")
    private val timeFmt = DateTimeFormatter.ofPattern("h:mm a")

    fun today(): LocalDate = LocalDate.now(zone)
    fun monday(d: LocalDate = today()): LocalDate = d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    fun weekDays(d: LocalDate = today()): List<LocalDate> = (0L..6L).map { monday(d).plusDays(it) }
    fun label(d: LocalDate): String = d.format(dayFmt)
    fun short(d: LocalDate): String = d.format(shortFmt)

    /** "2026-10-20T23:30Z" -> "7:30 PM" in the phone's time zone. */
    fun tipTime(iso: String): String {
        val instant = runCatching { Instant.parse(iso) }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(iso.replace("Z", ":00Z")).toInstant() }.getOrNull()
            ?: return ""
        return instant.atZone(ZoneId.systemDefault()).format(timeFmt)
    }
}
