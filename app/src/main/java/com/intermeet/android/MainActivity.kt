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

        // Initialize Places API with your API key stored in strings.xml
        Places.initialize(applicationContext, getString(R.string.google_maps_key))

        // Update FCM token and store current user ID
        updateFCMToken()
        storeCurrentUserId()

        // Setup BottomNavigationView
        bottomNav = findViewById(R.id.bottom_nav)
        setupNavigation()
        // Check for an intent with an "openFragment" extra to decide which fragment to open
        if (intent.hasExtra("openFragment")) {
            handleIntent(intent)
        } else if (savedInstanceState == null) {
            // Default to opening the DiscoverFragment if no specific fragment is requested and there is no saved state
            bottomNav.selectedItemId = R.id.navigation_discover
            navigateTo(DiscoverFragment.newInstance())
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)  // Update the activity's intent to the new intent
        handleIntent(intent)  // Handle the new intent
    }

    private fun setupNavigation() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_discover -> {
                    navigateTo(DiscoverFragment.newInstance())
                    true
                }
                R.id.navigation_account -> {
                    navigateTo(ProfileFragment.newInstance())
                    true
                }
                R.id.navigation_events -> {
                    navigateTo(EventsFragment.newInstance())
                    true
                }
                R.id.navigation_worth -> {
                    navigateTo(LikesPageFragment.newInstance())
                    true
                }
                R.id.navigation_chat -> {
                    navigateTo(ChatFragment.newInstance())
                    true
                }
                else -> false
            }
        }
    }


    private fun navigateTo(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    //private fun handleIntent(intent: Intent) {
    //    val fragmentToOpen = intent.getStringExtra("openFragment")
    //    when (fragmentToOpen) {
    //        "discover" -> navigateTo(DiscoverFragment.newInstance())
    //        "chat" -> navigateTo(ChatFragment.newInstance().apply {
    //            arguments = Bundle().apply { putString("userId", intent.getStringExtra("userId")) }
    //        })
    //        "like" -> navigateTo(LikesPageFragment.newInstance())
    //        else -> navigateTo(DiscoverFragment.newInstance())  // Default fragment
    //    }
    //}
    private fun handleIntent(intent: Intent) {
        val fragmentToOpen = intent.getStringExtra("openFragment")
        when (fragmentToOpen) {
            "discover" -> {
                navigateTo(DiscoverFragment.newInstance())
                bottomNav.selectedItemId = R.id.navigation_discover
            }
            "chat" -> {
                navigateTo(ChatFragment.newInstance().apply {
                    arguments = Bundle().apply { putString("userId", intent.getStringExtra("userId")) }
                })
                bottomNav.selectedItemId = R.id.navigation_chat
            }
            "like" -> {
                navigateTo(LikesPageFragment.newInstance())
                bottomNav.selectedItemId = R.id.navigation_worth
            }
            "events" -> {
                navigateTo(EventsFragment.newInstance())
                bottomNav.selectedItemId = R.id.navigation_events
            }
            else -> {
                navigateTo(DiscoverFragment.newInstance())  // Default fragment
                bottomNav.selectedItemId = R.id.navigation_discover
            }
        }
    }


    private fun updateFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let { token ->
                    FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
                        FirebaseDatabase.getInstance().getReference("users/$userId/fcmToken").setValue(token)
                    }
                }
            } else {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
            }
        }
    }

    private fun storeCurrentUserId() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            prefs.edit().putString("UserId", it).apply()
        }
    }
}





//test