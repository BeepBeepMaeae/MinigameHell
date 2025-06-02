package com.example.minigame

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

class CardGameActivity : AppCompatActivity(), PauseMenuFragment.PauseMenuListener {

    private lateinit var cardImageView: ImageView
    private lateinit var drawButton: Button
    private lateinit var remainingTextView: TextView
    private lateinit var pauseButton: Button
    private lateinit var profileImageView: ImageView

    private val api by lazy {
        Retrofit.Builder()
            .baseUrl("https://deckofcardsapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeckOfCardsApi::class.java)
    }

    private var deckId: String? = null
    private var score: Int = 0
    private var drawCount: Int = 0
    private val maxDraws = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_game)

        cardImageView = findViewById(R.id.cardImageView)
        drawButton = findViewById(R.id.drawButton)
        remainingTextView = findViewById(R.id.remainingTextView)
        pauseButton = findViewById(R.id.btnPause)
        profileImageView = findViewById(R.id.profileImageView)

        SharedPrefManager.getProfileImageUri(this)?.let { uriString ->
            Glide.with(this).load(Uri.parse(uriString)).into(profileImageView)
        }

        drawButton.setOnClickListener {
            drawCard()
        }

        pauseButton.setOnClickListener {
            SoundEffectManager.playClick(this)
            val pauseMenu = PauseMenuFragment()
            pauseMenu.show(supportFragmentManager, "PauseMenuFragment")
        }

        lifecycleScope.launch {
            initializeDeck()
        }
    }

    private suspend fun initializeDeck() {
        withContext(kotlinx.coroutines.Dispatchers.IO) {
            val response = api.newDeck()
                if (response.success) {
                    deckId = response.deck_id
                    Log.d("CardGame", "Deck initialized: $deckId")
                } //일단 오류 나서 try-catch문 없앰. 참고 부탁합니다 ~
        }
    }

    private fun drawCard() {
        val id = deckId ?: return

        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val response = api.drawCard(id, 1)
                val card = response.cards.firstOrNull()
                val remaining = response.remaining
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    card?.let {
                        Glide.with(this@CardGameActivity)
                            .load(it.image)
                            .into(cardImageView)
                        score += 100
                        drawCount++
                        remainingTextView.text = "점수: $score / 남은 카드 수: $remaining"

                        if (drawCount >= maxDraws) {
                            showResult()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CardGame", "Draw card failed", e)
            }
        }
    }

    private fun showResult() {
        // Firebase에만 업로드 (로컬 DB 저장 제거)
        val nickname = SharedPrefManager.getNickname(this)
        FirebaseManager.uploadScore("CardGame", nickname, score)

        val resultDialog = GameResultFragment.newInstance(score, "CardGame")
        resultDialog.setOnResultActionListener(object : GameResultFragment.ResultActionListener {
            override fun onRetry() {
                score = 0
                drawCount = 0
                lifecycleScope.launch {
                    initializeDeck()
                }
            }

            override fun onQuit() {
                val intent = Intent(this@CardGameActivity, GameSelectActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        })
        resultDialog.show(supportFragmentManager, "GameResultFragment")
    }

    override fun onResumeGame() {}

    override fun onRetryGame() {
        score = 0
        drawCount = 0
        lifecycleScope.launch {
            initializeDeck()
        }
    }

    override fun onQuitGame() {
        val intent = Intent(this, GameSelectActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
}

interface DeckOfCardsApi {
    @GET("api/deck/new/shuffle/?deck_count=1")
    suspend fun newDeck(): DeckResponse

    @GET("api/deck/{deck_id}/draw/?count=1")
    suspend fun drawCard(
        @Path("deck_id") deckId: String,
        @Query("count") count: Int
    ): DrawResponse
}

data class DeckResponse(
    val success: Boolean,
    val deck_id: String,
    val remaining: Int,
    val shuffled: Boolean
)

data class DrawResponse(
    val success: Boolean,
    val cards: List<Card>,
    val deck_id: String,
    val remaining: Int
)

data class Card(
    val image: String,
    val value: String,
    val suit: String,
    val code: String
)