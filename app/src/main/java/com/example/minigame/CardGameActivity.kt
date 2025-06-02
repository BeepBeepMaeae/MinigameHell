package com.example.minigame

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
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
        withContext(Dispatchers.IO) {
            try {
                val response = api.newDeck()
                if (response.success) {
                    deckId = response.deck_id
                    Log.d("CardGame", "Deck initialized: $deckId")
                }
            } catch (e: Exception) {
                Log.e("CardGame", "Deck initialization failed", e)
            }
        }
    }

    private fun drawCard() {
        val id = deckId ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = api.drawCard(id, 1)
                val card = response.cards.firstOrNull()
                val remaining = response.remaining
                withContext(Dispatchers.Main) {
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
        saveRanking("CardGame", score)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("게임 종료")
        builder.setMessage("최종 점수: $score\n다시 도전하시겠습니까?")
        builder.setPositiveButton("다시 시작") { _, _ ->
            score = 0
            drawCount = 0
            lifecycleScope.launch {
                initializeDeck()
            }
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

    private fun saveRanking(gameType: String, score: Int) {
        val nickname = SharedPrefManager.getNickname(this)
        val ranking = RankingEntity(gameType = gameType, nickname = nickname, score = score)

        val db = RankingDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            db.rankingDao().insertRanking(ranking)
        }
    }

    override fun onResumeGame() {
        // 추가 로직 없음
    }

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