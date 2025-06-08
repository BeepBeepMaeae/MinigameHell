package com.example.minigame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import android.widget.ImageView
import com.bumptech.glide.Glide
import android.net.Uri

class GameSelectActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var gameList: List<GameInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_select)

        // 프로필 이미지 로드
        val profileImageView = findViewById<ImageView>(R.id.profileImageView)
        SharedPrefManager.getProfileImageUri(this)?.let { uriString ->
            Glide.with(this).load(Uri.parse(uriString)).into(profileImageView)
        }

        viewPager = findViewById(R.id.viewPagerGames)
        gameList = listOf(
            GameInfo("카드 게임", R.drawable.ic_card_thumbnail),
            GameInfo("반응 속도 테스트", R.drawable.ic_reaction_thumbnail),
            GameInfo("랜덤 퀴즈", R.drawable.ic_quiz_thumbnail)
        )
        viewPager.adapter = GamePagerAdapter(gameList)

    }
    override fun onResume() {
        super.onResume()
        // 게임 선택 화면 진입 시 메인 테마 재생
        BgmManager.startBgm(this, R.raw.main_bgm)
    }
}