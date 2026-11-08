package com.example.data.repository

import android.util.Log
import com.example.data.local.ConversationDao
import com.example.data.local.ConversationEntity
import com.example.data.local.MessageEntity
import com.example.data.model.AssistantMode
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.InlineDataPart
import com.google.firebase.ai.type.TextPart
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "AiRepository"

class AiRepository(
    private val conversationDao: ConversationDao
) {
    val conversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()

    // Cache initialized GenerativeModels to eliminate re-instantiation overhead on every message
    private val modelCache = ConcurrentHashMap<String, GenerativeModel>()

    fun getMessages(conversationId: String): Flow<List<MessageEntity>> =
        conversationDao.getMessagesForConversation(conversationId)

    suspend fun createConversation(
        id: String,
        title: String,
        mode: AssistantMode
    ): ConversationEntity = withContext(Dispatchers.IO) {
        val conv = ConversationEntity(
            id = id,
            title = title,
            mode = mode.name,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        conversationDao.insertConversation(conv)
        conv
    }

    suspend fun saveMessage(message: MessageEntity) = withContext(Dispatchers.IO) {
        conversationDao.insertMessage(message)
    }

    suspend fun updateMessage(message: MessageEntity) = withContext(Dispatchers.IO) {
        conversationDao.updateMessage(message)
    }

    suspend fun deleteConversation(conversationId: String) = withContext(Dispatchers.IO) {
        conversationDao.deleteMessagesForConversation(conversationId)
        conversationDao.deleteConversation(conversationId)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        conversationDao.clearAllMessages()
        conversationDao.clearAllConversations()
    }

    /**
     * Constructs concise, high-performance system instructions for Saddu AI.
     */
    private fun buildSystemInstruction(mode: AssistantMode): String {
        return """
            You are "Saddu AI", a fast, highly capable, and independent AI assistant.
            Identity: Always introduce yourself as "Saddu AI" if asked.
            Tone: Sharp, friendly, direct, and warm. Use English and natural Hinglish (Hindi+English mix) fluently.
            Style: Be concise, clear, and fast without unnecessary fluff. Use markdown for headings, bullets, and syntax-highlighted code blocks (```language).
            Mode: ${mode.title} - ${mode.promptInstruction}
        """.trimIndent()
    }

    private fun getOrCreateModel(
        modelName: String,
        mode: AssistantMode,
        temperature: Float
    ): GenerativeModel {
        val cacheKey = "$modelName-${mode.name}-$temperature"
        return modelCache.getOrPut(cacheKey) {
            val systemText = buildSystemInstruction(mode)
            Firebase.ai.generativeModel(
                modelName = modelName,
                systemInstruction = content { text(systemText) },
                generationConfig = generationConfig {
                    this.temperature = temperature
                }
            )
        }
    }

    /**
     * Ultra-fast streaming response from Firebase AI Logic.
     * Uses gemini-3.5-flash-lite for instant responses, or gemini-3.8-flash for deep reasoning.
     */
    fun streamReply(
        conversationId: String,
        userPrompt: String,
        mode: AssistantMode,
        historyMessages: List<MessageEntity>,
        useTurboFast: Boolean = true,
        temperature: Float = 0.7f
    ): Flow<String> = flow {
        // gemini-3.5-flash-lite provides ultra-low latency; gemini-3.8-flash provides deep reasoning
        val modelName = if (useTurboFast) "gemini-3.5-flash-lite" else "gemini-3.8-flash"
        val model = getOrCreateModel(modelName, mode, temperature)

        // Keep last 6 messages to minimize token overhead and latency
        val historyContents = historyMessages
            .takeLast(6)
            .filter { it.content.isNotBlank() }
            .map { msg ->
                val role = if (msg.sender == "user") "user" else "model"
                content(role) { text(msg.content) }
            }

        val chat = model.startChat(history = historyContents)

        try {
            withTimeout(45_000L) {
                chat.sendMessageStream(userPrompt).collect { response ->
                    val textChunk = response.text.orEmpty()
                    if (textChunk.isNotEmpty()) {
                        emit(textChunk)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Streaming AI generation failed with model $modelName", e)
            throw e
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Transcribes recorded user audio using Gemini API via Firebase AI Logic.
     */
    suspend fun transcribeAudioWithGemini(
        audioBytes: ByteArray,
        mimeType: String = "audio/mp4"
    ): String = withContext(Dispatchers.IO) {
        val model = Firebase.ai.generativeModel("gemini-3.5-flash-lite")
        try {
            withTimeout(30_000L) {
                val prompt = "You are an accurate voice-to-text transcriber for Saddu AI. Transcribe the speech in this audio clip verbatim into text. Understand English, Hindi, and Hinglish accurately. Output ONLY the transcribed words with proper punctuation. Do not include any greeting, preamble, commentary, or markdown fences."
                val content = Content(
                    "user",
                    listOf(
                        InlineDataPart(audioBytes, mimeType),
                        TextPart(prompt)
                    )
                )
                val response = model.generateContent(content)
                response.text?.trim().orEmpty()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini audio transcription failed", e)
            throw e
        }
    }
}
