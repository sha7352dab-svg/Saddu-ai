package com.example.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "AudioRecorderManager"

sealed interface VoiceRecordingState {
    data object Idle : VoiceRecordingState
    data class Recording(val durationSeconds: Int, val amplitude: Float) : VoiceRecordingState
    data object Transcribing : VoiceRecordingState
    data class Error(val message: String) : VoiceRecordingState
}

class AudioRecorderManager(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var tickerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _recordingState = MutableStateFlow<VoiceRecordingState>(VoiceRecordingState.Idle)
    val recordingState: StateFlow<VoiceRecordingState> = _recordingState.asStateFlow()

    fun startRecording(): Boolean {
        try {
            stopCurrentSessionSilently()

            val file = File(context.cacheDir, "voice_record_${System.currentTimeMillis()}.m4a")
            currentFile = file

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(96000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder

            var seconds = 0
            _recordingState.value = VoiceRecordingState.Recording(0, 0f)

            tickerJob = scope.launch {
                var ticks = 0
                while (isActive) {
                    delay(100)
                    ticks++
                    if (ticks % 10 == 0) {
                        seconds++
                    }
                    val rawAmp = try {
                        mediaRecorder?.maxAmplitude ?: 0
                    } catch (_: Exception) {
                        0
                    }
                    // Normalize amplitude between 0.0 and 1.0 (maxAmplitude is up to 32767)
                    val normalized = (rawAmp / 32767f).coerceIn(0f, 1f)
                    _recordingState.value = VoiceRecordingState.Recording(seconds, normalized)

                    // Auto cap recording at 90 seconds to stay safely under Gemini payload limits
                    if (seconds >= 90) {
                        break
                    }
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio recording", e)
            _recordingState.value = VoiceRecordingState.Error("Failed to access microphone: ${e.localizedMessage}")
            stopCurrentSessionSilently()
            return false
        }
    }

    fun stopRecording(): ByteArray? {
        tickerJob?.cancel()
        tickerJob = null

        val recorder = mediaRecorder ?: return null
        val file = currentFile ?: return null

        return try {
            recorder.stop()
            recorder.release()
            mediaRecorder = null

            if (file.exists() && file.length() > 0) {
                val bytes = file.readBytes()
                file.delete()
                currentFile = null
                _recordingState.value = VoiceRecordingState.Transcribing
                bytes
            } else {
                _recordingState.value = VoiceRecordingState.Error("Recorded audio was empty.")
                file.delete()
                currentFile = null
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
            _recordingState.value = VoiceRecordingState.Error("Failed to finish recording: ${e.localizedMessage}")
            stopCurrentSessionSilently()
            null
        }
    }

    fun cancelRecording() {
        stopCurrentSessionSilently()
        _recordingState.value = VoiceRecordingState.Idle
    }

    fun resetState() {
        _recordingState.value = VoiceRecordingState.Idle
    }

    private fun stopCurrentSessionSilently() {
        tickerJob?.cancel()
        tickerJob = null
        try {
            mediaRecorder?.stop()
        } catch (_: Exception) {}
        try {
            mediaRecorder?.release()
        } catch (_: Exception) {}
        mediaRecorder = null

        try {
            currentFile?.delete()
        } catch (_: Exception) {}
        currentFile = null
    }
}
