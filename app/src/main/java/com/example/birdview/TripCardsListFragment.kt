package com.example.birdview

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.birdview.adapters.ObservationListAdapter
import com.example.birdview.adapters.TripcardListAdapter
import com.example.birdview.databinding.FragmentObservationsBinding
import com.example.birdview.models.TripCard
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate


class TripCardsListFragment : Fragment() {
    private lateinit var newRecyclerView : RecyclerView
    private lateinit var newArrayList : ArrayList<TripCard>
    private lateinit var binding: FragmentObservationsBinding
    private lateinit var prgLoad : ProgressBar
    private lateinit var btnCreateTripcard: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_trip_cards_list, container, false)
        prgLoad = view.findViewById(R.id.prgLoad)
        btnCreateTripcard = view.findViewById(R.id.btnNewTripcard)

        btnCreateTripcard.visibility = View.GONE
        newRecyclerView = view.findViewById(R.id.rvTripcards)
        newRecyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        newRecyclerView.setHasFixedSize(true)
        newArrayList = arrayListOf<TripCard>()

        btnCreateTripcard.setOnClickListener {
            showAddTripcardDialog()
        }

        getUserData()
        return view
    }

    private fun getUserData(){
        try{
            var speciesArrayList = arrayListOf<String>()

            newArrayList = arrayListOf()
            val user = FirebaseAuth.getInstance().currentUser
            val database = FirebaseDatabase.getInstance()
            val databaseReference = database.getReference("Users")
            databaseReference.child(user?.uid.toString()).get().addOnSuccessListener {
                if(it.exists()){
                    for(trip in it.child("tripcards").children){
                        //code attribution
                        //the following code was taken from Stack Overflow and adapted
                        //https://stackoverflow.com/questions/38232140/how-to-get-the-key-from-the-value-in-firebase
                        //Frank van Puffelen
                        //https://stackoverflow.com/users/209103/frank-van-puffelen
                        var speciesCount = 0
                        var tripObservationsCount = 0

                        val id = trip.key
                        val tripName = trip.child("name").getValue(String::class.java)
                        val tripDate = trip.child("date").getValue(Long::class.java)

                        //checks to see if user has added any observations to this tripcard
                        if (trip.child("observations").exists()){
                            for (tripObsr in trip.child("observations").children){
                                tripObservationsCount++ //count how many observations
                                val birdSciName = tripObsr.child("birdSciName")

                                //count how many different species there are in the trip
                                if (!speciesArrayList.contains(birdSciName.toString())){
                                    speciesCount++
                                }
                            }
                        }
                        val _trip = TripCard(id, tripName, tripDate,speciesCount, tripObservationsCount)
                        newArrayList.add(_trip)


                    }
                }
            }.addOnCompleteListener(){
                if (it.isComplete){
                    prgLoad.visibility = View.GONE
                    btnCreateTripcard.visibility = View.VISIBLE
                    newRecyclerView.adapter = TripcardListAdapter(newArrayList, 0, 0)
                }
                else{
                    Toast.makeText(context, "User data retrieval failed.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener(){
                Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show()
            }
        }
        catch(ex : Exception){
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddTripcardDialog(){
        /* this code was taken and adapted from StackOverflow
        * https://stackoverflow.com/questions/64700032/how-do-i-learn-which-context-i-have-to-use-for-each-situation-in-android
        * Author: Alejandro Agüero
        * https://stackoverflow.com/users/13943853/alejandro-ag%c3%bcero
        */
        val dialog = Dialog(requireContext())
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

                val database = FirebaseDatabase.getInstance()
                val databaseReference = database.getReference("Users")
                databaseReference.child(user?.uid.toString()).child("tripcards").push().setValue(trip).addOnCompleteListener() {
                    if (it.isComplete){
                        Toast.makeText(requireContext(), "Tripcard created successfully.", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(requireContext(), "User data retrieval failed.", Toast.LENGTH_SHORT).show()
                    }
                }.addOnCompleteListener(){
                    replaceFragment(TripCardsListFragment())
                    dialog.dismiss()
                }.addOnFailureListener(){
                    Toast.makeText(requireContext(), "Failure", Toast.LENGTH_SHORT).show()
                }

            }
            catch (ex : Exception){
                Toast.makeText(requireContext(), ex.message, Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        cancel.setOnClickListener {
            dialog.dismiss()
        }

    }

    fun replaceFragment(fragment: Fragment){
        if(fragment != null){
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.map, fragment)
            transaction.commit()
        }
    }

}