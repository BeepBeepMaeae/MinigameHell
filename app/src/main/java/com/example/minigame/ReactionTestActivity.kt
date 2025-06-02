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
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
            }
        }

        btnPause.setOnClickListener {
            SoundEffectManager.playClick(this)
            val pauseMenu = PauseMenuFragment()
            pauseMenu.show(supportFragmentManager, "PauseMenuFragment")
        }
    }

    private fun startRound() {
        tvInfo.text = "준비하세요..."
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

        // 로컬 DB 저장 제거 → Firebase만 업로드
        val nickname = SharedPrefManager.getNickname(this)
        FirebaseManager.uploadScore("Reaction", nickname, finalScore)

        val resultDialog = GameResultFragment.newInstance(finalScore, "Reaction")
        resultDialog.setOnResultActionListener(object : GameResultFragment.ResultActionListener {
            override fun onRetry() {
                reactionTimes.clear()
                round = 0
                startRound()
            }

            override fun onQuit() {
                val intent = Intent(this@ReactionTestActivity, GameSelectActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        })
        resultDialog.show(supportFragmentManager, "GameResultFragment")
    }

    override fun onPause() {
        super.onPause()
        pendingRunnable?.let { handler.removeCallbacks(it) }
    }

    override fun onResumeGame() {}

    override fun onRetryGame() {
        reactionTimes.clear()
        round = 0
        startRound()
    }

    override fun onQuitGame() {
        val intent = Intent(this, GameSelectActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
}