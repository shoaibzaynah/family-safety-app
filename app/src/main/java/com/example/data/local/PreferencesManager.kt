package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.DeviceRole
import com.example.data.model.DisguiseType
import com.example.data.model.GeofenceAlertLog
import com.example.data.model.GeofenceZone
import com.example.data.model.StealthDisguiseConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class PreferencesManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("safelink_preferences", Context.MODE_PRIVATE)

    private val _deviceRole = MutableStateFlow(getSavedDeviceRole())
    val deviceRole: StateFlow<DeviceRole> = _deviceRole.asStateFlow()

    private val _kidId = MutableStateFlow(getSavedKidId())
    val kidId: StateFlow<String> = _kidId.asStateFlow()

    private val _kidName = MutableStateFlow(getSavedKidName())
    val kidName: StateFlow<String> = _kidName.asStateFlow()

    private val _pairedKidIds = MutableStateFlow(getSavedPairedKidIds())
    val pairedKidIds: StateFlow<List<String>> = _pairedKidIds.asStateFlow()

    private val _disguiseConfig = MutableStateFlow(getSavedDisguiseConfig())
    val disguiseConfig: StateFlow<StealthDisguiseConfig> = _disguiseConfig.asStateFlow()

    private val _geofences = MutableStateFlow(getSavedGeofences())
    val geofences: StateFlow<List<GeofenceZone>> = _geofences.asStateFlow()

    private val _alertLogs = MutableStateFlow(getSavedAlertLogs())
    val alertLogs: StateFlow<List<GeofenceAlertLog>> = _alertLogs.asStateFlow()

    private fun getSavedDeviceRole(): DeviceRole {
        val roleStr = prefs.getString(KEY_ROLE, DeviceRole.UNSET.name) ?: DeviceRole.UNSET.name
        return try {
            DeviceRole.valueOf(roleStr)
        } catch (e: Exception) {
            DeviceRole.UNSET
        }
    }

    fun setDeviceRole(role: DeviceRole) {
        prefs.edit().putString(KEY_ROLE, role.name).apply()
        _deviceRole.value = role
    }

    private fun getSavedKidId(): String {
        var id = prefs.getString(KEY_KID_ID, null)
        if (id.isNullOrBlank()) {
            // Generate a memorable 6-character code e.g. "KID-4829"
            val randomDigits = (1000..9999).random()
            id = "KID-$randomDigits"
            prefs.edit().putString(KEY_KID_ID, id).apply()
        }
        return id
    }

    fun setKidId(id: String) {
        prefs.edit().putString(KEY_KID_ID, id).apply()
        _kidId.value = id
    }

    private fun getSavedKidName(): String {
        return prefs.getString(KEY_KID_NAME, "My Kid") ?: "My Kid"
    }

    fun setKidName(name: String) {
        prefs.edit().putString(KEY_KID_NAME, name).apply()
        _kidName.value = name
    }

    private fun getSavedPairedKidIds(): List<String> {
        val json = prefs.getString(KEY_PAIRED_KIDS, null) ?: return listOf(getSavedKidId())
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            if (list.isEmpty()) listOf(getSavedKidId()) else list
        } catch (e: Exception) {
            listOf(getSavedKidId())
        }
    }

    fun addPairedKidId(id: String) {
        val current = _pairedKidIds.value.toMutableList()
        if (!current.contains(id)) {
            current.add(id)
            savePairedKids(current)
        }
    }

    fun removePairedKidId(id: String) {
        val current = _pairedKidIds.value.toMutableList()
        current.remove(id)
        savePairedKids(current)
    }

    private fun savePairedKids(list: List<String>) {
        val array = JSONArray()
        list.forEach { array.put(it) }
        prefs.edit().putString(KEY_PAIRED_KIDS, array.toString()).apply()
        _pairedKidIds.value = list
    }

    private fun getSavedDisguiseConfig(): StealthDisguiseConfig {
        val typeStr = prefs.getString(KEY_DISGUISE_TYPE, DisguiseType.CALCULATOR.name) ?: DisguiseType.CALCULATOR.name
        val type = try { DisguiseType.valueOf(typeStr) } catch (e: Exception) { DisguiseType.CALCULATOR }
        val appName = prefs.getString(KEY_DISGUISE_NAME, "Calculator") ?: "Calculator"
        val appIcon = prefs.getString(KEY_DISGUISE_ICON, "calculator") ?: "calculator"
        val secretPin = prefs.getString(KEY_DISGUISE_PIN, "1234") ?: "1234"
        val isActive = prefs.getBoolean(KEY_DISGUISE_ACTIVE, false)
        return StealthDisguiseConfig(
            disguiseType = type,
            appName = appName,
            appIcon = appIcon,
            secretPin = secretPin,
            isDisguiseActive = isActive
        )
    }

    fun updateDisguiseConfig(config: StealthDisguiseConfig) {
        prefs.edit()
            .putString(KEY_DISGUISE_TYPE, config.disguiseType.name)
            .putString(KEY_DISGUISE_NAME, config.appName)
            .putString(KEY_DISGUISE_ICON, config.appIcon)
            .putString(KEY_DISGUISE_PIN, config.secretPin)
            .putBoolean(KEY_DISGUISE_ACTIVE, config.isDisguiseActive)
            .apply()
        _disguiseConfig.value = config
    }

    private fun getSavedGeofences(): List<GeofenceZone> {
        val json = prefs.getString(KEY_GEOFENCES, null)
        if (json.isNullOrBlank()) {
            // Default sample safe zones
            val defaults = listOf(
                GeofenceZone(
                    id = "home_zone",
                    name = "Home",
                    latitude = 37.7749,
                    longitude = -122.4194,
                    radiusMeters = 150f,
                    colorHex = "#10B981",
                    iconName = "home"
                ),
                GeofenceZone(
                    id = "school_zone",
                    name = "School",
                    latitude = 37.7833,
                    longitude = -122.4167,
                    radiusMeters = 300f,
                    colorHex = "#3B82F6",
                    iconName = "school"
                )
            )
            saveGeofences(defaults)
            return defaults
        }
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<GeofenceZone>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    GeofenceZone(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        name = obj.optString("name", "Zone"),
                        latitude = obj.optDouble("latitude", 0.0),
                        longitude = obj.optDouble("longitude", 0.0),
                        radiusMeters = obj.optDouble("radiusMeters", 150.0).toFloat(),
                        colorHex = obj.optString("colorHex", "#10B981"),
                        notifyOnEnter = obj.optBoolean("notifyOnEnter", true),
                        notifyOnExit = obj.optBoolean("notifyOnExit", true),
                        iconName = obj.optString("iconName", "place")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveGeofences(zones: List<GeofenceZone>) {
        val array = JSONArray()
        zones.forEach { zone ->
            val obj = JSONObject().apply {
                put("id", zone.id)
                put("name", zone.name)
                put("latitude", zone.latitude)
                put("longitude", zone.longitude)
                put("radiusMeters", zone.radiusMeters.toDouble())
                put("colorHex", zone.colorHex)
                put("notifyOnEnter", zone.notifyOnEnter)
                put("notifyOnExit", zone.notifyOnExit)
                put("iconName", zone.iconName)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_GEOFENCES, array.toString()).apply()
        _geofences.value = zones
    }

    fun addGeofence(zone: GeofenceZone) {
        val current = _geofences.value.toMutableList()
        current.removeAll { it.id == zone.id }
        current.add(zone)
        saveGeofences(current)
    }

    fun deleteGeofence(zoneId: String) {
        val current = _geofences.value.toMutableList()
        current.removeAll { it.id == zoneId }
        saveGeofences(current)
    }

    private fun getSavedAlertLogs(): List<GeofenceAlertLog> {
        val json = prefs.getString(KEY_ALERT_LOGS, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<GeofenceAlertLog>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    GeofenceAlertLog(
                        id = obj.optString("id", ""),
                        kidId = obj.optString("kidId", ""),
                        zoneName = obj.optString("zoneName", ""),
                        eventType = obj.optString("eventType", "ENTERED"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        message = obj.optString("message", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addAlertLog(log: GeofenceAlertLog) {
        val current = _alertLogs.value.toMutableList()
        current.add(0, log)
        if (current.size > 50) {
            // Keep last 50 alerts to avoid memory bloat
            current.removeAt(current.size - 1)
        }
        val array = JSONArray()
        current.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("kidId", item.kidId)
                put("zoneName", item.zoneName)
                put("eventType", item.eventType)
                put("timestamp", item.timestamp)
                put("message", item.message)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_ALERT_LOGS, array.toString()).apply()
        _alertLogs.value = current
    }

    companion object {
        private const val KEY_ROLE = "key_device_role"
        private const val KEY_KID_ID = "key_kid_id"
        private const val KEY_KID_NAME = "key_kid_name"
        private const val KEY_PAIRED_KIDS = "key_paired_kids"
        private const val KEY_DISGUISE_TYPE = "key_disguise_type"
        private const val KEY_DISGUISE_NAME = "key_disguise_name"
        private const val KEY_DISGUISE_ICON = "key_disguise_icon"
        private const val KEY_DISGUISE_PIN = "key_disguise_pin"
        private const val KEY_DISGUISE_ACTIVE = "key_disguise_active"
        private const val KEY_GEOFENCES = "key_geofences"
        private const val KEY_ALERT_LOGS = "key_alert_logs"
    }
}
