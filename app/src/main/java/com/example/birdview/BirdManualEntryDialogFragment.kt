package com.example.birdview

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.birdview.api_interfaces.ImagePickerCallback
import com.example.birdview.databinding.ActivitySignInBinding
import com.example.birdview.databinding.FragmentBirdManualEntryDialogBinding
import com.example.birdview.databinding.FragmentObservationListDialogBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.storage
import org.w3c.dom.Text
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BirdManualEntryDialogFragment(private val latitude : String, private val longitude : String, private val tripId: String?) : DialogFragment(),
    ImagePickerCallback {
    companion object {
        const val REQUEST_IMAGE_CAPTURE = 101
        const val REQUEST_OPEN_IMAGE = 102
    }

    private var mediaRecorder: MediaRecorder? = null
    private lateinit var recordingChronometer: Chronometer
    private lateinit var playbackChronometer: Chronometer
    private var mediaPlayer: MediaPlayer? = null
    private var isRecording = false
    private var isPlaying = false
    private var tempFile: File? = null
    private val RECORD_AUDIO_PERMISSION = android.Manifest.permission.RECORD_AUDIO
    private val WRITE_EXTERNAL_STORAGE_PERMISSION =
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val PERMISSION_REQUEST_CODE = 123
    private val storage = Firebase.storage
    private lateinit var image_bird: ImageView
    private lateinit var tv_record_sound: TextView
    private lateinit var tv_play_sound: TextView
    private lateinit var btnRecord: LinearLayout
    private lateinit var btnPlay: LinearLayout
    private lateinit var imgTakePicture : LinearLayout
    private lateinit var imgUploadImage : LinearLayout
    private lateinit var btnAddSighting : Button
    private lateinit var btnCancel : Button
    private lateinit var et_com_name : TextView
    private lateinit var et_sci_name : TextView
    private var encodedBitmap: String? = null

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bird_manual_entry_dialog, container, false)
        if (getDialog() != null && getDialog()?.getWindow() != null) {
            getDialog()?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            getDialog()?.getWindow()?.requestFeature(Window.FEATURE_NO_TITLE)
        }
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        imgTakePicture = view.findViewById<LinearLayout>(R.id.lyt_take_picture)
        imgUploadImage = view.findViewById<LinearLayout>(R.id.lyt_upload_image)
        btnAddSighting = view.findViewById<Button>(R.id.btnAddSighting)
        btnCancel = view.findViewById<Button>(R.id.btnCancel)
        et_com_name = view.findViewById<EditText>(R.id.etComName)
        et_sci_name = view.findViewById<EditText>(R.id.etSciName)
        image_bird = view.findViewById<ImageView>(R.id.img_bird)
        tv_record_sound = view.findViewById(R.id.tv_record_sound)
        tv_play_sound = view.findViewById(R.id.tv_play_sound)
        mediaPlayer = MediaPlayer()
        mediaRecorder = MediaRecorder()
        btnRecord = view.findViewById(R.id.lyt_record_sound)
        btnPlay = view.findViewById(R.id.lyt_play_sound)
        recordingChronometer = view.findViewById(R.id.recording_chronometer)
        recordingChronometer.base = SystemClock.elapsedRealtime()
        playbackChronometer = view.findViewById(R.id.playback_chronometer)
        playbackChronometer.base = SystemClock.elapsedRealtime()
        btnRecord.setOnClickListener {
            //Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show()
            toggleRecording()
        }
