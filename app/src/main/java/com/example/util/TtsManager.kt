package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

private const val TAG = "TtsManager"

class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _speakingMessageId = MutableStateFlow<String?>(null)
    val speakingMessageId: StateFlow<String?> = _speakingMessageId.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.let { engine ->
                    // Prefer Hindi or English (India) or default locale
                    val localeHindi = Locale("hi", "IN")
                    if (engine.isLanguageAvailable(localeHindi) >= TextToSpeech.LANG_AVAILABLE) {
                        engine.language = localeHindi
                    } else {
                        engine.language = Locale.ENGLISH
                    }
                    engine.setPitch(1.05f)
                    engine.setSpeechRate(1.0f)
                }

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _speakingMessageId.value = utteranceId
                    }

                    override fun onDone(utteranceId: String?) {
                        if (_speakingMessageId.value == utteranceId) {
                            _speakingMessageId.value = null
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        if (_speakingMessageId.value == utteranceId) {
                            _speakingMessageId.value = null
                        }
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        Log.w(TAG, "TTS Error: $errorCode on utterance $utteranceId")
                        if (_speakingMessageId.value == utteranceId) {
                            _speakingMessageId.value = null
                        }
                    }
                })
            } else {
                Log.e(TAG, "TTS Initialization failed with status $status")
            }
        }
    }

    fun speak(messageId: String, text: String) {
        if (!isInitialized || tts == null) return

        if (_speakingMessageId.value == messageId) {
            stop()
            return
        }

        stop()
        _speakingMessageId.value = messageId

        // Clean markdown syntax for cleaner speech
        val cleaned = text
            .replace(Regex("```[a-zA-Z]*"), "")
            .replace("`", "")
            .replace("#", "")
            .replace("*", "")
            .trim()

        tts?.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, messageId)
    }

    fun stop() {
        tts?.stop()
        _speakingMessageId.value = null
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
