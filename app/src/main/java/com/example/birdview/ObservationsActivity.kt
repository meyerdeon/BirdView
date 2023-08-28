package com.example.birdview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ObservationsActivity : AppCompatActivity() {
    private lateinit var newRecyclerView : RecyclerView
    private lateinit var newArrayList : ArrayList<Observation>
    lateinit var imageId : Array<Int>
    lateinit var birdName : Array<String>
    lateinit var birdScientificName : Array<String>
    lateinit var birdLocation : Array<String>
    lateinit var birdCount : Array<Int>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observations)

                imageId = arrayOf(R.drawable.image1, R.drawable.image2, R.drawable.image1, R.drawable.image2, R.drawable.image1)
        birdName = arrayOf("Bird Name", "Bird Name", "Bird Name", "Bird Name", "Bird Name")
        birdScientificName = arrayOf("Scientific Bird Name", "Scientific Bird Name", "Scientific Bird Name", "Scientific Bird Name", "Scientific Bird Name")
        birdLocation = arrayOf("Unknown", "Unknown", "Unknown", "Unknown", "Unknown")
        birdCount = arrayOf(1, 2, 1, 2, 1)
        newRecyclerView = findViewById(R.id.rvObservations)
        newRecyclerView.layoutManager = LinearLayoutManager(this)
        newRecyclerView.setHasFixedSize(true)
        newArrayList = arrayListOf<Observation>()
        getData()
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