package com.auctech.siprint.services

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://nabs7.000webhostapp.com/api/"

    val instance: Retrofit by lazy {
        val loggingInterceptor = HttpLoggingInterceptor()
        val loggingInterceptor2 = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        loggingInterceptor2.level = HttpLoggingInterceptor.Level.HEADERS

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(loggingInterceptor2)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
