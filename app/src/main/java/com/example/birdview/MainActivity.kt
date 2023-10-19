package com.example.birdview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.birdview.databinding.ActivityMainBinding
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

//    private fun showFullScreenDialog() {
//        replaceFragment(MapsFragment())
//        binding.bottomNavMenu.getMenu().findItem(R.id.miHome).setChecked(true);
//        val fragmentManager : FragmentManager = supportFragmentManager
//        val dialogFragment = BirdListDialogFragment(fragmentManager)
//        dialogFragment.show(fragmentManager, BirdListDialogFragment::class.java.simpleName)
//    }

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
        binding.fabActionButton.setOnClickListener{
            replaceFragment(BirdListDialogFragment(supportFragmentManager))
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