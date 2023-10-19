package com.example.birdview

import android.content.ContentProviderClient
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task

//import com.example.birdview.databinding.ActivityMapsBinding

class BirdLocationMapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var googleMap: GoogleMap

    private lateinit var fusedLoctionProviderClient: FusedLocationProviderClient

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
        val view = inflater.inflate(R.layout.fragment_bird_location_map, container, false)
        // Obtain latitude and longitude coordinates (replace with actual values)
        val latitude = arguments?.getString("latitude").toString()
        val longitude = arguments?.getString("longitude").toString()
        val latLng : LatLng = LatLng(latitude.toDouble(), longitude.toDouble())
//        val latitude = 37.7749
//        val longitude = -122.4194
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            googleMap = map
            // Now you can configure the map and add a marker
            val markerOptions = MarkerOptions()
                .position(latLng) // Replace with your latitude and longitude
                .title("Marker Title")
            googleMap.addMarker(markerOptions)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15F))
        }
        return view
    }

    override fun onMarkerClick(p0: Marker) = false
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.setOnMarkerClickListener(this)
    }
}