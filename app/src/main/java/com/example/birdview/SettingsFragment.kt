package com.example.birdview
import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.birdview.databinding.FragmentSettingsBinding
import com.example.birdview.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var dbRef: DatabaseReference

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 101
        const val REQUEST_OPEN_IMAGE = 102
    }

    private lateinit var img_profile_picture : ImageView
    private var encodedBitmap : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    //code attribution
    //the following code was taken from Stack Overflow and adapted
    //https://stackoverflow.com/questions/71681976/viewbinding-not-making-any-changes-inside-fragment
    //Dannly
    //https://stackoverflow.com/users/17518341/dannly
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        try {
            //code attribution
            //the following code was taken from Stack Overflow and adapted
            //https://stackoverflow.com/questions/53781154/kotlin-android-java-string-datetime-format-api21#:~:text=yyyy%20HH%3Amm%22)%3B%20String,%3D%20new%20SimpleDateFormat(%22dd.
            //arifng
            //https://stackoverflow.com/users/989643/arifng
            //GlobalVariablesMethods.user.categories?.add(cat)
            val database = FirebaseDatabase.getInstance()
            val databaseReference = database.getReference("Users")

            databaseReference.child(user?.uid.toString()).child("name").get().addOnSuccessListener {
                if (it.exists()){
                    val username = it.getValue(String::class.java)

                    binding.txtUsername.text = "Hello $username!"
                }

                else{
                    binding.txtUsername.text = "Hello user!"
                    Toast.makeText(context, "User data retrieval failed.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener(){
                Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show()
            }

            databaseReference.child(user?.uid.toString()).child("profilePicture").get().addOnSuccessListener {
                if (it.exists()){
                    val profileImage = it.child("profilePicture").getValue(String::class.java)
                    val image = GlobalMethods.decodeImage(profileImage)
                    binding.imgUserPP.setImageBitmap(image)
                }

                else{
                    Toast.makeText(context, "User data retrieval failed.", Toast.LENGTH_SHORT).show()
                }
            }.addOnCompleteListener(){

            }.addOnFailureListener(){
                Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show()
            }


        }
        catch (ex : Exception){
            Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
        }

        binding.btnSignOut.setOnClickListener(){
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, SplashPageActivity::class.java)
            startActivity(intent)
        }

        binding.imgUserPP.setOnClickListener{
            showReplacePPDialog()
        }

        dbRef = FirebaseDatabase.getInstance().getReference("Users")
        var isImperial = false
        //val user = FirebaseAuth.getInstance().currentUser

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

    private fun showReplacePPDialog(){


        val dialog = Dialog(requireContext())
        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.fragment_replace_profile_picture_dialog)

        val imgTakePicture = dialog.findViewById<LinearLayout>(R.id.lyt_take_picture)
        val imgUploadImage = dialog.findViewById<LinearLayout>(R.id.lyt_upload_image)
        val btnUpload = dialog.findViewById<Button>(R.id.btnUpload)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        img_profile_picture = dialog.findViewById<ImageView>(R.id.imgProfilePicture)


        imgTakePicture.setOnClickListener {
            // Handle the "Take Picture" option
            if (isCameraPermissionGranted()) {
                dispatchTakePictureIntent()
            } else {
                requestCameraPermission()
            }
            // You can open the camera or perform any other action
        }



        imgUploadImage.setOnClickListener {
            // Handle the "Upload Picture" option
            if (isReadStoragePermissionGranted()) {
                openImagePicker()
            } else {
                requestReadStoragePermission()
            }
            // You can trigger the image selection process
        }

        btnUpload.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            try {
                //Upload the users selected profile picture
                val _user : User = User(profilePicture = encodedBitmap)

                val database = FirebaseDatabase.getInstance()
                val databaseReference = database.getReference("Users")

                    databaseReference.child(user?.uid.toString()).child("profilePicture").setValue(_user).addOnCompleteListener() {
                        if (it.isComplete){

                            val image = GlobalMethods.decodeImage(encodedBitmap)
                            binding.imgUserPP.setImageBitmap(image) //set the code
                            Toast.makeText(context, "Profile picture has been edited", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Toast.makeText(context, "User data retrieval failed.", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnCompleteListener(){
                        dialog.dismiss()
                    }.addOnFailureListener(){
                        Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show()
                    }


            }
            catch (ex : Exception){
                Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

    }

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_IMAGE_CAPTURE
        )
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent,
            REQUEST_IMAGE_CAPTURE
        )
    }

    private fun isReadStoragePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestReadStoragePermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_OPEN_IMAGE
        )
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_OPEN_IMAGE)
    }

//    fun addData(){
//        if(imgCategory.drawable == null){
//            imageString = null
//        }
//        else{
//            imageString = encodedBitmap.toString()
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?,) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_OPEN_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val selectedImageUri: Uri? = data.data
                try {
                    val contentResolver = requireContext().contentResolver
                    val inputStream = selectedImageUri?.let { contentResolver.openInputStream(it) }
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    img_profile_picture.background = null
                    img_profile_picture.setImageBitmap(bitmap)
                    //binding.imgUserPP.setImageBitmap(bitmap)
                    encodedBitmap = GlobalMethods.encodeImage(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                // Do something with the selected image URI, such as displaying it or processing it.
            }
        }
        else{
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
                val imageBitmap = data?.extras?.get("data") as Bitmap
                img_profile_picture.background = null
                img_profile_picture.setImageBitmap(imageBitmap)
                //binding.imgUserPP.setImageBitmap(bitmap)
                encodedBitmap = GlobalMethods.encodeImage(imageBitmap)
                // Do something with the captured image, such as displaying it or saving it.
            }
        }
    }
    // clear the binding in order to avoid memory leaks
    //override fun onDestroyView() {
      //  super.onDestroyView()
      //  binding = null!!
   // }
}