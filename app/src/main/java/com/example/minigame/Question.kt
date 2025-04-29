package com.example.minigame

data class Question(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)