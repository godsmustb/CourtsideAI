package com.nunna.courtside.data

import android.content.Context

/** Loads the bundled player list (updated with each app release). */
object PlayerRepo {
    @Volatile private var cache: PlayerFile? = null

    fun load(ctx: Context): PlayerFile {
        cache?.let { return it }
        val text = ctx.assets.open("players.json").bufferedReader().use { it.readText() }
        val file = Store.json.decodeFromString(PlayerFile.serializer(), text)
        cache = file
        return file
    }
}
