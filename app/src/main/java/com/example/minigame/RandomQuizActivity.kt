package com.example.minigame

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RandomQuizActivity : AppCompatActivity(), PauseMenuFragment.PauseMenuListener {

    private lateinit var tvQuestion: TextView
    private lateinit var btnOption1: Button
    private lateinit var btnOption2: Button
    private lateinit var btnOption3: Button
    private lateinit var btnOption4: Button
    private lateinit var tvScore: TextView
    private lateinit var tvTimer: TextView
    private lateinit var btnPause: Button

    private val questionList = mutableListOf<Question>()
    private var currentIndex = 0
    private var score = 0
    private var currentTimer: CountDownTimer? = null
    private var currentStartTime = 0L

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

        btnPause.setOnClickListener {
            SoundEffectManager.playClick(this)
            val pauseMenu = PauseMenuFragment()
            pauseMenu.show(supportFragmentManager, "PauseMenuFragment")
        }

        loadQuestions()
        startNextQuestion()
    }

    private fun loadQuestions() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = triviaApi.getQuestions()
                val fetched = response.results.map { it.toQuestion() }

                withContext(Dispatchers.Main) {
                    questionList.clear()
                    questionList.addAll(fetched)
                    startNextQuestion()
                }
            } catch (e: Exception) {
                Log.e("Quiz", "문제 로딩 실패", e)
            }
        }
    }


    private fun startNextQuestion() {
        if (currentIndex >= questionList.size) {
            showResult()
            return
        }

        val q = questionList[currentIndex]
        tvQuestion.text = q.question
        btnOption1.text = q.options[0]
        btnOption2.text = q.options[1]
        btnOption3.text = q.options[2]
        btnOption4.text = q.options[3]
        tvTimer.text = "10"

        val buttons = listOf(btnOption1, btnOption2, btnOption3, btnOption4)

        buttons.forEachIndexed { index, btn ->
            btn.isEnabled = true
            btn.setOnClickListener {
                SoundEffectManager.playClick(this)

                currentTimer?.cancel()
                val elapsed = System.currentTimeMillis() - currentStartTime
                val bonus = ((10000 - elapsed) / 1000).toInt()
                if (index == q.correctIndex) {
                    score += 100 + bonus
                    Toast.makeText(this, "정답! +${100 + bonus}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "오답!", Toast.LENGTH_SHORT).show()
                }
                currentIndex++
                startNextQuestion()
            }
        }

        currentStartTime = System.currentTimeMillis()
        currentTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.text = "${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                Toast.makeText(this@RandomQuizActivity, "시간 초과!", Toast.LENGTH_SHORT).show()
                currentIndex++
                startNextQuestion()
            }
        }.start()
    }

    private fun showResult() {
        saveRanking("Quiz", score)

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("게임 종료")
        builder.setMessage("최종 점수: $score\n다시 도전하시겠습니까?")
        builder.setPositiveButton("다시 시작") { _, _ ->
            score = 0
            currentIndex = 0
            questionList.clear()
            loadQuestions()
            startNextQuestion()
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
        currentTimer?.cancel()
    }

    override fun onResumeGame() {
        // 필요 시 타이머 재개 로직 추가
    }

    override fun onRetryGame() {
        score = 0
        currentIndex = 0
        questionList.clear()
        loadQuestions()
        startNextQuestion()
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

    private val triviaApi: TriviaApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://opentdb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TriviaApi::class.java)
    }
}