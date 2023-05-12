package com.example.ims.receivers

import MOWING_SESSION_CHANNEL_ID
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.ims.R

class MowingSessionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val channelId = MOWING_SESSION_CHANNEL_ID
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.icon_grass)
            .setContentTitle("Mowing Session Started")
            .setContentText("It's trimming time!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(1, notificationBuilder.build())
    }
}
