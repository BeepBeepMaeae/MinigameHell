package com.example.minigame

import android.content.Context

object SharedPrefManager {
    private const val PREF_NAME = "minigame_prefs"
    private const val KEY_NICKNAME = "nickname"
    private const val KEY_PROFILE_IMAGE_URI = "profile_image_uri"

    fun getNickname(context: Context): String {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.getString(KEY_NICKNAME, "Player") ?: "Player"
    }

    fun setNickname(context: Context, nickname: String) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().putString(KEY_NICKNAME, nickname).apply()
    }

    fun setProfileImageUri(context: Context, uri: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PROFILE_IMAGE_URI, uri).apply()
    }

    fun getProfileImageUri(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PROFILE_IMAGE_URI, null)
    }
}