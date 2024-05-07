package com.intermeet.android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.intermeet.android.helperFunc.getUserDataRepository


class AccountCreationActivity : AppCompatActivity() {
    private lateinit var userDataRepository: UserDataRepository
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_creation)
        progressBar = findViewById(R.id.progressBar)
        userDataRepository = getUserDataRepository()

        registerUser()
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

        //val userData = UserDataRepository.userData
        //check after if broken
    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }
    private fun registerUser() {
        showLoading()
        val userData = userDataRepository.userData
        val email = userData?.email ?: ""
        val password = userData?.password ?: ""
        val latitude = userData?.latitude
        val longitude = userData?.longitude


        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Email and password cannot be empty.", Toast.LENGTH_SHORT).show()
            hideLoading()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Account creation success, proceed with storing user data and uploading images
                val user = task.result?.user
                user?.let {
                    uploadImagesToFirebaseStorage(user.uid) { isSuccess ->
                        if (isSuccess) {
                            storeUserDataToFirebaseDatabase(user.uid)
                            if (latitude != null && longitude != null) {
                                storeUserLocation(user.uid, latitude, longitude)
                            }
                        } else {
                            Toast.makeText(this, "Failed to upload images.", Toast.LENGTH_SHORT).show()
                            hideLoading()
                        }
                    }
                }
            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                hideLoading()
            }
        }
    }

    private fun uploadImagesToFirebaseStorage(userId: String, onCompletion: (Boolean) -> Unit) {
        val imageUris = UserDataRepository.userData?.photoUris ?: listOf()
        val storageRef = FirebaseStorage.getInstance().reference.child("users/$userId/images")
        val imageDownloadUrls = mutableListOf<String>()

        val uploadTasks = imageUris.mapNotNull { uriString ->
            val uri = android.net.Uri.parse(uriString.toString())
            uri.lastPathSegment?.let { fileName ->
                val imageRef = storageRef.child(fileName)
                imageRef.putFile(uri).continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    imageRef.downloadUrl
                }.addOnSuccessListener { downloadUri ->
                    imageDownloadUrls.add(downloadUri.toString())
                }
            }
        }

        if (uploadTasks.isNotEmpty()) {
            Tasks.whenAll(uploadTasks).addOnCompleteListener {
                if (it.isSuccessful) {
                    // Update UserDataModel with the download URLs
                    UserDataRepository.userData?.photoDownloadUrls = imageDownloadUrls
                    onCompletion(true)
                } else {
                    // Handle failure
                    onCompletion(false)
                }
            }
        } else {
            // If no valid URIs were found or uploadTasks list is empty
            onCompletion(false)
        }
    }


    private fun storeUserDataToFirebaseDatabase(userId: String) {
            val userData = UserDataRepository.userData?.apply {
            // Clear sensitive information before storing in the database
            email = null
            password = null
            UserDataRepository.userData?.photoUris?.clear()
        }

        Log.d("UserData", "${UserDataRepository.userData.toString()}")

        userData?.let {
            FirebaseDatabase.getInstance().getReference("users/$userId")
                .setValue(it)
                .addOnSuccessListener {
                    // Data storage successful, navigate to the next activity
                    navigateToNextActivity()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to store user data.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun storeUserLocation(userId: String, latitude: Double, longitude: Double) {
        val geoFire = GeoFire(FirebaseDatabase.getInstance().getReference("user_locations"))
        geoFire.setLocation(userId, GeoLocation(latitude, longitude)) { key, error ->
            if (error != null) {
                println("Error saving location for user $key")
            } else {
                println("Location saved for user $key")
            }
        }
    }

    private fun navigateToNextActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        //finish()
    }
}
