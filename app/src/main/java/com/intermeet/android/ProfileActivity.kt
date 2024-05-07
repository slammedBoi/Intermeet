package com.intermeet.android

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "ProfileActivity"
    }

    private lateinit var ivUserProfilePhoto: ImageView
    private lateinit var tvUserFirstName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Log.d(TAG, "userid: $userId")


        //val userId = "knIJTTeOHsa3ce4L84dbE7BUYQI2"
        val database = Firebase.database

        ivUserProfilePhoto = findViewById(R.id.ivUserProfilePhoto)
        tvUserFirstName = findViewById(R.id.tvUserFirstName)

        val userNameRef = database.getReference("users").child(userId)

        // Reference to the user's "photoDownloadURLs" node
        val userPhotosRef = database.getReference("users").child(userId)


        // ValueEventListener to read the "firstName" data
        userNameRef.addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get the value of "firstName"
                val userData = dataSnapshot.getValue(UserData::class.java)


                userData?.let { user ->
                    val firstName = user.firstName
                    val birthday = user.birthday
                    val age = calculateAge(birthday)

                    tvUserFirstName.text = "$firstName, $age"
                    }
                }

            override fun onCancelled(databaseError: DatabaseError) {
                // Log any errors
                Log.w(TAG, "loadUserName:onCancelled", databaseError.toException())
                // Handle error case, perhaps set TextView to an error message
                tvUserFirstName.text = getString(R.string.error_loading_data)
            }
        })

        // ValueEventListener to read the "photoDownloadURLs" data
        // ValueEventListener to read the "photoDownloadURLs" data
        userPhotosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userData = dataSnapshot.getValue(UserData::class.java)
                userData?.photoDownloadUrls?.firstOrNull()?.let { url ->
                    Glide.with(this@ProfileActivity)
                        .load(url)
                        .circleCrop()
                        .into(ivUserProfilePhoto)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Log any errors
                Log.w(TAG, "loadUserPhotos:onCancelled", databaseError.toException())
            }
        })

        val settingButton: View = findViewById(R.id.setting)
        settingButton.setOnClickListener {
            // Intent to navigate to the SecondActivity
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

    }
    private fun calculateAge(birthday: String?): Int {
        // Implement logic to calculate age based on birthday
        // Example: Parse birthday string, calculate age based on current date, and return age
        // For brevity, a simplified implementation is shown here:
        // Note: You may need to handle date parsing and calculation more accurately in a real app.

        if (birthday.isNullOrEmpty()) return 0 // Default age if birthday is not provided
        var day: Int? = null
        var month: Int? = null
        var year: Int? = null
        val parts = birthday.split("-")

        if (parts.size == 3) {
            day = parts[0].toIntOrNull()
            month = parts[1].toIntOrNull()
            year = parts[2].toIntOrNull()

            if (day != null && month != null && year != null) {
                println("Day: $day")
                println("Month: $month")
                println("Year: $year")
            }
        }

        val currentYear = java.time.LocalDate.now().year
        val currentMonth = java.time.LocalDate.now().monthValue
        val currentDay = java.time.LocalDate.now().dayOfMonth
        var currentAge = currentYear - year!!
        if(currentMonth < month!!) {
            currentAge -= 1
        }
        else if(currentMonth == month!!) {
            if(currentDay < day!!) {
                currentAge -= 1
            }
        }

        return currentAge
    }

}
