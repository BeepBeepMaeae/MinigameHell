package com.example.minigame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvRankingTitle = view.findViewById(R.id.tvRankingTitle)
        tvRankingList = view.findViewById(R.id.tvRankingList)

        val gameTitle = arguments?.getString(ARG_GAME_TITLE) ?: "Unknown"
        tvRankingTitle.text = "$gameTitle 랭킹"

        val db = RankingDatabase.getDatabase(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            val topRankings = db.rankingDao().getTop5Rankings(gameTitle)
            val formatted = topRankings.mapIndexed { i, r ->
                "${i + 1}등: ${r.nickname} - ${r.score}점"
            }.joinToString("\n")

            tvRankingList.text = if (formatted.isNotEmpty()) formatted else "아직 기록이 없습니다."
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