package com.example.birdview

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment

class ReplaceProfilePictureDialogFragment : DialogFragment() {
    companion object {
        const val REQUEST_IMAGE_CAPTURE = 101
        const val REQUEST_OPEN_IMAGE = 102
    }
    private lateinit var image_bird : ImageView
    private var encodedBitmap : String? = null

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val View = inflater.inflate(R.layout.fragment_replace_profile_picture_dialog, container, false)
        if (getDialog() != null && getDialog()?.getWindow() != null) {
            getDialog()?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            getDialog()?.getWindow()?.requestFeature(Window.FEATURE_NO_TITLE)
        }
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        val imgTakePicture = View.findViewById<LinearLayout>(R.id.lyt_take_picture)
        val imgUploadImage = View.findViewById<LinearLayout>(R.id.lyt_upload_image)
        val btnUpload = View.findViewById<Button>(R.id.btnUpload)
        val btnCancel = View.findViewById<Button>(R.id.btnCancel)


        return View
    }


}