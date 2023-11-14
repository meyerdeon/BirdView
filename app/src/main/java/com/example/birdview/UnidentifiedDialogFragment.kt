package com.example.birdview

import android.Manifest
import android.app.Activity
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.storage
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class UnidentifiedDialogFragment(private val latitude : String, private val longitude : String) : BottomSheetDialogFragment() {

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
    private var encodedBitmap: String? = null
    private lateinit var imgTakePicture : LinearLayout
    private lateinit var imgUploadImage : LinearLayout
    private lateinit var btnOK : Button
    private lateinit var btnCancel : Button

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_unidentified_dialog, container, false)
        if (getDialog() != null && getDialog()?.getWindow() != null) {
            getDialog()?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            getDialog()?.getWindow()?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        imgTakePicture = view.findViewById<LinearLayout>(R.id.lyt_take_picture)
        imgUploadImage = view.findViewById<LinearLayout>(R.id.lyt_upload_image)
        btnOK = view.findViewById<Button>(R.id.btnOk)
        btnCancel = view.findViewById<Button>(R.id.btnCancel)
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

        btnOK.setOnClickListener(){
            if(image_bird.drawable == null){
                Toast.makeText(context, "Please provide an image", Toast.LENGTH_SHORT).show()
            }
            else{
                btnOK.isEnabled = false
                btnCancel.isEnabled = false
                imgTakePicture.isEnabled = false
                imgUploadImage.isEnabled = false
                btnRecord.isEnabled = false
                btnPlay.isEnabled = false
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
                                    Toast.makeText(
                                        requireContext(),
                                        "Audio uploaded. URI: $downloadUri",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val obs : Observation = Observation(null, encodedBitmap, "Unspecified Species", "I don't know", downloadUri.toString(), latitude, longitude, output)
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
                                            Toast.makeText(context, "Observation could not be added.", Toast.LENGTH_SHORT).show()
                                        }
                                    }.addOnCompleteListener(){
                                        dismiss()
                                    }.addOnFailureListener(){
                                        Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show()
                                    }

                                }
                            } else {
                                Toast.makeText(requireContext(), "Error uploading audio: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
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

        // Old code does not work properly for newer versions
//        imgUploadImage.setOnClickListener {
//            // Handle the "Upload Picture" option
//            if (isReadStoragePermissionGranted()) {
//                openImagePicker()
//            } else {
//                requestReadStoragePermission()
//            }
//            // You can trigger the image selection process
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

    private val requestMediaPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (isMediaPermissionGranted(permissions, getReadStoragePermission())) {
                openImagePicker()
            } else {
                Toast.makeText(context, "Media permissions have been denied.", Toast.LENGTH_SHORT).show()
            }
        }

    private fun requestImagePickerPermissions() {
        val permissionsToRequest = arrayOf(getReadStoragePermission())

        // Request multiple permissions to access images
        requestMediaPermissionsLauncher.launch(permissionsToRequest)
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
                Toast.makeText(requireContext(), "Audio permissions have been denied", Toast.LENGTH_SHORT).show()
            }
        }
    private fun requestAudioPermissions() {
        val permissionsToRequest = audioPermissions

        // Request multiple permissions to access images
        requestAudioPermissionsLauncher.launch(permissionsToRequest)
    }

    private fun openImagePicker() {
        // Launch the image picker
        // Launch the image picker
        getContentLauncher.launch("image/*")
    }

    private fun getReadStoragePermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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

    private val getContentLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // Handle the selected image URI as needed
            try {
                val contentResolver = requireContext().contentResolver
                val inputStream = uri?.let { contentResolver.openInputStream(it) }
                val bitmap = BitmapFactory.decodeStream(inputStream)
                image_bird.background = null
                image_bird.setImageBitmap(bitmap)
                encodedBitmap = GlobalMethods.encodeImage(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
//                // Do something with the selected image URI, such as displaying it or processing it.
//            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

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
        if (requestCode == BirdManualEntryDialogFragment.REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            image_bird.background = null
            image_bird.setImageBitmap(imageBitmap)
            encodedBitmap = GlobalMethods.encodeImage(imageBitmap)
            // Do something with the captured image, such as displaying it or saving it.
        }
//        }
    }

    private fun toggleRecording() {
        if (!isRecording) {
            requestAudioPermissions()
        } else {
            stopRecording()
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
    }
}