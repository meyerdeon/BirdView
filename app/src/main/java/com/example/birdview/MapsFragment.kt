package com.example.birdview

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.birdview.api_interfaces.HotspotsApiInterface
import com.example.birdview.models.BirdHotspot
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
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

                                    )
                                hotspots.add(apiContent)
                            }
                        }else{
                            break
                        }
                    }

                    for (hotspot in hotspots){
                        val location = LatLng(hotspot.latitude!!.toDouble(), hotspot.longitude!!.toDouble())
                        placeMarkerOnMap(location)
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
    private fun placeMarkerOnMap(currentLatLng: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLng)
        markerOptions.title("$currentLatLng")
        mMap.addMarker(markerOptions)
    }

    private fun placeMarkerOnMap(currentLatLng: LatLng, num: Int) {
        val markerOptions = MarkerOptions().position(currentLatLng)
        markerOptions.title("My location")
        mMap.addMarker(markerOptions)
    }

    override fun onMarkerClick(p0: Marker) = false

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

}






