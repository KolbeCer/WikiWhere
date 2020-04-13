package com.example.wikiwhere.API

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST


interface UserAPI {


    @POST("signup")
    fun signUp(@Body user: User):Call<RegisterResponse>

    @POST("login")
    fun logIn(@Body user: User):Call<RegisterResponse>

    @GET("favorite")
    fun getFavs():Call<List<FavoritesResponse>>
}
