package com.example.minigame

data class TriviaResponse(
    val response_code: Int,
    val results: List<TriviaQuestion>
)

data class TriviaQuestion(
    val question: String,
    val correct_answer: String,
    val incorrect_answers: List<String>
)

fun TriviaQuestion.toQuestion(): Question {
    val allOptions = (incorrect_answers + correct_answer).shuffled()
    val correctIndex = allOptions.indexOf(correct_answer)
    return Question(question, allOptions, correctIndex)
}