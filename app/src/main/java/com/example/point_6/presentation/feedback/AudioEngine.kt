package com.example.point_6.presentation.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.point_6.domain.model.DrumType

class AudioEngine(context: Context) {
    private var soundPool: SoundPool
    private val soundMap = mutableMapOf<DrumType, Int>()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(audioAttributes)
            .build()

        loadSound(context, DrumType.SNARE, "snare.m4a")
        loadSound(context, DrumType.TOM1, "tom1.m4a")
        loadSound(context, DrumType.TOM2, "tom2.m4a")
        loadSound(context, DrumType.CYMBAL1, "cymbal1.m4a")
        loadSound(context, DrumType.CYMBAL2, "cymbal2.m4a")
        loadSound(context, DrumType.HI_HAT, "hihat.m4a")
    }

    private fun loadSound(context: Context, type: DrumType, filename: String) {
        try {
            val afd = context.assets.openFd(filename)
            val soundId = soundPool.load(afd, 1)
            soundMap[type] = soundId
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play(type: DrumType) {
        soundMap[type]?.let {
            soundPool.play(it, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}