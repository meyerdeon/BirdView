package com.example.birdview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.birdview.adapters.ObservationListAdapter
import com.example.birdview.adapters.TripcardListAdapter
import com.example.birdview.databinding.FragmentAddSightingBinding
import com.example.birdview.databinding.FragmentObservationsBinding
import com.example.birdview.models.TripCard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class TripObservationsListFragment(private val obsvCount: Int, private val speciescount: Int ) : Fragment() {
    private lateinit var newRecyclerView : RecyclerView
    private lateinit var newArrayList : ArrayList<Observation>
    private lateinit var newTripIdArrayList : ArrayList<String>
    private lateinit var binding: FragmentObservationsBinding
    private lateinit var prgLoad : ProgressBar
    private lateinit var btnAddSigthing : Button
    private lateinit var tvObservationsCount: TextView
    private lateinit var tvSpeciesCount: TextView
    private lateinit var tvDate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val View = inflater.inflate(R.layout.fragment_trip_observations_list, container, false)
        btnAddSigthing = View.findViewById(R.id.btnAddTripSighting)
        prgLoad = View.findViewById(R.id.prgLoad)
        tvObservationsCount = View.findViewById(R.id.tvObservationAmount)
        tvSpeciesCount = View.findViewById(R.id.tvSpeciesAmount)
        tvDate = View.findViewById(R.id.tvTripDate)

        btnAddSigthing.visibility = android.view.View.GONE
        tvObservationsCount.text = "${obsvCount ?: 0} observations"
        tvSpeciesCount.text = "${speciescount ?: 0} species"
        tvDate.text = arguments?.getString("date")

        newRecyclerView = View.findViewById(R.id.rvTripObservations)
        newRecyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        newRecyclerView.setHasFixedSize(true)
        newArrayList = arrayListOf<Observation>()

        getUserData()

        val tripId = arguments?.getString("tripId")

        btnAddSigthing.setOnClickListener {
            replaceFragment( BirdListDialogFragment(parentFragmentManager, tripId), null)
        }
        return View
    }

    fun replaceFragment(fragment: Fragment, id: String?){
        if(fragment != null){
            val bundle = Bundle()
            bundle.putString("tripId", id)
            fragment.arguments = bundle
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.map, fragment)
            transaction.commit()
        }
    }

    private fun getUserData(){
        try{
            newArrayList = arrayListOf()
            val user = FirebaseAuth.getInstance().currentUser
            val database = FirebaseDatabase.getInstance()
            val databaseReference = database.getReference("Users")
            databaseReference.child(user?.uid.toString()).get().addOnSuccessListener {
                if(it.exists()){
                    for(obsr in it.child("tripcards").child(arguments?.getString("tripId")!!).child("observations").children){
                        //code attribution
                        //the following code was taken from Stack Overflow and adapted
                        //https://stackoverflow.com/questions/38232140/how-to-get-the-key-from-the-value-in-firebase
                        //Frank van Puffelen
                        //https://stackoverflow.com/users/209103/frank-van-puffelen
                        val id = obsr.key
                        val birdImage = obsr.child("birdImage").getValue(String::class.java)
                        val birdComName = obsr.child("birdComName").getValue(String::class.java)
                        val birdSciName = obsr.child("birdSciName").getValue(String::class.java)
                        val latitude = obsr.child("latitude").getValue(String::class.java)
                        val longitude = obsr.child("longitude").getValue(String::class.java)
                        val dateAdded = obsr.child("dateAdded").getValue(String::class.java)
                        val obs = Observation(id, birdImage, birdComName, birdSciName,null, latitude, longitude, dateAdded)
                        newArrayList.add(obs)
                        newArrayList
                    }
                }
            }.addOnCompleteListener(){
                if (it.isComplete){
                    prgLoad.visibility = View.GONE
                    btnAddSigthing.visibility = View.VISIBLE
                    newRecyclerView.adapter = ObservationListAdapter(newArrayList, 1, arguments?.getString("tripId"))
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