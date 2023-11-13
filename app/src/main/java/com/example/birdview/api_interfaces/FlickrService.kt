package com.example.birdview.api_interfaces

import com.example.birdview.models.FlickrResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrService {
    @GET("services/rest/")
    fun searchPhotos(
        @Query("method") method: String = "flickr.photos.search",
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json",
        @Query("nojsoncallback") noJsonCallback: Int = 1,
        @Query("text") text: String, // The title or search query
        @Query("per_page") perPage: Int = 1
    ): Call<FlickrResponse>
}