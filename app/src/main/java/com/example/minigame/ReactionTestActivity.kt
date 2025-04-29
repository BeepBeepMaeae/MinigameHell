package com.example.minigame

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog

class ReactionTestActivity : AppCompatActivity(), PauseMenuFragment.PauseMenuListener {

    private lateinit var tvInfo: TextView
    private lateinit var btnPause: Button
    private lateinit var mainLayout: View

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
        mainLayout.setBackgroundColor(Color.WHITE)

        val delay = Random.nextLong(1500L, 3000L) // 1.5초~3초 랜덤 대기
        pendingRunnable = Runnable {
            tvInfo.text = "터치하세요!"
            mainLayout.setBackgroundColor(Color.GREEN)
            startTime = System.currentTimeMillis()
            isWaitingForTouch = true
        }
        handler.postDelayed(pendingRunnable!!, delay)
    }

    private fun showResult() {
        val avgReaction = reactionTimes.average().toInt()
        val finalScore = 10000 - avgReaction

        saveRanking("Reaction", finalScore)

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("게임 종료")
        builder.setMessage("평균 반응속도: ${avgReaction}ms\n점수: $finalScore\n다시 도전하시겠습니까?")
        builder.setPositiveButton("다시 시작") { _, _ ->
            reactionTimes.clear()
            round = 0
            startRound()
        }
        builder.setNegativeButton("나가기") { _, _ ->
            val intent = Intent(this, GameSelectActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
        builder.setCancelable(false)
        builder.show()
    }


    override fun onPause() {
        super.onPause()
        pendingRunnable?.let { handler.removeCallbacks(it) }
    }

    override fun onTouchEvent(event: android.view.MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    override fun onResumeGame() {
        // 아무것도 안 함
    }

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

    private fun saveRanking(gameType: String, score: Int) {
        val nickname = SharedPrefManager.getNickname(this)
        val ranking = RankingEntity(gameType = gameType, nickname = nickname, score = score)

        val db = RankingDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            db.rankingDao().insertRanking(ranking)
        }
    }
}