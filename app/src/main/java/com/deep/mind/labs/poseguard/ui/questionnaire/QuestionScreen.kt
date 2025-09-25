package com.deep.mind.labs.poseguard.ui.questionnaire

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deep.mind.labs.poseguard.viewmodel.QuestionViewModel
import java.util.*
@Composable
fun QuestionScreen(viewModel: QuestionViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    var isTtsInitialized by remember { mutableStateOf(false) }

    //Listen Part
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (spokenText != null) {
                viewModel.processSpeechResult(spokenText)
            }
        }
    }

    // Initialize TTS
    LaunchedEffect(Unit) {
        viewModel.initializeTts {
            isTtsInitialized = true
        }
    }

    // Start listening only after TTS finished
    LaunchedEffect(isTtsInitialized) {
        if (isTtsInitialized) {
            viewModel.shouldStartListening.collect { shouldStart ->
                val currentQuestion = viewModel.uiState.value.currentQuestion
                if (shouldStart && currentQuestion != null) {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    }
                    speechLauncher.launch(intent)
                }
            }
        }
    }

    //UI for question answer
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!isTtsInitialized) {
            // Show loading while TTS initializes
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text("Preparing question...", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            // Show question + options
            uiState.currentQuestion?.let { question ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = question.text, style = MaterialTheme.typography.headlineSmall)

                    Spacer(Modifier.height(16.dp))

                    question.options.forEach { option ->
                        val isSelected = option == uiState.selectedOption
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .background(
                                    if (isSelected) Color.Green else Color.LightGray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.processSpeechResult(option) }
                                .padding(12.dp)
                        ) {
                            Text(option, color = Color.Black)
                        }
                    }

                    if (uiState.isRetry) {
                        Spacer(Modifier.height(12.dp))
                        Text("Didn’t catch that, please try again.", color = Color.Red)
                    }
                }
            } ?: Text(
                "All questions completed!",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
