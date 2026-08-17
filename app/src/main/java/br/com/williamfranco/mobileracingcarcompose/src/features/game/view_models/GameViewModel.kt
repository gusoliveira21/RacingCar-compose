package br.com.williamfranco.mobileracingcarcompose.src.features.game.view_models

import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.williamfranco.mobileracingcarcompose.R
import br.com.williamfranco.mobileracingcarcompose.src.features.game.models.AccelerationData
import br.com.williamfranco.mobileracingcarcompose.src.features.game.models.MovementInput
import br.com.williamfranco.mobileracingcarcompose.src.features.game.models.NightRacingResourcePack
import br.com.williamfranco.mobileracingcarcompose.src.features.game.models.RacingResourcePack
import br.com.williamfranco.mobileracingcarcompose.src.features.game.repositories.HighscoreRepository
import br.com.williamfranco.mobileracingcarcompose.src.services.Constants.COLLISION_SCORE_PENALTY
import br.com.williamfranco.mobileracingcarcompose.src.services.Constants.DEFAULT_ACCELEROMETER_SENSITIVITY
import br.com.williamfranco.mobileracingcarcompose.src.services.Constants.INITIAL_GAME_SCORE
import br.com.williamfranco.mobileracingcarcompose.src.services.SoundService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface GameViewModel {
    val acceleration: StateFlow<AccelerationData>
    val movementInput: StateFlow<MovementInput>
    val gameScore: StateFlow<Int>
    val highscore: StateFlow<Int>
    val resourcePack: StateFlow<RacingResourcePack>
    val vibrateSharedFlow: MutableSharedFlow<Unit>

    fun setAcceleration(
        accelerationX: Float,
        accelerationY: Float,
        accelerationZ: Float,
        sensitivity: Int = DEFAULT_ACCELEROMETER_SENSITIVITY
    )

    fun increaseGameScore()
    fun resetGameScore()
    fun playBackgroundMusic()
    fun stopBackgroundMusic()
    fun releaseSounds()
    fun updateCarRect(carRect: Rect)
    fun updateBlockerRects(blockerRects: List<Rect>)
}

class GameViewModelImpl(
    private val highscoreRepository: HighscoreRepository,
    private val soundService: SoundService,
) : ViewModel(), GameViewModel {

    private val _acceleration = MutableStateFlow(AccelerationData(0f, 0f, 0f))
    override val acceleration = _acceleration.asStateFlow()

    private val _movementInput = MutableStateFlow(MovementInput.SwipeGestures)
    override val movementInput = _movementInput.asStateFlow()

    private val _gameScore = MutableStateFlow(INITIAL_GAME_SCORE)
    override val gameScore = _gameScore.asStateFlow()

    private val _highscore = MutableStateFlow(0)
    override val highscore = _highscore.asStateFlow()

    private val _resourcePack = MutableStateFlow<RacingResourcePack>(NightRacingResourcePack())
    override val resourcePack = _resourcePack.asStateFlow()

    override val vibrateSharedFlow = MutableSharedFlow<Unit>(replay = 1)

    private val carRectStateFlow = MutableStateFlow<Rect?>(null)
    private val blockerRectsStateFlow = MutableStateFlow<List<Rect>>(emptyList())

    private val carAndBlockerCollisionStateFlow =
        combine(carRectStateFlow.filterNotNull(), blockerRectsStateFlow) { carRect, blockerRects ->
            checkBlockerAndCarCollision(blockerRects, carRect)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    init {
        observeCollision()
        observeHighscore()

        soundService.loadSound(NEW_HIGHSCORE_SOUND_ID, R.raw.new_highscore)
        soundService.loadSound(BLOCKER_HIT_SOUND_ID, R.raw.blocker_hit)
        soundService.loadSound(MILESTONE_REACH_SOUND_ID, R.raw.milestone_reach)
    }

    private fun observeHighscore() {
        highscoreRepository.getHighScore().onEach {
            _highscore.value = it
        }.launchIn(viewModelScope)
    }

    private fun observeCollision() {
        carAndBlockerCollisionStateFlow.onEach { hasCollision ->
            if (hasCollision) {
                _gameScore.update { currentScore ->
                    val newScore = currentScore - COLLISION_SCORE_PENALTY
                    newScore.takeIf { it > INITIAL_GAME_SCORE } ?: INITIAL_GAME_SCORE
                }
                soundService.playSound(BLOCKER_HIT_SOUND_ID)
                vibrateSharedFlow.tryEmit(Unit)
            }
        }.launchIn(viewModelScope)
    }

    override fun setAcceleration(
        accelerationX: Float,
        accelerationY: Float,
        accelerationZ: Float,
        sensitivity: Int
    ) {
        _acceleration.update {
            it.copy(
                x = accelerationX * sensitivity,
                y = accelerationY * sensitivity,
                z = accelerationZ * sensitivity
            )
        }
    }

    override fun increaseGameScore() {
        _gameScore.update { currentScore ->
            (currentScore + 1).also { newScore ->
                saveNewHighscore(newScore)
                if (newScore % 10 == 0) {
                    soundService.playSound(MILESTONE_REACH_SOUND_ID)
                }
            }
        }
    }

    private fun saveNewHighscore(newScore: Int) {
        viewModelScope.launch {
            val currentHighscore = highscoreRepository.getHighScore().first()
            if (newScore > currentHighscore) {
                highscoreRepository.saveHighScore(newScore)
                soundService.playSound(NEW_HIGHSCORE_SOUND_ID)
            }
        }
    }

    override fun resetGameScore() {
        _gameScore.update { INITIAL_GAME_SCORE }
    }

    override fun playBackgroundMusic() {
        soundService.playBackgroundMusic()
    }

    override fun stopBackgroundMusic() {
        soundService.stopBackgroundMusic()
    }

    override fun releaseSounds() {
        soundService.release()
    }

    override fun updateCarRect(carRect: Rect) {
        carRectStateFlow.value = carRect
    }

    override fun updateBlockerRects(blockerRects: List<Rect>) {
        blockerRectsStateFlow.value = blockerRects
    }

    companion object {
        const val NEW_HIGHSCORE_SOUND_ID = 1
        const val BLOCKER_HIT_SOUND_ID = 2
        const val MILESTONE_REACH_SOUND_ID = 3
    }

    private fun checkBlockerAndCarCollision(blockerRects: List<Rect>, carRect: Rect): Boolean {
        return blockerRects.any { blockerRect ->
            blockerRect.overlaps(carRect)
        }
    }
}
