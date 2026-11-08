package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.ConversationEntity
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatDrawerContent(
    conversations: List<ConversationEntity>,
    currentConversationId: String?,
    onSelectConversation: (String) -> Unit,
    onNewChat: () -> Unit,
    onDeleteConversation: (String) -> Unit,
    onClearAll: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 16.dp, horizontal = 14.dp)
    ) {
        // App Header Brand
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(1.dp, CyanNeon, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_saddu_logo),
                    contentDescription = "Saddu AI",
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Saddu AI",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "Your Versatile Assistant",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CyanNeon,
                        fontSize = 11.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // New Chat Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CyanNeon)
                .clickable { onNewChat() }
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .testTag("drawer_new_chat_btn"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "New Chat",
                    tint = Color(0xFF00363D),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New Conversation",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00363D)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "RECENT CHATS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = TextMutedDark,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        )

        // Conversation History List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (conversations.isEmpty()) {
                item {
                    Text(
                        text = "No saved conversations yet.\nStart chatting with Saddu!",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMutedDark,
                            lineHeight = 16.sp
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                items(conversations, key = { it.id }) { conversation ->
                    val isSelected = conversation.id == currentConversationId
                    val itemBg = if (isSelected) SurfaceContainerHigh else Color.Transparent
                    val itemBorder = if (isSelected) CyanNeon.copy(alpha = 0.5f) else Color.Transparent

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(itemBg)
                            .border(1.dp, itemBorder, RoundedCornerShape(10.dp))
                            .clickable { onSelectConversation(conversation.id) }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("chat_item_${conversation.id}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = if (isSelected) CyanNeon else TextSecondaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = conversation.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) CyanNeon else MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                                    .format(Date(conversation.updatedAt)),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMutedDark,
                                    fontSize = 10.sp
                                )
                            )
                        }
                        IconButton(
                            onClick = { onDeleteConversation(conversation.id) },
                            modifier = Modifier.size(24.dp).testTag("delete_chat_${conversation.id}")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = TextMutedDark,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(
            color = SurfaceBorderDark,
            modifier = Modifier.padding(vertical = 10.dp)
        )

        // Bottom Actions (Settings, Clear All)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenSettings() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextSecondaryDark)
                )
            }

            if (conversations.isNotEmpty()) {
                IconButton(
                    onClick = onClearAll,
                    modifier = Modifier.size(36.dp).testTag("clear_all_chats_btn")
                ) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = "Clear All",
                        tint = TextMutedDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
