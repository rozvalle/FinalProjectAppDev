package com.example.finalprojectappdev

import com.example.finalprojectappdev.data.model.TriviaResponse
import retrofit2.Call
import retrofit2.http.GET

interface TriviaApi {
    @GET("api.php?amount=1&type=multiple&difficulty=easy")
    fun getQuestion(): Call<TriviaResponse>
}
