package com.intermeet.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.intermeet.android.helperFunc.getUserDataRepository

class PhotoUploadActivity : AppCompatActivity() {

    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private val imageUris = arrayOfNulls<Uri>(5)  // Assuming 5 ImageViews
    private lateinit var imageViews: List<ImageView>
    private var currentImageIndex = 0  // Track which ImageView is being updated

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_upload)

        initializeImagePickerLauncher()
        setupImageViewsAndNextButton()
    }

    private fun initializeImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                updateSelectedImage(it)
            }
        }
    }

    private fun setupImageViewsAndNextButton() {
        imageViews = listOf(
            findViewById(R.id.imageView1),
            findViewById(R.id.imageView2),
            findViewById(R.id.imageView3),
            findViewById(R.id.imageView4),
            findViewById(R.id.imageView5)
        )

        imageViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                currentImageIndex = index  // Remember which ImageView was clicked
                imagePickerLauncher.launch("image/*")  // Launch the image picker

            }
        }

        val nextButton: Button = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            val intent = Intent(this, SchoolActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateSelectedImage(uri: Uri) {
        // Update the ImageView and store the Uri
        imageUris[currentImageIndex] = uri
        imageViews[currentImageIndex].setImageURI(uri)
        storeSelectedUris()
    }

    private fun storeSelectedUris() {
        val userDataRepository = getUserDataRepository()
        // Filter out null URIs and store the list in userData repository
        UserDataRepository.userData?.photoUris = imageUris.filterNotNull().toMutableList()
    }
}
