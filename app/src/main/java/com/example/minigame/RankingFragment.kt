package com.example.minigame

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class RankingFragment : DialogFragment() {

    private lateinit var tvRankingTitle: TextView
    private lateinit var tvRankingList: TextView
    private lateinit var btnShare: Button
    private lateinit var profileImageView: ImageView

    companion object {
        private const val ARG_GAME_TITLE = "game_title"

        fun newInstance(gameTitle: String): RankingFragment {
            val fragment = RankingFragment()
            val args = Bundle()
            args.putString(ARG_GAME_TITLE, gameTitle)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ranking, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvRankingTitle = view.findViewById(R.id.tvRankingTitle)
        tvRankingList = view.findViewById(R.id.tvRankingList)
        btnShare = view.findViewById(R.id.btnShare)
        profileImageView = view.findViewById(R.id.profileImageView)

        SharedPrefManager.getProfileImageUri(requireContext())?.let { uriString ->
            Glide.with(this)
                .load(Uri.parse(uriString))
                .into(profileImageView)
        }

        val gameKey = arguments?.getString(ARG_GAME_TITLE) ?: GameTypes.QUIZ

        val displayName = when (gameKey) {
            GameTypes.QUIZ      -> "랜덤 퀴즈"
            GameTypes.CARD      -> "카드 게임"
            GameTypes.REACTION  -> "반응 속도 테스트"
            else                -> gameKey
        }
        tvRankingTitle.text = "$displayName 랭킹"

        FirebaseFirestore.getInstance()
            .collection("scores")
            .whereEqualTo("gameType", gameKey) // 정확히 일치해야 조회됨
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val formatted = querySnapshot.documents.mapIndexed { index, doc ->
                    val nickname = doc.getString("nickname") ?: ""
                    val score = doc.getLong("score")?.toInt() ?: 0
                    "${index + 1}등: $nickname - ${score}점"
                }.joinToString("\n")

                tvRankingList.text = if (formatted.isNotEmpty()) {
                    formatted
                } else {
                    "아직 기록이 없습니다."
                }
            }
            .addOnFailureListener { exception ->
                Log.e("RankingFragment", "Firestore 랭킹 조회 실패", exception)
                tvRankingList.text = "랭킹을 불러오는 중 오류가 발생했습니다."
            }

        btnShare.setOnClickListener {
            SoundEffectManager.playClick(requireContext())
            val shareText = tvRankingList.text.toString()
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, "공유할 앱 선택"))
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}