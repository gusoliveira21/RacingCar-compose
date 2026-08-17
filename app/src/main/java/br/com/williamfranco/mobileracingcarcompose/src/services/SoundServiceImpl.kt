package br.com.williamfranco.mobileracingcarcompose.src.services

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import br.com.williamfranco.mobileracingcarcompose.R

class SoundServiceImpl(private val context: Context) : SoundService {
    private val soundPool: SoundPool
    private val soundMap: HashMap<Int, Int> = HashMap()
    private var mediaPlayer: MediaPlayer? = null

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_GAME)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    override fun loadSound(soundId: Int, soundResourceId: Int) {
        val sound = soundPool.load(context, soundResourceId, 1)
        soundMap[soundId] = sound
    }

    override fun playSound(soundId: Int) {
        soundMap[soundId]?.let {
            soundPool.play(it, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    override fun playBackgroundMusic(musicResourceId: Int) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(context, musicResourceId)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    override fun stopBackgroundMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun release() {
        soundPool.release()
        mediaPlayer?.release()
    }
}
