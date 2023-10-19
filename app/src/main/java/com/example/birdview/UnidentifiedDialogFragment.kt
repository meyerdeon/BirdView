package com.example.birdview

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UnidentifiedDialogFragment(private val latitude : String, private val longitude : String) : BottomSheetDialogFragment() {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 101
        const val REQUEST_OPEN_IMAGE = 102
    }

    private lateinit var image_bird : ImageView
    private var encodedBitmap : String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_unidentified_dialog, container, false)
//        if (getDialog() != null && getDialog()?.getWindow() != null) {
//            getDialog()?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
//            getDialog()?.getWindow()?.requestFeature(Window.FEATURE_NO_TITLE)
//        }

        val imgTakePicture = view.findViewById<LinearLayout>(R.id.lyt_take_picture)
        val imgUploadImage = view.findViewById<LinearLayout>(R.id.lyt_upload_image)
        val btnOK = view.findViewById<Button>(R.id.btnOk)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        image_bird = view.findViewById<ImageView>(R.id.img_bird)

        btnCancel.setOnClickListener{
            dismiss()
        }

        btnOK.setOnClickListener(){
            if(image_bird.drawable == null){
                Toast.makeText(context, "Please provide an image", Toast.LENGTH_SHORT).show()
            }
            else{
                btnOK.isEnabled = false
                btnCancel.isEnabled = false
                imgTakePicture.isEnabled = false
                imgUploadImage.isEnabled = false
                val user = FirebaseAuth.getInstance().currentUser
                try {
                    //code attribution
                    //the following code was taken from Stack Overflow and adapted
                    //https://stackoverflow.com/questions/53781154/kotlin-android-java-string-datetime-format-api21#:~:text=yyyy%20HH%3Amm%22)%3B%20String,%3D%20new%20SimpleDateFormat(%22dd.
                    //xingbin
                    //https://stackoverflow.com/users/6690200/xingbin
                    val localDateTime = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy HH:mm")
                    val output = localDateTime.format(formatter)
                    val obs : Observation = Observation(null, encodedBitmap, "Unspecified Species", "I don't know", latitude, longitude, output)
                    //GlobalVariablesMethods.user.categories?.add(cat)
                    val database = FirebaseDatabase.getInstance()
                    val databaseReference = database.getReference("Users")
                    //code attribution
                    //the following code was taken from Stack Overflow and adapted
                    //https://stackoverflow.com/questions/60432256/on-insert-data-in-firebase-realtime-database-it-deletes-previous-data
                    //ashok
                    //https://stackoverflow.com/users/12746098/ashok
                    databaseReference.child(user?.uid.toString()).child("observations").push().setValue(obs).addOnCompleteListener() {
                        if (it.isComplete){
                            Toast.makeText(context, "Observation added successfully.", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Toast.makeText(context, "User data retrieval failed.", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnCompleteListener(){
                        dismiss()
                    }.addOnFailureListener(){
                        Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show()
                    }

                }
                catch (ex : Exception){
                    Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
                }
            }
        }


        imgTakePicture.setOnClickListener {
            // Handle the "Take Picture" option
            if (isCameraPermissionGranted()) {
                dispatchTakePictureIntent()
            } else {
                requestCameraPermission()
            }
            // You can open the camera or perform any other action
        }

        imgUploadImage.setOnClickListener {
            // Handle the "Upload Picture" option
            if (isReadStoragePermissionGranted()) {
                openImagePicker()
            } else {
                requestReadStoragePermission()
            }
            // You can trigger the image selection process
        }

        return view
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_IMAGE_CAPTURE
        )
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
    }

    private fun isReadStoragePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestReadStoragePermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_OPEN_IMAGE
        )
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_OPEN_IMAGE)
    }

//    fun addData(){
//        if(imgCategory.drawable == null){
//            imageString = null
//        }
//        else{
//            imageString = encodedBitmap.toString()
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_OPEN_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val selectedImageUri: Uri? = data.data
                try {
                    val contentResolver = requireContext().contentResolver
                    val inputStream = selectedImageUri?.let { contentResolver.openInputStream(it) }
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    image_bird.background = null
                    image_bird.setImageBitmap(bitmap)
                    encodedBitmap = GlobalMethods.encodeImage(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                // Do something with the selected image URI, such as displaying it or processing it.
            }
        }
        else{
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
                val imageBitmap = data?.extras?.get("data") as Bitmap
                image_bird.background = null
                image_bird.setImageBitmap(imageBitmap)
                encodedBitmap = GlobalMethods.encodeImage(imageBitmap)
                // Do something with the captured image, such as displaying it or saving it.
            }
        }
    }
}