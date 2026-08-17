package br.com.williamfranco.mobileracingcarcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import br.com.williamfranco.mobileracingcarcompose.design.theme.MobileRacingCarComposeTheme
import br.com.williamfranco.mobileracingcarcompose.src.features.game.view_models.GameViewModel
import br.com.williamfranco.mobileracingcarcompose.src.features.game.view_models.GameViewModelImpl
import br.com.williamfranco.mobileracingcarcompose.src.routes.RoutesApp
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModel<GameViewModelImpl>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileRacingCarComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    RoutesApp()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.playBackgroundMusic()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopBackgroundMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.releaseSounds()
    }
}
