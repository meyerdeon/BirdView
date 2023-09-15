package com.example.birdview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.birdview.adapters.ObservationAdapter

class ObservationsFragment : Fragment() {
    private lateinit var newRecyclerView : RecyclerView
    private lateinit var newArrayList : ArrayList<Observation>
    lateinit var imageId : Array<Int>
    lateinit var birdName : Array<String>
    lateinit var birdScientificName : Array<String>
    lateinit var birdLocation : Array<String>
    lateinit var birdCount : Array<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_observations, container, false)

        imageId = arrayOf(R.drawable.image1, R.drawable.image2, R.drawable.image1, R.drawable.image2, R.drawable.image1)
        birdName = arrayOf("Bird Name", "Bird Name", "Bird Name", "Bird Name", "Bird Name")
        birdScientificName = arrayOf("Scientific Bird Name", "Scientific Bird Name", "Scientific Bird Name", "Scientific Bird Name", "Scientific Bird Name")
        birdLocation = arrayOf("Unknown", "Unknown", "Unknown", "Unknown", "Unknown")
        birdCount = arrayOf(1, 2, 1, 2, 1)
        newRecyclerView = view.findViewById(R.id.rvObservations)
        newRecyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        newRecyclerView.setHasFixedSize(true)
        newArrayList = arrayListOf<Observation>()
        getData()

        return view
    }

    private fun getData(){
        for (i in 0 until imageId.size)
        {
            val item = Observation(imageId[i], birdName[i], birdScientificName[i], birdLocation[i], "Unknown date", birdCount[i])
            newArrayList.add(item)
        }
        newRecyclerView.adapter = ObservationAdapter(newArrayList)
    }
}