package com.tecmilenio.hawkconnect2

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val logging = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}

val httpClient = OkHttpClient.Builder()
    .addInterceptor(logging)
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl("https://hawkconnect.azurewebsites.net/")
    .client(httpClient)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

object RetrofitInstance {
    val api: APIService by lazy {
        retrofit.create(APIService::class.java)
    }
}


