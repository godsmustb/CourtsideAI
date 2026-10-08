package com.nunna.courtside.data

import android.content.Context
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/** Saves app data on the phone (SharedPreferences, as JSON). */
object Store {
    private const val PREFS = "courtside"
    private const val KEY_DATA = "data"
    private const val KEY_SIG = "injury_sig"
    private const val KEY_BASELINE = "injury_baseline"

    val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val sigSerializer = MapSerializer(String.serializer(), String.serializer())

    fun load(ctx: Context): AppData {
        val raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_DATA, null)
            ?: return AppData()
        return runCatching { json.decodeFromString(AppData.serializer(), raw) }.getOrDefault(AppData())
    }

    fun save(ctx: Context, data: AppData) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_DATA, json.encodeToString(AppData.serializer(), data)).apply()
    }

    /** Last injury status seen per player (used by the background alert so it only pings on changes). */
    fun loadSig(ctx: Context): Map<String, String> {
        val raw = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_SIG, null)
            ?: return emptyMap()
        return runCatching { json.decodeFromString(sigSerializer, raw) }.getOrDefault(emptyMap())
    }

    fun saveSig(ctx: Context, sig: Map<String, String>) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_SIG, json.encodeToString(sigSerializer, sig))
            .putBoolean(KEY_BASELINE, true).apply()
    }

    fun hasBaseline(ctx: Context): Boolean =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_BASELINE, false)
}
