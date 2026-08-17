package br.com.williamfranco.mobileracingcarcompose.src.features.game.views.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameStateTest {

    @Test
    fun defaultStatus_isStopped() {
        val gameState = GameState()

        assertTrue(gameState.isStopped())
        assertFalse(gameState.isRunning())
        assertFalse(gameState.isPaused())
        assertEquals("STOPPED", gameState.getStatusName())
    }

    @Test
    fun run_setsRunning() {
        val gameState = GameState()

        gameState.run()

        assertTrue(gameState.isRunning())
        assertEquals("RUNNING", gameState.getStatusName())
    }

    @Test
    fun pause_setsPaused() {
        val gameState = GameState()
        gameState.run()

        gameState.pause()

        assertTrue(gameState.isPaused())
        assertEquals("PAUSED", gameState.getStatusName())
    }

    @Test
    fun stop_setsStopped() {
        val gameState = GameState()
        gameState.run()

        gameState.stop()

        assertTrue(gameState.isStopped())
        assertEquals("STOPPED", gameState.getStatusName())
    }
}
