package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.firebase.FirebaseFamilyRepository
import com.example.data.local.PreferencesManager
import com.example.data.model.GeofenceZone
import com.example.data.model.KidDeviceState
import com.example.data.model.LocationBreadcrumb
import com.example.ui.components.KidLiveStatusCard
import com.example.ui.components.RadarMapView
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400

@Composable
fun ParentMainScreen(
    preferencesManager: PreferencesManager,
    repository: FirebaseFamilyRepository,
    onSwitchRole: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val pairedKids by preferencesManager.pairedKidIds.collectAsState()
    var activeKidId by remember { mutableStateOf(pairedKids.firstOrNull() ?: preferencesManager.kidId.value) }

    LaunchedEffect(activeKidId) {
        repository.startListeningToKid(activeKidId)
    }

    val kidState by repository.currentKidState.collectAsState()
    val geofences by preferencesManager.geofences.collectAsState()
    val breadcrumbs by repository.locationHistory.collectAsState()
    val alertLogs by preferencesManager.alertLogs.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Navy900,
                tonalElevation = 8.dp,
                modifier = Modifier.border(BorderStroke(1.dp, Navy700))
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.NearMe, contentDescription = "Radar") },
                    label = { Text("Radar", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanPrimary,
                        selectedTextColor = CyanPrimary,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400,
                        indicatorColor = Navy700
                    ),
                    modifier = Modifier.testTag("nav_radar_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Security, contentDescription = "Geofences") },
                    label = { Text("Safe Zones", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldSafe,
                        selectedTextColor = EmeraldSafe,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400,
                        indicatorColor = Navy700
                    ),
                    modifier = Modifier.testTag("nav_zones_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyanGlow,
                        selectedTextColor = CyanGlow,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400,
                        indicatorColor = Navy700
                    ),
                    modifier = Modifier.testTag("nav_history_tab")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontSize = 11.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Slate400,
                        unselectedTextColor = Slate400,
                        indicatorColor = Navy700
                    ),
                    modifier = Modifier.testTag("nav_settings_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Navy900)
        ) {
            when (selectedTab) {
                0 -> ParentDashboardContent(
                    preferencesManager = preferencesManager,
                    repository = repository,
                    kidState = kidState,
                    geofences = geofences,
                    breadcrumbs = breadcrumbs,
                    pairedKids = pairedKids,
                    activeKidId = activeKidId,
                    onSelectKid = { activeKidId = it }
                )
                1 -> ParentGeofencingScreen(
                    preferencesManager = preferencesManager,
                    geofences = geofences,
                    alertLogs = alertLogs,
                    kidState = kidState
                )
                2 -> ParentHistoryScreen(
                    breadcrumbs = breadcrumbs,
                    kidState = kidState
                )
                3 -> ParentSettingsScreen(
                    preferencesManager = preferencesManager,
                    repository = repository,
                    pairedKids = pairedKids,
                    activeKidId = activeKidId,
                    onAddKid = { preferencesManager.addPairedKidId(it) },
                    onRemoveKid = { preferencesManager.removePairedKidId(it) },
                    onSwitchRole = onSwitchRole
                )
            }
        }
    }
}

@Composable
fun ParentDashboardContent(
    preferencesManager: PreferencesManager,
    repository: FirebaseFamilyRepository,
    kidState: KidDeviceState,
    geofences: List<GeofenceZone>,
    breadcrumbs: List<LocationBreadcrumb>,
    pairedKids: List<String>,
    activeKidId: String,
    onSelectKid: (String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showPairDialog by remember { mutableStateOf(false) }

    fun openInGoogleMaps() {
        val lat = kidState.latitude
        val lng = kidState.longitude
        val label = Uri.encode(kidState.kidName)
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web maps intent
            val fallbackUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
            context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
        }
    }

    fun openGoogleMapsNavigation() {
        val lat = kidState.latitude
        val lng = kidState.longitude
        val uri = Uri.parse("google.navigation:q=$lat,$lng")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallbackUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
            context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Multi-Kid Switcher Ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            pairedKids.forEach { kidId ->
                val isSelected = kidId == activeKidId
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) CyanPrimary else Navy800,
                    border = BorderStroke(1.dp, if (isSelected) CyanGlow else Navy700),
                    modifier = Modifier.clickable { onSelectKid(kidId) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Navy900 else EmeraldSafe)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (kidId == preferencesManager.kidId.value) kidState.kidName else kidId,
                            color = if (isSelected) Navy900 else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Add Kid Chip
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Navy800,
                border = BorderStroke(1.dp, Navy700),
                modifier = Modifier.clickable { showPairDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Kid", tint = CyanGlow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pair Another", color = CyanGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Kid Live Status Card (Live Pulse, Battery %, Speed, GPS accuracy)
        KidLiveStatusCard(
            state = kidState,
            onSosDismiss = {
                repository.dismissSosAlert(activeKidId)
                Toast.makeText(context, "SOS Alert Acknowledged", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live Radar Map View
        Text(
            text = "LIVE RADAR & GEOFENCE MAP",
            color = Slate400,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        RadarMapView(
            kidState = kidState,
            geofences = geofences,
            breadcrumbs = breadcrumbs,
            modifier = Modifier.height(270.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Primary Action Controls: Google Maps Redirect & Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { openInGoogleMaps() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("open_google_maps_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Map, contentDescription = "Maps", tint = Navy900, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Maps", color = Navy900, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Button(
                onClick = { openGoogleMapsNavigation() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("navigate_google_maps_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Directions, contentDescription = "Navigate", tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Navigate", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            IconButton(
                onClick = {
                    repository.startListeningToKid(activeKidId)
                    Toast.makeText(context, "Location sync refreshed", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Navy800)
                    .border(BorderStroke(1.dp, Navy700), RoundedCornerShape(14.dp))
                    .testTag("refresh_location_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = CyanGlow)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Safe Zone Proximity Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Navy700), RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = Navy800),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SAFE ZONES PROXIMITY",
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${geofences.size} Active",
                        color = EmeraldSafe,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                geofences.forEach { zone ->
                    val dist = repository.calculateDistanceInMeters(
                        kidState.latitude, kidState.longitude, zone.latitude, zone.longitude
                    )
                    val isInside = dist <= zone.radiusMeters

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isInside) EmeraldSafe else Slate400)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = zone.name,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isInside) EmeraldSafe.copy(alpha = 0.15f) else Navy900
                        ) {
                            Text(
                                text = if (isInside) "Inside Zone" else "${dist.toInt()}m away",
                                color = if (isInside) EmeraldSafe else Slate400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showPairDialog) {
        var inputCode by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPairDialog = false },
            containerColor = Navy800,
            title = { Text("Pair Another Kid Device", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter the 6-character Pair Code shown on your child's phone:", color = Slate400, fontSize = 13.sp)
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { inputCode = it.uppercase() },
                        placeholder = { Text("e.g. KID-8492", color = Slate400) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Navy700
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputCode.isNotBlank()) {
                            preferencesManager.addPairedKidId(inputCode.trim())
                            onSelectKid(inputCode.trim())
                            showPairDialog = false
                            Toast.makeText(context, "Kid device paired successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Pair Device", color = Navy900, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPairDialog = false }) {
                    Text("Cancel", color = Slate400)
                }
            }
        )
    }
}
