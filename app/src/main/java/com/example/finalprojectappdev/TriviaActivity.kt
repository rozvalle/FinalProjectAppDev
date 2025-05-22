package com.example.finalprojectappdev

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.finalprojectappdev.data.model.TriviaQuestion
import com.example.finalprojectappdev.data.model.TriviaResponse
import com.example.finalprojectappdev.data.network.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TriviaActivity : AppCompatActivity() {

    private lateinit var questionText: TextView
    private lateinit var optionButtons: List<Button>
    private lateinit var scoreText: TextView
    private lateinit var questionProgressText: TextView

    private var currentQuestion: TriviaQuestion? = null
    private var shuffledOptions: List<String> = listOf()
    private var selectedCategoryId: Int? = null
    private lateinit var countdownText: TextView

    private var score = 0
    private var questionCount = 0
    private val totalQuestions = 20

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trivia)

        selectedCategoryId = intent.getIntExtra("CATEGORY_ID", -1)
        if (selectedCategoryId == -1) {
            selectedCategoryId = null // API handles null as "Any Category"
        }

        countdownText = findViewById(R.id.countdownText)
        questionProgressText = findViewById(R.id.questionProgressText)
        questionText = findViewById(R.id.questionText)
        scoreText = findViewById(R.id.scoreText)  // Initialize scoreText here
        optionButtons = listOf(
            findViewById(R.id.optionA),
            findViewById(R.id.optionB),
            findViewById(R.id.optionC),
            findViewById(R.id.optionD)
        )

        score = 0
        questionCount = 0
        updateScoreText()  // Show initial score

        fetchQuestion()

        optionButtons.forEach { button ->
            button.setOnClickListener {
                val selected = button.text.toString()
                val correct = currentQuestion?.correct_answer

                if (selected == correct) {
                    score += 1
                    Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Wrong! Correct: $correct", Toast.LENGTH_SHORT).show()
                }

                updateScoreText()
                optionButtons.forEach { it.isEnabled = false }

                countdownText.visibility = TextView.VISIBLE

                object : CountDownTimer(5000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val secondsLeft = millisUntilFinished / 1000
                        countdownText.text = "Next question in $secondsLeft..."
                    }

                    override fun onFinish() {
                        countdownText.visibility = TextView.GONE
                        if (questionCount >= totalQuestions) {
                            saveScore()
                        } else {
                            fetchQuestion()
                        }
                    }
                }.start()
            }
        }

        val backButton = findViewById<Button>(R.id.backButton)

        backButton.setOnClickListener {
            showExitConfirmation()
        }
    }

    private fun showExitConfirmation() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Exit Quiz")
        builder.setMessage("Are you sure you want to go back? Your quiz progress will not be saved.")
        builder.setPositiveButton("Yes") { _, _ ->
            finish() // go back to MainActivity
        }
        builder.setNegativeButton("No", null)
        builder.show()
    }


    private fun updateQuestionProgress() {
        questionProgressText.text = "Question $questionCount/$totalQuestions"
    }

    private fun updateScoreText() {
        scoreText.text = "Score: $score"
    }

    private fun fetchQuestion() {
        if (questionCount >= totalQuestions) {
            Toast.makeText(this, "Quiz finished! Your score: $score", Toast.LENGTH_LONG).show()
            saveScore()
            return
        }

        RetrofitInstance.api.getQuestion(category = selectedCategoryId).enqueue(object : Callback<TriviaResponse> {
            override fun onResponse(call: Call<TriviaResponse>, response: Response<TriviaResponse>) {
                val results = response.body()?.results
                if (!results.isNullOrEmpty()) {
                    val question = results[0]
                    currentQuestion = question
                    questionCount++
                    updateQuestionProgress()
                    displayQuestion(question)
                } else {
                    Toast.makeText(this@TriviaActivity, "No more questions found for this category. Try another one.", Toast.LENGTH_LONG).show()
                    finish() // Or navigate back to MainActivity
                }
            }

            override fun onFailure(call: Call<TriviaResponse>, t: Throwable) {
                Toast.makeText(this@TriviaActivity, "Failed to load question", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayQuestion(question: TriviaQuestion) {
        questionText.text = Html.fromHtml(question.question)

        shuffledOptions = (question.incorrect_answers + question.correct_answer).shuffled()
        for (i in optionButtons.indices) {
            optionButtons[i].text = Html.fromHtml(shuffledOptions[i])
            optionButtons[i].isEnabled = true
        }
    }

    private fun saveScore() {
        val user = auth.currentUser
        user?.let {
            val userScore = hashMapOf(
                "userId" to it.uid,
                "email" to it.email,
                "score" to score,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("leaderboard")
                .add(userScore)
                .addOnSuccessListener {
                    Toast.makeText(this, "Score saved! Your score: $score", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save score.", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(this, "User not logged in. Cannot save score.", Toast.LENGTH_SHORT).show()
        }
    }
}
