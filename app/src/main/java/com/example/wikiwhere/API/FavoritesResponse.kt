package com.example.wikiwhere.API



data class FavoritesResponse(
    val __v: Int,
    val _id: String,
    val favorite: Favorite,
    val userid: String
)

data class Favorite(
    val articleTitle: String,
    val articleURL: String,
    val placeLocation: PlaceLocation,
    val placeName: String
)

data class PlaceLocation(
    val lat: String,
    val lng: String
)
