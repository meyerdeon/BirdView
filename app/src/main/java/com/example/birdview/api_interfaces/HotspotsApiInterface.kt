package com.example.birdview.api_interfaces

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface HotspotsApiInterface {
    @GET() //"geo?lat={lat}&lng={lng}"
    suspend fun getHotspot(@Url url:String): Response<String>
}