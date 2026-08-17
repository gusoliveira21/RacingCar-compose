package br.com.williamfranco.mobileracingcarcompose.src.routes

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.williamfranco.mobileracingcarcompose.src.features.game.routes.GameRoute

@Composable
fun RoutesApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "gameView"
    ) {
        composable("gameView") {
            GameRoute()
        }
    }
}
