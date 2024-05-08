package com.intermeet.android

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {
    companion object {
        private const val TAG = "ProfileFragment"
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }

    private lateinit var ivUserProfilePhoto: ImageView
    private lateinit var tvUserFirstName: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivUserProfilePhoto = view.findViewById(R.id.ivUserProfilePhoto)
        tvUserFirstName = view.findViewById(R.id.tvUserFirstName)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = Firebase.database
        val userRef = database.getReference("users").child(userId)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userData = dataSnapshot.getValue(UserData::class.java)

                userData?.let { user ->
                    val firstName = user.firstName
                    val birthday = user.birthday
                    val age = calculateAge(birthday)
                    tvUserFirstName.text = "$firstName, $age"

                    user.photoDownloadUrls?.firstOrNull()?.let { url ->
                        if (isAdded) {
                            Glide.with(this@ProfileFragment)
                                .load(url)
                                .circleCrop()
                                .into(ivUserProfilePhoto)
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadUserData:onCancelled", databaseError.toException())
                tvUserFirstName.text = getString(R.string.error_loading_data)
            }
        })

        val settingsButton: Button = view.findViewById(R.id.settingButton)
        val editPreference: Button = view.findViewById(R.id.preferenceButton)
        val editUserInfo: Button = view.findViewById(R.id.editProfileButton)
        val tipCenterButton: Button = view.findViewById(R.id.tipCenter)
        val helpCenterButton: Button = view.findViewById(R.id.helpCeter)

        settingsButton.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }

        editPreference.setOnClickListener {
            val intent = Intent(activity, EditPreference::class.java).apply {
                putExtra("isEditMode", true)
            }
            startActivity(intent)
        }

        editUserInfo.setOnClickListener {
            val intent = Intent(activity, EditProfile::class.java).apply {
                putExtra("isEditMode", true)
            }
            startActivity(intent)
        }

        tipCenterButton.setOnClickListener {
            val intent = Intent(activity, TipCenter::class.java)
            startActivity(intent)
        }

        helpCenterButton.setOnClickListener {
            val intent = Intent(activity, HelpCenterActivity::class.java)
            startActivity(intent)
        }

        // Add button animations
        setupButtonAnimations(settingsButton)
        setupButtonAnimations(editPreference)
        setupButtonAnimations(editUserInfo)
        setupButtonAnimations(tipCenterButton)
        setupButtonAnimations(helpCenterButton)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateAge(birthday: String?): Int {
        if (birthday.isNullOrEmpty()) return 0
        var day: Int? = null
        var month: Int? = null
        var year: Int? = null
        val parts = birthday.split("-")

        if (parts.size == 3) {
            day = parts[0].toIntOrNull()
            month = parts[1].toIntOrNull()
            year = parts[2].toIntOrNull()
        }

        val currentYear = java.time.LocalDate.now().year
        val currentMonth = java.time.LocalDate.now().monthValue
        val currentDay = java.time.LocalDate.now().dayOfMonth
        var currentAge = currentYear - year!!
        if (currentMonth < month!!) {
            currentAge -= 1
        } else if (currentMonth == month!! && currentDay < day!!) {
            currentAge -= 1
        }

        return currentAge
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupButtonAnimations(button: View) {
        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(200).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                }
            }
            false // Return false to allow the click event to proceed
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "${this::class.java.simpleName} resumed")
        Log.d("NavigationStatus", "${this::class.java.simpleName} is now visible")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "${this::class.java.simpleName} paused")
    }
}
