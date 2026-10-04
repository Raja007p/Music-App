package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.playback.TuneFlowMediaService
import com.example.playback.TuneFlowPlayerManager

class TuneFlowWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, TuneFlowWidgetProvider::class.java))
        for (id in ids) {
            updateWidget(context, manager, id)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, TuneFlowWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, TuneFlowWidgetProvider::class.java))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }

        private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val playerManager = TuneFlowPlayerManager.getInstance(context.applicationContext)
            val currentSong = playerManager.currentSong.value
            val isPlaying = playerManager.isPlaying.value

            val views = RemoteViews(context.packageName, R.layout.widget_tuneflow)

            // Titles
            if (currentSong != null) {
                views.setTextViewText(R.id.widget_title, currentSong.title)
                views.setTextViewText(R.id.widget_artist, "${currentSong.artist} • ${currentSong.album}")
            } else {
                views.setTextViewText(R.id.widget_title, "TuneFlow")
                views.setTextViewText(R.id.widget_artist, "Tap to play music")
            }

            // Play/Pause icon
            views.setImageViewResource(
                R.id.widget_btn_play_pause,
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )

            // Open App on Title / Container click
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_title, openAppPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_artwork, openAppPendingIntent)

            // Previous
            val prevIntent = Intent(context, TuneFlowMediaService::class.java).apply {
                action = TuneFlowMediaService.ACTION_PREVIOUS
            }
            val prevPending = PendingIntent.getService(
                context,
                11,
                prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_prev, prevPending)

            // Play/Pause
            val playPauseIntent = Intent(context, TuneFlowMediaService::class.java).apply {
                action = TuneFlowMediaService.ACTION_PLAY_PAUSE
            }
            val playPausePending = PendingIntent.getService(
                context,
                12,
                playPauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_play_pause, playPausePending)

            // Next
            val nextIntent = Intent(context, TuneFlowMediaService::class.java).apply {
                action = TuneFlowMediaService.ACTION_NEXT
            }
            val nextPending = PendingIntent.getService(
                context,
                13,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_next, nextPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
