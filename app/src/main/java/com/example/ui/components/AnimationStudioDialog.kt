package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VioletGlow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class LiveAnimationType(
    val title: String,
    val emoji: String,
    val description: String,
    val samplePrompt: String
) {
    COSMIC_VORTEX(
        title = "Cosmic Vortex",
        emoji = "🌌",
        description = "Gravitational swirling particle stars with touch reactivity",
        samplePrompt = "Explain the math behind this Cosmic Vortex particle animation and how to add gravity wells in Jetpack Compose Canvas."
    ),
    NEON_WAVES(
        title = "Cyber Neon Waves",
        emoji = "⚡",
        description = "Harmonic fluid sine waves with cybernetic glowing gradients",
        samplePrompt = "Show me how to make an audio visualizer wave animation in Jetpack Compose Canvas with dynamic amplitude."
    ),
    QUANTUM_ATOM(
        title = "Quantum Orbitals",
        emoji = "⚛️",
        description = "Tilted 3D planetary orbital rings with electron trails",
        samplePrompt = "How do you project 3D rotation onto a 2D Compose Canvas for orbital particle animations?"
    ),
    BIOLUMINESCENT(
        title = "Firefly Glow",
        emoji = "🎆",
        description = "Floating organic glowing fireflies with soft light diffusion",
        samplePrompt = "Write an ambient organic floating particle swarm animation in Compose Canvas with alpha breathing cycles."
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimationStudioDialog(
    onDismiss: () -> Unit,
    onSendPromptToSaddu: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedType by remember { mutableStateOf(LiveAnimationType.COSMIC_VORTEX) }
    var speedMultiplier by remember { mutableFloatStateOf(1.0f) }
    var showCodeView by remember { mutableStateOf(false) }
    var touchPos by remember { mutableStateOf<Offset?>(null) }
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("animation_studio_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🎨",
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Saddu Animation Studio",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Live Interactive Canvas & Code Generator",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = CyanNeon,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMutedDark)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiveAnimationType.entries.forEach { type ->
                    val isSelected = type == selectedType
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) SurfaceContainerHigh else SurfaceContainerDark)
                            .border(1.dp, if (isSelected) CyanNeon else SurfaceBorderDark, RoundedCornerShape(20.dp))
                            .clickable { selectedType = type }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = type.emoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = type.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CyanNeon else TextSecondaryDark
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Canvas Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF090D14))
                    .border(1.dp, CyanNeon.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset -> touchPos = offset },
                            onDrag = { change, _ ->
                                touchPos = change.position
                            },
                            onDragEnd = { touchPos = null },
                            onDragCancel = { touchPos = null }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                when (selectedType) {
                    LiveAnimationType.COSMIC_VORTEX -> CosmicVortexCanvas(speed = speedMultiplier, touchOffset = touchPos)
                    LiveAnimationType.NEON_WAVES -> CyberNeonWavesCanvas(speed = speedMultiplier, touchOffset = touchPos)
                    LiveAnimationType.QUANTUM_ATOM -> QuantumAtomCanvas(speed = speedMultiplier)
                    LiveAnimationType.BIOLUMINESCENT -> BioluminescentCanvas(speed = speedMultiplier, touchOffset = touchPos)
                }

                // Interactive Hint Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x99000000))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "👆 Touch & drag canvas",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Speed Control Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Speed: ${String.format(java.util.Locale.US, "%.1fx", speedMultiplier)}",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(0.5f, 1.0f, 2.0f).forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (speedMultiplier == preset) CyanNeon.copy(alpha = 0.2f) else SurfaceContainerDark)
                                .border(1.dp, if (speedMultiplier == preset) CyanNeon else SurfaceBorderDark, RoundedCornerShape(8.dp))
                                .clickable { speedMultiplier = preset }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${preset}x",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = if (speedMultiplier == preset) CyanNeon else TextMutedDark
                                )
                            )
                        }
                    }
                }
            }

            Slider(
                value = speedMultiplier,
                onValueChange = { speedMultiplier = it },
                valueRange = 0.2f..3.0f,
                colors = SliderDefaults.colors(
                    thumbColor = CyanNeon,
                    activeTrackColor = CyanNeon,
                    inactiveTrackColor = SurfaceContainerDark
                )
            )

            // Action Buttons: Code Viewer & Ask Saddu AI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // View Code Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerDark)
                        .border(1.dp, SurfaceBorderDark, RoundedCornerShape(12.dp))
                        .clickable { showCodeView = !showCodeView }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Code,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showCodeView) "Hide Code" else "View Code",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon
                            )
                        )
                    }
                }

                // Ask Saddu AI Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyanNeon)
                        .clickable {
                            onDismiss()
                            onSendPromptToSaddu(selectedType.samplePrompt)
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF00363D),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ask Saddu AI",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00363D)
                            )
                        )
                    }
                }
            }

            // Code View Card (Expandable)
            if (showCodeView) {
                Spacer(modifier = Modifier.height(14.dp))
                val code = getSampleCodeForType(selectedType)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0B0E14))
                        .border(1.dp, SurfaceBorderDark, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "KOTLIN / JETPACK COMPOSE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                color = CyanNeon,
                                fontSize = 11.sp
                            )
                        )
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("code", code))
                                Toast.makeText(context, "Animation code copied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy code",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = code,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ----------------------------------------------------
// CANVAS ANIMATION 1: COSMIC VORTEX (Swirling Starfield)
// ----------------------------------------------------
@Composable
private fun CosmicVortexCanvas(speed: Float, touchOffset: Offset?) {
    val transition = rememberInfiniteTransition(label = "galaxy")
    val durationMs = (6000 / speed).toInt().coerceAtLeast(500)
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = touchOffset ?: Offset(size.width / 2f, size.height / 2f)
        val particleCount = 70

        // Draw central cosmic glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(CyanNeon.copy(alpha = 0.7f), VioletGlow.copy(alpha = 0.3f), Color.Transparent),
                center = center,
                radius = 70f
            ),
            radius = 70f,
            center = center
        )

        // Draw spiraling orbital particle arms
        for (i in 0 until particleCount) {
            val progress = i / particleCount.toFloat()
            val baseRadius = progress * (size.minDimension / 2.3f)
            val currentAngle = (angle * (1f + (1f - progress)) + (i * 18f)) * (PI.toFloat() / 180f)

            val x = center.x + baseRadius * cos(currentAngle)
            val y = center.y + baseRadius * sin(currentAngle)

            val particleColor = if (i % 3 == 0) CyanNeon else if (i % 3 == 1) VioletGlow else Color(0xFFFFB703)
            val radius = (1.5f + progress * 2.5f)

            drawCircle(
                color = particleColor.copy(alpha = (0.3f + progress * 0.7f).coerceIn(0f, 1f)),
                radius = radius,
                center = Offset(x, y)
            )
        }
    }
}

