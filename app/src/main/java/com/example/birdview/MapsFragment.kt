package com.example.birdview

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.NumberPicker
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
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

//import com.example.birdview.databinding.ActivityMapsBinding

class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{


    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val Base_Url = "https://api.ebird.org/v2/ref/"

   // private lateinit var binding: ActivityMapsBinding

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

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())

        getLocationUpdates()
        return view
    }

    //Get bird hotspots from api
    private fun getBirdHotspots(location: LatLng)
    {
        val hotspots = mutableListOf<BirdHotspot>()

        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(Base_Url)
            .build()

        val api = retrofitBuilder.create(HotspotsApiInterface::class.java)

        CoroutineScope(Dispatchers.Main).launch {
            if (location != null ){
                val result = api.getHotspot("hotspot/geo?dist=10&lat=${location!!.latitude}&lng=${location!!.longitude}")
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

        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener {
                if (it != null) {
                    source = LatLng(it.latitude, it.longitude)

                }else{
                    Toast.makeText(this.requireContext(), "Cannot get location.", Toast.LENGTH_SHORT).show()
                }

            }


        getDirections.setOnClickListener {

            var uri = Uri.parse("https://www.google.com/maps/dir/$source/$destination")
            var intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

            dialog.dismiss()
        }

    }

}






