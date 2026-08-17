package br.com.williamfranco.mobileracingcarcompose.src.features.game.view_models

import androidx.compose.ui.geometry.Rect
import br.com.williamfranco.mobileracingcarcompose.src.features.game.fakes.FakeHighscoreRepository
import br.com.williamfranco.mobileracingcarcompose.src.features.game.fakes.FakeSoundService
import br.com.williamfranco.mobileracingcarcompose.src.features.game.models.MovementInput
import br.com.williamfranco.mobileracingcarcompose.src.services.Constants.COLLISION_SCORE_PENALTY
import br.com.williamfranco.mobileracingcarcompose.src.services.Constants.INITIAL_GAME_SCORE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepo: FakeHighscoreRepository
    private lateinit var fakeSound: FakeSoundService
    private lateinit var viewModel: GameViewModelImpl

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeHighscoreRepository()
        fakeSound = FakeSoundService()
        viewModel = GameViewModelImpl(fakeRepo, fakeSound)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsSoundsAndDefaults() = runTest(testDispatcher) {
        assertEquals(3, fakeSound.loadedSounds.size)
        assertEquals(MovementInput.SwipeGestures, viewModel.movementInput.value)
        assertEquals(INITIAL_GAME_SCORE, viewModel.gameScore.value)
        assertEquals(0, viewModel.highscore.value)
    }

    @Test
    fun observeHighscore_updatesState() = runTest(testDispatcher) {
        fakeRepo.emitHighscore(42)
        advanceUntilIdle()

        assertEquals(42, viewModel.highscore.value)
    }

    @Test
    fun increaseGameScore_incrementsScore() = runTest(testDispatcher) {
        viewModel.increaseGameScore()
        advanceUntilIdle()

        assertEquals(1, viewModel.gameScore.value)
    }

    @Test
    fun increaseGameScore_savesNewHighscoreAndPlaysSound() = runTest(testDispatcher) {
        viewModel.increaseGameScore()
        advanceUntilIdle()

        assertEquals(listOf(1), fakeRepo.savedScores)
        assertTrue(
            fakeSound.playedSounds.contains(GameViewModelImpl.NEW_HIGHSCORE_SOUND_ID)
        )
    }

    @Test
    fun increaseGameScore_playsMilestoneSoundOnMultipleOfTen() = runTest(testDispatcher) {
        fakeRepo.emitHighscore(100)
        advanceUntilIdle()
        fakeSound.playedSounds.clear()

        repeat(10) { viewModel.increaseGameScore() }
        advanceUntilIdle()

        assertEquals(10, viewModel.gameScore.value)
        assertTrue(
            fakeSound.playedSounds.contains(GameViewModelImpl.MILESTONE_REACH_SOUND_ID)
        )
    }

    @Test
    fun resetGameScore_setsScoreToInitial() = runTest(testDispatcher) {
        viewModel.increaseGameScore()
        viewModel.increaseGameScore()
        advanceUntilIdle()

        viewModel.resetGameScore()

        assertEquals(INITIAL_GAME_SCORE, viewModel.gameScore.value)
    }

    @Test
    fun setAcceleration_appliesSensitivity() = runTest(testDispatcher) {
        viewModel.setAcceleration(1f, 2f, 3f, sensitivity = 5)

        val acceleration = viewModel.acceleration.value
        assertEquals(5f, acceleration.x)
        assertEquals(10f, acceleration.y)
        assertEquals(15f, acceleration.z)
    }

    @Test
    fun collision_penalizesScorePlaysHitAndVibrates() = runTest(testDispatcher) {
        repeat(10) { viewModel.increaseGameScore() }
        advanceUntilIdle()
        fakeSound.playedSounds.clear()

        val vibrateEvents = mutableListOf<Unit>()
        val vibrateJob = launch {
            viewModel.vibrateSharedFlow.collect { vibrateEvents.add(it) }
        }

        val carRect = Rect(0f, 0f, 10f, 10f)
        val blockerRect = Rect(5f, 5f, 15f, 15f)
        viewModel.updateCarRect(carRect)
        viewModel.updateBlockerRects(listOf(blockerRect))
        advanceUntilIdle()

        assertEquals(10 - COLLISION_SCORE_PENALTY, viewModel.gameScore.value)
        assertTrue(fakeSound.playedSounds.contains(GameViewModelImpl.BLOCKER_HIT_SOUND_ID))
        assertTrue(vibrateEvents.isNotEmpty())

        vibrateJob.cancel()
    }

    @Test
    fun collision_doesNotGoBelowInitialScore() = runTest(testDispatcher) {
        val carRect = Rect(0f, 0f, 10f, 10f)
        val blockerRect = Rect(0f, 0f, 5f, 5f)
        viewModel.updateCarRect(carRect)
        viewModel.updateBlockerRects(listOf(blockerRect))
        advanceUntilIdle()

        assertEquals(INITIAL_GAME_SCORE, viewModel.gameScore.value)
    }

    @Test
    fun backgroundMusic_delegatesToSoundService() = runTest(testDispatcher) {
        viewModel.playBackgroundMusic()
        assertTrue(fakeSound.backgroundMusicStarted)

        viewModel.stopBackgroundMusic()
        assertTrue(fakeSound.backgroundMusicStopped)
    }

    @Test
    fun releaseSounds_delegatesToSoundService() = runTest(testDispatcher) {
        viewModel.releaseSounds()
        assertTrue(fakeSound.released)
    }

    @Test
    fun increaseGameScore_doesNotSaveWhenNotHigherThanHighscore() = runTest(testDispatcher) {
        fakeRepo.emitHighscore(50)
        advanceUntilIdle()
        fakeRepo.savedScores.clear()

        viewModel.increaseGameScore()
        advanceUntilIdle()

        assertEquals(1, viewModel.gameScore.value)
        assertTrue(fakeRepo.savedScores.isEmpty())
        assertEquals(50, fakeRepo.getHighScore().first())
    }
}
