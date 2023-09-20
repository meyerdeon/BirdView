package com.example.birdview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.birdview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        replaceFragment(MapsFragment())
        //set bottom navigation view functionality
        binding.bottomNavMenu.setOnItemSelectedListener {
            when(it.itemId){
                R.id.miHome -> replaceFragment(MapsFragment())
                R.id.miObservations -> replaceFragment(ObservationsFragment())
                R.id.miTripCards -> replaceFragment(TripCardsFragment())
                R.id.miSettings -> replaceFragment(SettingsFragment())
            }
            true
        }
    }


    fun replaceFragment(fragment: Fragment){
        if(fragment != null){
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.map, fragment)
            transaction.commit()
        }
    }
}