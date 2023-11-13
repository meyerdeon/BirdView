package com.example.birdview.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.add
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.birdview.*
import com.example.birdview.models.TripCard
import java.time.LocalDate

class TripcardListAdapter(private val tripcardList : ArrayList<TripCard>, private var tripObservationsCount: Int, private var speciesCount: Int) : RecyclerView.Adapter<TripcardListAdapter.TripcardViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TripcardViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.trip_cards_viewholder, parent, false)
        return TripcardListAdapter.TripcardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TripcardListAdapter.TripcardViewHolder, position: Int) {
        val tripcard = tripcardList[position]

        tripObservationsCount = tripcard.observationsCount!!
        speciesCount = tripcard.speciesCount!!
        //code attribution
        //the following code was taken from Stack Overflow and adapted
        //https://stackoverflow.com/questions/59012821/android-kotlin-new-intent-on-click-event-from-recycler-view
        //Piyush Kalyan
        //https://stackoverflow.com/users/15750578/piyush-kalyan

        //if there are no observations, hide the species count textview
        if (tripcard.observationsCount!! <= 0) holder.speciesNum.visibility = View.GONE

        holder.tripName.text = tripcard.name.toString()
        holder.observationCount.text = "${tripcard.observationsCount.toString()} observations"
        holder.speciesNum.text = "${tripcard.speciesCount.toString()} species"

        //format the date
        var date = (LocalDate.ofEpochDay(tripcard.date!!).month).toString().subSequence(0, 3)
        val date1 = date.subSequence(1, 3).toString().lowercase()
        val date2 = "${(LocalDate.ofEpochDay(tripcard.date!!).month).toString().subSequence(0, 1)}${date1} "
        holder.dateCreated.text = "${LocalDate.ofEpochDay(tripcard.date!!).dayOfMonth} ${date2}"

        holder.imgExpand.setOnClickListener {v->
            replaceFragment(TripObservationsListFragment(tripcard.observationsCount, tripcard.speciesCount), tripcard.id, v, "${LocalDate.ofEpochDay(tripcard.date!!).dayOfMonth} ${date2}", tripcard.observationsCount, tripcard.speciesCount)
        }
    }

    override fun getItemCount(): Int {
        return tripcardList.size
    }

    fun replaceFragment(fragment: Fragment, id: String?, v: View, date: String, obsvCount: Int, speciesCount: Int){
        if(fragment != null){
            val bundle = Bundle()
            bundle.putString("tripId", id)
            bundle.putString("obsvCount", obsvCount.toString())
            bundle.putString("species", speciesCount.toString())
            bundle.putString("date", date)
            fragment.arguments = bundle
            val transaction = (v.context as FragmentActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.map, fragment)
            transaction.commit()
        }
    }

    class TripcardViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val tripName : TextView = itemView.findViewById(R.id.tvTripCardName)
        val observationCount : TextView = itemView.findViewById(R.id.tvObservationAmount)
        val speciesNum : TextView = itemView.findViewById(R.id.tvSpeciesAmount)
        val dateCreated : TextView = itemView.findViewById(R.id.tvTripDate)
        val imgExpand : ImageView = itemView.findViewById(R.id.image1)
    }
}
