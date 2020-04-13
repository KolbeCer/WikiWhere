package com.example.wikiwhere.API

data class FavoritesResponse (  val lat: String,
                                val lng: String,
                                val placeName: String,
                                val article: String,
                                val url: String
)