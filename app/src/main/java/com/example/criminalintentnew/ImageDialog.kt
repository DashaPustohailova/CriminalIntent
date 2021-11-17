package com.example.criminalintentnew

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.image_dialog.*
import java.io.File

lateinit var  im: ImageView
class ImageDialog: DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootView: View = inflater.inflate(R.layout.image_dialog, container, false)
        val photoFile = arguments?.getString("bitmap")
        val bitmap = BitmapFactory.decodeFile(photoFile)
        im= rootView.findViewById(R.id.image_dialogg) as ImageView
        im.setImageBitmap(bitmap)
        return rootView
    }


    companion object{
        fun newInstance(photoFile: String): ImageDialog {
            val args = Bundle().apply {
                putString("bitmap", photoFile)
            }
            return ImageDialog().apply {
                arguments = args
            }
        }
    }
}
