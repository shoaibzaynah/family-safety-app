package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class SafeLinkApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Channel for background location tracking service
            val locationChannel = NotificationChannel(
                CHANNEL_LOCATION_SERVICE,
                "SafeLink Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps background location sharing active for family safety"
                setShowBadge(false)
            }

            // Channel for emergency SOS & geofence alerts
            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "SafeLink Emergency & Geofence Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts for SOS and safe zone entries/exits"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(locationChannel)
            notificationManager.createNotificationChannel(alertsChannel)
        }
    }

    companion object {
        const val CHANNEL_LOCATION_SERVICE = "safelink_location_channel"
        const val CHANNEL_ALERTS = "safelink_alerts_channel"
    }
}
