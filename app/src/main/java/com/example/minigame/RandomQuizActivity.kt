package com.example.minigame

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
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
    private var lastEmotion: String? = null

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
                    lastEmotion = null
                    timeLimit = 15000L
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

        // 다음 문제 준비
        val q = questionList[currentIndex]
        tvScore.text    = "점수: $score"
        tvQuestion.text = q.question

        val buttons = listOf(btnOption1, btnOption2, btnOption3, btnOption4)
        buttons.forEachIndexed { idx, btn ->
            btn.text               = q.options[idx]
            btn.isEnabled          = true
            btn.alpha              = 1f
            btn.backgroundTintList = ContextCompat.getColorStateList(this, R.color.orange)
            btn.setTextColor(Color.WHITE)
        }

        // 웃는 얼굴이 인식되어 lastEmotion이 "happy"라면 오답 버튼 하나를 투명화
        if (lastEmotion?.lowercase() == "happy") {
            val wrongButtons = buttons.filterIndexed { idx, _ -> idx != q.correctIndex }
            if (wrongButtons.isNotEmpty()) {
                wrongButtons.random().apply {
                    alpha = 0.3f
                    isEnabled = false
                }
            }
            // 한 번만 적용
            lastEmotion = null
        }

        // 각 선택지에 클릭 리스너 설정
        buttons.forEachIndexed { idx, btn ->
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
                }, 3000L)
            }
        }

        // 타이머 시작
        tvTimer.text = (timeLimit / 1000).toString()
        currentTimer = object : CountDownTimer(timeLimit, 1000) {
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
        Intent(this, CameraCaptureActivity::class.java).also {
            startActivityForResult(it, REQUEST_FACE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_FACE_CAPTURE && resultCode == RESULT_OK && data != null) {
            if (!EmotionAnalyzer.isInitialized()) {
                EmotionAnalyzer.init(this)
            }

            val byteArray = data.getByteArrayExtra("captured_image")
            val bitmap = byteArray?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            if (bitmap != null) {
                val emotion = EmotionAnalyzer.predict(bitmap)
                Log.d("Emotion", "분석 결과: $emotion")

                // 감정에 따른 시간 제한 조정
                adjustDifficultyByEmotion(emotion)
                // 웃는(happy) 얼굴이면 다음 문제에서 오답 숨기기
                lastEmotion = emotion

            } else {
                Log.e("onActivityResult", "bitmap decode 실패 또는 이미지 데이터 없음")
            }
            // 얼굴 인식 뒤 바로 다음 문제 표시
            startNextQuestion()
        }
    }

    private fun adjustDifficultyByEmotion(emotion: String) {
        when (emotion.lowercase()) {
            "angry" -> {
                timeLimit = 20000L
                Toast.makeText(this, "화난 얼굴! 제한시간 20초!", Toast.LENGTH_SHORT).show()
            }
            "happy" -> {
                timeLimit = 30000L
                Toast.makeText(this, "기분 좋네요! 제한시간 30초!", Toast.LENGTH_SHORT).show()
            }
            "neutral" -> {
                timeLimit = 15000L
                Toast.makeText(this, "무표정이네요. 제한시간 15초!", Toast.LENGTH_SHORT).show()
            }
            "sad" -> {
                timeLimit = 18000L
                Toast.makeText(this, "슬퍼 보이네요. 제한시간 18초!", Toast.LENGTH_SHORT).show()
            }
            else -> {
                timeLimit = 15000L
                Toast.makeText(this, "감정 인식 실패. 기본 시간 15초.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showResult() {
        currentTimer?.cancel()

        FirebaseManager.uploadScore(
            gameType = GameTypes.QUIZ,
            nickname = SharedPrefManager.getNickname(this),
            score    = score
        )

        GameResultFragment.newInstance(score, GameTypes.QUIZ).apply {
            setOnResultActionListener(object : GameResultFragment.ResultActionListener {
                override fun onRetry() {
                    loadQuestions()
                }
                override fun onQuit() {
                    startActivity(
                        Intent(this@RandomQuizActivity, GameSelectActivity::class.java)
                            .apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
                    )
                    finish()
                }
            })
        }.show(supportFragmentManager, "GameResultFragment")
    }

    override fun onResumeGame() = Unit

    override fun onRetryGame() {
        currentTimer?.cancel()
        loadQuestions()
    }

    override fun onQuitGame() {
        startActivity(
            Intent(this, GameSelectActivity::class.java)
                .apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
        )
        finish()
    }
}