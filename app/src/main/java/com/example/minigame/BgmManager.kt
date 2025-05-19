package com.example.minigame

import android.content.Context
import android.media.MediaPlayer

object BgmManager {
    private var mediaPlayer: MediaPlayer? = null

    fun startBgm(context: Context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.bgm_music)
            mediaPlayer?.isLooping = true
            setVolume(context)
            mediaPlayer?.start()
        }
    }
//asdf

    fun stopBgm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun setVolume(context: Context) {
        val pref = context.getSharedPreferences("minigame_prefs", Context.MODE_PRIVATE)
        val volume = pref.getInt("background_volume", 50) / 100f
        mediaPlayer?.setVolume(volume, volume)
    }
}