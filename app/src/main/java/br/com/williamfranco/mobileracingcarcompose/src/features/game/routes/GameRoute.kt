package br.com.williamfranco.mobileracingcarcompose.src.features.game.routes

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.williamfranco.mobileracingcarcompose.src.features.game.view_models.GameViewModel
import br.com.williamfranco.mobileracingcarcompose.src.features.game.views.GameView
import br.com.williamfranco.mobileracingcarcompose.src.services.vibrateError
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GameRoute() {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: GameViewModel = koinViewModel(viewModelStoreOwner = activity)
    val context = LocalContext.current

    LaunchedEffect(context) {
        viewModel.vibrateSharedFlow.collect {
            context.vibrateError()
        }
    }

    val gameScore by viewModel.gameScore.collectAsStateWithLifecycle()
    val highscore by viewModel.highscore.collectAsStateWithLifecycle()
    val acceleration by viewModel.acceleration.collectAsStateWithLifecycle()
    val movementInput by viewModel.movementInput.collectAsStateWithLifecycle()
    val resourcePack by viewModel.resourcePack.collectAsStateWithLifecycle()

    GameView(
        isDevMode = { true },
        gameScore = { gameScore },
        highscore = { highscore },
        resourcePack = { resourcePack },
        acceleration = { acceleration },
        movementInput = { movementInput },
        onGameScoreIncrease = viewModel::increaseGameScore,
        onResetGameScore = viewModel::resetGameScore,
        onBlockerRectsDraw = viewModel::updateBlockerRects,
        onCarRectDraw = viewModel::updateCarRect,
        modifier = Modifier.fillMaxSize()
    )
}
