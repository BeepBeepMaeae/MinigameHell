package com.example.minigame

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FirebaseFirestore

class GameResultFragment : DialogFragment() {

    interface ResultActionListener {
        fun onRetry()
        fun onQuit()
    }

    private var listener: ResultActionListener? = null

    fun setOnResultActionListener(l: ResultActionListener) {
        listener = l
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvResult = view.findViewById<TextView>(R.id.tvResult)
        val btnRetry = view.findViewById<Button>(R.id.btnRetry)
        val btnQuit = view.findViewById<Button>(R.id.btnQuit)
        val btnShare = view.findViewById<Button>(R.id.btnShare)

        val score = arguments?.getInt(ARG_SCORE) ?: 0
        val game = arguments?.getString(ARG_GAME) ?: "?"
        tvResult.text = "$game 게임 결과\n점수: $score"

        SoundEffectManager.playResult(requireContext())

        btnRetry.setOnClickListener {
            val score = arguments?.getInt(ARG_SCORE) ?: 0
            val game = arguments?.getString(ARG_GAME) ?: "?"

            val gameKey = when (game) {
                "퀴즈" -> GameTypes.QUIZ
                "카드 게임" -> GameTypes.CARD
                "반응속도" -> GameTypes.REACTION
                else -> GameTypes.QUIZ
            }

            val nickname = SharedPrefManager.getNickname(requireContext()).ifBlank { "Player" }
            val timestamp = com.google.firebase.Timestamp.now()

            val scoreData = mapOf(
                "gameType" to gameKey,
                "nickname" to nickname,
                "score" to score,
                "timestamp" to timestamp
            )

            FirebaseFirestore.getInstance()
                .collection("scores")
                .add(scoreData)
                .addOnSuccessListener {
                    // 저장 성공 후 게임 재시작
                    listener?.onRetry()
                    dismiss()
                }
                .addOnFailureListener {
                    // 실패해도 일단 진행
                    listener?.onRetry()
                    dismiss()
                }
        }

        btnQuit.setOnClickListener {
            listener?.onQuit()

            val game = arguments?.getString(ARG_GAME) ?: "?"
            val gameKey = when (game) {
                "퀴즈" -> GameTypes.QUIZ
                "카드 게임" -> GameTypes.CARD
                "반응속도" -> GameTypes.REACTION
                else -> GameTypes.QUIZ // 기본값
            }
            dismiss()
        }


        btnShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "$game 게임에서 ${score}점을 달성했어요! 도전해보세요!")
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

    companion object {
        private const val ARG_SCORE = "score"
        private const val ARG_GAME = "game"

        fun newInstance(score: Int, game: String): GameResultFragment {
            val fragment = GameResultFragment()
            val args = Bundle()
            args.putInt(ARG_SCORE, score)
            args.putString(ARG_GAME, game)
            fragment.arguments = args
            return fragment
        }
    }
}