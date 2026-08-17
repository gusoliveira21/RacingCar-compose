package br.com.williamfranco.mobileracingcarcompose.src.features.game.fakes

import br.com.williamfranco.mobileracingcarcompose.src.features.game.repositories.HighscoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeHighscoreRepository(
    initialHighscore: Int = 0,
) : HighscoreRepository {

    private val _highscore = MutableStateFlow(initialHighscore)
    val savedScores = mutableListOf<Int>()

    override suspend fun saveHighScore(score: Int) {
        savedScores.add(score)
        _highscore.value = score
    }

    override fun getHighScore(): Flow<Int> = _highscore.asStateFlow()

    fun emitHighscore(score: Int) {
        _highscore.value = score
    }
}
