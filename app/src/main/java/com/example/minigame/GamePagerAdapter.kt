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
        val btnDescription: Button = itemView.findViewById(R.id.btnDescription)
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

        // 1) 게임 시작 버튼: Activity 호출
        holder.btnStartGame.setOnClickListener {
            SoundEffectManager.playClick(holder.itemView.context)
            val context = holder.itemView.context
            val intent = when (game.title) {
                "블랙 잭" -> Intent(context, CardGameActivity::class.java)
                "반응 속도 테스트" -> Intent(context, ReactionTestActivity::class.java)
                "랜덤 퀴즈" -> Intent(context, RandomQuizActivity::class.java)
                else -> null
            }
            intent?.let { context.startActivity(it) }
        }

        // 2) 설명 버튼: DialogFragment 띄우기
        holder.btnDescription.setOnClickListener {
            SoundEffectManager.playClick(holder.itemView.context)
            val imageRes = when (game.title) {
                "블랙 잭"           -> R.drawable.desc_cardgame
                "반응 속도 테스트"     -> R.drawable.desc_reaction
                "랜덤 퀴즈"           -> R.drawable.desc_quiz
                else                   -> R.drawable.desc_generic
            }
            GameDescriptionFragment
                .newInstance(imageRes)
                .show(
                    (holder.itemView.context as AppCompatActivity)
                        .supportFragmentManager,
                    "GameDescription"
                )
        }

        // 3) 순위 버튼: RankingFragment 띄우기
        holder.btnRanking.setOnClickListener {
            SoundEffectManager.playClick(holder.itemView.context)
            val gameKey = when (game.title) {
                "랜덤 퀴즈"        -> GameTypes.QUIZ
                "블랙 잭"        -> GameTypes.CARD
                "반응 속도 테스트" -> GameTypes.REACTION
                else               -> GameTypes.QUIZ
            }
            RankingFragment
                .newInstance(gameKey)
                .show(
                    (holder.itemView.context as AppCompatActivity)
                        .supportFragmentManager,
                    "RankingFragment"
                )
        }
    }

    override fun getItemCount(): Int = gameList.size
}