package com.example.minigame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import com.bumptech.glide.Glide
import android.net.Uri

class MainActivity : AppCompatActivity() {

    private lateinit var btnStart: Button
    private lateinit var btnSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 프로필 이미지 로드
        val profileImageView = findViewById<ImageView>(R.id.profileImageView)

        supportFragmentManager
         .setFragmentResultListener("profileImageChanged", this) { _, bundle ->
               bundle.getString("uri")?.let { uri ->
                     Glide.with(this).load(Uri.parse(uri)).into(profileImageView)
                   }
             }

        SharedPrefManager.getProfileImageUri(this)?.let { uriString ->
            Glide.with(this).load(Uri.parse(uriString)).into(profileImageView)
        }

        btnStart = findViewById(R.id.btnStart)
        btnSettings = findViewById(R.id.btnSettings)

        btnStart.setOnClickListener {
            SoundEffectManager.playClick(this)
            val intent = Intent(this, GameSelectActivity::class.java)
            startActivity(intent)
        }

        btnSettings.setOnClickListener {
            val settingsFragment = SettingsFragment()
            settingsFragment.show(supportFragmentManager, "SettingsFragment")
        }
    }

    override fun onResume() {
        super.onResume()
        BgmManager.startBgm(this, R.raw.main_bgm)
    }
}