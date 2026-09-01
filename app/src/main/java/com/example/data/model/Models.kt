package com.example.data.model

enum class DeviceRole {
    UNSET,
    KID,
    PARENT
}

enum class DisguiseType {
    CALCULATOR,
    NOTES,
    CLOCK,
    NONE
}

data class KidDeviceState(
    val kidId: String = "",
    val kidName: String = "Kid's Device",
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val accuracy: Float = 5.0f,
    val altitude: Double = 0.0,
    val speed: Float = 0.0f, // in km/h
    val bearing: Float = 0.0f,
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false,
    val isGpsEnabled: Boolean = true,
    val isOnline: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis(),
    val address: String = "Locating...",
    val sosAlert: Boolean = false,
    val sosTimestamp: Long = 0L,
    val deviceModel: String = "Android Device",
    val stealthAppName: String = "Calculator",
    val stealthAppIcon: String = "calculator",
    val stealthModeActive: Boolean = false
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "kidId" to kidId,
            "kidName" to kidName,
            "latitude" to latitude,
            "longitude" to longitude,
            "accuracy" to accuracy.toDouble(),
            "altitude" to altitude,
            "speed" to speed.toDouble(),
            "bearing" to bearing.toDouble(),
            "batteryLevel" to batteryLevel,
            "isCharging" to isCharging,
            "isGpsEnabled" to isGpsEnabled,
            "isOnline" to isOnline,
            "lastUpdated" to lastUpdated,
            "address" to address,
            "sosAlert" to sosAlert,
            "sosTimestamp" to sosTimestamp,
            "deviceModel" to deviceModel,
            "stealthAppName" to stealthAppName,
            "stealthAppIcon" to stealthAppIcon,
            "stealthModeActive" to stealthModeActive
        )
    }

    companion object {
        fun fromMap(data: Map<String, Any>?): KidDeviceState {
            if (data == null) return KidDeviceState()
            return KidDeviceState(
                kidId = data["kidId"] as? String ?: "",
                kidName = data["kidName"] as? String ?: "Kid's Device",
                latitude = (data["latitude"] as? Number)?.toDouble() ?: 37.7749,
                longitude = (data["longitude"] as? Number)?.toDouble() ?: -122.4194,
                accuracy = (data["accuracy"] as? Number)?.toFloat() ?: 5f,
                altitude = (data["altitude"] as? Number)?.toDouble() ?: 0.0,
                speed = (data["speed"] as? Number)?.toFloat() ?: 0f,
                bearing = (data["bearing"] as? Number)?.toFloat() ?: 0f,
                batteryLevel = (data["batteryLevel"] as? Number)?.toInt() ?: 100,
                isCharging = data["isCharging"] as? Boolean ?: false,
                isGpsEnabled = data["isGpsEnabled"] as? Boolean ?: true,
                isOnline = data["isOnline"] as? Boolean ?: true,
                lastUpdated = (data["lastUpdated"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                address = data["address"] as? String ?: "Locating...",
                sosAlert = data["sosAlert"] as? Boolean ?: false,
                sosTimestamp = (data["sosTimestamp"] as? Number)?.toLong() ?: 0L,
                deviceModel = data["deviceModel"] as? String ?: "Android Device",
                stealthAppName = data["stealthAppName"] as? String ?: "Calculator",
                stealthAppIcon = data["stealthAppIcon"] as? String ?: "calculator",
                stealthModeActive = data["stealthModeActive"] as? Boolean ?: false
            )
        }
    }
}

data class LocationBreadcrumb(
    val id: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val speed: Float = 0.0f,
    val accuracy: Float = 0.0f,
    val address: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "latitude" to latitude,
            "longitude" to longitude,
            "timestamp" to timestamp,
            "speed" to speed.toDouble(),
            "accuracy" to accuracy.toDouble(),
            "address" to address
        )
    }

    companion object {
        fun fromMap(data: Map<String, Any>?): LocationBreadcrumb {
            if (data == null) return LocationBreadcrumb()
            return LocationBreadcrumb(
                id = data["id"] as? String ?: "",
                latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                speed = (data["speed"] as? Number)?.toFloat() ?: 0f,
                accuracy = (data["accuracy"] as? Number)?.toFloat() ?: 0f,
                address = data["address"] as? String ?: ""
            )
        }
    }
}

data class GeofenceZone(
    val id: String = "",
    val name: String = "Home",
    val latitude: Double = 37.7749,
    val longitude: Double = -122.4194,
    val radiusMeters: Float = 200f,
    val colorHex: String = "#10B981", // emerald
    val notifyOnEnter: Boolean = true,
    val notifyOnExit: Boolean = true,
    val iconName: String = "home"
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "latitude" to latitude,
            "longitude" to longitude,
            "radiusMeters" to radiusMeters.toDouble(),
            "colorHex" to colorHex,
            "notifyOnEnter" to notifyOnEnter,
            "notifyOnExit" to notifyOnExit,
            "iconName" to iconName
        )
    }

    companion object {
        fun fromMap(data: Map<String, Any>?): GeofenceZone {
            if (data == null) return GeofenceZone()
            return GeofenceZone(
                id = data["id"] as? String ?: "",
                name = data["name"] as? String ?: "Home",
                latitude = (data["latitude"] as? Number)?.toDouble() ?: 37.7749,
                longitude = (data["longitude"] as? Number)?.toDouble() ?: -122.4194,
                radiusMeters = (data["radiusMeters"] as? Number)?.toFloat() ?: 200f,
                colorHex = data["colorHex"] as? String ?: "#10B981",
                notifyOnEnter = data["notifyOnEnter"] as? Boolean ?: true,
                notifyOnExit = data["notifyOnExit"] as? Boolean ?: true,
                iconName = data["iconName"] as? String ?: "home"
            )
        }
    }
}

data class GeofenceAlertLog(
    val id: String = "",
    val kidId: String = "",
    val zoneName: String = "",
    val eventType: String = "ENTERED", // "ENTERED" or "EXITED"
    val timestamp: Long = System.currentTimeMillis(),
    val message: String = ""
)

data class StealthDisguiseConfig(
    val disguiseType: DisguiseType = DisguiseType.CALCULATOR,
    val appName: String = "Calculator",
    val appIcon: String = "calculator",
    val secretPin: String = "1234",
    val isDisguiseActive: Boolean = false
)