// ----------------------------------------------------
// CANVAS ANIMATION 2: CYBER NEON WAVES (Fluid Harmonic)
// ----------------------------------------------------
@Composable
private fun CyberNeonWavesCanvas(speed: Float, touchOffset: Offset?) {
    val transition = rememberInfiniteTransition(label = "waves")
    val durationMs = (3500 / speed).toInt().coerceAtLeast(400)
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val midY = touchOffset?.y ?: (size.height / 2f)
        val waveCount = 3

        for (w in 0 until waveCount) {
            val path = Path()
            val amp = 28f + (w * 14f)
            val freq = 0.015f + (w * 0.005f)
            val wavePhase = phase + (w * 1.3f)

            path.moveTo(0f, midY)
            var x = 0f
            while (x <= size.width) {
                val y = midY + amp * sin(x * freq + wavePhase)
                path.lineTo(x, y)
                x += 6f
            }

            val waveColor = when (w) {
                0 -> CyanNeon
                1 -> VioletGlow
                else -> Color(0xFF00E676)
            }

            drawPath(
                path = path,
                color = waveColor.copy(alpha = 0.85f - (w * 0.2f)),
                style = Stroke(width = 3.5f - (w * 0.5f), cap = StrokeCap.Round)
            )
        }
    }
}

// ----------------------------------------------------
// CANVAS ANIMATION 3: QUANTUM ATOM ORBITALS
// ----------------------------------------------------
@Composable
private fun QuantumAtomCanvas(speed: Float) {
    val transition = rememberInfiniteTransition(label = "atom")
    val durationMs = (4000 / speed).toInt().coerceAtLeast(500)
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitalAngle"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val nucleusRadius = 14f

        // Pulsating Nucleus
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, CyanNeon, Color.Transparent),
                center = center,
                radius = 35f
            ),
            radius = nucleusRadius + (4f * sin(angle * PI.toFloat() / 90f)),
            center = center
        )

        // 3 Tilted Elliptical Orbital Rings
        val angles = listOf(0f, 60f, 120f)
        angles.forEachIndexed { idx, tiltAngle ->
            rotate(degrees = tiltAngle, pivot = center) {
                // Elliptical ring path
                val rx = size.minDimension * 0.38f
                val ry = size.minDimension * 0.16f

                val ringPath = Path().apply {
                    val steps = 60
                    for (i in 0..steps) {
                        val t = (i.toFloat() / steps) * 2 * PI.toFloat()
                        val px = center.x + rx * cos(t)
                        val py = center.y + ry * sin(t)
                        if (i == 0) moveTo(px, py) else lineTo(px, py)
                    }
                }

                drawPath(
                    path = ringPath,
                    color = VioletGlow.copy(alpha = 0.45f),
                    style = Stroke(width = 1.8f)
                )

                // Revolving Electron
                val electronT = (angle + (idx * 120f)) * (PI.toFloat() / 180f)
                val ex = center.x + rx * cos(electronT)
                val ey = center.y + ry * sin(electronT)

                drawCircle(
                    color = CyanNeon,
                    radius = 5.5f,
                    center = Offset(ex, ey)
                )
                // Electron Glow
                drawCircle(
                    color = CyanNeon.copy(alpha = 0.4f),
                    radius = 12f,
                    center = Offset(ex, ey)
                )
            }
        }
    }
}

