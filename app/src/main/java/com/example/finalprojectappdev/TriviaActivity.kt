package com.example.finalprojectappdev

import android.os.Bundle
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
    private lateinit var nextButton: Button
    private lateinit var scoreText: TextView   // Add this line

    private var currentQuestion: TriviaQuestion? = null
    private var shuffledOptions: List<String> = listOf()

    private var score = 0
    private var questionCount = 0
    private val totalQuestions = 10

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trivia)

        questionText = findViewById(R.id.questionText)
        scoreText = findViewById(R.id.scoreText)  // Initialize scoreText here
        optionButtons = listOf(
            findViewById(R.id.optionA),
            findViewById(R.id.optionB),
            findViewById(R.id.optionC),
            findViewById(R.id.optionD)
        )
        nextButton = findViewById(R.id.nextButton)

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

                updateScoreText()  // Update score display after answer

                // Disable buttons after answer selection
                optionButtons.forEach { it.isEnabled = false }
            }
        }

        nextButton.setOnClickListener {
            if (questionCount >= totalQuestions) {
                saveScore()
            } else {
                fetchQuestion()
            }
        }
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

        RetrofitInstance.api.getQuestion().enqueue(object : Callback<TriviaResponse> {
            override fun onResponse(call: Call<TriviaResponse>, response: Response<TriviaResponse>) {
                val question = response.body()?.results?.get(0)
                if (question != null) {
                    currentQuestion = question
                    displayQuestion(question)
                    questionCount++
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
