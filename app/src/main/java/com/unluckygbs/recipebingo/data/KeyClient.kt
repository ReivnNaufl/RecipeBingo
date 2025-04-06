package com.unluckygbs.recipebingo.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object KeyClient {
    private const val BASE_URL = "https://recipe-bingo-pass-manager.vercel.app/api/"

    val apiService: KeyApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KeyApiService::class.java)
    }
}
