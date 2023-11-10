package com.example.birdview.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.birdview.GlobalMethods
import com.example.birdview.ObservationListDialogFragment
import com.example.birdview.R
import com.example.birdview.models.TripCard
import java.time.LocalDate

class TripcardListAdapter(private val tripcardList : ArrayList<TripCard>, private val tripObservationsCount: Int, private val speciesCount: Int) : RecyclerView.Adapter<TripcardListAdapter.TripcardViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TripcardViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.trip_cards_viewholder, parent, false)
        return TripcardListAdapter.TripcardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TripcardListAdapter.TripcardViewHolder, position: Int) {
        val tripcard = tripcardList[position]
        //code attribution
        //the following code was taken from Stack Overflow and adapted
        //https://stackoverflow.com/questions/59012821/android-kotlin-new-intent-on-click-event-from-recycler-view
        //Piyush Kalyan
        //https://stackoverflow.com/users/15750578/piyush-kalyan
        holder.imgExpand.setOnClickListener {v->
            val dialogFragment = ObservationListDialogFragment()
            val args = Bundle()
            args.putString("id", tripcard.id)
            dialogFragment.arguments = args
            dialogFragment.show((v.context as FragmentActivity).supportFragmentManager, ObservationListDialogFragment::class.java.simpleName)
        }
        holder.tripName.text = tripcard.name.toString()
        holder.observationCount.text = "${tripObservationsCount.toString()} observations"
        holder.speciesNum.text = "${speciesCount.toString()} species"
        holder.dateCreated.text = "${LocalDate.ofEpochDay(tripcard.date!!).dayOfMonth} ${(LocalDate.ofEpochDay(tripcard.date!!).month).toString().subSequence(0, 3)}"

    }

    override fun getItemCount(): Int {
        return tripcardList.size
    }

    class TripcardViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val tripName : TextView = itemView.findViewById(R.id.tvTripCardName)
        val observationCount : TextView = itemView.findViewById(R.id.tvObservationAmount)
        val speciesNum : TextView = itemView.findViewById(R.id.tvSpeciesAmount)
        val dateCreated : TextView = itemView.findViewById(R.id.tvTripDate)
        val imgExpand : ImageView = itemView.findViewById(R.id.image1)
    }
}
