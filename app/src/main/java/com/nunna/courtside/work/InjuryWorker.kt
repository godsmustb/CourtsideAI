package com.nunna.courtside.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nunna.courtside.MainActivity
import com.nunna.courtside.R
import com.nunna.courtside.data.PlayerRepo
import com.nunna.courtside.data.Store
import com.nunna.courtside.engine.Names
import com.nunna.courtside.engine.Roster
import com.nunna.courtside.net.Espn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/** Every ~hour: checks the NBA injury report and pings the phone when one of OUR players changes. */
class InjuryWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val ctx = applicationContext
        val roster = Roster.of(Store.load(ctx), PlayerRepo.load(ctx).players)
        if (roster.isEmpty()) return@withContext Result.success()
        val injuries = runCatching { Espn.injuries() }.getOrElse { return@withContext Result.retry() }
        val mine = roster.map { Names.key(it.name) }.toSet()
        val now = injuries.filter { Names.key(it.player) in mine }
            .associate { Names.key(it.player) to "${it.status}|${it.comment}" }
        val old = Store.loadSig(ctx)
        if (Store.hasBaseline(ctx)) {
            val names = roster.associate { Names.key(it.name) to it.name }
            for ((k, sig) in now) {
                if (old[k] != sig) {
                    val (status, comment) = sig.split("|", limit = 2).let { it[0] to it.getOrElse(1) { "" } }
                    notify(ctx, k.hashCode(), "${names[k] ?: k}: $status", comment.ifBlank { "Check your lineup." })
                }
            }
            for (k in old.keys - now.keys) {
                if (k in mine) notify(ctx, k.hashCode(), "${names[k] ?: k} is off the injury report", "Probably available again. Check your lineup.")
            }
        }
        Store.saveSig(ctx, now)
        Result.success()
    }

    companion object {
        private const val CHANNEL = "injuries"

        fun schedule(ctx: Context) {
            val req = PeriodicWorkRequestBuilder<InjuryWorker>(1, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork("injury-check", ExistingPeriodicWorkPolicy.KEEP, req)
        }

        fun notify(ctx: Context, id: Int, title: String, text: String) {
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) return
            val mgr = ctx.getSystemService(NotificationManager::class.java)
            mgr.createNotificationChannel(NotificationChannel(CHANNEL, "Injury alerts", NotificationManager.IMPORTANCE_DEFAULT))
            val open = PendingIntent.getActivity(
                ctx, 0, Intent(ctx, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            val n = NotificationCompat.Builder(ctx, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(open)
                .setAutoCancel(true)
                .build()
            try {
                NotificationManagerCompat.from(ctx).notify(id, n)
            } catch (_: SecurityException) {
            }
        }
    }
}
