package com.deep.mind.labs.poseguard.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.deep.mind.labs.poseguard.ui.camera.CameraScreen
import com.deep.mind.labs.poseguard.ui.questionnaire.QuestionScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "camera") {
        composable("camera") { CameraScreen(navController) }
        composable("help") { QuestionScreen() }
    }
}