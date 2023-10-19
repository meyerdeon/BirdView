package com.example.birdview.adapters

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.birdview.BirdListDialogFragment
import com.example.birdview.GlobalMethods
import com.example.birdview.Observation
import com.example.birdview.ObservationListDialogFragment
import com.example.birdview.R
import com.example.birdview.SingleObservationFragment
import com.google.android.material.imageview.ShapeableImageView

class ObservationListAdapter(private val observationList : ArrayList<Observation>) : RecyclerView.Adapter<ObservationListAdapter.ObservationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, ViewType: Int): ObservationViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.bird_observation_item_layout, parent, false)
        return ObservationViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ObservationViewHolder, position: Int) {
        val observation = observationList[position]
        holder.birdImage.setImageResource(R.drawable.image1)
        //code attribution
        //the following code was taken from Stack Overflow and adapted
        //https://stackoverflow.com/questions/59012821/android-kotlin-new-intent-on-click-event-from-recycler-view
        //Piyush Kalyan
        //https://stackoverflow.com/users/15750578/piyush-kalyan
        holder.expandCollapseLayout.setOnClickListener {v->
            val dialogFragment = ObservationListDialogFragment()
            val args = Bundle()
            args.putString("id", observation.id)
            dialogFragment.arguments = args
            dialogFragment.show((v.context as FragmentActivity).supportFragmentManager, ObservationListDialogFragment::class.java.simpleName)
        }
        holder.birdComName.text = observation.birdComName.toString()
        holder.birdSciName.text = observation.birdSciName.toString()
        holder.birdDateAdded.text = "Date Added: " + observation.dateAdded.toString()
        if(observation.birdImage?.contains("https://")!!){
            Glide.with(holder.birdImage.context)
                .load(observation.birdImage)
                .into(holder.birdImage)
        }
        else{
           val image = GlobalMethods.decodeImage(observation.birdImage)
            holder.birdImage.setImageBitmap(image)

        }
      //  holder.birdCount.text = "Count: " + currentItem.birdCount.toString()
    }

    override fun getItemCount(): Int
    {
        return observationList.size
    }

    class ObservationViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val birdImage : ShapeableImageView = itemView.findViewById(R.id.img_bird)
        val birdComName : TextView = itemView.findViewById(R.id.tv_bird_com_name)
        val birdSciName : TextView = itemView.findViewById(R.id.tv_bird_sci_name)
        val birdDateAdded : TextView = itemView.findViewById(R.id.tv_bird_date_added)
        val expandCollapseLayout : LinearLayout = itemView.findViewById(R.id.expand_collapse_layout)
    }
}