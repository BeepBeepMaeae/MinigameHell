package com.example.minigame

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlin.random.Random

class ReactionTestActivity : AppCompatActivity(), PauseMenuFragment.PauseMenuListener {

    private lateinit var tvInfo: TextView
    private lateinit var btnPause: Button
    private lateinit var mainLayout: View
    private lateinit var profileImageView: ImageView

    private var isWaitingForTouch = false
    private var isLocked = false
    private var startTime = 0L
    private val attemptScores = mutableListOf<Int>()
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

        SharedPrefManager.getProfileImageUri(this)?.let {
            Glide.with(this).load(Uri.parse(it)).into(profileImageView)
        }

        startRound()

        mainLayout.setOnClickListener {
            if (isLocked) return@setOnClickListener

            if (isWaitingForTouch) {
                SoundEffectManager.playCorrect(this)
                val reactionTime = System.currentTimeMillis() - startTime
                val scoreForAttempt = 200 - (reactionTime / 10).toInt()
                attemptScores.add(scoreForAttempt)
                isWaitingForTouch = false
                round++
                nextOrFinish()
            } else {
                SoundEffectManager.playWrong(this)
                tvInfo.text = "너무 빨리 누르셨습니다!"
                mainLayout.setBackgroundColor(Color.RED)
                attemptScores.add(0)
                pendingRunnable?.let { handler.removeCallbacks(it) }
                isWaitingForTouch = false
                isLocked = true
                handler.postDelayed({
                    isLocked = false
                    round++
                    nextOrFinish()
                }, 3000L)
            }
        }

        btnPause.setOnClickListener {
            pendingRunnable?.let { handler.removeCallbacks(it) }
            SoundEffectManager.playClick(this)
            PauseMenuFragment().show(supportFragmentManager, "PauseMenuFragment")
        }
    }

    override fun onResume() {
        super.onResume()
        BgmManager.stopBgm()
    }

    override fun onPause() {
        super.onPause()
        pendingRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun startRound() {
        tvInfo.text = "준비하세요."
        mainLayout.setBackgroundColor(Color.WHITE)
        isWaitingForTouch = false
        isLocked = false
        pendingRunnable?.let { handler.removeCallbacks(it) }

        val delay = Random.nextLong(1500L, 5500L)
        pendingRunnable = Runnable {
            tvInfo.text = "터치하세요!"
            mainLayout.setBackgroundColor(Color.GREEN)
            startTime = System.currentTimeMillis()
            isWaitingForTouch = true
        }
        handler.postDelayed(pendingRunnable!!, delay)
    }

    private fun nextOrFinish() {
        if (round >= 5) showResult()
        else startRound()
    }

    private fun showResult() {
        val finalScore = attemptScores.sum()
        val nickname = SharedPrefManager.getNickname(this)
        FirebaseManager.uploadScore(GameTypes.REACTION, nickname, finalScore)

        GameResultFragment.newInstance(finalScore, GameTypes.REACTION).apply {
            setOnResultActionListener(object : GameResultFragment.ResultActionListener {
                override fun onRetry() = onRetryGame()
                override fun onQuit() {
                    startActivity(
                        Intent(this@ReactionTestActivity, GameSelectActivity::class.java)
                            .apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
                    )
                    finish()
                }
            })
        }.show(supportFragmentManager, "GameResultFragment")
    }

    // PauseMenuFragment callbacks
    override fun onResumeGame() = Unit

    override fun onRetryGame() {
        pendingRunnable?.let { handler.removeCallbacks(it) }
        attemptScores.clear()
        round = 0
        isWaitingForTouch = false
        isLocked = false
        tvInfo.text = "준비하세요."
        mainLayout.setBackgroundColor(Color.WHITE)
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