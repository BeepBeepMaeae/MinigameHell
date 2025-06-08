package com.example.minigame

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlin.random.Random

class ReactionTestActivity : AppCompatActivity(), PauseMenuFragment.PauseMenuListener {

    private lateinit var tvInfo: TextView
    private lateinit var btnPause: Button
    private lateinit var mainLayout: View
    private lateinit var profileImageView: ImageView

    private var isWaitingForTouch = false
    private var startTime = 0L
    private val reactionTimes = mutableListOf<Long>()
    private val handler = Handler(Looper.getMainLooper())
    private var round = 0
    private var pendingRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reaction_test)

        tvInfo = findViewById(R.id.tvInfo)
        btnPause = findViewById(R.id.btnPause)
        mainLayout = findViewById(R.id.reactionRoot)
        profileImageView = findViewById(R.id.profileImageView)

        // 프로필 이미지 표시
        SharedPrefManager.getProfileImageUri(this)?.let {
            Glide.with(this).load(Uri.parse(it)).into(profileImageView)
        }

        startRound()

        mainLayout.setOnClickListener {
            if (isWaitingForTouch) {
                val reactionTime = System.currentTimeMillis() - startTime
                reactionTimes.add(reactionTime)
                isWaitingForTouch = false
                round++

                if (round >= 5) {
                    showResult()
                } else {
                    startRound()
                }
            } else {
                // 너무 빨리 눌렀을 때
                SoundEffectManager.playTooEarly(this@ReactionTestActivity)
                Toast.makeText(
                    this@ReactionTestActivity,
                    "너무 빨리 누르셨습니다!",
                    Toast.LENGTH_SHORT
                ).show()
                // 기존 대기 콜백 제거
                pendingRunnable?.let { handler.removeCallbacks(it) }
                isWaitingForTouch = false
                // 1초 뒤에 다음 라운드 자동 시작
                handler.postDelayed({ startRound() }, 1000L)
            }
        }

        btnPause.setOnClickListener {
            SoundEffectManager.playClick(this)
            PauseMenuFragment().show(supportFragmentManager, "PauseMenuFragment")
        }
    }

    override fun onResume() {
        super.onResume()
        // 반응 속도 테스트 화면에서는 배경음악 완전 중단
        BgmManager.stopBgm()
    }

    override fun onPause() {
        super.onPause()
        // 남은 콜백 모두 제거
        pendingRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun startRound() {
        tvInfo.text = "준비하세요."
        mainLayout.setBackgroundColor(android.graphics.Color.WHITE)

        val delay = Random.nextLong(1500L, 3000L)
        pendingRunnable = Runnable {
            tvInfo.text = "터치하세요!"
            mainLayout.setBackgroundColor(android.graphics.Color.GREEN)
            startTime = System.currentTimeMillis()
            isWaitingForTouch = true
        }
        handler.postDelayed(pendingRunnable!!, delay)
    }

    private fun showResult() {
        val avgReaction = reactionTimes.average().toInt()
        val finalScore = 10000 - avgReaction

        // Firebase로 점수 업로드
        val nickname = SharedPrefManager.getNickname(this)
        FirebaseManager.uploadScore(GameTypes.REACTION, nickname, finalScore)

        val resultDialog = GameResultFragment.newInstance(finalScore, "Reaction")
        resultDialog.setOnResultActionListener(object : GameResultFragment.ResultActionListener {
            override fun onRetry() {
                reactionTimes.clear()
                round = 0
                startRound()
            }

            override fun onQuit() {
                startActivity(
                    Intent(this@ReactionTestActivity, GameSelectActivity::class.java)
                        .apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
                )
                finish()
            }
        })
        resultDialog.show(supportFragmentManager, "GameResultFragment")
    }

    override fun onResumeGame() = Unit

    override fun onRetryGame() {
        reactionTimes.clear()
        round = 0
        startRound()
    }

    override fun onQuitGame() {
        startActivity(
            Intent(this, GameSelectActivity::class.java)
                .apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
        )
        finish()
    }
}