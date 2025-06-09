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
import android.widget.Toast
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
    private var timeLimit = 15000L  // 기본 시간
    private val REQUEST_FACE_CAPTURE = 777


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

        tvQuestion       = findViewById(R.id.tvQuestion)
        btnOption1       = findViewById(R.id.btnOption1)
        btnOption2       = findViewById(R.id.btnOption2)
        btnOption3       = findViewById(R.id.btnOption3)
        btnOption4       = findViewById(R.id.btnOption4)
        tvScore          = findViewById(R.id.tvScore)
        tvTimer          = findViewById(R.id.tvTimer)
        btnPause         = findViewById(R.id.btnPause)
        profileImageView = findViewById(R.id.profileImageView)

        SharedPrefManager.getProfileImageUri(this)?.let {
            Glide.with(this).load(Uri.parse(it)).into(profileImageView)
        }

        btnPause.setOnClickListener {
            SoundEffectManager.playClick(this)
            // 일시정지해도 BGM·타이머는 멈추지 않음
            PauseMenuFragment().show(supportFragmentManager, "PauseMenuFragment")
        }

        loadQuestions()
    }

    override fun onResume() {
        super.onResume()
        BgmManager.startBgm(this, R.raw.quiz_bgm)
    }

    override fun onPause() {
        super.onPause()
        // 액티비티가 실제 백그라운드로 빠질 때만 정지
        currentTimer?.cancel()
        BgmManager.stopBgm()
    }

    private fun loadQuestions() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = triviaApi.getQuestions(amount = 10)
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
        tvScore.text    = "점수: $score"
        tvQuestion.text = q.question

        val buttons = listOf(btnOption1, btnOption2, btnOption3, btnOption4)
        buttons.forEachIndexed { idx, btn ->
            btn.text               = q.options[idx]
            btn.isEnabled          = true
            btn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.orange)
            btn.setTextColor(Color.WHITE)
            btn.setOnClickListener {
                currentTimer?.cancel()
                buttons.forEach { it.isEnabled = false }

                val secLeft = tvTimer.text.toString().toIntOrNull() ?: 0
                if (idx == q.correctIndex) {
                    score += 1 + secLeft
                    btn.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                    SoundEffectManager.playCorrect(this)
                } else {
                    btn.backgroundTintList = ColorStateList.valueOf(Color.RED)
                    buttons[q.correctIndex].backgroundTintList =
                        ColorStateList.valueOf(Color.GREEN)
                    SoundEffectManager.playWrong(this)
                }

                tvScore.text = "점수: $score"
                Handler(Looper.getMainLooper()).postDelayed({
                    currentIndex++
                    StartFaceCapture()
                    //startNextQuestion()
                }, 3000L)
            }
        }

        tvTimer.text = "15"
        currentTimer = object : CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = (millisUntilFinished / 1000).toInt()
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
                buttons[q.correctIndex].backgroundTintList =
                    ColorStateList.valueOf(Color.GREEN)
                SoundEffectManager.playWrong(this@RandomQuizActivity)
                Handler(Looper.getMainLooper()).postDelayed({
                    currentIndex++
                    startNextQuestion()
                }, 3000L)
            }
        }.start()
    }

    private fun StartFaceCapture() {
        val intent = Intent(this, CameraCaptureActivity::class.java)
        startActivityForResult(intent, REQUEST_FACE_CAPTURE)
    }

    private fun showResult() {
        currentTimer?.cancel()
        // BgmManager.stopBgm() 제거 → BGM은 계속 재생

        FirebaseManager.uploadScore(
            gameType = GameTypes.QUIZ,
            nickname = SharedPrefManager.getNickname(this),
            score    = score
        )

        GameResultFragment.newInstance(score, GameTypes.QUIZ).apply {
            setOnResultActionListener(object : GameResultFragment.ResultActionListener {
                override fun onRetry() {
                    // 다시하기 시 BGM을 멈추지 않고 바로 재시작
                    loadQuestions()
                }
                override fun onQuit() {
                    // 종료 시 finish() → onPause()에서 BGM 정지
                    startActivity(
                        Intent(this@RandomQuizActivity, GameSelectActivity::class.java)
                            .apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
                    )
                    finish()
                }
            })
        }.show(supportFragmentManager, "GameResultFragment")
    }


    // PauseMenuFragment 콜백
    override fun onResumeGame() = Unit

    override fun onRetryGame() {
        currentTimer?.cancel()
        // BGM 유지
        loadQuestions()
    }

    override fun onQuitGame() {
        startActivity(
            Intent(this, GameSelectActivity::class.java)
                .apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
        )
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_FACE_CAPTURE && resultCode == RESULT_OK) {
            val byteArray = data?.getByteArrayExtra("image")
            byteArray?.let {
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size)
                val emotion = EmotionAnalyzer.predict(bitmap)

                timeLimit = when (emotion) {
                    "happy" -> 7000L
                    "neutral" -> 10000L
                    "sad", "angry" -> 13000L
                    else -> 10000L
                }

                Toast.makeText(this, "감정: $emotion → 제한시간 ${timeLimit / 1000}초", Toast.LENGTH_SHORT).show()
            }

            // 감정 예측 끝났으니 다음 문제로
            startNextQuestion()
        }
    }

}