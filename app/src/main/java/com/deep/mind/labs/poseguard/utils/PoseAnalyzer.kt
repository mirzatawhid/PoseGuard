package com.deep.mind.labs.poseguard.utils

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.compose.ui.geometry.Offset
import com.deep.mind.labs.poseguard.viewmodel.CameraViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlin.math.min

class PoseAnalyzer(private val viewModel: CameraViewModel) {

    private val MIN_LIKELIHOOD = 0.7f

    private val options = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()

    private val detector = PoseDetection.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    fun analyze(imageProxy: ImageProxy, viewWidth: Float, viewHeight: Float, isFrontCamera: Boolean = true) {
        if (viewWidth <= 0f || viewHeight <= 0f) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        detector.process(inputImage)
            .addOnSuccessListener { pose ->
                handlePose(
                    pose,
                    imageProxy.width.toFloat(),
                    imageProxy.height.toFloat(),
                    viewWidth,
                    viewHeight,
                    isFrontCamera
                )
                imageProxy.close()
            }
            .addOnFailureListener { e ->
                Log.e("PoseAnalyzer", "Pose detection failed: $e")
                imageProxy.close()
            }
    }

    private fun handlePose(
        pose: Pose,
        imageWidth: Float,
        imageHeight: Float,
        canvasWidth: Float,
        canvasHeight: Float,
        isFrontCamera: Boolean
    ) {
        val scale = min(canvasWidth / imageWidth, canvasHeight / imageHeight)
        val offsetX = (canvasWidth - imageWidth * scale) / 2f
        val offsetY = (canvasHeight - imageHeight * scale) / 2f

        fun PoseLandmark?.toCanvas(): Offset? {
            if (this == null || inFrameLikelihood < MIN_LIKELIHOOD) return null
            val scaledX = position.x * scale
            val scaledY = position.y * scale
            val canvasX = if (isFrontCamera) (imageWidth * scale - scaledX) + offsetX else scaledX + offsetX
            val canvasY = scaledY + offsetY
            return Offset(canvasX, canvasY)
        }

        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST).toCanvas()
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST).toCanvas()
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER).toCanvas()
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).toCanvas()
        val head = pose.getPoseLandmark(PoseLandmark.NOSE).toCanvas()

        viewModel.updateLandmarks(leftWrist, leftShoulder, rightWrist, rightShoulder, head)
    }

}
