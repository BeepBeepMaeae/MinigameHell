package com.example.minigame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class RankingFragment : DialogFragment() {

    private lateinit var tvRankingTitle: TextView
    private lateinit var tvRankingList: TextView

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

        val gameTitle = arguments?.getString(ARG_GAME_TITLE) ?: "Unknown"
        tvRankingTitle.text = "$gameTitle 랭킹"

        // (임시) 더미 데이터 출력
        val dummyData = listOf(
            "1등: Player1 - 5000점",
            "2등: Player2 - 4200점",
            "3등: Player3 - 3900점",
            "4등: Player4 - 3000점",
            "5등: Player5 - 2800점"
        )

        tvRankingList.text = dummyData.joinToString("\n")
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}