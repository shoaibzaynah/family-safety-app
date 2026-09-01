package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.SafeLinkApp
import com.example.data.firebase.FirebaseFamilyRepository
import com.example.data.local.PreferencesManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationTrackerService : Service() {

    private val TAG = "LocationTrackerService"

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var repository: FirebaseFamilyRepository

    private var locationCallback: LocationCallback? = null
    private var batteryReceiver: BroadcastReceiver? = null
    private var gpsStateReceiver: BroadcastReceiver? = null

    private var currentBatteryLevel: Int = 100
    private var isCharging: Boolean = false
    private var isGpsEnabled: Boolean = true

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(applicationContext)
        repository = FirebaseFamilyRepository(applicationContext, preferencesManager)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        registerBatteryReceiver()
        registerGpsStateReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_SERVICE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundServiceWithNotification()
        startLocationUpdates()

        return START_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val disguiseConfig = preferencesManager.disguiseConfig.value
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // If disguise is active, use stealth notification title/text
        val (title, text) = if (disguiseConfig.isDisguiseActive) {
            Pair("System Optimization Active", "Battery and background performance are optimized")
        } else {
            Pair("SafeLink Protection Active", "Sharing live location with family dashboard")
        }

        val notification: Notification = NotificationCompat.Builder(this, SafeLinkApp.CHANNEL_LOCATION_SERVICE)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        try {
            // Location Request optimized for low battery & free tier budget (Interval 30s, displacement 15m)
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30_000L)
                .setMinUpdateIntervalMillis(15_000L)
                .setMinUpdateDistanceMeters(15.0f)
                .setWaitForAccurateLocation(false)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location: Location = result.lastLocation ?: return
                    val speedKmh = location.speed * 3.6f // convert m/s to km/h

                    checkGpsStatus()

                    repository.updateKidLocation(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        speed = speedKmh,
                        bearing = location.bearing,
                        batteryLevel = currentBatteryLevel,
                        isCharging = isCharging,
                        isGpsEnabled = isGpsEnabled,
                        forceImmediate = false
                    )
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )

            // Also request last known location immediately
            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                if (lastLoc != null) {
                    repository.updateKidLocation(
                        latitude = lastLoc.latitude,
                        longitude = lastLoc.longitude,
                        accuracy = lastLoc.accuracy,
                        speed = lastLoc.speed * 3.6f,
                        bearing = lastLoc.bearing,
                        batteryLevel = currentBatteryLevel,
                        isCharging = isCharging,
                        isGpsEnabled = isGpsEnabled,
                        forceImmediate = true
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting location updates: ${e.message}")
        }
    }

    private fun registerBatteryReceiver() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level != -1 && scale != -1) {
                        currentBatteryLevel = (level * 100 / scale.toFloat()).toInt()
                    }
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)
    }

    private fun registerGpsStateReceiver() {
        gpsStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    checkGpsStatus()
                }
            }
        }
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(gpsStateReceiver, filter)
    }

    private fun checkGpsStatus() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
    }

    override fun onDestroy() {
        super.onDestroy()
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        batteryReceiver?.let {
            try { unregisterReceiver(it) } catch (e: Exception) {}
        }
        gpsStateReceiver?.let {
            try { unregisterReceiver(it) } catch (e: Exception) {}
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_SERVICE = "ACTION_START_LOCATION_SERVICE"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_LOCATION_SERVICE"

        fun startService(context: Context) {
            val intent = Intent(context, LocationTrackerService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, LocationTrackerService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
        }
    }
}
