package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VioletGlow
import com.example.util.VoiceRecordingState

@Composable
fun VoiceRecordingBar(
    state: VoiceRecordingState,
    onCancel: () -> Unit,
    onStopAndSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceContainerHigh)
            .border(1.dp, CyanNeon.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("voice_recording_bar"),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is VoiceRecordingState.Recording -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel button
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF331618))
                            .border(1.dp, Color(0xFFE53935), CircleShape)
                            .testTag("voice_cancel_btn")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cancel recording",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Recording timer & animated audio visualizer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    ) {
                        // Pulsing red recording dot
                        PulsingRecordingDot()

                        Spacer(modifier = Modifier.width(8.dp))

                        // Duration text (e.g. 00:05)
                        val mins = state.durationSeconds / 60
                        val secs = state.durationSeconds % 60
                        Text(
                            text = String.format(java.util.Locale.US, "%02d:%02d", mins, secs),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Audio amplitude equalizer bars
                        AudioEqualizerBars(amplitude = state.amplitude)
                    }

                    // Done & Send button
                    IconButton(
                        onClick = onStopAndSend,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CyanNeon)
                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            .testTag("voice_send_btn")
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Send voice to Gemini",
                            tint = Color(0xFF00363D),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            is VoiceRecordingState.Transcribing -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = CyanNeon,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "✨ Gemini is transcribing your voice...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = CyanNeon,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            else -> {}
        }
    }
}

@Composable
private fun PulsingRecordingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_dot")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(Color(0xFFFF1744).copy(alpha = alpha))
    )
}

@Composable
private fun AudioEqualizerBars(amplitude: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_osc")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.height(24.dp)
    ) {
        val barCount = 7
        for (i in 0 until barCount) {
            val baseScale = kotlin.math.sin(phase + (i * 0.9f)).toFloat()
            // Height influenced by live microphone amplitude + wave oscillation
            val minHeight = 4f
            val maxHeight = 22f
            val dynamicHeight = (minHeight + (maxHeight - minHeight) * (0.3f * kotlin.math.abs(baseScale) + 0.7f * amplitude)).dp

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(dynamicHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (i % 2 == 0) CyanNeon else VioletGlow)
            )
        }
    }
}
