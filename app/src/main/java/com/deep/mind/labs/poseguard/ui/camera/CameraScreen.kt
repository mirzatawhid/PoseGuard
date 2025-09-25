package com.deep.mind.labs.poseguard.ui.camera

import android.annotation.SuppressLint
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.deep.mind.labs.poseguard.utils.PoseAnalyzer
import com.deep.mind.labs.poseguard.viewmodel.CameraViewModel
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnsafeOptInUsageError")
@Composable
fun CameraScreen(
    navController: NavController,
    viewModel: CameraViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner

    val landmarks by viewModel.landmarks.collectAsState()
    val handsDetected by viewModel.handsRaisedDetected.collectAsState()

    val executor = remember { Executors.newSingleThreadExecutor() }
    val poseAnalyzer = remember { PoseAnalyzer(viewModel) }

    var canvasWidth by remember { mutableFloatStateOf(0f) }
    var canvasHeight by remember { mutableFloatStateOf(0f) }

    // Camera selector state → start with front camera
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_FRONT) }

    // PreviewView is remembered so we can re-bind camera
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { layoutCoordinates ->
                canvasWidth = layoutCoordinates.size.width.toFloat()
                canvasHeight = layoutCoordinates.size.height.toFloat()
            }
    ) {
        // CameraX Preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Re-bind camera whenever lensFacing changes
        LaunchedEffect(lensFacing) {
            val cameraProvider = ProcessCameraProvider.getInstance(context).get()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(executor) { imageProxy ->
                        poseAnalyzer.analyze(
                            imageProxy,
                            canvasWidth,
                            canvasHeight
                        )
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.Builder().requireLensFacing(lensFacing).build(),
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Transparent AppBar
        CenterAlignedTopAppBar(
            title = { Text("Raise Your Both Hand", color = Color.White) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Black.copy(alpha = 0.5f), // half opacity background
                titleContentColor = Color.White
            ),
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Switch Camera Button (bottom-right)
        IconButton(
            onClick = {
                lensFacing =
                    if (lensFacing == CameraSelector.LENS_FACING_FRONT)
                        CameraSelector.LENS_FACING_BACK
                    else
                        CameraSelector.LENS_FACING_FRONT
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 64.dp, end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Switch Camera",
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        }

        // Overlay Landmarks
        Canvas(modifier = Modifier.fillMaxSize()) {
            landmarks.leftWrist?.let { drawCircle(Color.Red, 15f, it) }
            landmarks.leftShoulder?.let { drawCircle(Color.Blue, 15f, it) }
            landmarks.rightWrist?.let { drawCircle(Color.Red, 15f, it) }
            landmarks.rightShoulder?.let { drawCircle(Color.Blue, 15f, it) }
            landmarks.head?.let { drawCircle(Color.Green, 15f, it) }

            val leftPos = landmarks.leftWrist
            val leftSPos = landmarks.leftShoulder
            val rightPos = landmarks.rightWrist
            val rightSPos = landmarks.rightShoulder

            if (leftSPos != null && rightSPos != null) {
                drawLine(Color.Magenta, leftSPos, rightSPos, strokeWidth = 5f)
            }
            if (leftSPos != null && leftPos != null) {
                drawLine(Color.Yellow, leftSPos, leftPos, strokeWidth = 5f)
            }
            if (rightSPos != null && rightPos != null) {
                drawLine(Color.Yellow, rightSPos, rightPos, strokeWidth = 5f)
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
