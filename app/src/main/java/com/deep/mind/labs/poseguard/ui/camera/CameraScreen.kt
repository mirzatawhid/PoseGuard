package com.deep.mind.labs.poseguard.ui.camera

import android.annotation.SuppressLint
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deep.mind.labs.poseguard.utils.PoseAnalyzer
import com.deep.mind.labs.poseguard.viewmodel.CameraViewModel
import java.util.concurrent.Executors

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraScreen(
    navController : NavController,
    viewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
    val landmarks by viewModel.landmarks.collectAsState()
    val handsDetected by viewModel.handsRaisedDetected.collectAsState()

    val executor = remember { Executors.newSingleThreadExecutor() }
    val poseAnalyzer = remember { PoseAnalyzer(viewModel) }

    Box(modifier = Modifier.fillMaxSize()) {
        // CameraX Preview
        AndroidView(factory = { ctx ->
            val previewView = androidx.camera.view.PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(executor) { imageProxy ->
                            poseAnalyzer.analyze(imageProxy, previewView.width.toFloat(),previewView.height.toFloat())
                        }
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        ctx as androidx.lifecycle.LifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }, modifier = Modifier.fillMaxSize())

        // Overlay Landmarks
        Canvas(modifier = Modifier.fillMaxSize()) {
            landmarks.leftWrist?.let { drawCircle(Color.Red, 15f, it) }
            landmarks.leftShoulder?.let { drawCircle(Color.Blue, 15f, it) }
            landmarks.rightWrist?.let { drawCircle(Color.Red, 15f, it) }
            landmarks.rightShoulder?.let { drawCircle(Color.Blue, 15f, it) }
            landmarks.head?.let { drawCircle(Color.Green, 15f, it) }

            // Draw lines connecting head to wrists

            val leftPos = landmarks.leftWrist
            val leftSPos = landmarks.leftShoulder
            val rightPos = landmarks.rightWrist
            val rightSPos = landmarks.rightShoulder

            if (leftSPos != null && rightSPos != null) {
                drawLine(
                    color = Color.Magenta,
                    start = leftSPos,
                    end = rightSPos,
                    strokeWidth = 5f
                )
            }

            if (leftSPos != null && leftPos != null) {
                drawLine(
                    color = Color.Yellow,
                    start = leftSPos,
                    end = leftPos,
                    strokeWidth = 5f
                )
            }

            if (rightSPos != null && rightPos != null) {
                drawLine(
                    color = Color.Yellow,
                    start = rightSPos,
                    end = rightPos,
                    strokeWidth = 5f
                )
            }
        }

        // Navigation after detection
        LaunchedEffect(handsDetected) {
            if (handsDetected) {
                navController.navigate("help") {
                    popUpTo("camera") { inclusive = true }
                }
                viewModel.resetDetection()
            }
        }
    }
}


