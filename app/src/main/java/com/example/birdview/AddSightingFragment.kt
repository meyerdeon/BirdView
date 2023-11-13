package com.example.birdview

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaPlayer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.birdview.adapters.BirdListAdapter
import com.example.birdview.api_interfaces.EBirdApiService
import com.example.birdview.api_interfaces.FlickrService
import com.example.birdview.api_interfaces.XenoCantoService
import com.example.birdview.models.Bird
import com.example.birdview.models.BirdWithImage
import com.example.birdview.models.FlickrResponse
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class BirdListDialogFragment(private val fragmentManager : FragmentManager) : Fragment() {
    private lateinit var newRecyclerView : RecyclerView
    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 101
    }
    // private var newArrayList = mutableListOf<ActualObservation>()
    //private var urls = mutableListOf<String>()
    private lateinit var text : TextView
    private lateinit var prgLoad : ProgressBar
    private lateinit var cardViewUnidentified : CardView
    private lateinit var cardViewManualEntry : CardView
    private lateinit var currentLatLng : LatLng
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_sighting, container, false)
//        if (getDialog() != null && getDialog()?.getWindow() != null) {
//            getDialog()?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
//            getDialog()?.getWindow()?.requestFeature(Window.FEATURE_NO_TITLE)
//        }
        text = view.findViewById<TextView>(R.id.imgBird)
        prgLoad = view.findViewById(R.id.prgLoad)
        cardViewUnidentified = view.findViewById(R.id.cardViewUnidentified)
        cardViewManualEntry = view.findViewById(R.id.cardViewManualSighting)
        text.setText(null)
        newRecyclerView = view.findViewById(R.id.rvObservations)
        newRecyclerView.layoutManager = LinearLayoutManager(view.context)
        newRecyclerView.setHasFixedSize(true)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireContext())

        mediaPlayer = MediaPlayer()
        checkLocationPermissions()

        cardViewUnidentified.setOnClickListener{
            val childFragment = UnidentifiedDialogFragment(currentLatLng.latitude.toString(), currentLatLng.longitude.toString())
            childFragment.show(fragmentManager, UnidentifiedDialogFragment::class.java.simpleName)
        }

        cardViewManualEntry.setOnClickListener(){
            val childFragment = BirdManualEntryDialogFragment(currentLatLng.latitude.toString(), currentLatLng.longitude.toString())
            childFragment.show(fragmentManager, BirdManualEntryDialogFragment::class.java.simpleName)
        }

        return view
    }

    fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this.requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this.requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            // Location permissions have not been granted; request them
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            // Location permissions have been granted; you can proceed with location-related tasks
            // e.g., start location updates
            val location = fusedLocationProviderClient.lastLocation
            location.addOnSuccessListener {
                if (it != null) {
                    currentLatLng = LatLng(it.latitude, it.longitude)
//                    Toast.makeText(
//                        context,
//                        it.latitude.toString() + it.longitude.toString(),
//                        Toast.LENGTH_SHORT
//                    ).show()
                    findBirds(it.latitude, it.longitude)
                    //       placeMarkerOnMap(currentLatLng)
                    //     mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15F))
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val location = fusedLocationProviderClient.lastLocation
            location.addOnSuccessListener {
                if (it != null) {
                    val currentLatLng = LatLng(it.latitude, it.longitude)
                    Toast.makeText(
                        context,
                        it.latitude.toString() + it.longitude.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    findBirds(it.latitude, it.longitude)
                    //       placeMarkerOnMap(currentLatLng)
                    //     mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15F))
                }
            }
            } else {
                Toast.makeText(context, "Cannot get your location.", Toast.LENGTH_SHORT).show()
            }
        }
    }

