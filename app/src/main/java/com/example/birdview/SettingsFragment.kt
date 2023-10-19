package com.example.birdview
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.birdview.databinding.ActivitySignUpBinding
import com.example.birdview.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var dbRef: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    //https://stackoverflow.com/questions/71681976/viewbinding-not-making-any-changes-inside-fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSignOut.setOnClickListener(){
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, SplashPageActivity::class.java)
            startActivity(intent)
        }
        dbRef = FirebaseDatabase.getInstance().getReference("Users")
        var isImperial = false
        val user = FirebaseAuth.getInstance().currentUser

        //pull settings data from database
        dbRef.child(user?.uid.toString()).child("Settings").child("unitMeasurement").get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")
            if (it.exists()){
                if (it.value!!.toString().equals("imperial")){
                    isImperial = true
                    binding.switchUnitMeasurement.isChecked = true
                }else{
                    binding.switchUnitMeasurement.isChecked = false
                }
            }else{
                binding.switchUnitMeasurement.isChecked = false
            }
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

        dbRef.child(user?.uid.toString()).child("Settings").child("mapRangePreference").get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")
            if (it.exists()){
                binding.discreteSlider.value = it.value.toString().toFloat()
                binding.txtMapRangePreference.text = if (isImperial)"${it.value.toString().toFloat().toInt()}mi" else "${it.value.toString().toFloat().toInt()}km"
            }else{
                binding.discreteSlider.value = 50f
                binding.txtMapRangePreference.text = "50km"
            }
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

        //save map range preference when user decides to change
        binding.discreteSlider.addOnChangeListener { slider, value, fromUser ->
            dbRef.child(user?.uid.toString()).child("Settings").child("mapRangePreference").setValue(value)
                .addOnFailureListener{
                    Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_LONG
                    ).show()
                }
            if (binding.switchUnitMeasurement.isChecked){
                binding.txtMapRangePreference.text = "${value.toInt()}mi"
            }else{
                binding.txtMapRangePreference.text = "${value.toInt()}km"
            }

        }

        //save unit measurement preference when user decides to change
        binding.switchUnitMeasurement.setOnCheckedChangeListener { compoundButton, b ->
            if (b){

                dbRef.child(user?.uid.toString()).child("Settings").child("unitMeasurement").setValue("imperial")
                    .addOnFailureListener{
                        Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_LONG
                        ).show()
                    }
                binding.txtMapRangePreference.text = binding.txtMapRangePreference.text.toString().replace("km", "mi")
            }else{
                dbRef.child(user?.uid.toString()).child("Settings").child("unitMeasurement").setValue("metric")
                    .addOnFailureListener{
                        Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_LONG
                        ).show()
                    }
                binding.txtMapRangePreference.text = binding.txtMapRangePreference.text.toString().replace("mi", "km")
            }
        }
    }

    // clear the binding in order to avoid memory leaks
    //override fun onDestroyView() {
      //  super.onDestroyView()
      //  binding = null!!
   // }
}