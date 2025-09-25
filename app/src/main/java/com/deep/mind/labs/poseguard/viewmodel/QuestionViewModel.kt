package com.deep.mind.labs.poseguard.viewmodel

import android.app.Application
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.deep.mind.labs.poseguard.data.Question
import com.deep.mind.labs.poseguard.data.QuestionUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*


class QuestionViewModel(application: Application) : AndroidViewModel(application),
    TextToSpeech.OnInitListener {

    //dummy questions
    private val questions = listOf(
        Question(1, "What is your favorite color?", listOf("Red", "Blue", "Green")),
        Question(2, "Which fruit do you like?", listOf("Apple", "Banana", "Orange")),
        Question(3, "What is your favorite pet?", listOf("Dog", "Cat")),
        Question(4, "Which season do you like?", listOf("Summer", "Winter", "Rainy")),
        Question(5, "What is your favorite sport?", listOf("Football", "Cricket", "Tennis"))
    )

    private val _uiState = MutableStateFlow(QuestionUiState(currentQuestion = questions[0]))
    val uiState: StateFlow<QuestionUiState> = _uiState

    private val _shouldStartListening = MutableSharedFlow<Boolean>()
    val shouldStartListening: SharedFlow<Boolean> = _shouldStartListening

    private var hasSpokenCurrentQuestion = false

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        tts = TextToSpeech(application, this)
    }


    fun initializeTts(onInitialized: () -> Unit) {
        if (isTtsReady) {
            onInitialized()
        } else {
            viewModelScope.launch {
                while (!isTtsReady) kotlinx.coroutines.delay(50)
                onInitialized()
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            isTtsReady = true

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    viewModelScope.launch { _shouldStartListening.emit(true) }
                }

                override fun onError(utteranceId: String?) {}
            })

            speakCurrentQuestion()
        }
    }

    fun speakCurrentQuestion() {
        val question = _uiState.value.currentQuestion ?: return
        if (!isTtsReady || hasSpokenCurrentQuestion) return

        val textToSpeak = buildString {
            append(question.text)
            append(". Options are: ")
            append(question.options.joinToString(", "))
        }

        tts?.speak(
            textToSpeak,
            TextToSpeech.QUEUE_FLUSH,
            Bundle(),
            "question_${question.id}"
        )
        hasSpokenCurrentQuestion = true
    }

    fun processSpeechResult(speech: String) {
        val current = _uiState.value.currentQuestion ?: return
        val matchedOption = current.options.firstOrNull {
            speech.contains(it, ignoreCase = true)
        }

        if (matchedOption != null) {
            _uiState.value = _uiState.value.copy(selectedOption = matchedOption, isRetry = false)
            viewModelScope.launch {
                kotlinx.coroutines.delay(1500)
                moveToNextQuestion()
            }
        } else {
            _uiState.value = _uiState.value.copy(isRetry = true, selectedOption = null)
            speakRetry()
        }
    }

    private fun moveToNextQuestion() {
        val nextIndex = _uiState.value.questionIndex + 1
        if (nextIndex < questions.size) {
            hasSpokenCurrentQuestion = false
            _uiState.value = QuestionUiState(
                currentQuestion = questions[nextIndex],
                questionIndex = nextIndex
            )
            speakCurrentQuestion()
        } else {
            tts?.speak(
                "Thank you, you have answered all questions",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "done"
            )
            _uiState.value = _uiState.value.copy(currentQuestion = null)
        }
    }

    private fun speakRetry() {
        if (isTtsReady) {
            tts?.speak(
                "Sorry, I didn't catch that. Please try again.",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "retry"
            )
        }
    }

    override fun onCleared() {
        tts?.stop()
        tts?.shutdown()
        super.onCleared()
    }
}



