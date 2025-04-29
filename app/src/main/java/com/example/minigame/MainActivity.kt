package com.example.minigame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnStart: Button
    private lateinit var btnSettings: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        BgmManager.startBgm(this)
    }

    override fun onPause() {
        super.onPause()
        BgmManager.stopBgm()
    }
}