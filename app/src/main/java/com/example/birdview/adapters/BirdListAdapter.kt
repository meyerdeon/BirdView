package com.example.birdview.adapters


import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.LinearLayout
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
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class BirdListAdapter(private val birds: List<BirdWithImage>, private val latitude : String,
                      private val longitude : String, private val fragmentManager : FragmentManager,
                      private val tripId: String?, private val mediaPlayer : MediaPlayer) : RecyclerView.Adapter<BirdListAdapter.ViewHolder>() {
    private var previousViewHolder: ViewHolder? = null
    private var currentViewHolder: ViewHolder? = null
    private var lastClickTime: Long = 0
    private val clickThreshold = 500

    private fun stopAndPlay(audioUrl: String, context: Context) {

        try
        {
        // Set audio attributes
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )

        // Set an error listener to handle any errors during preparation
        mediaPlayer.setOnErrorListener { mp, what, extra ->
            if (previousViewHolder != null) {
                previousViewHolder!!.playAudio.setText("Play Audio")
            }
            Toast.makeText(
                context,
                "Please note the media player is still preparing or a network error has occurred.",
                Toast.LENGTH_SHORT
            ).show()
            false
        }

        // Set a prepared listener to start playback when prepared
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
        }

        // Set a completion listener to reset the MediaPlayer when playback completes
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.reset()
            currentViewHolder!!.playAudio.text = "Play Audio"
        }

        // Set the data source and prepare asynchronously
        mediaPlayer.setDataSource(audioUrl)
        mediaPlayer.prepareAsync()
        } catch (e: Exception) {
            // Handle any exceptions during preparation
            if(previousViewHolder!=null){
                previousViewHolder!!.playAudio.setText("Play Audio")
            }
            if(currentViewHolder!=null){
                currentViewHolder!!.playAudio.setText("Play Audio")
            }
            mediaPlayer.reset()
            Toast.makeText(context, "An error has occur. Error: ${e}. Please note rapid clicking will not be allowed.", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
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
            bundle.putString("recording", bird.recording)
            childFragment.arguments = bundle
        }

        holder.playAudio.setOnClickListener(){
            val currentTime = System.currentTimeMillis()

            // Check if the time between consecutive clicks is greater than the threshold
            if (currentTime - lastClickTime > clickThreshold) {
                // Process the click only if the threshold is met
                lastClickTime = currentTime

                previousViewHolder = currentViewHolder

                // Store the current ViewHolder
                currentViewHolder = holder
                //  previousViewHolder?.let { notifyItemChanged(it.adapterPosition) }
                if(holder.playAudio.text.equals("Play Audio")){

                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.stop()
                        mediaPlayer.reset()
                        if(previousViewHolder!=null){
                            previousViewHolder!!.playAudio.setText("Play Audio")
                        }
                    }
                    if(bird.recording!=null){
                        holder.playAudio.setText("Stop Audio")
                        stopAndPlay(bird.recording!!, it.context)
                    }
                    else{
                        Toast.makeText(it.context, "No audifound", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    if(holder.playAudio.text.equals("Stop Audio")){
                        mediaPlayer.stop()
                        mediaPlayer.reset()
                        holder.playAudio.setText("Play Audio")
                    }
                }
            }
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
        val playAudio : Button = itemView.findViewById(R.id.btn_play_audio)
    }
}