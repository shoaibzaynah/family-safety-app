package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KidDeviceState
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.RoseDanger
import com.example.ui.theme.RoseDangerGlow
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400

@Composable
fun KidLiveStatusCard(
    state: KidDeviceState,
    modifier: Modifier = Modifier,
    onSosDismiss: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SosPulse")
    val pulseBorderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SosBorder"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .border(
                BorderStroke(
                    width = if (state.sosAlert) 2.dp else 1.dp,
                    color = if (state.sosAlert) RoseDanger.copy(alpha = pulseBorderAlpha) else Navy700
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("kid_live_status_card"),
        colors = CardDefaults.cardColors(
            containerColor = if (state.sosAlert) Color(0xFF2C1014) else Navy800
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // SOS Alert Banner if active
            AnimatedVisibility(visible = state.sosAlert) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = RoseDanger,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "SOS Alert",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "🚨 EMERGENCY SOS TRIGGERED!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Button(
                            onClick = onSosDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Acknowledge", color = RoseDanger, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Top Header: Name + Live badge + Last Updated
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(CyanPrimary, Color(0xFF6366F1)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.kidName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = state.kidName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = state.deviceModel,
                            color = Slate400,
                            fontSize = 12.sp
                        )
                    }
                }

                // Online/Offline Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (state.isOnline) EmeraldSafe.copy(alpha = 0.15f) else RoseDanger.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (state.isOnline) EmeraldSafe.copy(alpha = 0.5f) else RoseDanger.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (state.isOnline) EmeraldSafe else RoseDanger)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isOnline) "ONLINE" else "OFFLINE",
                            color = if (state.isOnline) EmeraldSafe else RoseDanger,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3-Column Metrics Grid (Battery, Speed, GPS Signal)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Battery Metric
                MetricBadge(
                    icon = if (state.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                    iconTint = if (state.batteryLevel > 20) EmeraldSafe else RoseDanger,
                    label = "Battery",
                    value = "${state.batteryLevel}%${if (state.isCharging) " ⚡" else ""}",
                    modifier = Modifier.weight(1f)
                )

                // Speed Metric
                MetricBadge(
                    icon = Icons.Default.Speed,
                    iconTint = CyanGlow,
                    label = "Speed",
                    value = if (state.speed > 0.5f) "${"%.1f".format(state.speed)} km/h" else "Still",
                    modifier = Modifier.weight(1f)
                )

                // GPS Metric
                MetricBadge(
                    icon = if (state.isGpsEnabled) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                    iconTint = if (state.isGpsEnabled) EmeraldSafe else AmberWarning,
                    label = "GPS",
                    value = if (state.isGpsEnabled) "±${state.accuracy.toInt()}m" else "Cellular",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MetricBadge(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Navy900,
        border = BorderStroke(1.dp, Navy700)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1
            )
            Text(
                text = label,
                color = Slate400,
                fontSize = 10.sp
            )
        }
    }
}
