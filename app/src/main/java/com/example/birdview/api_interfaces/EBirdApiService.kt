package com.example.birdview.api_interfaces

import com.example.birdview.models.Bird
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface EBirdApiService {
    @GET("data/obs/geo/recent")
    fun getBirdObservations(
        @Query("lat") latitude: Double,
        @Query("lng") longitude: Double,
        @Query("dist") distanceKm: Int, // Optional: You can specify a distance parameter
        @Query("maxResults") maxResults: Int, // Optional: Limit the number of results
        @Query("back") back: Int, // Optional: Retrieve historical data
        @Query("fmt") format: String = "json", // Optional: JSON format
        @Query("key") apiKey: String
    ): Call<List<Bird>>
}