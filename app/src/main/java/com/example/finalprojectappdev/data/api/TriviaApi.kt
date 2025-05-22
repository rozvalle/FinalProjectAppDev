package com.example.finalprojectappdev

import com.example.finalprojectappdev.data.model.TriviaResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TriviaApi {
    @GET("api.php?amount=1&difficulty=easy&type=multiple")
    fun getQuestion(
        @Query("amount") amount: Int = 1,
        @Query("category") category: Int? = null,
        @Query("type") type: String = "multiple",
        @Query("difficulty") difficulty: String = "easy"
    ): Call<TriviaResponse>
}
