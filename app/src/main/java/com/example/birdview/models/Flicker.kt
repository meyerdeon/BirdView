package com.example.birdview.models

data class FlickrResponse(
    val photos: FlickrPhotos
)

data class FlickrPhotos(
    val photo: List<FlickrPhoto>
)

data class FlickrPhoto(
    val id: String,
    val title: String,
    val farm: Int,
    val server: String,
    val secret: String
)