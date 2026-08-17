package br.com.williamfranco.mobileracingcarcompose.src.features.game.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import br.com.williamfranco.mobileracingcarcompose.src.features.game.models.AccelerationData
import br.com.williamfranco.mobileracingcarcompose.src.features.game.models.MovementInput
import br.com.williamfranco.mobileracingcarcompose.src.features.game.models.MovementInput.Accelerometer
import br.com.williamfranco.mobileracingcarcompose.src.features.game.models.MovementInput.SwipeGestures
import br.com.williamfranco.mobileracingcarcompose.src.features.game.models.MovementInput.TapGestures
import br.com.williamfranco.mobileracingcarcompose.src.features.game.models.RacingResourcePack
import br.com.williamfranco.mobileracingcarcompose.src.features.game.views.state.BackgroundState
import br.com.williamfranco.mobileracingcarcompose.src.features.game.views.state.BlockersState
import br.com.williamfranco.mobileracingcarcompose.src.features.game.views.state.CarState
import br.com.williamfranco.mobileracingcarcompose.src.features.game.views.state.GameState
import br.com.williamfranco.mobileracingcarcompose.src.services.Constants
import br.com.williamfranco.mobileracingcarcompose.src.services.Constants.CAR_MOVEMENT_SPRING_ANIMATION_STIFFNESS
import br.com.williamfranco.mobileracingcarcompose.src.services.Constants.TICKER_ANIMATION_DURATION

@Composable
fun GameView(
    gameScore: () -> Int,
    highscore: () -> Int,
    acceleration: () -> AccelerationData,
    movementInput: () -> MovementInput,
    resourcePack: () -> RacingResourcePack,
    isDevMode: () -> Boolean,
    onGameScoreIncrease: () -> Unit,
    onResetGameScore: () -> Unit,
    onBlockerRectsDraw: (List<Rect>) -> Unit,
    onCarRectDraw: (Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val carImageDrawableBitmap = ImageBitmap.imageResource(resourcePack().carImageDrawable)
    val backgroundImageBitmap = ImageBitmap.imageResource(resourcePack().backgroundImageDrawable)
    val blockerImageBitmap = ImageBitmap.imageResource(resourcePack().blockerImageDrawable)

    val gameState by remember {
        mutableStateOf(GameState())
    }
    val carState by remember {
        mutableStateOf(
            CarState(image = carImageDrawableBitmap)
        )
    }

    val blockersState by remember {
        mutableStateOf(
            BlockersState(image = blockerImageBitmap)
        )
    }
    val backgroundState by remember {
        mutableStateOf(
            BackgroundState(
                image = backgroundImageBitmap,
                onGameScoreIncrease = {
                    if (gameState.isRunning())
                        onGameScoreIncrease()
                }
            )
        )
    }

    val backgroundSpeed by remember {
        derivedStateOf {
            (gameScore() / Constants.GAME_SCORE_TO_VELOCITY_RATIO) + Constants.INITIAL_VELOCITY
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "infinite")

    val ticker by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = TICKER_ANIMATION_DURATION, easing = LinearEasing)
        ),
        label = "ticker"
    )

    BoxWithConstraints(modifier = modifier) {
        ticker

        LaunchedEffect(movementInput()) {
            if (movementInput() == Accelerometer)
                carState.moveWithAcceleration(acceleration())
        }

        val carOffsetIndex by animateFloatAsState(
            targetValue = carState.getPosition().fromLeftOffsetIndex(),
            label = "car offset index",
            animationSpec = spring(stiffness = CAR_MOVEMENT_SPRING_ANIMATION_STIFFNESS)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (gameState.isRunning()) {
                        when (movementInput()) {
                            TapGestures ->
                                Modifier.detectCarPositionByPointerInput(maxWidth = maxWidth.value.toInt()) { position ->
                                    carState.moveWithTapGesture(position)
                                }

                            SwipeGestures -> Modifier.detectSwipeDirection(maxWidth.value.toInt()) { swipeDirection ->
                                carState.moveWithSwipeGesture(swipeDirection)
                            }

                            Accelerometer -> Modifier
                        }
                    } else
                        Modifier
                )
        ) {
            GameCanvas(
                gameState = gameState,
                backgroundState = backgroundState,
                backgroundSpeed = backgroundSpeed,
                blockersState = blockersState,
                carState = carState,
                carOffsetIndex = carOffsetIndex,
                onBlockerRectsDraw = onBlockerRectsDraw,
                onCarRectDraw = onCarRectDraw,
                modifier = Modifier.fillMaxSize(),
            )

            AnimatedVisibility(
                visible = gameState.isStopped() || gameState.isPaused(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                GameStateIndicator(
                    gameState = gameState,
                    onStartClicked = { gameState.run() }
                )
            }
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            TopInfoTexts(
                gameScore = gameScore,
                highscore = highscore,
                modifier = Modifier.fillMaxWidth()
            )
            TopActionButtons(
                onPauseGameState = { gameState.pause() },
                onResetGameScore = onResetGameScore,
                isDevMode = isDevMode(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
