package com.example.minigame

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Game2048Activity : AppCompatActivity(), PauseMenuFragment.PauseMenuListener {

    private lateinit var gridLayout: GridLayout
    private lateinit var tvScore: TextView
    private lateinit var btnPause: Button
    private lateinit var gestureDetector: GestureDetector

    private val board = Array(4) { Array(4) { 0 } }
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_2048)

        gridLayout = findViewById(R.id.gridLayout2048)
        tvScore = findViewById(R.id.tvScore)
        btnPause = findViewById(R.id.btnPause)

        setupBoard()

        btnPause.setOnClickListener {
            SoundEffectManager.playClick(this)
            val pauseMenu = PauseMenuFragment()
            pauseMenu.show(supportFragmentManager, "PauseMenuFragment")
        }

        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false

                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            moveRight()
                        } else {
                            moveLeft()
                        }
                        return true
                    }
                } else {
                    if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            moveDown()
                        } else {
                            moveUp()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun setupBoard() {
        gridLayout.removeAllViews()
        gridLayout.rowCount = 4
        gridLayout.columnCount = 4

        for (i in 0 until 16) {
            val cell = TextView(this).apply {
                text = ""
                textSize = 24f
                setBackgroundResource(R.drawable.cell_background)
                setPadding(8, 8, 8, 8)
                gravity = android.view.Gravity.CENTER
            }
            gridLayout.addView(cell)
        }
        score = 0
        tvScore.text = "Score: $score"
        addRandomTile()
        addRandomTile()
        updateBoard()
        if (isGameOver()) {
            showGameOverDialog()
        }
    }

    private fun moveLeft() {
        var moved = false

        for (i in 0..3) {
            val newRow = mutableListOf<Int>()
            for (j in 0..3) {
                if (board[i][j] != 0) {
                    newRow.add(board[i][j])
                }
            }
            var j = 0
            while (j < newRow.size - 1) {
                if (newRow[j] == newRow[j + 1]) {
                    newRow[j] *= 2
                    updateScore(newRow[j])
                    newRow.removeAt(j + 1)
                    j++
                }
                j++
            }
            while (newRow.size < 4) {
                newRow.add(0)
            }
            for (j in 0..3) {
                if (board[i][j] != newRow[j]) {
                    board[i][j] = newRow[j]
                    moved = true
                }
            }
        }

        if (moved) {
            addRandomTile()
            updateBoard()
        }
    }

    private fun moveRight() {
        var moved = false

        for (i in 0..3) {
            val newRow = mutableListOf<Int>()
            for (j in 3 downTo 0) {
                if (board[i][j] != 0) {
                    newRow.add(board[i][j])
                }
            }
            var j = 0
            while (j < newRow.size - 1) {
                if (newRow[j] == newRow[j + 1]) {
                    newRow[j] *= 2
                    updateScore(newRow[j])
                    newRow.removeAt(j + 1)
                    j++
                }
                j++
            }
            while (newRow.size < 4) {
                newRow.add(0)
            }
            for (j in 0..3) {
                if (board[i][3 - j] != newRow[j]) {
                    board[i][3 - j] = newRow[j]
                    moved = true
                }
            }
        }

        if (moved) {
            addRandomTile()
            updateBoard()
        }
    }

    private fun moveUp() {
        var moved = false

        for (j in 0..3) {
            val newCol = mutableListOf<Int>()
            for (i in 0..3) {
                if (board[i][j] != 0) {
                    newCol.add(board[i][j])
                }
            }
            var i = 0
            while (i < newCol.size - 1) {
                if (newCol[i] == newCol[i + 1]) {
                    newCol[i] *= 2
                    updateScore(newCol[i])
                    newCol.removeAt(i + 1)
                    i++
                }
                i++
            }
            while (newCol.size < 4) {
                newCol.add(0)
            }
            for (i in 0..3) {
                if (board[i][j] != newCol[i]) {
                    board[i][j] = newCol[i]
                    moved = true
                }
            }
        }

        if (moved) {
            addRandomTile()
            updateBoard()
        }
    }

    private fun moveDown() {
        var moved = false

        for (j in 0..3) {
            val newCol = mutableListOf<Int>()
            for (i in 3 downTo 0) {
                if (board[i][j] != 0) {
                    newCol.add(board[i][j])
                }
            }
            var i = 0
            while (i < newCol.size - 1) {
                if (newCol[i] == newCol[i + 1]) {
                    newCol[i] *= 2
                    updateScore(newCol[i])
                    newCol.removeAt(i + 1)
                    i++
                }
                i++
            }
            while (newCol.size < 4) {
                newCol.add(0)
            }
            for (i in 0..3) {
                if (board[3 - i][j] != newCol[i]) {
                    board[3 - i][j] = newCol[i]
                    moved = true
                }
            }
        }

        if (moved) {
            addRandomTile()
            updateBoard()
        }
    }

    private fun updateScore(points: Int) {
        score += points
        tvScore.text = "Score: $score"
    }

    private fun addRandomTile() {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (i in 0..3) {
            for (j in 0..3) {
                if (board[i][j] == 0) {
                    emptyCells.add(Pair(i, j))
                }
            }
        }
        if (emptyCells.isNotEmpty()) {
            val (row, col) = emptyCells.random()
            board[row][col] = if (Math.random() < 0.9) 2 else 4
        }
    }

    private fun updateBoard() {
        for (i in 0..3) {
            for (j in 0..3) {
                val index = i * 4 + j
                val cell = gridLayout.getChildAt(index) as TextView
                val value = board[i][j]
                cell.text = if (value == 0) "" else value.toString()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onResumeGame() {
        // 아무것도 안 함 (계속 진행)
    }

    override fun onRetryGame() {
        setupBoard()
    }

    override fun onQuitGame() {
        val intent = Intent(this, GameSelectActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private fun isGameOver(): Boolean {
        // 빈 칸이 있으면 게임 오버 아님
        for (i in 0..3) {
            for (j in 0..3) {
                if (board[i][j] == 0) {
                    return false
                }
            }
        }

        // 인접한 칸끼리 같은 값이 있으면 게임 오버 아님
        for (i in 0..3) {
            for (j in 0..3) {
                if (i < 3 && board[i][j] == board[i + 1][j]) return false
                if (j < 3 && board[i][j] == board[i][j + 1]) return false
            }
        }

        return true
    }

    private fun showGameOverDialog() {
        saveRanking("2048", score)

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("게임 오버")
        builder.setMessage("최종 점수: $score\n다시 시작하시겠습니까?")
        builder.setPositiveButton("다시 시작") { _, _ ->
            setupBoard()
        }
        builder.setNegativeButton("종료") { _, _ ->
            val intent = Intent(this, GameSelectActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun saveRanking(gameType: String, score: Int) {
        val nickname = SharedPrefManager.getNickname(this)
        val ranking = RankingEntity(gameType = gameType, nickname = nickname, score = score)

        val db = RankingDatabase.getDatabase(this)

        // suspend 함수는 CoroutineScope에서 호출해야 함
        CoroutineScope(Dispatchers.IO).launch {
            db.rankingDao().insertRanking(ranking)
        }
    }
}