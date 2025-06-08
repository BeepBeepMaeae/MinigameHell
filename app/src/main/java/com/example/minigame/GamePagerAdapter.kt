package com.example.minigame

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class GamePagerAdapter(private val gameList: List<GameInfo>) :
    RecyclerView.Adapter<GamePagerAdapter.GameViewHolder>() {

    inner class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgThumbnail: ImageView = itemView.findViewById(R.id.imgThumbnail)
        val tvGameTitle: TextView = itemView.findViewById(R.id.tvGameTitle)
        val btnStartGame: Button = itemView.findViewById(R.id.btnStartGame)
        val btnRanking: Button = itemView.findViewById(R.id.btnRanking)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game_page, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = gameList[position]

        holder.imgThumbnail.setImageResource(game.thumbnailResId)
        holder.tvGameTitle.text = game.title

        holder.btnStartGame.setOnClickListener {
            SoundEffectManager.playClick(holder.itemView.context)

            val context = holder.itemView.context
            val intent = when (game.title) {
                "카드 게임" -> Intent(context, CardGameActivity::class.java)
                "반응 속도 테스트" -> Intent(context, ReactionTestActivity::class.java)
                "랜덤 퀴즈" -> Intent(context, RandomQuizActivity::class.java)
                else -> null
            }
            intent?.let {
                context.startActivity(it)
            }
        }

        // GamePagerAdapter.kt
        holder.btnRanking.setOnClickListener {
            SoundEffectManager.playClick(holder.itemView.context)

            // 한글 표시명이 아니라 internal key를 매핑
            val gameKey = when (game.title) {
                "랜덤 퀴즈"          -> GameTypes.QUIZ      // "Quiz"
                "카드 게임"          -> GameTypes.CARD      // "CardGame"
                "반응 속도 테스트"   -> GameTypes.REACTION  // "ReactionTest"
                else                 -> GameTypes.QUIZ
            }

            val fragment = RankingFragment.newInstance(gameKey)
            fragment.show(
                (holder.itemView.context as AppCompatActivity).supportFragmentManager,
                "RankingFragment"
            )
        }
    }

    override fun getItemCount(): Int = gameList.size
}