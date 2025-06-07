// RandomQuizActivity.kt
package com.example.minigame

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RandomQuizActivity : AppCompatActivity(), PauseMenuFragment.PauseMenuListener {

    private lateinit var tvQuestion: TextView
    private lateinit var btnOption1: AppCompatButton
    private lateinit var btnOption2: AppCompatButton
    private lateinit var btnOption3: AppCompatButton
    private lateinit var btnOption4: AppCompatButton
    private lateinit var tvScore: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnPause: AppCompatButton
    private lateinit var profileImageView: ImageView

    private val questionList = mutableListOf<Question>()
    private var currentIndex = 0
    private var score = 0
    private var currentTimer: CountDownTimer? = null

    private val triviaApi: TriviaApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://opentdb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TriviaApi::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_random_quiz)

        tvQuestion = findViewById(R.id.tvQuestion)
        btnOption1 = findViewById(R.id.btnOption1)
        btnOption2 = findViewById(R.id.btnOption2)
        btnOption3 = findViewById(R.id.btnOption3)
        btnOption4 = findViewById(R.id.btnOption4)
        tvScore = findViewById(R.id.tvScore)
        tvTimer = findViewById(R.id.tvTimer)
        btnPause = findViewById(R.id.btnPause)
        profileImageView = findViewById(R.id.profileImageView)

        SharedPrefManager.getProfileImageUri(this)?.let {
            Glide.with(this).load(Uri.parse(it)).into(profileImageView)
        }

        btnPause.setOnClickListener {
            // 일시정지에서도 타이머 멈추기
            currentTimer?.cancel()
            SoundEffectManager.playClick(this)
            PauseMenuFragment().show(supportFragmentManager, "PauseMenuFragment")
        }

        loadQuestions()
    }

    override fun onPause() {
        super.onPause()
        // 액티비티가 pause 상태가 되면 무조건 타이머 취소
        currentTimer?.cancel()
    }

    private fun loadQuestions() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = triviaApi.getQuestions()
                val fetched = response.results.map { it.toQuestion() }
                withContext(Dispatchers.Main) {
                    questionList.clear()
                    questionList.addAll(fetched)
                    score = 0
                    currentIndex = 0
                    startNextQuestion()
                }
            } catch (e: Exception) {
                Log.e("Quiz", "문제 로딩 실패", e)
            }
        }
    }

    private fun startNextQuestion() {
        currentTimer?.cancel()

        if (currentIndex >= questionList.size) {
            showResult()
            return
        }

        val q = questionList[currentIndex]
        tvScore.text = "점수: $score"
        tvQuestion.text = q.question

        btnOption1.text = q.options[0]
        btnOption2.text = q.options[1]
        btnOption3.text = q.options[2]
        btnOption4.text = q.options[3]

        tvTimer.text = "15"
        val buttons = listOf(btnOption1, btnOption2, btnOption3, btnOption4)
        val correctIndex = q.correctIndex
        val defaultTint = ContextCompat.getColorStateList(this, R.color.orange)

        buttons.forEach { btn ->
            btn.isEnabled = true
            btn.backgroundTintList = defaultTint
            btn.setTextColor(Color.WHITE)
            btn.setOnClickListener {
                currentTimer?.cancel()
                buttons.forEach { it.isEnabled = false }

                val secLeft = tvTimer.text.toString().toIntOrNull() ?: 0

                if (btn == buttons[correctIndex]) {
                    score += 1 + secLeft
                    btn.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    SoundEffectManager.playCorrect(this)
                } else {
                    btn.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    buttons[correctIndex].backgroundTintList =
                        ColorStateList.valueOf(Color.GREEN)
                    SoundEffectManager.playWrong(this)
                }

                tvScore.text = "점수: $score"

                Handler(Looper.getMainLooper()).postDelayed({
                    currentIndex++
                    startNextQuestion()
                }, 3000L)
            }
        }

        currentTimer = object : CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = millisUntilFinished / 1000
                tvTimer.text = "$sec"
                if (sec <= 5) {
                    SoundEffectManager.playTimerTick(this@RandomQuizActivity)
                    val root = window.decorView.findViewById<View>(android.R.id.content)
                    root.setBackgroundColor(Color.argb(80, 255, 0, 0))
                    Handler(Looper.getMainLooper()).postDelayed({
                        root.setBackgroundColor(Color.WHITE)
                    }, 100L)
                }
            }

            override fun onFinish() {
                buttons.forEach { it.isEnabled = false }
                buttons[correctIndex].backgroundTintList =
                    ColorStateList.valueOf(Color.GREEN)
                SoundEffectManager.playWrong(this@RandomQuizActivity)

                Handler(Looper.getMainLooper()).postDelayed({
                    currentIndex++
                    startNextQuestion()
                }, 3000L)
            }
        }.start()
    }

    private fun showResult() {
        currentTimer?.cancel()
        FirebaseManager.uploadScore("Quiz", SharedPrefManager.getNickname(this), score)

        GameResultFragment.newInstance(score, GameTypes.QUIZ).apply {
            setOnResultActionListener(object : GameResultFragment.ResultActionListener {
                override fun onRetry() = loadQuestions()
                override fun onQuit() {
                    // 중도 퇴실 시에도 타이머 취소
                    currentTimer?.cancel()
                    startActivity(Intent(this@RandomQuizActivity, GameSelectActivity::class.java)
                        .apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) })
                    finish()
                }
            })
        }.show(supportFragmentManager, "GameResultFragment")
    }

    override fun onResumeGame() {
        // “계속하기” 시 필요한 로직 추가 가능
    }

    override fun onRetryGame() {
        currentTimer?.cancel()
        loadQuestions()
    }

    override fun onQuitGame() {
        currentTimer?.cancel()
        startActivity(Intent(this, GameSelectActivity::class.java)
            .apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) })
        finish()
    }
}