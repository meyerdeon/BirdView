package com.example.birdview.api_interfaces

import com.example.birdview.models.XenoCantoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface XenoCantoService {
    @GET("recordings")
    fun getRecordings(@Query("query") query: String): Call<XenoCantoResponse>
}