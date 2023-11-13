package com.example.birdview.models

data class Bird(
    val speciesCode: String,
    val comName: String,
    val sciName: String,
)

data class BirdWithImage(
    val speciesCode: String,
    val comName: String,
    val sciName: String,
    var url : String?,
    var recording : String?
)
