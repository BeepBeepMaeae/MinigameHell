package com.example.minigame

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

object BgmManager {
    private var mediaPlayer: MediaPlayer? = null
    private var currentResId: Int? = null

    /**
     * 지정한 리소스의 음악을 루프 재생합니다.
     * 동일한 트랙이 이미 재생 중이면 재생을 유지합니다.
     */
    fun startBgm(context: Context, @RawRes resId: Int) {
        // 이미 같은 트랙이 재생 중이면 아무 것도 하지 않음
        if (mediaPlayer != null && currentResId == resId && mediaPlayer!!.isPlaying) {
            return
        }
        // 기존 플레이어 해제
        mediaPlayer?.stop()
        mediaPlayer?.release()

        // 새 플레이어 생성
        val player = MediaPlayer.create(context, resId).apply {
            isLooping = true
            // 볼륨 설정
            val pref = context.getSharedPreferences("minigame_prefs", Context.MODE_PRIVATE)
            val vol = pref.getInt("background_volume", 50) / 100f
            setVolume(vol, vol)
            start()
        }

        mediaPlayer = player
        currentResId = resId
    }

    /** 재생 중인 음악을 멈추고 리소스 해제 */
    fun stopBgm() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentResId = null
    }

    /**
     * 실행 중인 플레이어에 현재 설정된 볼륨만 다시 적용합니다.
     * (SettingsFragment에서 즉시 볼륨 변경용)
     */
    fun setVolume(context: Context) {
        mediaPlayer?.let { player ->
            val pref = context.getSharedPreferences("minigame_prefs", Context.MODE_PRIVATE)
            val vol = pref.getInt("background_volume", 50) / 100f
            player.setVolume(vol, vol)
        }
    }
}