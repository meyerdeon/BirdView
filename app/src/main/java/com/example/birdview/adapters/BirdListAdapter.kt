package com.example.birdview.adapters


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.birdview.BirdAddDialogFragment
import com.example.birdview.Observation
import com.example.birdview.R
import com.example.birdview.UnidentifiedDialogFragment
import com.example.birdview.models.BirdWithImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class BirdListAdapter(private val birds: List<BirdWithImage>, private val latitude : String,
                      private val longitude : String, private val fragmentManager : FragmentManager,
                      private val tripId: String?) : RecyclerView.Adapter<BirdListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bird_list_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bird = birds[position]
        holder.birdName.text = bird.comName
        holder.birdSciName.setText(bird.sciName)
        Glide.with(holder.image.context)
            .load(bird.url)
            .into(holder.image)
        //https://www.geeksforgeeks.org/how-to-apply-onclicklistener-to-recyclerview-items-in-android/
        holder.itemView.setOnClickListener {v ->

            val childFragment = BirdAddDialogFragment(tripId)
            childFragment.show(fragmentManager, BirdAddDialogFragment::class.java.simpleName)
            val bundle = Bundle()
            bundle.putString("latitude", latitude)
            bundle.putString("longitude", longitude)
            bundle.putString("url", bird.url)
            bundle.putString("comName", bird.comName)
            bundle.putString("sciName", bird.sciName)
            //bundle.putString("tripId", tripId)
            childFragment.arguments = bundle
        }
    }

    override fun getItemCount(): Int {
        return birds.size
    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
      //  val birdImage : ShapeableImageView = itemView.findViewById(R.id.imgBird_location)
        val image : ImageView = itemView.findViewById(R.id.imgBird_location)
        val birdName : TextView = itemView.findViewById(R.id.tv_bird_name_location)
        val birdSciName : TextView = itemView.findViewById(R.id.tv_scientific_bird_name_location)
    }
}