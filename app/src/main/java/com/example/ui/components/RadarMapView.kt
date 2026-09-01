package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeofenceZone
import com.example.data.model.KidDeviceState
import com.example.data.model.LocationBreadcrumb
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.RoseDanger
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarMapView(
    kidState: KidDeviceState,
    geofences: List<GeofenceZone>,
    breadcrumbs: List<LocationBreadcrumb>,
    modifier: Modifier = Modifier,
    onMapClick: () -> Unit = {}
) {
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )

    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseRadius"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(Navy800, Navy900),
                    center = Offset.Unspecified,
                    radius = 800f
                )
            )
            .border(1.dp, Navy700, RoundedCornerShape(24.dp))
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    zoomLevel = (zoomLevel * zoom).coerceIn(0.5f, 3.5f)
                    panOffsetX += pan.x
                    panOffsetY += pan.y
                }
            }
            .testTag("radar_map_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2 + panOffsetX, size.height / 2 + panOffsetY)
            val maxRadius = (size.minDimension / 2) * 0.85f * zoomLevel

            // Draw concentric radar range rings
            val ringCount = 4
            val ringDistances = listOf("200m", "500m", "1km", "2km")
            for (i in 1..ringCount) {
                val r = (maxRadius / ringCount) * i
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                    radius = r,
                    center = center,
                    style = Stroke(
                        width = 1.2f,
                        pathEffect = if (i == ringCount) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null
                    )
                )
            }

            // Draw Crosshair Axes
            drawLine(
                color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1f
            )
            drawLine(
                color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1f
            )

            // Draw Radar Rotating Sweep Beam
            rotate(degrees = sweepAngle, pivot = center) {
                val sweepBrush = Brush.sweepGradient(
                    0.0f to Color(0x0000D2FF),
                    0.85f to Color(0x0000D2FF),
                    1.0f to Color(0x5500D2FF),
                    center = center
                )
                drawCircle(
                    brush = sweepBrush,
                    radius = maxRadius,
                    center = center
                )
            }

            // Draw Geofence Zones relative to Kid
            val scaleFactor = (maxRadius / 800f) // roughly 800 meters per radius at 1x
            geofences.forEach { zone ->
                val dLat = zone.latitude - kidState.latitude
                val dLng = zone.longitude - kidState.longitude
                // Approx conversion: 1 deg lat ~ 111,000m, 1 deg lon ~ 111,000 * cos(lat)
                val metersX = (dLng * 111320.0 * cos(Math.toRadians(kidState.latitude))).toFloat()
                val metersY = (-dLat * 110540.0).toFloat()

                val zoneCenter = Offset(
                    center.x + (metersX * scaleFactor),
                    center.y + (metersY * scaleFactor)
                )
                val zoneRadius = zone.radiusMeters * scaleFactor

                val zoneColor = try {
                    Color(android.graphics.Color.parseColor(zone.colorHex))
                } catch (e: Exception) {
                    EmeraldSafe
                }

                // Fill translucent
                drawCircle(
                    color = zoneColor.copy(alpha = 0.18f),
                    radius = zoneRadius,
                    center = zoneCenter
                )
                // Border dashed
                drawCircle(
                    color = zoneColor.copy(alpha = 0.8f),
                    radius = zoneRadius,
                    center = zoneCenter,
                    style = Stroke(
                        width = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                )
            }

            // Draw Breadcrumbs Trail
            if (breadcrumbs.size >= 2) {
                val path = Path()
                var first = true
                breadcrumbs.take(15).forEach { point ->
                    val dLat = point.latitude - kidState.latitude
                    val dLng = point.longitude - kidState.longitude
                    val metersX = (dLng * 111320.0 * cos(Math.toRadians(kidState.latitude))).toFloat()
                    val metersY = (-dLat * 110540.0).toFloat()
                    val ptOffset = Offset(
                        center.x + (metersX * scaleFactor),
                        center.y + (metersY * scaleFactor)
                    )
                    if (first) {
                        path.moveTo(ptOffset.x, ptOffset.y)
                        first = false
                    } else {
                        path.lineTo(ptOffset.x, ptOffset.y)
                    }

                    // Dot for history breadcrumb
                    drawCircle(
                        color = CyanPrimary.copy(alpha = 0.6f),
                        radius = 4f,
                        center = ptOffset
                    )
                }
                drawPath(
                    path = path,
                    color = CyanGlow.copy(alpha = 0.4f),
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )
            }

            // Draw Kid's Live Location Marker at center
            // 1. Accuracy circle
            val accuracyRadius = (kidState.accuracy * scaleFactor).coerceIn(8f, 60f)
            drawCircle(
                color = if (kidState.sosAlert) RoseDanger.copy(alpha = 0.25f) else CyanPrimary.copy(alpha = 0.15f),
                radius = accuracyRadius,
                center = center
            )

            // 2. Animated Pulse Wave
            drawCircle(
                color = if (kidState.sosAlert) RoseDanger.copy(alpha = pulseAlpha) else CyanGlow.copy(alpha = pulseAlpha),
                radius = pulseRadius * zoomLevel,
                center = center
            )

            // 3. Core Marker Pin
            drawCircle(
                color = if (kidState.sosAlert) RoseDanger else CyanPrimary,
                radius = 12f,
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = center
            )

            // 4. Heading Direction Cone if moving
            if (kidState.speed > 1.0f) {
                rotate(degrees = kidState.bearing, pivot = center) {
                    val arrowPath = Path().apply {
                        moveTo(center.x, center.y - 20f)
                        lineTo(center.x - 7f, center.y - 10f)
                        lineTo(center.x + 7f, center.y - 10f)
                        close()
                    }
                    drawPath(
                        path = arrowPath,
                        color = Color.White
                    )
                }
            }
        }

        // Radar HUD Overlay Info (Top Left)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Navy900.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
                .border(1.dp, Navy700, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (kidState.isOnline) EmeraldSafe else RoseDanger)
                )
                Text(
                    text = if (kidState.isOnline) " LIVE GPS" else " OFFLINE",
                    color = if (kidState.isOnline) EmeraldSafe else RoseDanger,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            Text(
                text = "Precision: ±${kidState.accuracy.toInt()}m",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
            if (kidState.speed > 0.5f) {
                Text(
                    text = "Speed: ${"%.1f".format(kidState.speed)} km/h",
                    color = CyanGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Map Control Floating Buttons (Right Side)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Navy800.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Navy700),
                modifier = Modifier.size(40.dp)
            ) {
                IconButton(
                    onClick = { zoomLevel = (zoomLevel * 1.3f).coerceAtMost(3.5f) },
                    modifier = Modifier.testTag("zoom_in_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Navy800.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Navy700),
                modifier = Modifier.size(40.dp)
            ) {
                IconButton(
                    onClick = { zoomLevel = (zoomLevel / 1.3f).coerceAtLeast(0.5f) },
                    modifier = Modifier.testTag("zoom_out_button")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Navy800.copy(alpha = 0.9f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Navy700),
                modifier = Modifier.size(40.dp)
            ) {
                IconButton(
                    onClick = {
                        panOffsetX = 0f
                        panOffsetY = 0f
                        zoomLevel = 1.0f
                    },
                    modifier = Modifier.testTag("recenter_button")
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Recenter on Kid", tint = CyanPrimary)
                }
            }
        }

        // Bottom Banner with Address Snippet
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(14.dp),
            color = Navy900.copy(alpha = 0.88f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Navy700)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = kidState.kidName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = kidState.address,
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
                Text(
                    text = "${"%.4f".format(kidState.latitude)}, ${"%.4f".format(kidState.longitude)}",
                    color = CyanGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
