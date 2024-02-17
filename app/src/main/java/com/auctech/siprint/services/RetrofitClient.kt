package com.auctech.siprint.services

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://si-sms.net/api/"

    val instance: Retrofit by lazy {
        val loggingInterceptor = HttpLoggingInterceptor()
        val loggingInterceptor2 = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        loggingInterceptor2.level = HttpLoggingInterceptor.Level.HEADERS

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(loggingInterceptor2)
        client.connectTimeout(10, TimeUnit.SECONDS); // Set connection timeout
        client.readTimeout(10, TimeUnit.SECONDS);

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
