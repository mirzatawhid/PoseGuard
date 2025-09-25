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

class PoseAnalyzer(private val viewModel: CameraViewModel) {

    private val MIN_LIKELIHOOD = 0.7f

    private val options = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()

    private val detector = PoseDetection.getClient(options)

    @OptIn(ExperimentalGetImage::class)
    fun analyze(imageProxy: ImageProxy, pWidth: Float, pHeight: Float) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        detector.process(inputImage)
            .addOnSuccessListener { pose ->
                handlePose(pose, imageProxy.width.toFloat(), imageProxy.height.toFloat(),pWidth,pHeight)
                imageProxy.close()
            }
            .addOnFailureListener { e ->
                Log.e("PoseAnalyzer", "Pose detection failed: $e")
                imageProxy.close()
            }
    }

    fun mapToViewCoordinates(
        landmarkX: Float,
        landmarkY: Float,
        imageWidth: Float,
        imageHeight: Float,
        viewWidth: Float,
        viewHeight: Float,
        isFrontCamera: Boolean = true
    ): Offset {
        val scaleX = viewWidth / imageWidth
        val scaleY = viewHeight / imageHeight

        val x = if (isFrontCamera) viewWidth - (landmarkX * scaleX) else landmarkX * scaleX
        val y = landmarkY * scaleY

        return Offset(x, y)
    }



    private fun handlePose(
        pose: Pose,
        imageWidth: Float,
        imageHeight: Float,
        viewWidth: Float,
        viewHeight: Float
    ) {

        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val head = pose.getPoseLandmark(PoseLandmark.NOSE)

        // Only use landmarks if likelihood > threshold
        val leftOffset = if (leftWrist != null && leftWrist.inFrameLikelihood >= MIN_LIKELIHOOD) {
            mapToViewCoordinates(leftWrist.position.x, leftWrist.position.y, imageWidth, imageHeight, viewWidth, viewHeight)
        } else null

        val rightOffset = if (rightWrist != null && rightWrist.inFrameLikelihood >= MIN_LIKELIHOOD) {
            mapToViewCoordinates(rightWrist.position.x, rightWrist.position.y, imageWidth, imageHeight, viewWidth, viewHeight)
        } else null

        val leftSOffset = if (leftShoulder != null && leftShoulder.inFrameLikelihood >= MIN_LIKELIHOOD) {
            mapToViewCoordinates(leftShoulder.position.x, leftShoulder.position.y, imageWidth, imageHeight, viewWidth, viewHeight)
        } else null

        val rightSOffset = if (rightShoulder != null && rightShoulder.inFrameLikelihood >= MIN_LIKELIHOOD) {
            mapToViewCoordinates(rightShoulder.position.x, rightShoulder.position.y, imageWidth, imageHeight, viewWidth, viewHeight)
        } else null

        val headOffset = if (head != null && head.inFrameLikelihood >= MIN_LIKELIHOOD) {
            mapToViewCoordinates(head.position.x, head.position.y, imageWidth, imageHeight, viewWidth, viewHeight)
        } else null

        viewModel.updateLandmarks(leftOffset, leftSOffset, rightOffset, rightSOffset, headOffset)
    }


}



