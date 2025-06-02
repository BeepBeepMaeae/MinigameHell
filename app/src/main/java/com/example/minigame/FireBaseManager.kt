package com.example.minigame

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

object FirebaseManager {

    private val db = FirebaseFirestore.getInstance()

    fun uploadScore(gameType: String, nickname: String, score: Int) {
        val data = hashMapOf(
            "gameType" to gameType,
            "nickname" to nickname,
            "score" to score,
            "timestamp" to Date() // Firestore 서버시간 기준
        )

        db.collection("scores")
            .add(data)
            .addOnSuccessListener {
                Log.d("FirebaseManager", "점수 업로드 성공: ${it.id}")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseManager", "점수 업로드 실패", e)
            }
    }
}