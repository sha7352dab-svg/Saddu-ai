package com.example

import com.example.data.model.AssistantMode
import com.example.util.VoiceRecordingState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testAssistantModes() {
        assertEquals(6, AssistantMode.entries.size)
        assertTrue(AssistantMode.entries.contains(AssistantMode.ALL_ROUNDER))
        assertTrue(AssistantMode.entries.contains(AssistantMode.ANIMATION))
        assertTrue(AssistantMode.entries.contains(AssistantMode.CODING))
        assertTrue(AssistantMode.entries.contains(AssistantMode.CREATIVE))
        assertTrue(AssistantMode.entries.contains(AssistantMode.LOGIC_MATH))
        assertTrue(AssistantMode.entries.contains(AssistantMode.HINGLISH_DOST))
    }

    @Test
    fun testModePromptsContainInstructions() {
        AssistantMode.entries.forEach { mode ->
            assertNotNull(mode.title)
            assertNotNull(mode.description)
            assertTrue(mode.promptInstruction.isNotBlank())
        }
    }

    @Test
    fun testVoiceRecordingStates() {
        val idleState = VoiceRecordingState.Idle
        val recordingState = VoiceRecordingState.Recording(durationSeconds = 5, amplitude = 0.65f)
        val transcribingState = VoiceRecordingState.Transcribing
        val errorState = VoiceRecordingState.Error("Test error")

        assertTrue(idleState is VoiceRecordingState.Idle)
        assertEquals(5, recordingState.durationSeconds)
        assertEquals(0.65f, recordingState.amplitude, 0.01f)
        assertTrue(transcribingState is VoiceRecordingState.Transcribing)
        assertEquals("Test error", errorState.message)
    }
}
