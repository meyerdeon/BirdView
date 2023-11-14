package com.example.birdview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.birdview.adapters.ObservationListAdapter
import com.example.birdview.databinding.FragmentObservationListDialogBinding
import com.example.birdview.databinding.FragmentObservationsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ObservationsFragment : Fragment() {
    private lateinit var newRecyclerView : RecyclerView
    private lateinit var newArrayList : ArrayList<Observation>
    private lateinit var binding: FragmentObservationsBinding
    private lateinit var prgLoad : ProgressBar
    private lateinit var tv_data : TextView
//    lateinit var imageId : Array<Int>
//    lateinit var birdName : Array<String>
//    lateinit var birdScientificName : Array<String>
//    lateinit var birdLocation : Array<String>
//    lateinit var birdCount : Array<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_observations, container, false)
        prgLoad = view.findViewById(R.id.prgLoad)
        tv_data = view.findViewById(R.id.tv_data)
//        imageId = arrayOf(R.drawable.image1, R.drawable.image2, R.drawable.image1, R.drawable.image2, R.drawable.image1)
//        birdName = arrayOf("Bird Name", "Bird Name", "Bird Name", "Bird Name", "Bird Name")
//        birdScientificName = arrayOf("Scientific Bird Name", "Scientific Bird Name", "Scientific Bird Name", "Scientific Bird Name", "Scientific Bird Name")
//        birdLocation = arrayOf("Unknown", "Unknown", "Unknown", "Unknown", "Unknown")
//        birdCount = arrayOf(1, 2, 1, 2, 1)
        newRecyclerView = view.findViewById(R.id.rvObservations)
        newRecyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        newRecyclerView.setHasFixedSize(true)
        newArrayList = arrayListOf<Observation>()
        getUserData()

        return view
    }

//    private fun getData(){
//        for (i in 0 until imageId.size)
//        {
//            val item = Observation(imageId[i], birdName[i], birdScientificName[i], birdLocation[i], "Unknown date", birdCount[i])
//            newArrayList.add(item)
//        }
//        newRecyclerView.adapter = ObservationListAdapter(newArrayList)
//    }

    private fun getUserData(){
        try{
            tv_data.visibility = View.GONE
            newArrayList = arrayListOf()
            val user = FirebaseAuth.getInstance().currentUser
            val database = FirebaseDatabase.getInstance()
            val databaseReference = database.getReference("Users")
            databaseReference.child(user?.uid.toString()).get().addOnSuccessListener {
                if(it.exists()){
                    for(obsr in it.child("observations").children){
                        //code attribution
                        //the following code was taken from Stack Overflow and adapted
                        //https://stackoverflow.com/questions/38232140/how-to-get-the-key-from-the-value-in-firebase
                        //Frank van Puffelen
                        //https://stackoverflow.com/users/209103/frank-van-puffelen
                        val id = obsr.key
                        val birdImage = obsr.child("birdImage").getValue(String::class.java)
                        val birdComName = obsr.child("birdComName").getValue(String::class.java)
                        val birdSciName = obsr.child("birdSciName").getValue(String::class.java)
                        val birdRecording = obsr.child("recording").getValue(String::class.java)
                        val latitude = obsr.child("latitude").getValue(String::class.java)
                        val longitude = obsr.child("longitude").getValue(String::class.java)
                        val dateAdded = obsr.child("dateAdded").getValue(String::class.java)
                        val obs = Observation(id, birdImage, birdComName, birdSciName, birdRecording, latitude, longitude, dateAdded)
                        newArrayList.add(obs)
                    }
                }
            }.addOnCompleteListener(){
                if (it.isComplete){
                    prgLoad.visibility = View.GONE
                    if(newArrayList.size == 0){
                        tv_data.text = "No Observations Found"
                        tv_data.visibility = View.VISIBLE
                    }
                    newRecyclerView.adapter = ObservationListAdapter(newArrayList, 1, null)
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