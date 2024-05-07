package com.intermeet.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
//import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.intermeet.android.helperFunc.getUserDataRepository

class LocationActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPermissionRequest: androidx.activity.result.ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initializePermissionRequest()
        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        checkPermissionAndGetLocation() // Check permissions every time the activity resumes
    }

    private fun initializePermissionRequest() {
        locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted) {
                // Precise location access granted
                Toast.makeText(this, "Fine location access granted.", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            } else if (coarseLocationGranted) {
                // Only approximate location access granted
                Toast.makeText(this, "Coarse location access granted.", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            } else {
                // No location access granted
                Toast.makeText(this, "Location access denied.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.allow).setOnClickListener {
            checkPermissionAndGetLocation()
        }
        findViewById<Button>(R.id.dont_allow).setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }
    }

    private fun checkPermissionAndGetLocation() {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocationPermission || !hasCoarseLocationPermission) {
            // Request both permissions if not already granted
            locationPermissionRequest.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        } else {
            // Permissions are already granted
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(this) { location: Location? ->
            location?.let {
                val userDataRepository = getUserDataRepository()
                val userData = userDataRepository.userData ?: UserDataModel()
                userData.latitude = location.latitude
                userData.longitude = location.longitude
                Log.d("LocationActivity", "Location: ${location.latitude}, ${location.longitude}")
                startActivity(Intent(this, NotificationActivity::class.java))
            } ?: Toast.makeText(this, "Failed to get current location.", Toast.LENGTH_SHORT).show()

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener(this) { location ->
                if (location != null) {
                    // Logic to handle location object
                    val userDataRepository = getUserDataRepository()
                    val userData = UserDataRepository.userData ?: UserDataModel()
                    userData.latitude = location.latitude
                    userData.longitude = location.longitude

                }
            }
    }
    //This code fucked up the program
    // Permission request launcher
    //val requestPermissionLauncher =
    //    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
    //        if (isGranted) {
    //            // Permission granted, proceed with getting the current location
    //            getCurrentLocation()
    //        } else {
    //            // Handle the case where permission is denied
    //            Toast.makeText(this, "Location permission denied", Toast.LENGTH_LONG).show()
    //        }
    //    }
    }
}
