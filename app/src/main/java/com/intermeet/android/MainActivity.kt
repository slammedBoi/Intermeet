package com.intermeet.android

//import LikesPageFragment
import LikesPageFragment
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.android.libraries.places.api.Places
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Places.initialize(applicationContext, "@string/google_maps_key")
        updateFCMToken()
        storeCurrentUserId()

        bottomNav = findViewById(R.id.bottom_nav)
        setupNavigation()

        handleIntent(intent)  // Ensure the intent is handled to check for any fragment-specific navigation.
        // Set the default selected item in the bottom navigation
        if (!intent.hasExtra("openFragment")) {
            bottomNav.selectedItemId = R.id.navigation_discover
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun setupNavigation() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_discover -> navigateTo(DiscoverFragment.newInstance())
                R.id.navigation_account -> navigateTo(ProfileFragment.newInstance())
                R.id.navigation_events -> navigateTo(EventsFragment.newInstance())
                R.id.navigation_worth -> navigateTo(LikesPageFragment.newInstance())
                R.id.navigation_chat -> navigateTo(ChatFragment.newInstance())
                else -> false
            }
        }
    }

    private fun navigateTo(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }

    private fun handleIntent(intent: Intent) {
        if (intent.hasExtra("openFragment")) {
            val fragment = when (intent.getStringExtra("openFragment")) {
                "chat" -> ChatFragment.newInstance()
                else -> DiscoverFragment.newInstance()  // Ensure the default fragment is DiscoverFragment
            }
            navigateTo(fragment)
            bottomNav.selectedItemId = when (fragment) {
                is ChatFragment -> R.id.navigation_chat
                else -> R.id.navigation_discover
            }
        }

    }

    private fun updateFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let {
                    FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
                        FirebaseDatabase.getInstance().getReference("users/$userId/fcmToken").setValue(it)
                    }
                }
            }
        }
    }
    private fun storeCurrentUserId() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Optionally, use SharedPreferences to save the user ID
            val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            prefs.edit().putString("UserId", userId).apply()

            // Alternatively, pass this userId to your ViewModel if it's already set up to accept it
            // viewModel.setUserId(userId)
        }
    }
}





//test