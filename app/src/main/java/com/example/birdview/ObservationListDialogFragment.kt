package com.example.birdview

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.IOException
import java.util.Locale


class ObservationListDialogFragment(private val tripId: String?) : DialogFragment() {

    private lateinit var lytInfo : LinearLayout
    private lateinit var imgShare : ImageView
    private lateinit var btnPlayAudio : Button
    private lateinit var recording : String
    private lateinit var imgBird : ImageView
    private lateinit var prgLoad : ProgressBar
    private lateinit var tvBirdDateAdded : TextView
    private lateinit var tvBirdComName : TextView
    private lateinit var tvBirdSciName : TextView
    private lateinit var tvBirdLongitudeLatitude : TextView
    private lateinit var tvBirdSpecifiedLocation : TextView
    private lateinit var imgclose : ImageView
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var chronometerObservationDuration : Chronometer
    private lateinit var chronometerObservationElapsed : Chronometer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lytInfo.visibility = View.GONE
        imgShare.visibility = View.GONE
        imgclose.visibility = View.GONE
        mediaPlayer = MediaPlayer()
    }

    private fun stopAndPlay(audioUrl: String) {

        // Set audio attributes
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )

        // Set an error listener to handle any errors during preparation
        mediaPlayer.setOnErrorListener { mp, what, extra ->
            Toast.makeText(context, "Please note the media player is still preparing or a network error has occurred.", Toast.LENGTH_SHORT).show()
            false
        }

        // Set a prepared listener to start playback when prepared
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
            chronometerObservationElapsed.base = SystemClock.elapsedRealtime()
            chronometerObservationElapsed.start()
        }

        // Set a completion listener to reset the MediaPlayer when playback completes
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.reset()
            chronometerObservationElapsed.stop()
            chronometerObservationElapsed.base = SystemClock.elapsedRealtime()
            btnPlayAudio.text = "Play Audio"
        }

        // Set the data source and prepare asynchronously
        try {
            mediaPlayer.setDataSource(audioUrl)
            mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            // Handle any exceptions during preparation
            btnPlayAudio.text = "Play Audio"
            chronometerObservationElapsed.stop()
            chronometerObservationElapsed.base = SystemClock.elapsedRealtime()
            mediaPlayer.reset()
            Toast.makeText(context, "An error has occur. Error: ${e}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_observation_list_dialog, container, false)
        if (getDialog() != null && getDialog()?.getWindow() != null) {
            getDialog()?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            getDialog()?.getWindow()?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        lytInfo = view.findViewById(R.id.lytInfo)
        imgShare = view.findViewById(R.id.img_share)
        btnPlayAudio = view.findViewById(R.id.btn_play_audio)
        prgLoad = view.findViewById(R.id.prgLoad)
        imgBird = view.findViewById(R.id.img_bird)
        tvBirdDateAdded = view.findViewById(R.id.tv_bird_date_added)
        tvBirdComName = view.findViewById(R.id.tv_bird_com_name)
        tvBirdSciName = view.findViewById(R.id.tv_bird_sci_name)
        tvBirdLongitudeLatitude = view.findViewById(R.id.tv_bird_longitude_latitude)
        tvBirdSpecifiedLocation = view.findViewById(R.id.tv_bird_specified_location)
        imgclose = view.findViewById<ImageView>(R.id.img_close)
        chronometerObservationDuration = view.findViewById(R.id.chronometerObservationDuration)
        chronometerObservationElapsed = view.findViewById(R.id.chronometerObservationElapsed)
        imgclose.setOnClickListener(){
            dismiss()
        }

        btnPlayAudio.setOnClickListener(){
            //  previousViewHolder?.let { notifyItemChanged(it.adapterPosition) }
            if(btnPlayAudio.text.equals("Play Audio")){

                btnPlayAudio.setText("Stop Audio")

                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                    chronometerObservationElapsed.stop()
                    chronometerObservationElapsed.base = SystemClock.elapsedRealtime()
                    mediaPlayer.reset()

                }
                stopAndPlay(recording)
            }
            else{
                if(btnPlayAudio.text.equals("Stop Audio")){
                    mediaPlayer.stop()
                    mediaPlayer.reset()
                    chronometerObservationElapsed.stop()
                    chronometerObservationElapsed.base = SystemClock.elapsedRealtime()
                    btnPlayAudio.setText("Play Audio")
                }
            }
        }

        val id = arguments?.getString("id")
        //val tripId = arguments?.getString("tripId")
        val isTripObservation = arguments?.getString("tripObservation")

        if (id != null) {
            try {
                val database = FirebaseDatabase.getInstance()
                val databaseReference = database.getReference("Users")
                val user = FirebaseAuth.getInstance().currentUser
                databaseReference.child(user?.uid.toString()).get().addOnSuccessListener {
                    if(it.exists()){
                        if (!tripId.isNullOrEmpty()){
                            for(obsr in it.child("tripcards").child(tripId!!).child("observations").children  ) {
                                //code attribution
                                //the following code was taken from Stack Overflow and adapted
                                //https://stackoverflow.com/questions/38232140/how-to-get-the-key-from-the-value-in-firebase
                                //Frank van Puffelen
                                //https://stackoverflow.com/users/209103/frank-van-puffelen
                                val obsId = obsr.key

                                if (obsId?.lowercase().equals(id.lowercase())) {
                                    val birdImage = obsr.child("birdImage").getValue(String::class.java)
                                    val birdComName = obsr.child("birdComName").getValue(String::class.java)
                                    val birdSciName = obsr.child("birdSciName").getValue(String::class.java)
                                    recording = obsr.child("recording").getValue(String::class.java).toString()
                                    val latitude = obsr.child("latitude").getValue(String::class.java)
                                    val longitude = obsr.child("longitude").getValue(String::class.java)
                                    val dateAdded = obsr.child("dateAdded").getValue(String::class.java)
                                    if(birdImage?.contains("https://")!!){
                                        Glide.with(imgBird.context)
                                            .load(birdImage)
                                            .into(imgBird)
                                    }
                                    else{
                                        val image = GlobalMethods.decodeImage(birdImage)
                                        imgBird.setImageBitmap(image)

                                    }
                                    tvBirdComName.text = "You have seen " + birdComName
                                    tvBirdSciName.text = birdSciName
                                    tvBirdLongitudeLatitude.text = "Latitude: " + latitude + " Longitude: " + longitude
                                    //code attribution
                                    //the following code was taken from Stack Overflow and adapted
                                    //https://stackoverflow.com/questions/43862079/how-to-get-city-name-using-latitude-and-longitude-in-android
                                    //PEHLAJ
                                    //https://stackoverflow.com/users/6027638/pehlaj
                                    val gcd = Geocoder(this.requireContext(), Locale.getDefault())
                                    var addresses: List<Address>? = null
                                    try {
                                        if (latitude != null) {
                                            if (longitude != null) {
                                                addresses = gcd.getFromLocation(latitude.toDouble(), longitude.toDouble(), 1)
                                            }
                                        }
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                    if (addresses != null && addresses.size > 0) {
                                        val locality: String = addresses[0].locality
                                        val state : String = addresses[0].adminArea
                                        val country : String = addresses[0].countryName
                                        tvBirdSpecifiedLocation.text = locality + ", " + state + ", " + country
                                    }
                                    tvBirdDateAdded.text = dateAdded

                                    val mapFragment = BirdLocationMapFragment()
                                    val bundle = Bundle()
                                    bundle.putString("latitude", latitude)
                                    bundle.putString("longitude", longitude)
                                    mapFragment.arguments = bundle
                                    childFragmentManager.beginTransaction()
                                        .replace(R.id.mapContainer, mapFragment)
                                        .commit()
//
//                                val location = LatLng(latitude, longitude)
//                                val markerOptions = MarkerOptions().position(location)
//                                markerOptions.title(title)
//                                binding.map.add.addMarker(markerOptions)
////        val location = LatLng(latitude, longitude)
////        mMap.addMarker(MarkerOptions().position(location).title(title))
////        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
//                                mMap.animateCamera(
////                                    CameraUpdateFactory.newLatLngZoom(location, 15F)
//                                if (latitude != null) {
//                                    if (longitude != null) {
//                                        mapFragment.addMarker(latitude.toDouble(), longitude.toDouble(), birdComName.toString())
//                                    }
//                                }
                                }
                            }
                        }else{

                            for(obsr in it.child("observations").children  ) {
                                //code attribution
                                //the following code was taken from Stack Overflow and adapted
                                //https://stackoverflow.com/questions/38232140/how-to-get-the-key-from-the-value-in-firebase
                                //Frank van Puffelen
                                //https://stackoverflow.com/users/209103/frank-van-puffelen
                                val obsId = obsr.key

                            if (obsId?.lowercase().equals(id.lowercase())) {
                                val birdImage = obsr.child("birdImage").getValue(String::class.java)
                                val birdComName = obsr.child("birdComName").getValue(String::class.java)
                                val birdSciName = obsr.child("birdSciName").getValue(String::class.java)
                                recording = obsr.child("recording").getValue(String::class.java).toString()
                                val latitude = obsr.child("latitude").getValue(String::class.java)
                                val longitude = obsr.child("longitude").getValue(String::class.java)
                                val dateAdded = obsr.child("dateAdded").getValue(String::class.java)
                                if(birdImage?.contains("https://")!!){
                                    Glide.with(imgBird.context)
                                        .load(birdImage)
                                        .into(imgBird)
                                }
                                else{
                                    val image = GlobalMethods.decodeImage(birdImage)
                                    imgBird.setImageBitmap(image)

                                    }
                                    tvBirdComName.text = "You have seen " + birdComName
                                    tvBirdSciName.text = birdSciName
                                    tvBirdLongitudeLatitude.text = "Latitude: " + latitude + " Longitude: " + longitude
                                    //code attribution
                                    //the following code was taken from Stack Overflow and adapted
                                    //https://stackoverflow.com/questions/43862079/how-to-get-city-name-using-latitude-and-longitude-in-android
                                    //PEHLAJ
                                    //https://stackoverflow.com/users/6027638/pehlaj
                                    val gcd = Geocoder(this.requireContext(), Locale.getDefault())
                                    var addresses: List<Address>? = null
                                    try {
                                        if (latitude != null) {
                                            if (longitude != null) {
                                                addresses = gcd.getFromLocation(latitude.toDouble(), longitude.toDouble(), 1)
                                            }
                                        }
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                    if (addresses != null && addresses.size > 0) {
                                        val locality: String = addresses[0].locality
                                        val state : String = addresses[0].adminArea
                                        val country : String = addresses[0].countryName
                                        tvBirdSpecifiedLocation.text = locality + ", " + state + ", " + country
                                    }
                                    tvBirdDateAdded.text = dateAdded

                                    val mapFragment = BirdLocationMapFragment()
                                    val bundle = Bundle()
                                    bundle.putString("latitude", latitude)
                                    bundle.putString("longitude", longitude)
                                    mapFragment.arguments = bundle
                                    childFragmentManager.beginTransaction()
                                        .replace(R.id.mapContainer, mapFragment)
                                        .commit()
//
//                                val location = LatLng(latitude, longitude)
//                                val markerOptions = MarkerOptions().position(location)
//                                markerOptions.title(title)
//                                binding.map.add.addMarker(markerOptions)
////        val location = LatLng(latitude, longitude)
////        mMap.addMarker(MarkerOptions().position(location).title(title))
////        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
//                                mMap.animateCamera(
////                                    CameraUpdateFactory.newLatLngZoom(location, 15F)
//                                if (latitude != null) {
//                                    if (longitude != null) {
//                                        mapFragment.addMarker(latitude.toDouble(), longitude.toDouble(), birdComName.toString())
//                                    }
//                                }
                                }
                            }
                        }
                    }
                }.addOnCompleteListener(){
                    lytInfo.visibility = View.VISIBLE
                    imgShare.visibility = View.VISIBLE
                    prgLoad.visibility = View.GONE
                    imgclose.visibility = View.VISIBLE
                    val duration = getAudioFileDuration(recording)
                    setChronometerDuration(chronometerObservationDuration, duration)

                }.addOnFailureListener(){
                    Toast.makeText(context, "User data retrieval failed.", Toast.LENGTH_SHORT).show()
                }
            }
            catch (ex : Exception){
                Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    fun setChronometerDuration(chronometer: Chronometer, durationInMillis: Long) {
        val seconds = (durationInMillis / 1000).toInt()
        val minutes = seconds / 60
        //val hours = minutes / 60

        //val formattedTime = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
        val formattedTime = String.format("%02d:%02d", minutes % 60, seconds % 60)
        chronometer.text = formattedTime
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }

    fun getAudioFileDuration(audioFilePath: String): Long {
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(audioFilePath)
            mediaPlayer.prepare()
            return mediaPlayer.duration.toLong()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            mediaPlayer.release()
        }
        return 0L
    }

    override fun onDestroy() {
        super.onDestroy()

        // Release MediaPlayer when the fragment is being destroyed
        mediaPlayer.release()
        chronometerObservationElapsed.stop()
    }
}