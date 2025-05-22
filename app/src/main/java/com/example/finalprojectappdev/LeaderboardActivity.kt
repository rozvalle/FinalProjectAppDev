package com.example.finalprojectappdev

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalprojectappdev.adapter.LeaderboardAdapter
import com.example.finalprojectappdev.data.model.LeaderboardEntry
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LeaderboardActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LeaderboardAdapter
    private val leaderboardList = mutableListOf<LeaderboardEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        recyclerView = findViewById(R.id.leaderboardRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = LeaderboardAdapter(leaderboardList)
        recyclerView.adapter = adapter

        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        db.collection("leaderboard")
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { scoresSnapshot ->
                leaderboardList.clear()
                val tasks = scoresSnapshot.documents.map { doc ->
                    val userId = doc.getString("userId") ?: ""
                    val score = doc.getLong("score")?.toInt() ?: 0

                    db.collection("users").document(userId).get().continueWith { userTask ->
                        val username = userTask.result?.getString("username") ?: "Unknown"
                        LeaderboardEntry(username, score)
                    }
                }

                Tasks.whenAllSuccess<LeaderboardEntry>(tasks).addOnSuccessListener { results ->
                    leaderboardList.addAll(results)
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load leaderboard", Toast.LENGTH_SHORT).show()
            }
    }
}
