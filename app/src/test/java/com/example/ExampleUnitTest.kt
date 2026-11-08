package com.example

import com.example.data.model.AssistantMode
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
}
