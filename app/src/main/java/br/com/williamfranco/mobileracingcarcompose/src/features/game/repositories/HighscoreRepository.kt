package br.com.williamfranco.mobileracingcarcompose.src.features.game.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.highscoreDataStore: DataStore<Preferences> by preferencesDataStore(name = "highscore")

interface HighscoreRepository {
    suspend fun saveHighScore(score: Int)
    fun getHighScore(): Flow<Int>
}

class HighscoreRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
) : HighscoreRepository {

    override suspend fun saveHighScore(score: Int) {
        dataStore.edit {
            it[HIGHSCORE_DATASTORE_KEY] = score
        }
    }

    override fun getHighScore(): Flow<Int> {
        return dataStore.data.map {
            it[HIGHSCORE_DATASTORE_KEY] ?: 0
        }
    }

    companion object {
        val HIGHSCORE_DATASTORE_KEY = intPreferencesKey("highscore_datastore_key")
    }
}
