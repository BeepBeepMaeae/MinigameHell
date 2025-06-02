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

class GameResultFragment : DialogFragment() {

    interface ResultActionListener {
        fun onRetry()
        fun onQuit()
    }

    private var listener: ResultActionListener? = null

    fun setOnResultActionListener(l: ResultActionListener) {
        listener = l
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

        btnRetry.setOnClickListener {
            listener?.onRetry()
            dismiss()
        }

        btnQuit.setOnClickListener {
            listener?.onQuit()
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