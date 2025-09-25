package com.deep.mind.labs.poseguard.data

data class QuestionUiState(
    val currentQuestion: Question? = null,
    val selectedOption: String? = null,
    val isRetry: Boolean = false,
    val questionIndex: Int = 0
)
