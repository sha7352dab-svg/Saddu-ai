package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.model.AssistantMode
import com.example.data.repository.AiRepository
import com.example.util.TtsManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "ChatViewModel"

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = AiRepository(db.conversationDao())
    val ttsManager = TtsManager(application)
    val audioRecorder = com.example.util.AudioRecorderManager(application)
    val voiceRecordingState: StateFlow<com.example.util.VoiceRecordingState> = audioRecorder.recordingState

    val conversations: StateFlow<List<ConversationEntity>> = repository.conversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    // In-memory streaming message to avoid repetitive SQLite writes and frame drops on every token
    private val _streamingMessage = MutableStateFlow<MessageEntity?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<MessageEntity>> = _currentConversationId
        .flatMapLatest { id ->
            if (id != null) repository.getMessages(id) else flowOf(emptyList())
        }
        .combine(_streamingMessage) { dbList, streaming ->
            if (streaming != null && streaming.conversationId == _currentConversationId.value) {
                dbList + streaming
            } else {
                dbList
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _selectedMode = MutableStateFlow(AssistantMode.ALL_ROUNDER)
    val selectedMode: StateFlow<AssistantMode> = _selectedMode.asStateFlow()

    private val _isTurboMode = MutableStateFlow(true) // Fast mode (gemini-3.5-flash-lite) by default
    val isTurboMode: StateFlow<Boolean> = _isTurboMode.asStateFlow()

    private val _temperature = MutableStateFlow(0.7f)
    val temperature: StateFlow<Float> = _temperature.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val speakingMessageId: StateFlow<String?> = ttsManager.speakingMessageId

    init {
        viewModelScope.launch {
            repository.conversations.collect { list ->
                if (_currentConversationId.value == null && list.isNotEmpty()) {
                    _currentConversationId.value = list.first().id
                    try {
                        val savedMode = AssistantMode.valueOf(list.first().mode)
                        _selectedMode.value = savedMode
                    } catch (_: Exception) {}
                }
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun setMode(mode: AssistantMode) {
        _selectedMode.value = mode
    }

    fun toggleTurboMode() {
        _isTurboMode.value = !_isTurboMode.value
    }

    fun setTemperature(temp: Float) {
        _temperature.value = temp
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun createNewConversation(mode: AssistantMode = _selectedMode.value) {
        _currentConversationId.value = null
        _streamingMessage.value = null
        _selectedMode.value = mode
        _inputText.value = ""
        _errorMessage.value = null
        ttsManager.stop()
    }

    fun selectConversation(id: String) {
        _currentConversationId.value = id
        _streamingMessage.value = null
        _errorMessage.value = null
        ttsManager.stop()
        viewModelScope.launch {
            val conv = conversations.value.find { it.id == id }
            if (conv != null) {
                try {
                    _selectedMode.value = AssistantMode.valueOf(conv.mode)
                } catch (_: Exception) {}
            }
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            if (_currentConversationId.value == id) {
                _currentConversationId.value = null
                _streamingMessage.value = null
            }
            repository.deleteConversation(id)
        }
    }

    fun clearAllConversations() {
        viewModelScope.launch {
            _currentConversationId.value = null
            _streamingMessage.value = null
            repository.clearAll()
            ttsManager.stop()
        }
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty() || _isGenerating.value) return

        _inputText.value = ""
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                _isGenerating.value = true

                var convId = _currentConversationId.value
                if (convId == null) {
                    val newId = UUID.randomUUID().toString()
                    val title = if (trimmed.length > 36) trimmed.take(36) + "..." else trimmed
                    repository.createConversation(newId, title, _selectedMode.value)
                    _currentConversationId.value = newId
                    convId = newId
                }

                // 1. Insert user message in database
                val userMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    conversationId = convId,
                    sender = "user",
                    content = trimmed,
                    timestamp = System.currentTimeMillis()
                )
                repository.saveMessage(userMsg)

                // 2. Prepare in-memory assistant streaming message
                val assistantMsgId = UUID.randomUUID().toString()
                val currentTurbo = _isTurboMode.value
                val activeModel = if (currentTurbo) "gemini-3.5-flash-lite" else "gemini-3.8-flash"

                _streamingMessage.value = MessageEntity(
                    id = assistantMsgId,
                    conversationId = convId,
                    sender = "saddu",
                    content = "",
                    timestamp = System.currentTimeMillis(),
                    isStreaming = true,
                    modelUsed = activeModel
                )

                // 3. Ultra-fast stream accumulation in memory
                val currentHistory = messages.value.filter { it.id != assistantMsgId }
                val accumulated = StringBuilder()

                repository.streamReply(
                    conversationId = convId,
                    userPrompt = trimmed,
                    mode = _selectedMode.value,
                    historyMessages = currentHistory,
                    useTurboFast = currentTurbo,
                    temperature = _temperature.value
                ).collect { chunk ->
                    accumulated.append(chunk)
                    // Update in-memory state only - zero disk write latency
                    _streamingMessage.value = MessageEntity(
                        id = assistantMsgId,
                        conversationId = convId,
                        sender = "saddu",
                        content = accumulated.toString(),
                        timestamp = System.currentTimeMillis(),
                        isStreaming = true,
                        modelUsed = activeModel
                    )
                }

                // 4. Stream finished: write completed message to SQLite once
                val finalAssistantMsg = MessageEntity(
                    id = assistantMsgId,
                    conversationId = convId,
                    sender = "saddu",
                    content = accumulated.toString().ifEmpty { "Haanji! Main ready hoon, agla sawaal poochhiye!" },
                    timestamp = System.currentTimeMillis(),
                    isStreaming = false,
                    modelUsed = activeModel
                )
                repository.saveMessage(finalAssistantMsg)

            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI Generation timed out", e)
                _errorMessage.value = "Response timed out. Please try again."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI Generation error", e)
                _errorMessage.value = e.localizedMessage ?: "Failed to generate AI response."
            } finally {
                _streamingMessage.value = null
                _isGenerating.value = false
            }
        }
    }

    fun toggleTts(message: MessageEntity) {
        ttsManager.speak(message.id, message.content)
    }

    fun startVoiceRecording(): Boolean {
        _errorMessage.value = null
        ttsManager.stop()
        return audioRecorder.startRecording()
    }

    fun stopVoiceRecordingAndTranscribe(autoSend: Boolean = true) {
        viewModelScope.launch {
            val audioBytes = audioRecorder.stopRecording()
            if (audioBytes == null || audioBytes.isEmpty()) {
                return@launch
            }
            try {
                val transcribedText = repository.transcribeAudioWithGemini(audioBytes)
                audioRecorder.resetState()
                if (transcribedText.isNotBlank()) {
                    _inputText.value = transcribedText
                    if (autoSend) {
                        sendMessage(transcribedText)
                    }
                } else {
                    _errorMessage.value = "No speech detected in audio. Please try speaking again."
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Audio transcription failed", e)
                audioRecorder.resetState()
                _errorMessage.value = "Gemini voice transcription failed: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    fun cancelVoiceRecording() {
        audioRecorder.cancelRecording()
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorder.cancelRecording()
        ttsManager.shutdown()
    }
}
