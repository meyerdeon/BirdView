package com.example.birdview

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.birdview.api_interfaces.HotspotsApiInterface
import com.example.birdview.models.BirdHotspot
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import kotlin.math.roundToInt

//import com.example.birdview.databinding.ActivityMapsBinding

/**
 * Code attribution
 *
 * Some of this code was taken from Youtube
 * https://www.youtube.com/watch?v=Iq9yQmVOThE
 * Author: Coding Adventure
 * https://youtube.com/@codingadventure1369
 *
 * Some of this code was taken from Youtube
 * https://www.youtube.com/watch?v=yXKhU_8ujxU
 * Author: DroidTutorials
 * https://www.youtube.com/@droidtutorials3688
 *
 * Some of this code was taken from developers.google.com
 * https://developers.google.com/maps/documentation/urls/android-intents#kotlin_12
 * Author: Google
 *
 * Some of this code was taken from Youtube
 * https://www.youtube.com/watch?v=t-3TOke8tq8
 * Author: Philip Lackner
 * https://www.youtube.com/@PhilippLackner
 *
 * Some of this code was taken from Youtube
 * https://www.youtube.com/watch?v=5gFrXGbQsc8&list=PLklWDN5GwmGEuxBXialPT1LRzpHWZuU6P&index=2
 * Author: Yash Nagayach
 * https://www.youtube.com/@YashNagayachCode
 *
 * Some of this code was taken from Youtube
 * https://www.youtube.com/watch?v=urLA8z6-l3k&list=PLgCYzUzKIBE-vInwQhGSdnbyJ62nixHCt&index=2
 * Auhtor: CodingWithMitch
 * https://www.youtube.com/@codingwithmitch
 */

class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{


    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val Base_Url = "https://api.ebird.org/v2/ref/"
    private lateinit var dbRef: DatabaseReference

    var distance: Float? = null
   // private lateinit var binding: ActivityMapsBinding

    private lateinit var btnCurrentLocation: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

       // binding = ActivityMapsBinding.inflate(layoutInflater)
       // setContentView(binding.root)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_maps, container, false)

