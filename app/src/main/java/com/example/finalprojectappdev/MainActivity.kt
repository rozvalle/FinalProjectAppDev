package com.example.finalprojectappdev

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var logoutButton: Button
    private lateinit var startTriviaButton: Button
    private lateinit var leaderboardButton: Button
    private lateinit var auth: FirebaseAuth
    private val categoryMap = mapOf(
        "Any Category" to -1,
        "General Knowledge" to 9,
        "Books" to 10,
        "Film" to 11,
        "Music" to 12,
        "Musicals and Theatres" to 13,
        "Video Games" to 15,
        "Science and Nature" to 17,
        "Computers" to 18,
        "Mathematics" to 19,
        "Mythology" to 20,
        "Sports" to 21,
        "Geography" to 22,
        "History" to 23,
        "Animals" to 27,
        "Comics" to 29,
        "Anime and Manga" to 31,
        "Cartoons and Animations" to 32
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val spinner = findViewById<Spinner>(R.id.categorySpinner)

        auth = FirebaseAuth.getInstance()
        logoutButton = findViewById(R.id.logoutButton)
        startTriviaButton = findViewById(R.id.startTriviaButton)
        leaderboardButton = findViewById(R.id.leaderboardButton) // ← New line

        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        startTriviaButton.setOnClickListener {
            val selectedCategory = spinner.selectedItem.toString()
            val categoryId = categoryMap[selectedCategory] ?: -1

            val intent = Intent(this, TriviaActivity::class.java)
            intent.putExtra("CATEGORY_ID", categoryId)
            startActivity(intent)
        }

        leaderboardButton.setOnClickListener {
            val intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
        }
    }
}
