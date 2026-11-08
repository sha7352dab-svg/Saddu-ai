package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VioletGlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    isTurboMode: Boolean,
    onToggleTurboMode: () -> Unit,
    temperature: Float,
    onTemperatureChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("settings_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Saddu AI Preferences",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMutedDark)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Turbo Fast Speed Switch Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerDark)
                    .border(1.dp, if (isTurboMode) CyanNeon else SurfaceBorderDark, RoundedCornerShape(12.dp))
                    .clickable { onToggleTurboMode() }
                    .padding(14.dp)
                    .testTag("turbo_mode_toggle_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = if (isTurboMode) CyanNeon else TextMutedDark,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "⚡ Turbo Fast Mode",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTurboMode) CyanNeon else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                            Text(
                                text = if (isTurboMode)
                                    "Ultra-low latency with Gemini 3.5 Flash-Lite (Instant)"
                                else
                                    "Balanced deep reasoning with Gemini 3.8 Flash",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondaryDark,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                    Switch(
                        checked = isTurboMode,
                        onCheckedChange = { onToggleTurboMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00363D),
                            checkedTrackColor = CyanNeon,
                            uncheckedThumbColor = TextMutedDark,
                            uncheckedTrackColor = SurfaceContainerDark
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // AI Model Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerDark)
                    .border(1.dp, SurfaceBorderDark, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Active Engine",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMutedDark)
                        )
                        Text(
                            text = if (isTurboMode) "Gemini 3.5 Flash-Lite (Super Fast)" else "Gemini 3.8 Flash (Standard)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon
                            )
                        )
                        Text(
                            text = "Optimized in-memory token stream with 0-disk latency",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Voice & Speech Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerDark)
                    .border(1.dp, SurfaceBorderDark, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        tint = VioletGlow,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Voice-to-Text Engine",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMutedDark)
                        )
                        Text(
                            text = "Gemini Multimodal STT (Active)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = VioletGlow
                            )
                        )
                        Text(
                            text = "High-fidelity English, Hindi & Hinglish voice recognition",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Temperature Slider (Creativity vs Precision)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Creativity & Temperature",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = String.format(java.util.Locale.US, "%.2f", temperature),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = CyanNeon,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (temperature < 0.4f) "Precise & Deterministic (Great for coding & math)"
                    else if (temperature < 0.8f) "Balanced & Dynamic (Great for general conversation)"
                    else "High Creativity (Great for storytelling & brainstorming)",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMutedDark, fontSize = 12.sp)
                )
                Slider(
                    value = temperature,
                    onValueChange = onTemperatureChange,
                    valueRange = 0.1f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanNeon,
                        activeTrackColor = CyanNeon,
                        inactiveTrackColor = SurfaceContainerDark
                    ),
                    modifier = Modifier.padding(top = 4.dp).testTag("temperature_slider")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About Persona Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerDark)
                    .border(1.dp, SurfaceBorderDark, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = VioletGlow,
                        modifier = Modifier.size(20.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Persona: Saddu AI",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Friendly, sharp, reliable, and deeply engaging AI assistant. Specializes in coding, writing, problem-solving, creative tasks, and natural bilingual conversation in English & Hinglish.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondaryDark,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ElevatedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = CyanNeon,
                    contentColor = Color(0xFF00363D)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("close_settings_btn")
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
