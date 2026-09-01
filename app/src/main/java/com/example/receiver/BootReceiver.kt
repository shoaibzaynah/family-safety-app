package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.PreferencesManager
import com.example.data.model.DeviceRole
import com.example.service.LocationTrackerService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val preferencesManager = PreferencesManager(context)
            if (preferencesManager.deviceRole.value == DeviceRole.KID) {
                LocationTrackerService.startService(context)
            }
        }
    }
}
