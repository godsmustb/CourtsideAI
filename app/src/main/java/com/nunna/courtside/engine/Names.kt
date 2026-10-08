package com.nunna.courtside.engine

/** Name and team helpers so ESPN's spelling matches ours. */
object Names {
    private val suffix = Regex("\\b(jr|sr|ii|iii|iv)\\b")
    private val punct = Regex("[.'’`]")
    private val spaces = Regex("\\s+")

    /** "Jimmy Butler III" and "Jimmy Butler" both become "jimmy butler". */
    fun key(name: String): String =
        name.lowercase().replace(punct, "").replace(suffix, "").replace(spaces, " ").trim()

    private val alias = mapOf(
        "GSW" to "GS", "NOP" to "NO", "NYK" to "NY", "SAS" to "SA", "UTA" to "UTAH",
        "WAS" to "WSH", "PHO" to "PHX", "BRK" to "BKN", "CHO" to "CHA",
    )

    /** ESPN-style team abbreviation. */
    fun team(abbr: String): String {
        val up = abbr.trim().uppercase()
        return alias[up] ?: up
    }

    private val byName = mapOf(
        "atlanta hawks" to "ATL", "boston celtics" to "BOS", "brooklyn nets" to "BKN",
        "charlotte hornets" to "CHA", "chicago bulls" to "CHI", "cleveland cavaliers" to "CLE",
        "dallas mavericks" to "DAL", "denver nuggets" to "DEN", "detroit pistons" to "DET",
        "golden state warriors" to "GS", "houston rockets" to "HOU", "indiana pacers" to "IND",
        "la clippers" to "LAC", "los angeles clippers" to "LAC", "los angeles lakers" to "LAL",
        "memphis grizzlies" to "MEM", "miami heat" to "MIA", "milwaukee bucks" to "MIL",
        "minnesota timberwolves" to "MIN", "new orleans pelicans" to "NO", "new york knicks" to "NY",
        "oklahoma city thunder" to "OKC", "orlando magic" to "ORL", "philadelphia 76ers" to "PHI",
        "phoenix suns" to "PHX", "portland trail blazers" to "POR", "sacramento kings" to "SAC",
        "san antonio spurs" to "SA", "toronto raptors" to "TOR", "utah jazz" to "UTAH",
        "washington wizards" to "WSH",
    )

    fun teamFromName(displayName: String): String? = byName[displayName.trim().lowercase()]
}
