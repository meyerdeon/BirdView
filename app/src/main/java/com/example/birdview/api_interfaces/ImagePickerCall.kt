package com.example.birdview.api_interfaces

import android.graphics.Bitmap
import android.net.Uri

interface ImagePickerCallback {
    fun onImagePickerResult(uri: Uri?)
}