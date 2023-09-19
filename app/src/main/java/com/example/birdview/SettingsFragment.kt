package com.example.birdview

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.birdview.databinding.ActivitySignUpBinding
import com.example.birdview.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding


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
    }

    // clear the binding in order to avoid memory leaks
    //override fun onDestroyView() {
      //  super.onDestroyView()
      //  binding = null!!
   // }
}