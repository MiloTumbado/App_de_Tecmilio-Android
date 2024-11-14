package com.tecmilenio.hawkconnect2

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: APIService by lazy {
        Retrofit.Builder()
            .baseUrl("https://hawkconnect.azurewebsites.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(APIService::class.java)
    }
}