        btnCurrentLocation = view.findViewById(R.id.btnCurrentLocation)
        dbRef = FirebaseDatabase.getInstance().getReference("Users")

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())

        getLocationUpdates()

        btnCurrentLocation.setOnClickListener {
            setUpMap()
        }
        return view
    }

    //Get bird hotspots from api
    private fun getBirdHotspots(location: LatLng)
    {
        val hotspots = mutableListOf<BirdHotspot>()

        var result: Response<String>





        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(Base_Url)
            .build()

        val api = retrofitBuilder.create(HotspotsApiInterface::class.java)

        CoroutineScope(Dispatchers.Main).launch {
            if (location != null ){

                val getDistance = async{
                    //get distance
                    getMapRange()
                }

                result = api.getHotspot("hotspot/geo?dist=${getDistance.await()?:50}&lat=${location!!.latitude}&lng=${location!!.longitude}")
                if (result.isSuccessful && result.body() != null){

                    var results = result.body().toString()
                    val lines = results.split("\n")

                    // Read the results line by line
                    for (line in lines) {
                        if (line!= ""){
                            val parts = line.split(",")

                            for (part in parts){
                                val apiContent = BirdHotspot(

                                    parts[4].toDouble(),
                                    parts[5].toDouble(),
                                    parts[6].toString()

                                    )
                                hotspots.add(apiContent)
                            }
                        }else{
                            break
                        }
                    }

                    for (hotspot in hotspots){
                        val location = LatLng(hotspot.latitude!!.toDouble(), hotspot.longitude!!.toDouble())
                        placeMarkerOnMap(location, hotspot.locationName!!)
                    }
                }else{
                    Toast.makeText(this@MapsFragment.requireContext(), "There are no bird hotspots nearby", Toast.LENGTH_SHORT).show()

                }
            }

        }
    }

    suspend fun getMapRange(): Float{
        var distance = 50f //default value for map range incase user has not set their preference yet
        val user = FirebaseAuth.getInstance().currentUser
        dbRef.child(user?.uid.toString()).child("Settings").child("mapRangePreference").get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")
            if (it.value != null){
               distance = it.value.toString().toFloat()
            }

        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

        dbRef.child(user?.uid.toString()).child("Settings").child("unitMeasurement").get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")
            if (it.exists()){
                if (it.value!!.toString().equals("imperial")){
                    //convert distance to imperial
                    if (distance != null){
                        distance = distance!!*1.60934f

                    }

                }
            }
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

        delay(2000)
        return distance
    }
    /********************************************************************************************/
    /** implemented map members */
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        setUpMap()

    }
    private fun placeMarkerOnMap(currentLatLng: LatLng, locatioName: String) {
        val markerOptions = MarkerOptions().position(currentLatLng)
        markerOptions.title("$locatioName")
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
        mMap.addMarker(markerOptions)
    }

    private fun placeMarkerOnMap(currentLatLng: LatLng, num: Int) {
        val markerOptions = MarkerOptions().position(currentLatLng)
        markerOptions.title("My location")
        mMap.addMarker(markerOptions)
    }

    override fun onMarkerClick(marker: Marker): Boolean
    {
        showDirectionsDialog(marker.title.toString(), marker.position)

        return false
    }

    /********************************************************************************************/
    private fun setUpMap(){

        if (ActivityCompat.checkSelfPermission(this.requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this.requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this.requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }

        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener {
                if (it != null) {
                    val location = LatLng(it.latitude, it.longitude)

                    placeMarkerOnMap(location!!, 0)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location!!, 15F))

                    getBirdHotspots(location)
                }else{
                    Toast.makeText(this.requireContext(), "Cannot get location.", Toast.LENGTH_SHORT).show()
                }

            }

        }

    /********************************************************************************************/
    /** Location updates methods**/
    private fun getLocationUpdates()
    {


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireContext())
        locationRequest = LocationRequest()
        locationRequest.interval = 50000
        locationRequest.fastestInterval = 50000
        locationRequest.smallestDisplacement = 170f // 170 m = 0.1 mile
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //set according to your app function
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                if (locationResult.locations.isNotEmpty()) {
                    // get latest location
                    val location =
                        locationResult.lastLocation

                }


            }
        }
    }

    //start location updates
    private fun startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    // stop location updates
    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    // stop receiving location update when activity not visible/foreground
    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    // start receiving location update when activity  visible/foreground
    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }
    /********************************************************************************************/

    private fun showDirectionsDialog(place: String, destination: LatLng){
        var source: LatLng? = null

        val dialog = Dialog(requireContext())
        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.directions_bottom_sheet_layout)

        val getDirections = dialog.findViewById<Button>(R.id.btnGetDirections)
        val locationName = dialog.findViewById<TextView>(R.id.txtLocationName)
        val displayDistance = dialog.findViewById<TextView>(R.id.txtDistance)

        locationName.text = place

        if (place.equals("My location")){
            getDirections.isVisible = false
        }

        if (ActivityCompat.checkSelfPermission(this.requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this.requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this.requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }

        //the following code was taken and adapted from StackOverflow
        //https://stackoverflow.com/questions/6981916/how-to-calculate-distance-between-two-locations-using-their-longitude-and-latitu
        //author: sandeepmaaram
        //https://stackoverflow.com/users/2720929/sandeepmaaram
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener {
                if (it != null) {
                    source = LatLng(it.latitude, it.longitude)
                    val src = Location("source")
                    val dest = Location("destination")
                    src.set(it)
                    dest.latitude = destination.latitude
                    dest.longitude = destination.longitude
                    val distance = (src).distanceTo(dest) * 0.001 //convert to kilometers
                    displayDistance.text = "${distance.roundToInt()}km away"
                }else{
                    Toast.makeText(this.requireContext(), "Cannot get location.", Toast.LENGTH_SHORT).show()
                }

            }


        getDirections.setOnClickListener {

            /*var uri = Uri.parse("https://www.google.com/maps/dir/$source/$destination")
            var intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
*/

            val gmmIntentUri =
                Uri.parse("google.navigation:q=${destination!!.latitude},${destination!!.longitude}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
            dialog.dismiss()
        }

    }

}






