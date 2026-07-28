package com.example.stepcounter.presentation

import android.Manifest

import android.R

import android.app.NotificationChannel

import android.app.NotificationManager

import android.content.Context

import android.content.pm.PackageManager

import android.os.Build

import androidx.core.app.NotificationCompat

import androidx.core.app.NotificationManagerCompat

import androidx.core.content.ContextCompat






const val CHANNEL_ID = "fitness_alerts"
const val HEART_RATE_NOTIFICATION_ID = 1
fun showNotification(
    context: Context, notificationId: Int, title: String, message: String
) {
    if (
        Build.VERSION.SDK_INT >=
        Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }

    val notification =
        NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(
                R.drawable
                    .ic_dialog_info
            )
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(
                NotificationCompat
                    .PRIORITY_DEFAULT
            )
            .setAutoCancel(true)
            .build()

    NotificationManagerCompat
        .from(context)
        .notify(
            notificationId,
            notification
        )
}
fun createNotificationChannel(
    context: Context
) {
    val channel = NotificationChannel(
        CHANNEL_ID,
        "Fitness Alerts",
        NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
        description =
            "Heart-rate and activity reminders"
    }

    val notificationManager =
        context.getSystemService(
            NotificationManager::class.java
        )

    notificationManager.createNotificationChannel(
        channel
    )
}
class NotificationUtils {
}