package com.deep.mind.labs.poseguard.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PoseLandmarks(
    val leftWrist: Offset?,
    val leftShoulder: Offset?,
    val rightWrist: Offset?,
    val rightShoulder: Offset?,
    val head: Offset?
)

class CameraViewModel : ViewModel() {

    // For overlay
    private val _landmarks = MutableStateFlow(PoseLandmarks(null, null, null,null,null))
    val landmarks: StateFlow<PoseLandmarks> = _landmarks

    // Detection state
    private val _handsRaisedDetected = MutableStateFlow(false)
    val handsRaisedDetected: StateFlow<Boolean> = _handsRaisedDetected

    private var consecutiveFrames = 0
    private val requiredFrames = 3 // Confirm detection after 3 frames

    fun updateLandmarks(leftWrist: Offset?, leftShoulder: Offset?, rightWrist: Offset?,rightShoulder: Offset?, head: Offset?) {
        _landmarks.value = PoseLandmarks(leftWrist, leftShoulder, rightWrist, rightShoulder, head)

        if (leftWrist != null && rightWrist != null && head != null) {
            processPose(leftWrist.y, rightWrist.y, head.y)
        } else {
            consecutiveFrames = 0
        }
    }

    private fun processPose(leftWristY: Float, rightWristY: Float, headY: Float) {
        viewModelScope.launch {
            if (leftWristY < headY && rightWristY < headY) {
                consecutiveFrames++
                if (consecutiveFrames >= requiredFrames) {
                    _handsRaisedDetected.value = true
                }
            } else {
                consecutiveFrames = 0
            }
        }
    }

    fun resetDetection() {
        consecutiveFrames = 0
        _handsRaisedDetected.value = false
    }
}



