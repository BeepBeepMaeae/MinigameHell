package com.example.minigame

import android.content.Context
import android.media.MediaPlayer

object SoundEffectManager {
    private var clickPlayer: MediaPlayer? = null

    fun playClick(context: Context) {
        val pref = context.getSharedPreferences("minigame_prefs", Context.MODE_PRIVATE)
        val volume = pref.getInt("effect_volume", 50) / 100f

        clickPlayer = MediaPlayer.create(context, R.raw.click_sound)
        clickPlayer?.setVolume(volume, volume)
        clickPlayer?.setOnCompletionListener {
            it.release()
        }
        clickPlayer?.start()
    }
}