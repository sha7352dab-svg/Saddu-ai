package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.ChatScreen
import com.example.ui.ChatViewModel
import com.example.ui.theme.SadduAITheme
import com.example.util.configureAppCheck

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        // STEP 2: Initialize UI
        setContent {
            SadduAITheme {
                ChatScreen(viewModel = viewModel)
            }
        }
    }
}
