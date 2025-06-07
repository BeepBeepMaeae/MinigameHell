package com.example.minigame
import android.text.Html
import android.os.Build

data class TriviaResponse(
    val response_code: Int,
    val results: List<TriviaQuestion>
)

data class TriviaQuestion(
    val question: String,
    val correct_answer: String,
    val incorrect_answers: List<String>
)


fun String.htmlDecode(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
    } else {
        Html.fromHtml(this).toString()
    }
}

fun TriviaQuestion.toQuestion(): Question {
    val decodedQuestion = question.htmlDecode()
    val decodedCorrect = correct_answer.htmlDecode()
    val decodedIncorrect = incorrect_answers.map { it.htmlDecode() }

    val allOptions = (decodedIncorrect + decodedCorrect).shuffled()
    val correctIndex = allOptions.indexOf(decodedCorrect)

    return Question(decodedQuestion, allOptions, correctIndex)
}