package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.MessageEntity
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.SadduBubbleDark
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.UserBubbleDark
import com.example.ui.theme.VioletGlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: MessageEntity,
    isSpeaking: Boolean,
    onToggleTts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == "user"
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            // Saddu AI Avatar
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF131722))
                    .border(1.dp, CyanNeon.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_saddu_logo),
                    contentDescription = "Saddu AI",
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            // Sender name & time badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 3.dp, start = 2.dp, end = 2.dp)
            ) {
                if (!isUser) {
                    Text(
                        text = "Saddu AI",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            fontSize = 12.sp
                        )
                    )
                }
                Text(
                    text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp)),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMutedDark,
                        fontSize = 10.sp
                    )
                )
            }

            // Message Bubble Card
            val bubbleShape = if (isUser) {
                RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
            } else {
                RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
            }

            val bubbleBg = if (isUser) UserBubbleDark else SadduBubbleDark
            val bubbleBorder = if (isUser) SurfaceBorderDark else SurfaceBorderDark.copy(alpha = 0.8f)

            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(bubbleBg)
                    .border(1.dp, bubbleBorder, bubbleShape)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    if (message.content.isEmpty() && message.isStreaming) {
                        // Thinking indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = CyanNeon,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Saddu AI is thinking...",
                                style = MaterialTheme.typography.bodyMedium.copy(color = CyanNeon)
                            )
                        }
                    } else {
                        MarkdownText(
                            text = message.content,
                            textColor = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Pulsing streaming dot at bottom if active
                    if (message.isStreaming && message.content.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        StreamingPulseDot()
                    }
                }
            }

            // Action row for Saddu AI responses (Copy, TTS Speak, Share)
            if (!isUser && message.content.isNotEmpty() && !message.isStreaming) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Saddu AI Response", message.content)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied response to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp).testTag("copy_message_btn")
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleTts,
                        modifier = Modifier.size(28.dp).testTag("speak_message_btn")
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                            contentDescription = if (isSpeaking) "Stop voice" else "Read aloud",
                            tint = if (isSpeaking) CyanNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, message.content)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share via"))
                        },
                        modifier = Modifier.size(28.dp).testTag("share_message_btn")
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User Avatar
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerDark)
                    .border(1.dp, VioletGlow.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "You",
                    tint = VioletGlow,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun StreamingPulseDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(CyanNeon.copy(alpha = alpha))
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "typing...",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                color = CyanNeon.copy(alpha = alpha)
            )
        )
    }
}
