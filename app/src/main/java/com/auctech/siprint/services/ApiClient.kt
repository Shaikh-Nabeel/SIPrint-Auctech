package com.auctech.siprint.services

import com.auctech.siprint.home.response.ResponseDashboard
import com.auctech.siprint.home.response.ResponseDateFilter
import com.auctech.siprint.home.response.ResponseSearch
import com.auctech.siprint.initials.response.ResponseLogin
import com.auctech.siprint.initials.response.ResponseOtpVerification
import com.auctech.siprint.initials.response.ResponseSignup
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiClient {

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("userLogin")
    fun userLogin(
        @Field("phone") phone: String
    ): Call<ResponseLogin>

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("otpVerification")
    fun otpVerification(
        @Field("otp") otp: String,
        @Field("phone") phone: String
    ): Call<ResponseOtpVerification>

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("updatePdf")
    fun updatePdf(
        @Field("docID") docID: String,
        @Field("user_id") user_id: String,
        @Field("label") label: String,
        @Field("description") description: String,
    ): Call<ResponseSignup>

    @Multipart
    @POST("userRegistration")
    fun userRegistration(
        @Part("id") userId: RequestBody,
        @Part("name") name: RequestBody,
        @Part("email") email: RequestBody,
        @Part photo: MultipartBody.Part
    ): Call<ResponseSignup>

    @GET("dashboard")
    fun getDashboardData(
        @Query("user_id") userId: String,
        @Query("offset") offset: Int
    ): Call<ResponseDashboard>

    @GET("pdfFilter")
    fun getPdfFilter(
        @Query("user_id") userId: String,
        @Query("starting") starting: String,
        @Query("ending") ending: String,
        @Query("offset") offset: Int
    ): Call<ResponseDateFilter>


    @Multipart
    @POST("uploadPdf")
    fun uploadPdf(
        @Part("id") userId: RequestBody,
        @Part("owner") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part pdf: MultipartBody.Part
    ): Call<ResponseSignup>

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("searchPdf")
    fun getSearch(
        @Field("user_id") userId: String,
        @Field("search") search: String,
        @Field("offset") offset: Int,
    ): Call<ResponseSearch>

}

