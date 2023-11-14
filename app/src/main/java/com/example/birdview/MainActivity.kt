package com.example.birdview

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.birdview.databinding.ActivityMainBinding
import com.example.birdview.models.TripCard
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


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
                R.id.miTripCards -> replaceFragment(TripCardsListFragment())
                R.id.miSettings -> replaceFragment(SettingsFragment())
                R.id.miAddSighting -> showAddItemDialog()
            }
            true
        }
//        binding.fabActionButton.setOnClickListener{
//            for (i in 0 until binding.bottomNavMenu.menu.size()) {
//                binding.bottomNavMenu.menu.getItem(i).isChecked = false
//            }
//            replaceFragment(BirdListDialogFragment(supportFragmentManager))
//        }
    }


    fun replaceFragment(fragment: Fragment){
        if(fragment != null){
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.map, fragment)
            transaction.commit()
        }
    }

    private fun showAddItemDialog(){
        /* this code was taken and adapted from StackOverflow
        * https://stackoverflow.com/questions/64700032/how-do-i-learn-which-context-i-have-to-use-for-each-situation-in-android
        * Author: Alejandro Agüero
        * https://stackoverflow.com/users/13943853/alejandro-ag%c3%bcero
        */
        val dialog = Dialog(this)
        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.add_button_bottom_sheet_layout)

        val addTripcard = dialog.findViewById<Button>(R.id.btnAddTripCard)
        val addSighting = dialog.findViewById<Button>(R.id.btnAddSighting)

        addTripcard.setOnClickListener {
            showAddTripcardDialog()
            dialog.dismiss()
        }

        addSighting.setOnClickListener {
            replaceFragment(AddSightingFragment(supportFragmentManager, null))
            dialog.dismiss()
        }

    }

    private fun showAddTripcardDialog(){
        /* this code was taken and adapted from StackOverflow
        * https://stackoverflow.com/questions/64700032/how-do-i-learn-which-context-i-have-to-use-for-each-situation-in-android
        * Author: Alejandro Agüero
        * https://stackoverflow.com/users/13943853/alejandro-ag%c3%bcero
        */
        val dialog = Dialog(this)
        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.create_tripcard_bottom_sheet_layout)

        val tripcardName = dialog.findViewById<TextInputEditText>(R.id.etTripName)
        val createTripcard = dialog.findViewById<Button>(R.id.btnCreateTripcard)
        val cancel = dialog.findViewById<Button>(R.id.btnCancel)

        createTripcard.setOnClickListener {
            //store tripcard in firebase

            val user = FirebaseAuth.getInstance().currentUser
            try {

                val trip = TripCard(null, tripcardName.text.toString(), LocalDate.now().toEpochDay(), null, null)
                //GlobalVariablesMethods.user.categories?.add(cat)
                val database = FirebaseDatabase.getInstance()
                val databaseReference = database.getReference("Users")
                //code attribution
                //the following code was taken from Stack Overflow and adapted
                //https://stackoverflow.com/questions/60432256/on-insert-data-in-firebase-realtime-database-it-deletes-previous-data
                //ashok
                //https://stackoverflow.com/users/12746098/ashok
                databaseReference.child(user?.uid.toString()).child("tripcards").push().setValue(trip).addOnCompleteListener() {
                    if (it.isComplete){
                        Toast.makeText(this.applicationContext, "Tripcard created successfully.", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this.applicationContext, "User data retrieval failed.", Toast.LENGTH_SHORT).show()
                    }
                }.addOnCompleteListener(){
                    replaceFragment(TripCardsListFragment())
                    dialog.dismiss()
                }.addOnFailureListener(){
                    Toast.makeText(this.applicationContext, "Failure", Toast.LENGTH_SHORT).show()
                }

            }
            catch (ex : Exception){
                Toast.makeText(this.applicationContext, ex.message, Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        cancel.setOnClickListener {
            dialog.dismiss()
        }

    }
}