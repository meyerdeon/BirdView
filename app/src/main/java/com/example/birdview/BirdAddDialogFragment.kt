package com.example.birdview

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class BirdAddDialogFragment(private val tripId: String?) : BottomSheetDialogFragment() {

    private lateinit var image_bird : ImageView
    private var encodedBitmap : String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bird_add_dialog, container, false)
        val latitude = arguments?.getString("latitude").toString()
        val longitude = arguments?.getString("longitude").toString()
        val url = arguments?.getString("url").toString()
        val comName = arguments?.getString("comName").toString()
        val sciName = arguments?.getString("sciName").toString()
       // val tripId = arguments?.getString("tripId").toString()

        if (getDialog() != null && getDialog()?.getWindow() != null) {
            getDialog()?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            getDialog()?.getWindow()?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        val tv_com_name = view.findViewById<TextView>(R.id.tv_bird_com_name)
        val btnOK = view.findViewById<Button>(R.id.btnOk)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        image_bird = view.findViewById<ImageView>(R.id.img_bird)

        Glide.with(image_bird.context)
            .load(url)
            .into(image_bird)
        tv_com_name.text = comName

        btnCancel.setOnClickListener{
            dismiss()
        }

        btnOK.setOnClickListener(){
            btnOK.isEnabled = false
            btnCancel.isEnabled = false
            val user = FirebaseAuth.getInstance().currentUser
            try {
                //code attribution
                //the following code was taken from Stack Overflow and adapted
                //https://stackoverflow.com/questions/53781154/kotlin-android-java-string-datetime-format-api21#:~:text=yyyy%20HH%3Amm%22)%3B%20String,%3D%20new%20SimpleDateFormat(%22dd.
                //arifng
                //https://stackoverflow.com/users/989643/arifng
                val localDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("dd LLLL yyyy HH:mm")
                val output = localDateTime.format(formatter)
                val obs : Observation = Observation(null, url, comName, sciName, latitude, longitude, output)
                //GlobalVariablesMethods.user.categories?.add(cat)
                val database = FirebaseDatabase.getInstance()
                val databaseReference = database.getReference("Users")
                //code attribution
                //the following code was taken from Stack Overflow and adapted
                //https://stackoverflow.com/questions/60432256/on-insert-data-in-firebase-realtime-database-it-deletes-previous-data
                //ashok
                //https://stackoverflow.com/users/12746098/ashok

                if (!tripId.isNullOrEmpty()){
                    databaseReference.child(user?.uid.toString()).child("tripcards").child(tripId).child("observations").push().setValue(obs).addOnCompleteListener() {
                        if (it.isComplete){
                            Toast.makeText(context, "Observation added successfully.", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Toast.makeText(context, "User data retrieval failed.", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnCompleteListener(){
                        dismiss()
                    }.addOnFailureListener(){
                        Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    databaseReference.child(user?.uid.toString()).child("observations").push().setValue(obs).addOnCompleteListener() {
                        if (it.isComplete){
                            Toast.makeText(context, "Observation added successfully.", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Toast.makeText(context, "User data retrieval failed.", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnCompleteListener(){
                        dismiss()
                    }.addOnFailureListener(){
                        Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show()
                    }
                }

            }
            catch (ex : Exception){
                Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }
}