//    override fun onStart() {
//        super.onStart()
//        val dialog = dialog
//        if (dialog != null) {
//            val width = ViewGroup.LayoutParams.MATCH_PARENT
//            val height = ViewGroup.LayoutParams.MATCH_PARENT
//            dialog.window?.setLayout(width, height)
//        }
//    }

    fun findBirds(latitude : Double, longitude: Double){
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.ebird.org/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val eBirdApiService = retrofit.create(EBirdApiService::class.java)
//        val latitude = -25.78619571078393 // Replace with your current latitude
//        val longitude = 28.29921991113278 // Replace with your current longitude
        var apiKey = "p7epg6jpkgis" // Replace with your eBird API key

        val call =
            eBirdApiService.getBirdObservations(latitude, longitude, 10, 30, 30, "json", apiKey)
        val newArrayList = arrayListOf<BirdWithImage>()
        call.enqueue(object : Callback<List<Bird>> {
            override fun onResponse(
                call: Call<List<Bird>>,
                response: Response<List<Bird>>
            ) {
                if (response.isSuccessful) {
                    val observations = response.body()
                    if (observations != null) {
                        for (obs in observations) {
                            newArrayList.add(
                                BirdWithImage(
                                    obs.speciesCode,
                                    obs.comName,
                                    obs.sciName,
                                    null,
                                    null
                                )
                            )
                        }
                    }
                    searchImagesByText(newArrayList, latitude, longitude)
                    //   newRecyclerView.adapter = BirdAdapter(newArrayList)
                    // Process the bird observations here
                } else {
                    Toast.makeText(context, "Error processing birds", Toast.LENGTH_SHORT).show()
                    // Handle error
                }

                //displayData()
            }

            override fun onFailure(call: Call<List<Bird>>, t: Throwable) {
                Toast.makeText(context, "Error processing birds "+t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
    fun searchImagesByText(newArrayList: ArrayList<BirdWithImage>, latitude : Double, longitude: Double) {
//        val apiKey = "d53d32be5b38ab4a962a8f7f433a5d57" // Replace with your Flickr API key
//        var count = 0
//        val flickrService = Retrofit.Builder()
//            .baseUrl("https://api.flickr.com/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(FlickrService::class.java)
//
//        for (i in 0 until newArrayList.size){
//            val flickrCall = flickrService.searchPhotos(apiKey = apiKey, text = newArrayList[i].comName)
//            //  var url = ""
//            flickrCall.enqueue(object : Callback<FlickrResponse> {
//                override fun onResponse(call: Call<FlickrResponse>, response: Response<FlickrResponse>) {
//                    if (response.isSuccessful) {
//                        val flickrResponse = response.body()
//
//                        // Extract image URLs from the response and display them
//                        val imageUrls = flickrResponse?.photos?.photo?.map { photo ->
//                            "https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}_m.jpg"
//                        }
//
//                        // Display the image URLs as needed
//                        if (imageUrls != null) {
//                            for (imageUrl in imageUrls) {
//                                //item.url = imageUrl
//                                //  urls.add(imageUrl)
//                                newArrayList[i].url = imageUrl
//                                count++;
//                                text.setText(null)
//                                //text.setText(imageUrl)
//                                if(count == newArrayList.size){
//                                    newRecyclerView.adapter = BirdListAdapter(newArrayList, latitude.toString(), longitude.toString(), fragmentManager, mediaplayer)
//                                    //code attribution
//                                    //the following code was taken from Stack Overflow and adapted
//                                    //https://stackoverflow.com/questions/5442183/using-the-animated-circle-in-an-imageview-while-loading-stuff
//                                    //WSBT
//                                    //https://stackoverflow.com/users/1032613/wsbt
//                                    prgLoad.setVisibility(View.GONE);
//                                    text.setText("Click on a bird to add it to your observation list.")
//                                }
//                            }
//                        }
//                    }
//                    else {
//                        Toast.makeText(context, "Error processing images", Toast.LENGTH_SHORT).show()
//                    }
//                }
//
//                override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
//                    Toast.makeText(context, "Error processing images " + t.message, Toast.LENGTH_SHORT).show()
//                }
//            })
//        }
        val apiKey = "d53d32be5b38ab4a962a8f7f433a5d57" // Replace with your Flickr API key
        val flickrService = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FlickrService::class.java)
        val deferredList = ArrayList<Deferred<Any>>() // Use Deferred<Any> to handle both success and failure

        val coroutineScope = CoroutineScope(Dispatchers.Main)

        for (i in 0 until newArrayList.size) {
            val flickrCall = flickrService.searchPhotos(apiKey = apiKey, text = newArrayList[i].sciName)

            val deferred = coroutineScope.async(Dispatchers.IO) {
                try {
                    val response = flickrCall.execute()

                    if (response.isSuccessful) {
                        val flickrResponse = response.body()

                        val imageUrls = flickrResponse?.photos?.photo?.map { photo ->
                            "https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}_m.jpg"
                        }

                        if (imageUrls != null && imageUrls.isNotEmpty()) {
                            newArrayList[i].url = imageUrls.first()
                        }

                        // Return a success indicator
                        Unit
                    } else {
                        // Handle Flickr API error
                        Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                        // Return the error as an exception
                        throw RuntimeException("Flickr API error: ${response.code()}")
                    }
                } catch (t: Throwable) {
                    // Handle network error
                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    // Return the error as an exception
                    throw t
                }
            }

            deferredList.add(deferred)
        }

        coroutineScope.launch {
            deferredList.awaitAll()

            // Check for any errors
            if (deferredList.any { it.isCompleted && it.getCompleted() is Throwable }) {
                // Handle errors if needed
                Toast.makeText(context, "Some requests failed", Toast.LENGTH_SHORT).show()
            }
            searchRecordings(newArrayList, latitude, longitude)
//            getRecordings(newArrayList)
            // All requests completed (with or without errors)
//            newArrayList.add(0, ActualObservation("Unknown", "Unknown", "", "", ""))
//            newRecyclerView.adapter = BirdAdapter(newArrayList)
//            prgLoad.visibility = View.GONE
//            text.text = "Click on a bird to add it to your observation list."
        }
    }

    fun searchRecordings(newArrayList: ArrayList<BirdWithImage>, latitude : Double, longitude: Double){
        val xenoCantoService = Retrofit.Builder()
            .baseUrl("https://xeno-canto.org/api/2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XenoCantoService::class.java)
        val deferredList = ArrayList<Deferred<Any>>() // Use Deferred<Any> to handle both success and failure

        val coroutineScope = CoroutineScope(Dispatchers.Main)

        coroutineScope.launch {
            for (i in 0 until newArrayList.size) {
                val xenoCantoCall = xenoCantoService.getRecordings(query = "gen:${newArrayList[i].sciName}")

                val deferred = async(Dispatchers.IO) {
                    try {
                        val response = xenoCantoCall.execute()

                        if (response.isSuccessful) {
                            val recordings = response.body()

                            if ((recordings?.numRecordings ?: 0) > 0) {
                                // Assuming you want to use the first recording URL
                                newArrayList[i].recording = recordings!!.recordings[0].file
                            } else {
                                Toast.makeText(context, "No recordings found for the specified bird.", Toast.LENGTH_SHORT).show()
                            }

                            // Return success
                            Unit
                        } else {
                            Toast.makeText(context, "Error ${response.code()}", Toast.LENGTH_SHORT).show()
                            // Return the error code
                            response.code()
                        }
                    } catch (t: Throwable) {
                        Toast.makeText(context, " Error ${t.message}", Toast.LENGTH_SHORT).show()
                        // Return the exception
                        t
                    }
                }

                deferredList.add(deferred)

                // Introduce a delay of 1 second between requests
                delay(1000)
            }

            deferredList.awaitAll()

            // Check for any errors
            if (deferredList.any { it.isCompleted && it.getCompleted() is Throwable }) {
                // Handle errors if needed
                Toast.makeText(context, "Some requests failed", Toast.LENGTH_SHORT).show()
            }

            // All requests completed (with or without errors)
            newRecyclerView.adapter = BirdListAdapter(newArrayList, latitude.toString(), longitude.toString(), fragmentManager, mediaPlayer)
            //code attribution
            //the following code was taken from Stack Overflow and adapted
            //https://stackoverflow.com/questions/5442183/using-the-animated-circle-in-an-imageview-while-loading-stuff
            //WSBT
            //https://stackoverflow.com/users/1032613/wsbt
            prgLoad.visibility = View.GONE
            text.text = "Click on a bird to add it to your observation list."
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Release MediaPlayer when the fragment is being destroyed
        mediaPlayer.release()
        Toast.makeText(context, "Hel", Toast.LENGTH_SHORT).show()
    }
}