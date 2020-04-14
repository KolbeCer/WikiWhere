package com.example.wikiwhere.API

import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST


interface UserAPI {


    @POST("user/signup")
    fun signUp(@Body user: User):Call<RegisterResponse>

    @POST("user/login")
    fun logIn(@Body user: User):Call<RegisterResponse>


    @GET("wiki/wiki/get")
    fun getFavs(@Header("Authorization") authtoken: String):Call<List<FavoritesResponse>>

}
