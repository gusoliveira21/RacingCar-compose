package br.com.williamfranco.mobileracingcarcompose.src.services

import br.com.williamfranco.mobileracingcarcompose.R

interface SoundService {
    fun loadSound(soundId: Int, soundResourceId: Int)
    fun playSound(soundId: Int)
    fun playBackgroundMusic(musicResourceId: Int = R.raw.background)
    fun stopBackgroundMusic()
    fun release()
}
