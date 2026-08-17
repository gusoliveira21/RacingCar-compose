package br.com.williamfranco.mobileracingcarcompose.src.features.game.fakes

import br.com.williamfranco.mobileracingcarcompose.src.services.SoundService

class FakeSoundService : SoundService {
    val loadedSounds = mutableListOf<Pair<Int, Int>>()
    val playedSounds = mutableListOf<Int>()
    var backgroundMusicStarted = false
        private set
    var backgroundMusicStopped = false
        private set
    var released = false
        private set

    override fun loadSound(soundId: Int, soundResourceId: Int) {
        loadedSounds.add(soundId to soundResourceId)
    }

    override fun playSound(soundId: Int) {
        playedSounds.add(soundId)
    }

    override fun playBackgroundMusic(musicResourceId: Int) {
        backgroundMusicStarted = true
        backgroundMusicStopped = false
    }

    override fun stopBackgroundMusic() {
        backgroundMusicStopped = true
        backgroundMusicStarted = false
    }

    override fun release() {
        released = true
    }
}
