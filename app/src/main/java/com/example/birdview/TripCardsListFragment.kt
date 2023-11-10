package com.example.birdview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.birdview.adapters.ObservationListAdapter
import com.example.birdview.adapters.TripcardListAdapter
import com.example.birdview.databinding.FragmentObservationsBinding
import com.example.birdview.models.TripCard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class TripCardsListFragment : Fragment() {
    private lateinit var newRecyclerView : RecyclerView
    private lateinit var newArrayList : ArrayList<TripCard>
    private lateinit var binding: FragmentObservationsBinding
    private lateinit var prgLoad : ProgressBar
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

        newRecyclerView = view.findViewById(R.id.rvTripcards)
        newRecyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        newRecyclerView.setHasFixedSize(true)
        newArrayList = arrayListOf<TripCard>()

        getUserData()
        return view
    }

    private fun getUserData(){
        try{
            var speciesArrayList = arrayListOf<String>()
            var speciesCount = 0
            var tripObservationsCount = 0
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
                        val id = trip.key
                        val tripName = trip.child("name").getValue(String::class.java)
                        val tripDate = trip.child("date").getValue(Long::class.java)

                        val _trip = TripCard(id, tripName, tripDate)
                        newArrayList.add(_trip)

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
                    }
                }
            }.addOnCompleteListener(){
                if (it.isComplete){
                    prgLoad.visibility = View.GONE
                    newRecyclerView.adapter = TripcardListAdapter(newArrayList, tripObservationsCount, speciesCount)
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

}