package com.example.minigame

import android.content.Intent
import android.graphics.Color
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class CardGameActivity : AppCompatActivity(), PauseMenuFragment.PauseMenuListener {

    private lateinit var drawButton: Button
    private lateinit var stopButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var pauseButton: Button
    private lateinit var profileImageView: ImageView
    private lateinit var drawnCardsContainer: FlexboxLayout

    private val api by lazy {
        Retrofit.Builder()
            .baseUrl("https://deckofcardsapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeckOfCardsApi::class.java)
    }

    private var deckId: String? = null
    private var totalScore = 0
    private var triesLeft = 5
    private var currentSum = 0
    private var aceCount = 0
    private var deckRemaining = 0

    override fun onResume() {
        super.onResume()
        BgmManager.startBgm(this, R.raw.cardgame_bgm)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_game)

        drawButton = findViewById(R.id.drawButton)
        stopButton = findViewById(R.id.stopButton)
        statusTextView = findViewById(R.id.remainingTextView)
        pauseButton = findViewById(R.id.btnPause)
        profileImageView = findViewById(R.id.profileImageView)
        drawnCardsContainer = findViewById(R.id.drawnCardsContainer)

        SharedPrefManager.getProfileImageUri(this)?.let {
            Glide.with(this).load(Uri.parse(it)).into(profileImageView)
        }

        drawButton.setOnClickListener {
            SoundEffectManager.playCardDraw(this)
            drawCard()
        }
        stopButton.setOnClickListener {
            SoundEffectManager.playClick(this)
            stopCurrentTry()
        }
        pauseButton.setOnClickListener {
            SoundEffectManager.playClick(this)
            PauseMenuFragment().show(supportFragmentManager, "PauseMenuFragment")
        }

        lifecycleScope.launch { initializeDeck() }
        updateStatus()
    }

    private suspend fun initializeDeck() = withContext(Dispatchers.IO) {
        val response = api.newDeck()
        if (response.success) {
            deckId = response.deck_id
            deckRemaining = response.remaining
        }
        withContext(Dispatchers.Main) { updateStatus() }
    }

    private fun drawCard() {
        val wasStopAvailable = currentSum in 12..21
        val id = deckId ?: return
        drawButton.isEnabled = false
        stopButton.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = api.drawCard(id, 1)
                val card = response.cards.firstOrNull()
                val remaining = response.remaining
                withContext(Dispatchers.Main) {
                    deckRemaining = remaining
                    card?.let {
                        val miniCard = ImageView(this@CardGameActivity).apply {
                            layoutParams = FlexboxLayout.LayoutParams( dpToPx(80), dpToPx(120) )
                                .apply { setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4)) }
                            setImageResource(R.drawable.card_back)
                        }
                        drawnCardsContainer.addView(miniCard)

                        val alphaDur = if (wasStopAvailable) 200L else 300L
                        val flipDur = if (wasStopAvailable) 150L else 200L
                        if (wasStopAvailable) miniCard.setColorFilter(Color.argb(60,255,0,0))

                        miniCard.animate().alpha(0.5f).setDuration(alphaDur).withEndAction {
                            miniCard.animate().alpha(1f).setDuration(alphaDur).start()
                        }.start()

                        miniCard.animate().rotationY(90f).setDuration(flipDur).withEndAction {
                            SoundEffectManager.playEffect(this@CardGameActivity, R.raw.card_flip_sound)
                            Glide.with(this@CardGameActivity).load(it.image).into(miniCard)
                            miniCard.rotationY = -90f
                            miniCard.clearColorFilter()
                            miniCard.animate().rotationY(0f).setDuration(flipDur).withEndAction {
                                // 카드 값 계산
                                val value = when (it.value) {
                                    "ACE" -> { aceCount++; 11 }
                                    "JACK","QUEEN","KING" -> 10
                                    else -> it.value.toIntOrNull() ?: 0
                                }
                                currentSum += value
                                while (currentSum>21 && aceCount>0) {
                                    currentSum -= 10; aceCount--
                                }
                                updateStatus()

                                if (currentSum > 21) {
                                    // 플립 애니메이션 후, 버스트 → 게임 결과 순서 보장
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        SoundEffectManager.Bust(this@CardGameActivity)
                                        Toast.makeText(
                                            this@CardGameActivity,
                                            "버스트! 0점 처리",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        // 버스트 사운드가 재생된 후 결과 표시
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            endTry(0)
                                        }, 1000) // 버스트 사운드 재생 시간(밀리초)
                                    }, 500) // 플립 완료 후 살짝 대기
                                } else {
                                    drawButton.isEnabled = true
                                    stopButton.isEnabled = currentSum in 12..21
                                }

                                if (deckRemaining <= 0) {
                                    Toast.makeText(
                                        this@CardGameActivity,
                                        "덱 소진, 덱을 섞습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    lifecycleScope.launch { initializeDeck() }
                                }
                            }.start()
                        }.start()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { drawButton.isEnabled = true }
            }
        }
    }

    private fun stopCurrentTry() {
        if (currentSum in 12..21) {
            totalScore += currentSum
            Toast.makeText(this, "$currentSum 점 획득!", Toast.LENGTH_SHORT).show()
            endTry(if (currentSum==21) 1 else 0)
        } else {
            Toast.makeText(this, "12 이상부터 멈출 수 있습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun endTry(extraTry: Int) {
        triesLeft = triesLeft - 1 + extraTry
        currentSum = 0; aceCount = 0
        drawnCardsContainer.removeAllViews()
        drawButton.isEnabled = true; stopButton.isEnabled = false
        if (triesLeft <= 0) showResult() else updateStatus()
    }

    private fun showResult() {
        FirebaseManager.uploadScore("CardGame", SharedPrefManager.getNickname(this), totalScore)
        GameResultFragment.newInstance(totalScore, GameTypes.CARD).apply {
            setOnResultActionListener(object : GameResultFragment.ResultActionListener {
                override fun onRetry() {
                    totalScore=0; triesLeft=5; currentSum=0; aceCount=0
                    drawnCardsContainer.removeAllViews()
                    lifecycleScope.launch{ initializeDeck() }
                    updateStatus()
                }
                override fun onQuit() {
                    startActivity(
                        Intent(this@CardGameActivity, GameSelectActivity::class.java)
                            .apply{ addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
                    )
                    finish()
                }
            })
        }.show(supportFragmentManager, "GameResultFragment")
    }

    private fun updateStatus() {
        statusTextView.text = "누적 점수: $totalScore\n남은 시도: $triesLeft\n현재 합계: $currentSum\n덱 남은 카드: $deckRemaining"

        // 버튼을 항상 활성 상태로 두되, 시각적으로만 구분
        stopButton.isEnabled = true
        stopButton.alpha = if (currentSum in 12..21) 1f else 0.5f
    }

    private fun dpToPx(dp:Int) = (dp * resources.displayMetrics.density).toInt()

    override fun onResumeGame() = Unit
// CardGameActivity.kt 에서

    override fun onRetryGame() {
        // 1) 모든 상태값 초기화
        totalScore = 0
        triesLeft   = 5
        currentSum  = 0
        aceCount    = 0

        // 2) 화면에 그려진 카드 제거
        drawnCardsContainer.removeAllViews()

        // 3) 버튼 상태 초기화
        drawButton.isEnabled = true
        stopButton.isEnabled = false

        // 4) 덱 새로 초기화 & 상태 표시 갱신
        lifecycleScope.launch { initializeDeck() }
        updateStatus()
    }

    override fun onQuitGame() {
        startActivity(
            Intent(this, GameSelectActivity::class.java).apply{ addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
        )
        finish()
    }
}

interface DeckOfCardsApi {
    @GET("api/deck/new/shuffle/?deck_count=1") suspend fun newDeck(): DeckResponse
    @GET("api/deck/{deck_id}/draw/?count=1") suspend fun drawCard(
        @Path("deck_id") deckId:String, @Query("count") count:Int
    ): DrawResponse
}

data class DeckResponse(val success:Boolean, val deck_id:String, val remaining:Int, val shuffled:Boolean)
data class DrawResponse(val success:Boolean, val cards:List<Card>, val deck_id:String, val remaining:Int)
data class Card(val image:String, val value:String, val suit:String, val code:String)