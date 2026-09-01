package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PreferencesManager
import com.example.data.model.GeofenceAlertLog
import com.example.data.model.GeofenceZone
import com.example.data.model.KidDeviceState
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun ParentGeofencingScreen(
    preferencesManager: PreferencesManager,
    geofences: List<GeofenceZone>,
    alertLogs: List<GeofenceAlertLog>,
    kidState: KidDeviceState
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy900)
            .testTag("parent_geofencing_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Geofencing Safe Zones",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Get instant notifications when ${kidState.kidName} enters or leaves",
                            color = Slate400,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSafe),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Zone", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Zones List Header
            item {
                Text(
                    text = "ACTIVE SAFE ZONES (${geofences.size})",
                    color = Slate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            items(geofences, key = { it.id }) { zone ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    border = BorderStroke(1.dp, Navy700),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            val zoneColor = try {
                                Color(android.graphics.Color.parseColor(zone.colorHex))
                            } catch (e: Exception) {
                                EmeraldSafe
                            }

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(zoneColor.copy(alpha = 0.2f))
                                    .border(1.dp, zoneColor, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    when (zone.iconName) {
                                        "home" -> Icons.Default.Home
                                        "school" -> Icons.Default.School
                                        "park" -> Icons.Default.Park
                                        else -> Icons.Default.Place
                                    },
                                    contentDescription = zone.name,
                                    tint = zoneColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = zone.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Radius: ${zone.radiusMeters.toInt()}m  •  ${"%.4f".format(zone.latitude)}, ${"%.4f".format(zone.longitude)}",
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                preferencesManager.deleteGeofence(zone.id)
                                Toast.makeText(context, "Deleted ${zone.name}", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Zone", tint = Slate400)
                        }
                    }
                }
            }

            // Geofence Activity Logs Header
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "GEOFENCE ALERT HISTORY (${alertLogs.size})",
                    color = Slate400,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            if (alertLogs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Navy800),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "No safe zone entry/exit events logged yet. When your child travels, alerts will appear here in real-time.",
                            color = Slate400,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(alertLogs, key = { it.id }) { log ->
                    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                    val dateStr = sdf.format(Date(log.timestamp))
                    val isEnter = log.eventType == "ENTERED"

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Navy800,
                        border = BorderStroke(1.dp, Navy700)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isEnter) EmeraldSafe.copy(alpha = 0.2f) else RoseDanger.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isEnter) Icons.Default.Place else Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = if (isEnter) EmeraldSafe else RoseDanger,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = log.message,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = dateStr,
                                    color = Slate400,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showAddDialog) {
        var zoneName by remember { mutableStateOf("") }
        var zoneLat by remember { mutableStateOf(kidState.latitude.toString()) }
        var zoneLng by remember { mutableStateOf(kidState.longitude.toString()) }
        var radiusMeters by remember { mutableFloatStateOf(200f) }
        var selectedColor by remember { mutableStateOf("#10B981") }
        var selectedIcon by remember { mutableStateOf("home") }
        var notifyEnter by remember { mutableStateOf(true) }
        var notifyExit by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Navy800,
            title = { Text("Create Safe Zone", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = zoneName,
                        onValueChange = { zoneName = it },
                        label = { Text("Zone Name (e.g. Home, School)", color = Slate400) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = EmeraldSafe,
                            unfocusedBorderColor = Navy700
                        ),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            zoneLat = kidState.latitude.toString()
                            zoneLng = kidState.longitude.toString()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy700),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Use Kid's Current Location", color = CyanGlow, fontSize = 11.sp)
                    }

                    Text("Radius: ${radiusMeters.toInt()} meters", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = radiusMeters,
                        onValueChange = { radiusMeters = it },
                        valueRange = 50f..2000f,
                        steps = 39,
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldSafe,
                            activeTrackColor = EmeraldSafe,
                            inactiveTrackColor = Navy700
                        )
                    )

                    // Preset Color choices
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("#10B981", "#3B82F6", "#8B5CF6", "#F59E0B", "#EC4899").forEach { hex ->
                            val c = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (selectedColor == hex) 3.dp else 0.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = hex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val lat = zoneLat.toDoubleOrNull() ?: kidState.latitude
                        val lng = zoneLng.toDoubleOrNull() ?: kidState.longitude
                        if (zoneName.isNotBlank()) {
                            val newZone = GeofenceZone(
                                id = UUID.randomUUID().toString(),
                                name = zoneName.trim(),
                                latitude = lat,
                                longitude = lng,
                                radiusMeters = radiusMeters,
                                colorHex = selectedColor,
                                notifyOnEnter = notifyEnter,
                                notifyOnExit = notifyExit,
                                iconName = selectedIcon
                            )
                            preferencesManager.addGeofence(newZone)
                            showAddDialog = false
                            Toast.makeText(context, "Safe zone '${zoneName}' created!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSafe)
                ) {
                    Text("Save Zone", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = Slate400)
                }
            }
        )
    }
}