// ----------------------------------------------------
// CANVAS ANIMATION 4: BIOLUMINESCENT FIREFLIES
// ----------------------------------------------------
@Composable
private fun BioluminescentCanvas(speed: Float, touchOffset: Offset?) {
    val transition = rememberInfiniteTransition(label = "fireflies")
    val durationMs = (5000 / speed).toInt().coerceAtLeast(600)
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "organicTime"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val attractor = touchOffset ?: Offset(size.width / 2f, size.height / 2f)
        val fireflyCount = 35

        for (i in 0 until fireflyCount) {
            val seedX = (i * 97) % size.width.toInt()
            val seedY = (i * 131) % size.height.toInt()
            val freqX = 1f + (i % 4) * 0.5f
            val freqY = 1.2f + (i % 3) * 0.4f

            val dx = 22f * cos(time * freqX + i)
            val dy = 26f * sin(time * freqY + (i * 2))

            var x = seedX + dx
            var y = seedY + dy

            // Touch Attractor pull
            if (touchOffset != null) {
                x += (attractor.x - x) * 0.25f
                y += (attractor.y - y) * 0.25f
            }

            val alphaPulse = (0.3f + 0.7f * sin(time * 2f + i)).coerceIn(0.1f, 1f)
            val color = if (i % 2 == 0) CyanNeon else Color(0xFF69F0AE)

            // Outer soft glow
            drawCircle(
                color = color.copy(alpha = alphaPulse * 0.35f),
                radius = 12f,
                center = Offset(x, y)
            )
            // Inner Core
            drawCircle(
                color = Color.White.copy(alpha = alphaPulse),
                radius = 2.5f,
                center = Offset(x, y)
            )
        }
    }
}

private fun getSampleCodeForType(type: LiveAnimationType): String {
    return when (type) {
        LiveAnimationType.COSMIC_VORTEX -> """
// Jetpack Compose 60fps Cosmic Particle Vortex
@Composable
fun CosmicVortex() {
    val transition = rememberInfiniteTransition(label = "vortex")
    val angle by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing))
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        for (i in 0 until 80) {
            val progress = i / 80f
            val r = progress * (size.minDimension / 2.2f)
            val theta = (angle + i * 20f) * (PI / 180f)
            drawCircle(
                color = Color.Cyan.copy(alpha = progress),
                radius = 2f + progress * 3f,
                center = Offset(center.x + r * cos(theta).toFloat(), center.y + r * sin(theta).toFloat())
            )
        }
    }
}
""".trimIndent()
        LiveAnimationType.NEON_WAVES -> """
// Harmonic Neon Waves Visualizer in Jetpack Compose
@Composable
fun CyberNeonWaves() {
    val transition = rememberInfiniteTransition(label = "waves")
    val phase by transition.animateFloat(
        initialValue = 0f, targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing))
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val midY = size.height / 2f
        val path = Path().apply {
            moveTo(0f, midY)
            for (x in 0..size.width.toInt() step 5) {
                lineTo(x.toFloat(), midY + 30f * sin(x * 0.02f + phase))
            }
        }
        drawPath(path, Color(0xFF00E5FF), style = Stroke(width = 4f))
    }
}
""".trimIndent()
        LiveAnimationType.QUANTUM_ATOM -> """
// 3D Tilted Elliptical Orbitals in Jetpack Compose
@Composable
fun QuantumAtom() {
    val transition = rememberInfiniteTransition(label = "atom")
    val angle by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing))
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        listOf(0f, 60f, 120f).forEach { tilt ->
            rotate(tilt, center) {
                drawOval(Color.Magenta.copy(alpha = 0.4f), style = Stroke(2f))
                val theta = angle * (PI / 180f)
                val ex = center.x + (size.width * 0.35f) * cos(theta).toFloat()
                val ey = center.y + (size.height * 0.15f) * sin(theta).toFloat()
                drawCircle(Color.Cyan, radius = 6f, center = Offset(ex, ey))
            }
        }
    }
}
""".trimIndent()
        LiveAnimationType.BIOLUMINESCENT -> """
// Bioluminescent Fireflies Ambient Floating Particles
@Composable
fun Fireflies() {
    val transition = rememberInfiniteTransition(label = "fireflies")
    val time by transition.animateFloat(
        initialValue = 0f, targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing))
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        for (i in 0 until 40) {
            val x = (i * 83) % size.width + 20f * cos(time + i)
            val y = (i * 127) % size.height + 25f * sin(time * 1.5f + i)
            drawCircle(Color(0xFF69F0AE), radius = 3f, center = Offset(x, y))
        }
    }
}
""".trimIndent()
    }
}
