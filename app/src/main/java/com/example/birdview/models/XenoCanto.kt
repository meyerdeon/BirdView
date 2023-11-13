package com.example.birdview.models

import com.google.gson.annotations.SerializedName

data class XenoCantoRecording(
    @SerializedName("id") val id: String,
    @SerializedName("file") val file: String
)

data class XenoCantoResponse(
    @SerializedName("numRecordings") val numRecordings: Int,
    @SerializedName("recordings") val recordings: List<XenoCantoRecording>
)
