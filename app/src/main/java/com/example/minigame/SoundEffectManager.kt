package com.example.minigame

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

object SoundEffectManager {
    private var clickPlayer: MediaPlayer? = null

    // 기존 클릭 효과음
    fun playClick(context: Context) {
        playEffect(context, R.raw.click_sound)
    }

    // 범용 효과음 재생 함수
    fun playEffect(context: Context, @RawRes resId: Int) {
        val volume = context
            .getSharedPreferences("minigame_prefs", Context.MODE_PRIVATE)
            .getInt("effect_volume", 50) / 100f
        MediaPlayer.create(context, resId).apply {
            setVolume(volume, volume)
            setOnCompletionListener { it.release() }
            start()
        }
    }

    // 카드 뽑기 사운드
    fun playCardDraw(context: Context) {
        playEffect(context, R.raw.card_draw_sound)
    }

    // 타이머 틱 사운드
    fun playTimerTick(context: Context) {
        playEffect(context, R.raw.timer_tick_sound)
    }

    fun Bust(context: Context) {
        playEffect(context, R.raw.bust)
    }

    // 정답/오답 사운드
    fun playCorrect(context: Context) {
        playEffect(context, R.raw.correct_sound)
    }
    fun playWrong(context: Context) {
        playEffect(context, R.raw.wrong_sound)
    }

    // 결과 화면 오픈 사운드
    fun playResult(context: Context) {
        playEffect(context, R.raw.result_sound)
    }
}