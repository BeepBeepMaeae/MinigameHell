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
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

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

        // View 참조 연결
        tvRankingTitle   = view.findViewById(R.id.tvRankingTitle)
        tvRankingList    = view.findViewById(R.id.tvRankingList)
        btnShare         = view.findViewById(R.id.btnShare)
        profileImageView = view.findViewById(R.id.profileImageView)

        // 1) 프로필 이미지 로드 (Shared Preferences에 저장된 URI가 있으면 Glide로 표시)
        SharedPrefManager.getProfileImageUri(requireContext())?.let { uriString ->
            Glide.with(this)
                .load(Uri.parse(uriString))
                .into(profileImageView)
        }

        // 2) 전달받은 게임 제목으로 타이틀 설정
        val gameTitle = arguments?.getString(ARG_GAME_TITLE) ?: "Unknown"
        tvRankingTitle.text = "$gameTitle 랭킹"

        // 3) Firestore에서 Top5 랭킹 조회
        val db = FirebaseFirestore.getInstance()
        db.collection("scores")
            .whereEqualTo("gameType", gameTitle)
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // 문서 목록을 순회하며 “1등: 닉네임 – 점수점” 형태로 문자열 생성
                val formatted = querySnapshot.documents.mapIndexed { index, doc ->
                    val nickname = doc.getString("nickname") ?: ""
                    val score    = doc.getLong("score")?.toInt() ?: 0
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

        // 4) 공유 버튼 클릭 시, 현재 화면에 표시된 랭킹 텍스트를 공유 인텐트로 전달
        btnShare.setOnClickListener {
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
        // DialogFragment 너비/높이 조정
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}