package com.example.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.ui.components.AnimationStudioDialog
import com.example.ui.components.ChatDrawerContent
import com.example.ui.components.MessageBubble
import com.example.ui.components.MicrophonePermissionDialog
import com.example.ui.components.ModeSelectorChips
import com.example.ui.components.QuickPromptsView
import com.example.ui.components.SettingsSheet
import com.example.ui.components.VoiceRecordingBar
import com.example.util.VoiceRecordingState
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.VioletGlow
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceContainerDark
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val keyboardController = LocalSoftwareKeyboardController.current

    val conversations by viewModel.conversations.collectAsState()
    val currentConvId by viewModel.currentConversationId.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()
    val isTurboMode by viewModel.isTurboMode.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val speakingMessageId by viewModel.speakingMessageId.collectAsState()
    val temperature by viewModel.temperature.collectAsState()

    val voiceRecordingState by viewModel.voiceRecordingState.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    var showAnimationStudio by remember { mutableStateOf(false) }
    var showMicPermissionRationale by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Scroll to bottom on messages update
    LaunchedEffect(messages.size, messages.lastOrNull()?.content?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Microphone permission launcher for Gemini Voice-to-Text
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val started = viewModel.startVoiceRecording()
            if (!started) {
                Toast.makeText(context, "Could not start audio recorder", Toast.LENGTH_SHORT).show()
            }
        } else {
            showMicPermissionRationale = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets(0)
            ) {
                ChatDrawerContent(
                    conversations = conversations,
                    currentConversationId = currentConvId,
                    onSelectConversation = { id ->
                        viewModel.selectConversation(id)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onNewChat = {
                        viewModel.createNewConversation()
                        coroutineScope.launch { drawerState.close() }
                    },
                    onDeleteConversation = { id ->
                        viewModel.deleteConversation(id)
                    },
                    onClearAll = {
                        viewModel.clearAllConversations()
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenSettings = {
                        showSettings = true
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, CyanNeon, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_saddu_logo),
                                    contentDescription = "Saddu AI",
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Saddu AI",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(EmeraldSuccess)
                                    )
                                }
                                Text(
                                    text = if (isTurboMode) "⚡ Turbo (Instant) • ${selectedMode.title}" else "🎯 Balanced • ${selectedMode.title}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CyanNeon,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("open_drawer_btn")
                        ) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isTurboMode) CyanNeon.copy(alpha = 0.2f) else SurfaceContainerDark)
                                .border(1.dp, if (isTurboMode) CyanNeon else SurfaceBorderDark, RoundedCornerShape(16.dp))
                                .clickable { viewModel.toggleTurboMode() }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("top_turbo_toggle_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Toggle Turbo",
                                    tint = if (isTurboMode) CyanNeon else TextMutedDark,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isTurboMode) "Turbo" else "Normal",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTurboMode) CyanNeon else TextMutedDark,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(VioletGlow.copy(alpha = 0.2f))
                                .border(1.dp, VioletGlow.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                                .clickable { showAnimationStudio = true }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("top_animation_studio_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎨", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Studio",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = VioletGlow,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { viewModel.createNewConversation() },
                            modifier = Modifier.testTag("top_new_chat_btn")
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "New Chat",
                                tint = CyanNeon
                            )
                        }
                        IconButton(
                            onClick = { showSettings = true },
                            modifier = Modifier.testTag("top_settings_btn")
                        ) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.statusBarsPadding()
                )
            },
            bottomBar = {
                // Bottom Input Bar with WindowInsets handling
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    // Active Voice TTS indicator bar
                    AnimatedVisibility(
                        visible = speakingMessageId != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceContainerHigh)
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = CyanNeon,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Saddu AI is speaking...",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CyanNeon,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                            IconButton(
                                onClick = { viewModel.ttsManager.stop() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Stop,
                                    contentDescription = "Stop speech",
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    val isVoiceActive = voiceRecordingState is VoiceRecordingState.Recording ||
                            voiceRecordingState is VoiceRecordingState.Transcribing

                    if (isVoiceActive) {
                        VoiceRecordingBar(
                            state = voiceRecordingState,
                            onCancel = { viewModel.cancelVoiceRecording() },
                            onStopAndSend = { viewModel.stopVoiceRecordingAndTranscribe(autoSend = true) },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    } else {
                        // Input Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mic Button for Gemini Voice-to-Text
                            IconButton(
                                onClick = {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        val started = viewModel.startVoiceRecording()
                                        if (!started) {
                                            Toast.makeText(context, "Could not start audio recorder", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(CyanNeon.copy(alpha = 0.15f))
                                    .border(1.dp, CyanNeon, CircleShape)
                                    .testTag("voice_input_btn")
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Voice Input with Gemini",
                                    tint = CyanNeon,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                        // Text Field
                        TextField(
                            value = inputText,
                            onValueChange = { viewModel.onInputTextChanged(it) },
                            placeholder = {
                                Text(
                                    text = when (selectedMode) {
                                        com.example.data.model.AssistantMode.CODING -> "Ask a coding question or debug code..."
                                        com.example.data.model.AssistantMode.CREATIVE -> "Draft email, write story, or brainstorm..."
                                        com.example.data.model.AssistantMode.LOGIC_MATH -> "Ask logic problem, math, or analysis..."
                                        com.example.data.model.AssistantMode.HINGLISH_DOST -> "Poochho Saddu se kuch bhi, bindass! 🙏"
                                        else -> "Ask Saddu AI anything (English/Hinglish)..."
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMutedDark),
                                    maxLines = 1
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(SurfaceContainerDark)
                                .border(1.dp, SurfaceBorderDark, RoundedCornerShape(24.dp))
                                .testTag("chat_input_field"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SurfaceContainerDark,
                                unfocusedContainerColor = SurfaceContainerDark,
                                disabledContainerColor = SurfaceContainerDark,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = CyanNeon
                            ),
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (inputText.isNotBlank() && !isGenerating) {
                                        viewModel.sendMessage(inputText)
                                        keyboardController?.hide()
                                    }
                                }
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Send / Progress Button
                        val canSend = inputText.isNotBlank() && !isGenerating
                        val sendBg = if (canSend) CyanNeon else SurfaceContainerDark

                        IconButton(
                            onClick = {
                                if (canSend) {
                                    viewModel.sendMessage(inputText)
                                    keyboardController?.hide()
                                }
                            },
                            enabled = canSend || isGenerating,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(sendBg)
                                .border(1.dp, if (canSend) CyanNeon else SurfaceBorderDark, CircleShape)
                                .testTag("send_message_btn")
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = CyanNeon,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (canSend) Color(0xFF00363D) else TextMutedDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
            containerColor = MaterialTheme.colorScheme.background,
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Mode Selector Chips
                ModeSelectorChips(
                    selectedMode = selectedMode,
                    onSelectMode = { mode -> viewModel.setMode(mode) }
                )

                // Error Message banner
                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let { errorText ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF3B1216))
                                .border(1.dp, Color(0xFFE53935), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFFF8A80),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorText,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFF8A80))
                                )
                            }
                            IconButton(
                                onClick = { viewModel.clearError() },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color(0xFFFF8A80),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Chat Messages or Quick Prompts
                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        QuickPromptsView(
                            mode = selectedMode,
                            onSelectPrompt = { prompt ->
                                viewModel.sendMessage(prompt)
                            }
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            MessageBubble(
                                message = msg,
                                isSpeaking = speakingMessageId == msg.id,
                                onToggleTts = { viewModel.toggleTts(msg) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }

    // Settings Modal Sheet
    if (showSettings) {
        SettingsSheet(
            isTurboMode = isTurboMode,
            onToggleTurboMode = { viewModel.toggleTurboMode() },
            temperature = temperature,
            onTemperatureChange = { viewModel.setTemperature(it) },
            onDismiss = { showSettings = false }
        )
    }

    // Interactive Animation Studio Sheet
    if (showAnimationStudio) {
        AnimationStudioDialog(
            onDismiss = { showAnimationStudio = false },
            onSendPromptToSaddu = { prompt ->
                viewModel.sendMessage(prompt)
            }
        )
    }

    // Microphone Permission Dialog
    if (showMicPermissionRationale) {
        MicrophonePermissionDialog(
            onRequestPermission = {
                showMicPermissionRationale = false
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            },
            onDismiss = { showMicPermissionRationale = false }
        )
    }
}