//
        btnPlay.setOnClickListener {
            togglePlaySound()
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnAddSighting.setOnClickListener() {
            try{
                if (et_com_name.text.isNullOrEmpty() || et_sci_name.text.isNullOrEmpty()) {
                    if (et_com_name.text.isNullOrEmpty()) {
                        et_com_name.error = "Please enter the common name of the bird."
                    }
                    if (et_sci_name.text.isNullOrEmpty()) {
                        et_sci_name.error = "Please enter the scientific name of the bird."
                    }
                    Toast.makeText(context, "Please complete all fields", Toast.LENGTH_SHORT).show()
                } else {
                    et_com_name.error = null
                    et_sci_name.error = null
                    if (image_bird.drawable == null) {
                        Toast.makeText(context, "Please provide an image", Toast.LENGTH_SHORT).show()

                    }
                    else if(tempFile == null){
                        Toast.makeText(context, "Please record a sound", Toast.LENGTH_SHORT).show()
                    }
                    else if (tempFile != null && tempFile?.length() == 0L){
                        Toast.makeText(context, "Please record a sound", Toast.LENGTH_SHORT).show()
                    }
                    else
                    {
                        btnAddSighting.isEnabled = false
                        imgTakePicture.isEnabled = false
                        imgUploadImage.isEnabled = false
                        et_com_name.isEnabled = false
                        et_sci_name.isEnabled = false
                        btnCancel.isEnabled = false
                        btnPlay.isEnabled = false
                        btnRecord.isEnabled = false
                        val user = FirebaseAuth.getInstance().currentUser
// .child("tripcards").child(tripId)
                        if ((!tripId.isNullOrEmpty())){
                            //add manual entry to tripcard
                            try {
                                //code attribution
                                //the following code was taken from Stack Overflow and adapted
                                //https://stackoverflow.com/questions/53781154/kotlin-android-java-string-datetime-format-api21#:~:text=yyyy%20HH%3Amm%22)%3B%20String,%3D%20new%20SimpleDateFormat(%22dd.
                                //arifng
                                //https://stackoverflow.com/users/989643/arifng
                                val localDateTime = LocalDateTime.now()
                                val formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy HH:mm")
                                val output = localDateTime.format(formatter)
                                tempFile?.let { file ->
                                    // Upload the file to Firebase Storage
                                    val storageRef = storage.reference.child("${user?.uid.toString()}/audio/${file.name}")
                                    val uploadTask = storageRef.putFile(Uri.fromFile(file))

                                    uploadTask.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            // File upload successful
                                            // You can get the download URL if needed
                                            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                                tempFile?.delete()
                                                Toast.makeText(requireContext(), "Audio uploaded. URI: $downloadUri", Toast.LENGTH_SHORT).show()
                                                val obs: Observation = Observation(
                                                    null,
                                                    encodedBitmap,
                                                    et_com_name.text.toString(),
                                                    et_sci_name.text.toString(),
                                                    downloadUri.toString(),
                                                    latitude,
                                                    longitude,
                                                    output
                                                )
                                                //GlobalVariablesMethods.user.categories?.add(cat)
                                                val database = FirebaseDatabase.getInstance()
                                                val databaseReference = database.getReference("Users")
                                                //code attribution
                                                //the following code was taken from Stack Overflow and adapted
                                                //https://stackoverflow.com/questions/60432256/on-insert-data-in-firebase-realtime-database-it-deletes-previous-data
                                                //ashok
                                                //https://stackoverflow.com/users/12746098/ashok
                                                databaseReference.child(user?.uid.toString()).child("tripcards").child(tripId).child("observations").push()
                                                    .setValue(obs).addOnCompleteListener() {
                                                        if (it.isComplete) {
                                                            Toast.makeText(
                                                                context,
                                                                "Observation added successfully.",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {
                                                            Toast.makeText(
                                                                context,
                                                                "Observation could not be added.",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }.addOnFailureListener() {
                                                        Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show()
                                                    }

                                                databaseReference.child(user?.uid.toString()).child("observations").push()
                                                    .setValue(obs).addOnCompleteListener() {
                                                        dismiss()
                                                    }.addOnFailureListener() {
                                                        Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show()
                                                    }

                                            }
                                        } else {
                                            Toast.makeText(requireContext(), "Error uploading audio: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } catch (ex: Exception) {
                                Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            try {
                                //code attribution
                                //the following code was taken from Stack Overflow and adapted
                                //https://stackoverflow.com/questions/53781154/kotlin-android-java-string-datetime-format-api21#:~:text=yyyy%20HH%3Amm%22)%3B%20String,%3D%20new%20SimpleDateFormat(%22dd.
                                //arifng
                                //https://stackoverflow.com/users/989643/arifng
                                val localDateTime = LocalDateTime.now()
                                val formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy HH:mm")
                                val output = localDateTime.format(formatter)
                                tempFile?.let { file ->
                                    // Upload the file to Firebase Storage
                                    val storageRef = storage.reference.child("${user?.uid.toString()}/audio/${file.name}")
                                    val uploadTask = storageRef.putFile(Uri.fromFile(file))

                                    uploadTask.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            // File upload successful
                                            // You can get the download URL if needed
                                            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                                tempFile?.delete()
                                                Toast.makeText(requireContext(), "Audio uploaded. URI: $downloadUri", Toast.LENGTH_SHORT).show()
                                                val obs: Observation = Observation(
                                                    null,
                                                    encodedBitmap,
                                                    et_com_name.text.toString(),
                                                    et_sci_name.text.toString(),
                                                    downloadUri.toString(),
                                                    latitude,
                                                    longitude,
                                                    output
                                                )
                                                //GlobalVariablesMethods.user.categories?.add(cat)
                                                val database = FirebaseDatabase.getInstance()
                                                val databaseReference = database.getReference("Users")
                                                //code attribution
                                                //the following code was taken from Stack Overflow and adapted
                                                //https://stackoverflow.com/questions/60432256/on-insert-data-in-firebase-realtime-database-it-deletes-previous-data
                                                //ashok
                                                //https://stackoverflow.com/users/12746098/ashok
                                                databaseReference.child(user?.uid.toString()).child("observations").push()
                                                    .setValue(obs).addOnCompleteListener() {
                                                        if (it.isComplete) {
                                                            Toast.makeText(
                                                                context,
                                                                "Observation added successfully.",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {
                                                            Toast.makeText(
                                                                context,
                                                                "Observation could not be added.",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }.addOnCompleteListener() {
                                                        dismiss()
                                                    }.addOnFailureListener() {
                                                        Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show()
                                                    }

                                            }
                                        } else {
                                            Toast.makeText(requireContext(), "Error uploading audio: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } catch (ex: Exception) {
                                Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
                            }
                        }

                    }
                }
            }
            catch (e : Exception){
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
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

        // Old code, not working properly anymore
//        imgUploadImage.setOnClickListener {
//            // Handle the "Upload Picture" option
//            if (isReadStoragePermissionGranted()) {
//                openImagePicker()
//            } else {
//                requestReadStoragePermission()
//            }
        // You can trigger the image selection process
//        }

        imgUploadImage.setOnClickListener {
            // Toast.makeText(context, "hello", Toast.LENGTH_SHORT).show()
            // Handle the "Upload Picture" option
            requestImagePickerPermissions()
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

    // Old code, not working properly anymore
//    private fun isReadStoragePermissionGranted(): Boolean {
//        return ContextCompat.checkSelfPermission(
//            requireContext(),
//            Manifest.permission.READ_EXTERNAL_STORAGE
//        ) == PackageManager.PERMISSION_GRANTED
//    }
//
//    private fun requestReadStoragePermission() {
//        ActivityCompat.requestPermissions(
//            requireActivity(),
//            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
//            REQUEST_OPEN_IMAGE
//        )
//    }
//
//    private fun openImagePicker() {
//        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        startActivityForResult(intent, REQUEST_OPEN_IMAGE)
//    }

    private val requestMediaPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (isMediaPermissionGranted(permissions, getReadStoragePermission())) {
                openImagePicker()
            } else {
                Toast.makeText(context, "Media permissions have been denied.", Toast.LENGTH_SHORT).show()
            }
        }

    private fun requestImagePickerPermissions() {
        try{
            val permissionsToRequest = arrayOf(getReadStoragePermission())

            // Request multiple permissions to access images
            requestMediaPermissionsLauncher.launch(permissionsToRequest)
        }
        catch (e:Exception){
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private val audioPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )

    private val requestAudioPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (isAudioPermissionGranted(permissions, android.Manifest.permission.RECORD_AUDIO)) {
                startRecording()
            } else {
                // Handle permission denial
                Toast.makeText(context, "Audio permissions have been denied", Toast.LENGTH_SHORT).show()
            }
        }
    private fun requestAudioPermissions() {
        val permissionsToRequest = audioPermissions

        // Request multiple permissions to access images
        requestAudioPermissionsLauncher.launch(permissionsToRequest)
    }

    private fun openImagePicker() {
        try{
            getContentLauncher.launch("image/*")
        }
        catch (e:Exception){
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
        // Launch the image picker
        // Launch the image picker
    }

    private fun getReadStoragePermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    private fun isMediaPermissionGranted(
        permissions: Map<String, Boolean>,
        permission: String
    ): Boolean {
        return permissions[permission] == true
    }

    private fun isAudioPermissionGranted(
        permissions: Map<String, Boolean>,
        permission: String
    ): Boolean {
        return permissions[permission] == true
    }

    override fun onImagePickerResult(uri: Uri?) {
        // Handle the selected image URI as needed
        try {
//            Toast.makeText(context, "Testing"+ uri.toString(), Toast.LENGTH_SHORT).show()
            val contentResolver = requireContext().contentResolver
            val inputStream = uri?.let { contentResolver.openInputStream(it) }
            val bitmap = BitmapFactory.decodeStream(inputStream)
            image_bird.background = null
            image_bird.setImageBitmap(bitmap)
            encodedBitmap = GlobalMethods.encodeImage(bitmap)
        } catch (e: IOException) {
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private val getContentLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // Call the callback method implemented in the hosting DialogFragment
            if(uri==null){
                (parentFragment as? ImagePickerCallback)?.onImagePickerResult(uri)
            }
            else{
                onImagePickerResult(uri)
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        try{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)

            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent()
                } else {
                    Toast.makeText(
                        context,
                        "Camera permission denied. Cannot open camera.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        catch (e:Exception){
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try{
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
                val imageBitmap = data?.extras?.get("data") as Bitmap
                image_bird.background = null
                image_bird.setImageBitmap(imageBitmap)
                encodedBitmap = GlobalMethods.encodeImage(imageBitmap)
                // Do something with the captured image, such as displaying it or saving it.
            }
        }
        catch (e:Exception){
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
//        if (requestCode == REQUEST_OPEN_IMAGE && resultCode == Activity.RESULT_OK) {
//            if (data != null) {
//                val selectedImageUri: Uri? = data.data
//                try {
//                    val contentResolver = requireContext().contentResolver
//                    val inputStream = selectedImageUri?.let { contentResolver.openInputStream(it) }
//                    val bitmap = BitmapFactory.decodeStream(inputStream)
//                    image_bird.background = null
//                    image_bird.setImageBitmap(bitmap)
//                    encodedBitmap = GlobalMethods.encodeImage(bitmap)
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//                // Do something with the selected image URI, such as displaying it or processing it.
//            }
//        }
//        else{
//        }
    }

    private fun toggleRecording() {
        try{
            if (!isRecording) {
                requestAudioPermissions()
            } else {
                stopRecording()
            }
        }
        catch(e:Exception){
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun togglePlaySound() {
        try{
            if (!isPlaying) {
                if(tempFile!=null && tempFile?.length() != 0L){
                    playAudio(tempFile!!)
                }
                else{
                    Toast.makeText(context, "Please record a sound", Toast.LENGTH_SHORT).show()
                }
            } else {
                stopPlaying()
            }
        }
        catch (e:Exception){
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }

    }

    private fun startRecording() {
        try {
            tempFile = createTempFile("audio_record_${System.currentTimeMillis()}", ".mp3")
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
                setOutputFile(tempFile!!.absolutePath)
                prepare()
                start()
            }

            recordingChronometer.base = SystemClock.elapsedRealtime()
            recordingChronometer.start()
            isRecording = true
            btnPlay.isEnabled = false
            tv_record_sound.setText("Stop Recording")
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Error starting recording: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        try{
            mediaRecorder?.apply {
                stop()
                release()
            }
            recordingChronometer.stop()
            mediaRecorder = null
            btnPlay.isEnabled = true
            tv_record_sound.setText("Record Sound")
            isRecording = false
            Toast.makeText(context, "You can play the audio now.", Toast.LENGTH_SHORT).show()
        }
        catch (e:Exception){
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopPlaying(){
        try{
            mediaPlayer?.apply {
                stop()
                release()
            }
            playbackChronometer.stop()
            playbackChronometer.base = SystemClock.elapsedRealtime()
            mediaPlayer = null

            tv_play_sound.setText("Play Sound")
            isPlaying = false
        }
        catch (e:Exception){
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun playAudio(file: File) {
        try{
            if(tempFile != null && tempFile?.length() != 0L){
                try {
                    mediaPlayer = MediaPlayer().apply {
                        reset()
                        setDataSource(file.absolutePath)
                        prepare()
                        start()

                    }
                    mediaPlayer?.setOnErrorListener { mp, what, extra ->
                        Toast.makeText(context, "Please note the media player is still preparing.", Toast.LENGTH_SHORT).show()
                        false
                    }

                    // Set a completion listener to reset the MediaPlayer when playback completes
                    mediaPlayer?.setOnCompletionListener {
                        mediaPlayer?.reset()
                        tv_play_sound.text = "Play Sound"
                        playbackChronometer.stop()
                        playbackChronometer.base = SystemClock.elapsedRealtime()
                    }

                    // Set a prepared listener to start playback when prepared
                    mediaPlayer?.setOnPreparedListener {
                        mediaPlayer?.start()
                    }

                    playbackChronometer.base = SystemClock.elapsedRealtime()
                    playbackChronometer.start()
                    tv_play_sound.setText("Stop Playing")
                    isPlaying = true
                } catch (e: IOException) {
                    Toast.makeText(requireContext(), "Error playing audio: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(context, "Please record a sound", Toast.LENGTH_SHORT).show()
            }
        }
        catch (e:Exception){
            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        mediaRecorder?.release()
        mediaRecorder = null
        playbackChronometer.stop()
        recordingChronometer.stop()
    }

    fun replaceFragment(fragment: Fragment){
        if(fragment != null){
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.map, fragment)
            transaction.commit()
        }
    }
}