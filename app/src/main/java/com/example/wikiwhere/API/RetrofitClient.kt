package com.example.wikiwhere.API


import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


internal object RetrofitClient {
    const val BASE_URL = "https://wiki-where.herokuapp.com/api/"
    private var retrofit: Retrofit? = null
    val client: Retrofit?
        get() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build()
            }
            return retrofit
        }
}