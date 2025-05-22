package com.example.finalprojectappdev

import android.os.Bundle
import android.text.Html
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.finalprojectappdev.data.model.TriviaQuestion
import com.example.finalprojectappdev.data.model.TriviaResponse
import com.example.finalprojectappdev.data.network.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TriviaActivity : AppCompatActivity() {

    private lateinit var questionText: TextView
    private lateinit var optionButtons: List<Button>
    private lateinit var nextButton: Button

    private var currentQuestion: TriviaQuestion? = null
    private var shuffledOptions: List<String> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trivia)

        questionText = findViewById(R.id.questionText)
        optionButtons = listOf(
            findViewById(R.id.optionA),
            findViewById(R.id.optionB),
            findViewById(R.id.optionC),
            findViewById(R.id.optionD)
        )
        nextButton = findViewById(R.id.nextButton)

        fetchQuestion()

        optionButtons.forEach { button ->
            button.setOnClickListener {
                val selected = button.text.toString()
                val correct = currentQuestion?.correct_answer
                if (selected == correct) {
                    Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Wrong! Correct: $correct", Toast.LENGTH_SHORT).show()
                }
            }
        }

        nextButton.setOnClickListener {
            fetchQuestion()
        }
    }

    private fun fetchQuestion() {
        RetrofitInstance.api.getQuestion().enqueue(object : Callback<TriviaResponse> {
            override fun onResponse(call: Call<TriviaResponse>, response: Response<TriviaResponse>) {
                val question = response.body()?.results?.get(0)
                if (question != null) {
                    currentQuestion = question
                    displayQuestion(question)
                }
            }

            override fun onFailure(call: Call<TriviaResponse>, t: Throwable) {
                Toast.makeText(this@TriviaActivity, "Failed to load question", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayQuestion(question: TriviaQuestion) {
        // Decode HTML characters like &quot;
        questionText.text = Html.fromHtml(question.question)

        shuffledOptions = (question.incorrect_answers + question.correct_answer).shuffled()
        for (i in optionButtons.indices) {
            optionButtons[i].text = Html.fromHtml(shuffledOptions[i])
        }
    }
}
