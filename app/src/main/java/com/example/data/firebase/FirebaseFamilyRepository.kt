package com.example.data.firebase

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import com.example.data.local.PreferencesManager
import com.example.data.model.GeofenceAlertLog
import com.example.data.model.GeofenceZone
import com.example.data.model.KidDeviceState
import com.example.data.model.LocationBreadcrumb
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class FirebaseFamilyRepository(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private val TAG = "FirebaseFamilyRepo"
    private val scope = CoroutineScope(Dispatchers.IO)

    // Local cached states for parent tracking
    private val _currentKidState = MutableStateFlow(KidDeviceState())
    val currentKidState: StateFlow<KidDeviceState> = _currentKidState.asStateFlow()

    private val _locationHistory = MutableStateFlow<List<LocationBreadcrumb>>(emptyList())
    val locationHistory: StateFlow<List<LocationBreadcrumb>> = _locationHistory.asStateFlow()

    private val _isFirebaseConnected = MutableStateFlow(false)
    val isFirebaseConnected: StateFlow<Boolean> = _isFirebaseConnected.asStateFlow()

    private var firestore: FirebaseFirestore? = null
    private var liveListener: ListenerRegistration? = null
    private var historyListener: ListenerRegistration? = null

    // Quota optimization variables
    private var lastUploadedLat: Double = 0.0
    private var lastUploadedLng: Double = 0.0
    private var lastUploadTime: Long = 0L
    private var lastHistoryUploadTime: Long = 0L
    private var lastKnownInsideZones = mutableSetOf<String>()

    init {
        try {
            firestore = FirebaseFirestore.getInstance()
            _isFirebaseConnected.value = true
        } catch (e: Exception) {
            Log.w(TAG, "Firestore initialization fallback: ${e.message}")
            _isFirebaseConnected.value = false
        }
    }

    /**
     * Called by KID device: Uploads state to Firebase if significant change occurred
     * strictly optimized to prevent exceeding free tier limits!
     */
    fun updateKidLocation(
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        speed: Float,
        bearing: Float,
        batteryLevel: Int,
        isCharging: Boolean,
        isGpsEnabled: Boolean,
        forceImmediate: Boolean = false
    ) {
        val now = System.currentTimeMillis()
        val timeSinceLast = now - lastUploadTime
        val distanceMoved = calculateDistanceInMeters(lastUploadedLat, lastUploadedLng, latitude, longitude)

        // Free tier budget filter:
        // Update if forced (SOS/Manual Ping), or moved > 20 meters, or 60s elapsed heartbeat
        val shouldUpdate = forceImmediate ||
                (lastUploadedLat == 0.0 && lastUploadedLng == 0.0) ||
                distanceMoved >= 20.0 ||
                (timeSinceLast >= 60_000L)

        if (!shouldUpdate) {
            return
        }

        scope.launch {
            val address = reverseGeocode(latitude, longitude)
            val kidId = preferencesManager.kidId.value
            val kidName = preferencesManager.kidName.value
            val disguise = preferencesManager.disguiseConfig.value

            val updatedState = KidDeviceState(
                kidId = kidId,
                kidName = kidName,
                latitude = latitude,
                longitude = longitude,
                accuracy = accuracy,
                altitude = 0.0,
                speed = speed,
                bearing = bearing,
                batteryLevel = batteryLevel,
                isCharging = isCharging,
                isGpsEnabled = isGpsEnabled,
                isOnline = true,
                lastUpdated = now,
                address = address,
                sosAlert = false,
                deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                stealthAppName = disguise.appName,
                stealthAppIcon = disguise.appIcon,
                stealthModeActive = disguise.isDisguiseActive
            )

            _currentKidState.value = updatedState
            lastUploadedLat = latitude
            lastUploadedLng = longitude
            lastUploadTime = now

            // Check Geofences locally on kid's device
            checkGeofences(updatedState)

            // Save to Firestore single document
            try {
                firestore?.collection("devices")
                    ?.document(kidId)
                    ?.set(updatedState.toMap(), SetOptions.merge())
                    ?.addOnSuccessListener {
                        _isFirebaseConnected.value = true
                    }
                    ?.addOnFailureListener {
                        Log.e(TAG, "Firestore write error: ${it.message}")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write to Firestore: ${e.message}")
            }

            // Write breadcrumb history point only if moved > 50m and > 2 minutes elapsed (Budget saving)
            if (distanceMoved >= 50.0 && (now - lastHistoryUploadTime >= 120_000L)) {
                lastHistoryUploadTime = now
                val breadcrumb = LocationBreadcrumb(
                    id = UUID.randomUUID().toString(),
                    latitude = latitude,
                    longitude = longitude,
                    timestamp = now,
                    speed = speed,
                    accuracy = accuracy,
                    address = address
                )
                try {
                    firestore?.collection("devices")
                        ?.document(kidId)
                        ?.collection("history")
                        ?.document(now.toString())
                        ?.set(breadcrumb.toMap())
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    /**
     * Trigger Emergency SOS alert from Kid device to Parent
     */
    fun triggerSosAlert() {
        scope.launch {
            val kidId = preferencesManager.kidId.value
            val current = _currentKidState.value
            val now = System.currentTimeMillis()
            val sosState = current.copy(
                sosAlert = true,
                sosTimestamp = now,
                lastUpdated = now
            )
            _currentKidState.value = sosState

            try {
                firestore?.collection("devices")
                    ?.document(kidId)
                    ?.set(sosState.toMap(), SetOptions.merge())
            } catch (e: Exception) {
                Log.e(TAG, "SOS write error: ${e.message}")
            }
        }
    }

    /**
     * Dismiss SOS alert from Parent or Kid
     */
    fun dismissSosAlert(kidId: String) {
        scope.launch {
            val current = _currentKidState.value
            _currentKidState.value = current.copy(sosAlert = false)
            try {
                firestore?.collection("devices")
                    ?.document(kidId)
                    ?.update("sosAlert", false)
            } catch (e: Exception) {
                Log.e(TAG, "Dismiss SOS error: ${e.message}")
            }
        }
    }

    /**
     * Called by PARENT device: Start real-time listening to a Kid's location
     */
    fun startListeningToKid(kidId: String) {
        if (kidId.isBlank()) return

        liveListener?.remove()
        historyListener?.remove()

        try {
            liveListener = firestore?.collection("devices")
                ?.document(kidId)
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Firestore listen failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val state = KidDeviceState.fromMap(snapshot.data)
                        _currentKidState.value = state
                        _isFirebaseConnected.value = true

                        // Check geofences on parent side as well
                        checkGeofences(state)
                    }
                }

            // Listen to recent history (limited to 25 items for budget optimization)
            historyListener = firestore?.collection("devices")
                ?.document(kidId)
                ?.collection("history")
                ?.orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                ?.limit(25)
                ?.addSnapshotListener { snapshots, error ->
                    if (error != null) return@addSnapshotListener
                    if (snapshots != null) {
                        val list = snapshots.documents.mapNotNull { doc ->
                            LocationBreadcrumb.fromMap(doc.data)
                        }
                        _locationHistory.value = list
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Firestore listen error: ${e.message}")
        }
    }

    fun stopListening() {
        liveListener?.remove()
        historyListener?.remove()
        liveListener = null
        historyListener = null
    }

    private fun checkGeofences(state: KidDeviceState) {
        val zones = preferencesManager.geofences.value
        val nowInside = mutableSetOf<String>()

        zones.forEach { zone ->
            val dist = calculateDistanceInMeters(state.latitude, state.longitude, zone.latitude, zone.longitude)
            val isInside = dist <= zone.radiusMeters

            if (isInside) {
                nowInside.add(zone.id)
                if (!lastKnownInsideZones.contains(zone.id) && zone.notifyOnEnter) {
                    // Entered Safe Zone
                    val alert = GeofenceAlertLog(
                        id = UUID.randomUUID().toString(),
                        kidId = state.kidId,
                        zoneName = zone.name,
                        eventType = "ENTERED",
                        timestamp = System.currentTimeMillis(),
                        message = "${state.kidName} has entered ${zone.name} (${dist.toInt()}m from center)"
                    )
                    preferencesManager.addAlertLog(alert)
                }
            } else {
                if (lastKnownInsideZones.contains(zone.id) && zone.notifyOnExit) {
                    // Exited Safe Zone
                    val alert = GeofenceAlertLog(
                        id = UUID.randomUUID().toString(),
                        kidId = state.kidId,
                        zoneName = zone.name,
                        eventType = "EXITED",
                        timestamp = System.currentTimeMillis(),
                        message = "${state.kidName} left ${zone.name}"
                    )
                    preferencesManager.addAlertLog(alert)
                }
            }
        }

        lastKnownInsideZones = nowInside
    }

    private fun reverseGeocode(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val street = addr.thoroughfare ?: addr.subLocality ?: ""
                val locality = addr.locality ?: addr.adminArea ?: ""
                if (street.isNotBlank() && locality.isNotBlank()) {
                    "$street, $locality"
                } else if (locality.isNotBlank()) {
                    locality
                } else {
                    addr.getAddressLine(0) ?: "Lat: %.4f, Lng: %.4f".format(lat, lng)
                }
            } else {
                "Lat: %.4f, Lng: %.4f".format(lat, lng)
            }
        } catch (e: Exception) {
            "Lat: %.4f, Lng: %.4f".format(lat, lng)
        }
    }

    /**
     * Haversine formula to calculate distance in meters between 2 coordinates
     */
    fun calculateDistanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        if (lat1 == 0.0 || lon1 == 0.0 || lat2 == 0.0 || lon2 == 0.0) return 0.0
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
