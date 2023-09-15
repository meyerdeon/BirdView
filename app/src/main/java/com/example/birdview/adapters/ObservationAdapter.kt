package com.example.birdview.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.birdview.Observation
import com.example.birdview.R
import com.example.birdview.SingleObservationFragment
import com.google.android.material.imageview.ShapeableImageView

class ObservationAdapter(private val clientList : ArrayList<Observation>) : RecyclerView.Adapter<ObservationAdapter.ClientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, ViewType: Int): ClientViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.observation_item_layout, parent, false)
        return ClientViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        val currentItem = clientList[position]
        holder.birdImage.setImageResource(R.drawable.image1)
        //code attribution
        //the following code was taken from Stack Overflow and adapted
        //https://stackoverflow.com/questions/59012821/android-kotlin-new-intent-on-click-event-from-recycler-view
        //Piyush Kalyan
        //https://stackoverflow.com/users/15750578/piyush-kalyan
        holder.expandCollapseLayout.setOnClickListener {v->
//            if(holder.expandableLayout.visibility == View.GONE) {
//                TransitionManager.beginDelayedTransition(holder.cardViewObservation, AutoTransition())
//                holder.expandableLayout.visibility = View.VISIBLE
//                holder.imgExpandCollapse.setImageResource(R.drawable.baseline_expand_less_24)
//            }
//            else{
//                TransitionManager.beginDelayedTransition(holder.cardViewObservation, AutoTransition())
//                holder.expandableLayout.visibility = View.GONE
//                holder.imgExpandCollapse.setImageResource(R.drawable.baseline_expand_more_24)
//            }
            val intent = Intent(v.context, SingleObservationFragment::class.java)
            v.context.startActivity(intent)
        }
        holder.birdName.text = currentItem.birdName.toString()
        holder.birdScientificName.text = currentItem.birdScientificName.toString()
        holder.birdLocation.text = "Location: " + currentItem.birdLocation.toString()
        holder.birdDateAdded.text = "Date Added: " + currentItem.dateAdded.toString()
        holder.birdCount.text = "Count: " + currentItem.birdCount.toString()
    }

    override fun getItemCount(): Int
    {
        return clientList.size
    }

    class ClientViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val expandableLayout : LinearLayout = itemView.findViewById(R.id.expandableLayout)
        val imgExpandCollapse : ImageView = itemView.findViewById(R.id.img_expand_collapse)
        val birdImage : ShapeableImageView = itemView.findViewById(R.id.imgBird)
        val birdName : TextView = itemView.findViewById(R.id.tv_bird_name)
        val birdDateAdded : TextView = itemView.findViewById(R.id.tv_bird_date_added)
        val cardViewObservation : CardView = itemView.findViewById(R.id.cardViewObservation)
        val birdLocation : TextView = itemView.findViewById(R.id.tv_bird_location)
        val birdScientificName : TextView = itemView.findViewById(R.id.tv_scientific_bird_name)
        val birdCount : TextView = itemView.findViewById(R.id.tv_bird_count)
        val expandCollapseLayout : LinearLayout = itemView.findViewById(R.id.expand_collapse_layout)
    }
}