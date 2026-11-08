package com.example.ui.components

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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun MicrophonePermissionDialog(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("mic_permission_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyanNeon.copy(alpha = 0.2f))
                        .border(1.dp, CyanNeon, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Microphone Access",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Saddu AI uses the Gemini API to transcribe your voice in real time with high accuracy in English, Hindi, and Hinglish.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondaryDark,
                        lineHeight = 20.sp
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Please allow microphone permission when prompted so you can speak freely to Saddu AI.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMutedDark,
                        fontSize = 12.sp
                    )
                )
            }
        },
        confirmButton = {
            ElevatedButton(
                onClick = onRequestPermission,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = CyanNeon,
                    contentColor = Color(0xFF00363D)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("grant_mic_permission_btn")
            ) {
                Text("Allow Access", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_mic_permission_btn")
            ) {
                Text("Not Now", color = TextSecondaryDark)
            }
        }
    )
}
