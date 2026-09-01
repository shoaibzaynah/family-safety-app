package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.data.firebase.FirebaseFamilyRepository
import com.example.data.local.PreferencesManager
import com.example.data.model.DisguiseType
import com.example.data.model.KidDeviceState
import com.example.data.model.StealthDisguiseConfig
import com.example.service.LocationTrackerService
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400

@Composable
fun KidScreen(
    preferencesManager: PreferencesManager,
    repository: FirebaseFamilyRepository,
    kidState: KidDeviceState,
    onEnterDisguise: () -> Unit,
    onSwitchRole: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var kidName by remember { mutableStateOf(preferencesManager.kidName.value) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showDisguiseDialog by remember { mutableStateOf(false) }
    var disguiseConfig by remember { mutableStateOf(preferencesManager.disguiseConfig.value) }

    var isServiceRunning by remember { mutableStateOf(true) }
    var sosSent by remember { mutableStateOf(false) }

    // Check permissions status
    var hasFineLoc by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
        )
    }
    var hasBgLoc by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PermissionChecker.PERMISSION_GRANTED
            } else true
        )
    }
    var hasNotification by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PermissionChecker.PERMISSION_GRANTED
            } else true
        )
    }

    // Permission launcher
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasFineLoc = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotification = results[Manifest.permission.POST_NOTIFICATIONS] == true
        }
        if (hasFineLoc) {
            LocationTrackerService.startService(context)
        }
    }

    LaunchedEffect(Unit) {
        // Auto-start tracker service when kid screen is open
        if (hasFineLoc) {
            LocationTrackerService.startService(context)
        } else {
            val perms = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionsLauncher.launch(perms.toTypedArray())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy900)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Kid Protection Mode",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Live background telemetry active",
                        color = Slate400,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Navy800,
                    border = BorderStroke(1.dp, Navy700)
                ) {
                    TextButton(onClick = onSwitchRole) {
                        Text("Switch Role", color = CyanGlow, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Live Sharing Status Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, EmeraldSafe.copy(alpha = 0.5f)), RoundedCornerShape(18.dp))
                    .shadow(10.dp, RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D251E)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(EmeraldSafe)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sharing Location in Background",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "SafeLink stays active even if closed or phone reboots",
                            color = Color(0xFFA7F3D0),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = isServiceRunning,
                        onCheckedChange = { checked ->
                            isServiceRunning = checked
                            if (checked) {
                                LocationTrackerService.startService(context)
                            } else {
                                LocationTrackerService.stopService(context)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldSafe,
                            checkedTrackColor = Color(0xFF065F46)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Device Pair Code Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Navy700), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "YOUR DEVICE PAIR CODE",
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = preferencesManager.kidId.value,
                            color = CyanPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("SafeLink Pair Code", preferencesManager.kidId.value)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Pair code copied!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyanGlow)
                        }
                    }
                    Text(
                        text = "Enter this 6-letter code in the Parent app to pair instantly with 0 login",
                        color = Slate400,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Kid Name Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Navy900, RoundedCornerShape(12.dp))
                            .clickable { showEditNameDialog = true }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Device Label", color = Slate400, fontSize = 10.sp)
                            Text(text = preferencesManager.kidName.value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = CyanGlow, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🚨 Emergency SOS 1-Touch Button
            Button(
                onClick = {
                    repository.triggerSosAlert()
                    sosSent = true
                    Toast.makeText(context, "🚨 Emergency SOS Sent to Parent!", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .testTag("kid_sos_button"),
                colors = ButtonDefaults.buttonColors(containerColor = RoseDanger),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = "SOS", tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (sosSent) "SOS ALERT SENT TO PARENT!" else "EMERGENCY SOS (TAP ONCE)",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stealth Disguise & App Customization Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Navy700), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF6366F1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Calculate, contentDescription = "Disguise", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Stealth Camouflage Mode",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Disguise app as ${disguiseConfig.appName}",
                                    color = Slate400,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        IconButton(onClick = { showDisguiseDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Disguise", tint = CyanGlow)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "When activated, SafeLink turns into a working Calculator/Notes tool. Enter secret PIN '${disguiseConfig.secretPin}=' into the calculator to return here.",
                        color = Slate300,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            preferencesManager.updateDisguiseConfig(disguiseConfig.copy(isDisguiseActive = true))
                            onEnterDisguise()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.VisibilityOff, contentDescription = "Hide", tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enter Stealth Disguise Screen", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Permissions Checklist
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Navy700), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "PERMISSIONS HEALTH CHECK",
                        color = Slate400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Permission 1: GPS Location
                    PermissionItemRow(
                        title = "Location Access (Fine GPS)",
                        subtitle = "Required for real-time tracking",
                        isGranted = hasFineLoc,
                        onGrant = {
                            permissionsLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    )

                    // Permission 2: Background "Allow all the time"
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        PermissionItemRow(
                            title = "Background Location (All the time)",
                            subtitle = "Keep sending coordinates when app is closed",
                            isGranted = hasBgLoc,
                            onGrant = {
                                permissionsLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                            }
                        )
                    }

                    // Permission 3: Battery Optimization
                    PermissionItemRow(
                        title = "Battery Unrestricted / Exemption",
                        subtitle = "Prevents OS from killing background GPS service",
                        isGranted = true,
                        onGrant = {
                            try {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        var tempName by remember { mutableStateOf(kidName) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = Navy800,
            title = { Text("Edit Device Name", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Kid's Name", color = Slate400) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = Navy700
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotBlank()) {
                            kidName = tempName
                            preferencesManager.setKidName(tempName)
                        }
                        showEditNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Save", color = Navy900, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel", color = Slate400)
                }
            }
        )
    }

    // Edit Disguise Config Dialog
    if (showDisguiseDialog) {
        var tempAppName by remember { mutableStateOf(disguiseConfig.appName) }
        var tempPin by remember { mutableStateOf(disguiseConfig.secretPin) }
        var tempType by remember { mutableStateOf(disguiseConfig.disguiseType) }

        AlertDialog(
            onDismissRequest = { showDisguiseDialog = false },
            containerColor = Navy800,
            title = { Text("Disguise & Stealth Settings", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Choose camouflage tool:", color = Slate400, fontSize = 13.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                tempType = DisguiseType.CALCULATOR
                                tempAppName = "Calculator"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (tempType == DisguiseType.CALCULATOR) CyanPrimary else Navy700
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Calculator", color = if (tempType == DisguiseType.CALCULATOR) Navy900 else Color.White, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                tempType = DisguiseType.NOTES
                                tempAppName = "Quick Notes"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (tempType == DisguiseType.NOTES) CyanPrimary else Navy700
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Notes", color = if (tempType == DisguiseType.NOTES) Navy900 else Color.White, fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = tempAppName,
                        onValueChange = { tempAppName = it },
                        label = { Text("App Display Name", color = Slate400) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Navy700
                        )
                    )

                    OutlinedTextField(
                        value = tempPin,
                        onValueChange = { if (it.length <= 6) tempPin = it },
                        label = { Text("Secret PIN Code", color = Slate400) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = Navy700
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newConfig = disguiseConfig.copy(
                            appName = tempAppName,
                            secretPin = tempPin,
                            disguiseType = tempType,
                            appIcon = if (tempType == DisguiseType.CALCULATOR) "calculator" else "notes"
                        )
                        disguiseConfig = newConfig
                        preferencesManager.updateDisguiseConfig(newConfig)
                        showDisguiseDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Apply Disguise", color = Navy900, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisguiseDialog = false }) {
                    Text("Cancel", color = Slate400)
                }
            }
        )
    }
}

@Composable
fun PermissionItemRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isGranted) EmeraldSafe else AmberWarning,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
            Text(
                text = subtitle,
                color = Slate400,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 22.dp)
            )
        }

        if (!isGranted) {
            OutlinedButton(
                onClick = onGrant,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, CyanGlow),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text("Enable", color = CyanGlow, fontSize = 11.sp)
            }
        }
    }
}
