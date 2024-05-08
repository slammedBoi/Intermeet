package com.intermeet.android


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.intermeet.android.helperFunc.getUserDataRepository

//import com.intermeet.android.helperFunc.getUserDataRepository

// Define the PreferenceActivity class that extends AppCompatActivity and implements listener interfaces.
class EditPreference : AppCompatActivity(), DistanceFragment.OnDistanceSelectedListener, AgeFragment.OnAgeSelectedListener {
    companion object {
        private const val TAG = "PreferenceActivity"
    }

    // Declare UI elements that will be initialized later.
    private lateinit var backButton: Button
    private lateinit var tvDistance: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvReligion: TextView
    private lateinit var tvEthnicity: TextView
    private lateinit var tvInterest: TextView
    private lateinit var tvDrink: TextView
    private lateinit var tvDrugs: TextView
    private lateinit var tvSmoking: TextView
    private lateinit var tvPolitics: TextView

    // Declare UserDataRespository
    private lateinit var userDataRepository: UserDataRepository

    // Initialize arrays to hold the selection options.
    private val religion = arrayOf("Agnostic", "Atheist", "Buddhist", "Catholic", "Christian", "Hindu", "Jewish", "Muslim", "Sikh", "Spiritual", "Other", "Open to all")
    private val ethnicity = arrayOf("Black/African Descent", "East Asian", "Hispanic/Latino", "Middle Eastern", "Native American", "Pacific Islander", "South Asian", "Southeast Asian", "White/Caucasian", "Other", "Open to all")
    private val interested = arrayOf("Men", "Women", "Nonbinary", "Everyone")
    private val drinking = arrayOf("Yes", "No")
    private val drugs = arrayOf("Yes", "No")
    private val smoking = arrayOf("Yes", "No")
    private val politics = arrayOf("Liberal", "Moderate", "Conservative", "Not Political", "Other", "Open to anything")

    // Variables to store user-selected preferences.
    private var selectedDistance: Int = 0
    private var selectedReligion: String? = null
    private var selectedEthnicity: String? = null
    private var selectedInterested: String? = null
    private var selectedMaxAge: Int? = null
    private var selectedMinAge: Int? = null
    private var selectedDrink: String? = null
    private var selectedDrugs: String? = null
    private var selectedSmoking: String? = null
    private var selectedPolitics: String? = null

    // The onCreate method is called when the activity is starting.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_preferences) // Sets the UI layout for this Activity.
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        userDataRepository = getUserDataRepository()
        // Linking variables with their respective view components in the layout.

        tvInterest = findViewById(R.id.tvInterested)
        tvInterest.setOnClickListener { showInterestedPicker() }

        tvDistance = findViewById(R.id.tvDistance)
        tvDistance.setOnClickListener {
            // Create an instance of the DistanceFragment and set the listener.
            val fragment = DistanceFragment().also {
                it.setDistanceListener(this)
                toolbar.visibility = View.GONE // Hide the back button when the fragment is shown.
            }

            // Replace the current fragment/container with the DistanceFragment.
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container1, fragment)
                .addToBackStack(null)
                .commit()
        }

        // Initialize TextViews and set onClickListeners to show selection dialogs.
        tvReligion = findViewById(R.id.tvReligion)
        tvReligion.setOnClickListener { showReligionPicker() }

        tvEthnicity = findViewById(R.id.tvEthnicity)
        tvEthnicity.setOnClickListener { showEthnicityPicker() }

        tvAge = findViewById(R.id.tvAge)
        tvAge.setOnClickListener {
            // Create an instance of the AgeFragment and set the listener.
            val fragment = AgeFragment().also {
                it.setAgeListener(this)
                toolbar.visibility = View.GONE // Hide the back button when the fragment is shown.
            }

            // Replace the current fragment/container with the AgeFragment.
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container2, fragment)
                .addToBackStack(null)
                .commit()
        }

        // More onClickListeners for remaining preferences.
        tvDrink = findViewById(R.id.tvDrink)
        tvDrink.setOnClickListener { showDrinkPicker() }

        tvDrugs = findViewById(R.id.tvDrugs)
        tvDrugs.setOnClickListener { showDrugsPicker() }

        tvSmoking = findViewById(R.id.tvSmoking)
        tvSmoking.setOnClickListener { showSmokingPicker() }

        tvPolitics = findViewById(R.id.tvPolitics)
        tvPolitics.setOnClickListener { showPoliticsPicker() }

        val isEditMode = intent.getBooleanExtra("isEditMode", false)
        if (isEditMode) {
            loadUserPreferences()
        }


        // Setting the backButton's onClickListener to navigate to the DescriptionActivity.
        toolbar.setNavigationOnClickListener {


            val intent = Intent(this, MainActivity::class.java).apply {
                // Clear all activities on top of MainActivity and bring it to the top
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }


            startActivity(intent)

        }
    }

    // Method to display a picker dialog for selecting religion preference.
    private fun showReligionPicker() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = religion.size - 1
            displayedValues = religion
            wrapSelectorWheel = true
        }

        AlertDialog.Builder(this).apply {
            setTitle("Who are you comfortable with")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedReligion = religion[numberPicker.value]
                tvReligion.text = "${religion[numberPicker.value]} >"
                toolbar.visibility = View.VISIBLE
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?:return@setPositiveButton

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                val userData = userDataRepository.userData ?: UserDataModel().apply {
                    religionPreference = selectedReligion
                }
                val userDataMap = mapOf(
                    "religionPreference" to userData.religionPreference,

                )
                // Update Firebase with the new userData
                userRef.updateChildren(userDataMap)
                    .addOnSuccessListener {
                        Log.d("UpdateFirebase", "Successfully updated user data in Firebase.")
                        // Handle success, perhaps by showing a toast or navigating
                    }
                    .addOnFailureListener { e ->
                        Log.w("UpdateFirebase", "Failed to update user data in Firebase.", e)
                        // Handle failure, perhaps by showing an error message
                    }



            }

            setNegativeButton("Cancel", null)
        }.show()
    }

    // Method to display a picker dialog for selecting interested-in gender.
    private fun showInterestedPicker() {
        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = interested.size - 1
            displayedValues = interested
            wrapSelectorWheel = true
        }
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        AlertDialog.Builder(this).apply {
            setTitle("Is there someone who you are looking for specifically?")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedInterested = interested[numberPicker.value]
                tvInterest.text = "${interested[numberPicker.value]} >"
                toolbar.visibility = View.VISIBLE
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                val userData = userDataRepository.userData ?: UserDataModel().apply {
                    genderPreference = selectedInterested
                }
                val userDataMap = mapOf(
                    "genderPreference" to userData.genderPreference,
                )
                // Update Firebase with the new userData
                userRef.updateChildren(userDataMap)
                    .addOnSuccessListener {
                        Log.d("UpdateFirebase", "Successfully updated user data in Firebase.")
                        // Handle success, perhaps by showing a toast or navigating
                    }
                    .addOnFailureListener { e ->
                        Log.w("UpdateFirebase", "Failed to update user data in Firebase.", e)
                        // Handle failure, perhaps by showing an error message
                    }


            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    // Method to display a picker dialog for selecting ethnicity preference.
    private fun showEthnicityPicker() {
        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = ethnicity.size - 1
            displayedValues = ethnicity
            wrapSelectorWheel = true
        }
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        AlertDialog.Builder(this).apply {
            setTitle("Who are you comfortable with?")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedEthnicity = ethnicity[numberPicker.value]
                tvEthnicity.text = "${ethnicity[numberPicker.value]} >"
                toolbar.visibility = View.VISIBLE
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                val userData = userDataRepository.userData ?: UserDataModel().apply {
                    ethnicityPreference = selectedEthnicity
                }
                val userDataMap = mapOf(
                    "ethnicityPreference" to userData.ethnicityPreference,
                )
                // Update Firebase with the new userData
                userRef.updateChildren(userDataMap)
                    .addOnSuccessListener {
                        Log.d("UpdateFirebase", "Successfully updated user data in Firebase.")
                        // Handle success, perhaps by showing a toast or navigating
                    }
                    .addOnFailureListener { e ->
                        Log.w("UpdateFirebase", "Failed to update user data in Firebase.", e)
                        // Handle failure, perhaps by showing an error message
                    }



            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    // Method to display a picker dialog for selecting drinking preference.
    private fun showDrinkPicker() {
        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = drinking.size - 1
            displayedValues = drinking
            wrapSelectorWheel = true
        }
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        AlertDialog.Builder(this).apply {
            setTitle("Mind if they drink?")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedDrink = drinking[numberPicker.value]
                tvDrink.text = "${drinking[numberPicker.value]} >"
                toolbar.visibility = View.VISIBLE
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                val userData = userDataRepository.userData ?: UserDataModel().apply {
                    drinkingPreference = selectedDrink

                }
                val userDataMap = mapOf(
                    "drinkingPreference" to userData.drinkingPreference,

                )
                // Update Firebase with the new userData
                userRef.updateChildren(userDataMap)
                    .addOnSuccessListener {
                        Log.d("UpdateFirebase", "Successfully updated user data in Firebase.")
                        // Handle success, perhaps by showing a toast or navigating
                    }
                    .addOnFailureListener { e ->
                        Log.w("UpdateFirebase", "Failed to update user data in Firebase.", e)
                        // Handle failure, perhaps by showing an error message
                    }


            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    // Method to display a picker dialog for selecting drug use preference.
    private fun showDrugsPicker() {
        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = drugs.size - 1
            displayedValues = drugs
            wrapSelectorWheel = true
        }
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        AlertDialog.Builder(this).apply {
            setTitle("Care if they take anything interesting?")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedDrugs = drugs[numberPicker.value]
                tvDrugs.text = "${drugs[numberPicker.value]} >"
                toolbar.visibility = View.VISIBLE
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                val userData = userDataRepository.userData ?: UserDataModel().apply {
                    drugsPreference = selectedDrugs

                }
                val userDataMap = mapOf(
                    "drugsPreference" to userData.drugsPreference,


                )
                // Update Firebase with the new userData
                userRef.updateChildren(userDataMap)
                    .addOnSuccessListener {
                        Log.d("UpdateFirebase", "Successfully updated user data in Firebase.")
                        // Handle success, perhaps by showing a toast or navigating
                    }
                    .addOnFailureListener { e ->
                        Log.w("UpdateFirebase", "Failed to update user data in Firebase.", e)
                        // Handle failure, perhaps by showing an error message
                    }


            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    // Method to display a picker dialog for selecting smoking preference.
    private fun showSmokingPicker() {
        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = smoking.size - 1
            displayedValues = smoking
            wrapSelectorWheel = true
        }
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        AlertDialog.Builder(this).apply {
            setTitle("Do you mind if they smoke?")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedSmoking = smoking[numberPicker.value]
                tvSmoking.text = "${smoking[numberPicker.value]} >"
                toolbar.visibility = View.VISIBLE
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                val userData = userDataRepository.userData ?: UserDataModel().apply {
                    smokingPreference = selectedSmoking
                }
                val userDataMap = mapOf(
                    "smokingPreference" to userData.smokingPreference,

                )
                // Update Firebase with the new userData
                userRef.updateChildren(userDataMap)
                    .addOnSuccessListener {
                        Log.d("UpdateFirebase", "Successfully updated user data in Firebase.")
                        // Handle success, perhaps by showing a toast or navigating
                    }
                    .addOnFailureListener { e ->
                        Log.w("UpdateFirebase", "Failed to update user data in Firebase.", e)
                        // Handle failure, perhaps by showing an error message
                    }


            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    // Method to display a picker dialog for selecting political preference.
    private fun showPoliticsPicker() {
        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = politics.size - 1
            displayedValues = politics
            wrapSelectorWheel = true
        }
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        AlertDialog.Builder(this).apply {
            setTitle("Care what side of the political spectrum they're on?")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedPolitics = politics[numberPicker.value]
                tvPolitics.text = "${politics[numberPicker.value]} >"
                toolbar.visibility = View.VISIBLE
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                val userData = userDataRepository.userData ?: UserDataModel().apply {
                    politicsPreference = selectedPolitics
                }
                val userDataMap = mapOf(
                    "politicsPreference" to userData.politicsPreference,


                )
                // Update Firebase with the new userData
                userRef.updateChildren(userDataMap)
                    .addOnSuccessListener {
                        Log.d("UpdateFirebase", "Successfully updated user data in Firebase.")
                        // Handle success, perhaps by showing a toast or navigating
                    }
                    .addOnFailureListener { e ->
                        Log.w("UpdateFirebase", "Failed to update user data in Firebase.", e)
                        // Handle failure, perhaps by showing an error message
                    }


            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    // Implementation of the OnDistanceSelectedListener interface.
    override fun onDistanceSelected(distance: Int) {
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        selectedDistance = distance  // Assign the selected distance to the variable.
        tvDistance.text = getString(R.string.selected_distance, distance)  // Update the TextView to display the selected distance.
        toolbar.visibility = View.VISIBLE  // Make the back button visible again.
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val database = Firebase.database
        val userRef = database.getReference("users").child(userId)

        val userData = userDataRepository.userData ?: UserDataModel().apply {
            maxDistancePreference = selectedDistance

        }
        val userDataMap = mapOf(

            "maxDistancePreference" to userData.maxDistancePreference,


        )
        // Update Firebase with the new userData
        userRef.updateChildren(userDataMap)
            .addOnSuccessListener {
                Log.d("UpdateFirebase", "Successfully updated user data in Firebase.")
                // Handle success, perhaps by showing a toast or navigating
            }
            .addOnFailureListener { e ->
                Log.w("UpdateFirebase", "Failed to update user data in Firebase.", e)
                // Handle failure, perhaps by showing an error message
            }


    }

    // Implementation of the OnAgeSelectedListener interface.
    override fun onAgeSelected(minAge: Int, maxAge: Int) {
        tvAge.text = "$minAge - $maxAge years old"  // Update the TextView to display the selected age range.
        selectedMinAge = minAge  // Assign the selected minimum age to the variable.
        selectedMaxAge = maxAge  // Assign the selected maximum age to the variable.
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val database = Firebase.database
        val userRef = database.getReference("users").child(userId)

        val userData = userDataRepository.userData ?: UserDataModel().apply {
            minAgePreference = selectedMinAge
            maxAgePreference = selectedMaxAge
        }
        val userDataMap = mapOf(
            "minAgePreference" to userData.minAgePreference,
            "maxAgePreference" to userData.maxAgePreference

        )
        // Update Firebase with the new userData
        userRef.updateChildren(userDataMap)
            .addOnSuccessListener {
                Log.d("UpdateFirebase", "Successfully updated user data in Firebase.")
                // Handle success, perhaps by showing a toast or navigating
            }
            .addOnFailureListener { e ->
                Log.w("UpdateFirebase", "Failed to update user data in Firebase.", e)
                // Handle failure, perhaps by showing an error message
            }
        toolbar.visibility = View.VISIBLE  // Make the back button visible again.


    }
    private fun loadUserPreferences() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = Firebase.database
        val userDrinkingRef = database.getReference("users").child(userId).child("drinking")
        userDrinkingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Here, you just get the value of "drinking"
                selectedDrink = dataSnapshot.getValue<String>()
                tvDrink.text = "${selectedDrink} >"


            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
                Log.w("EditPreferenceActivity", "loadPost:onCancelled", databaseError.toException())
            }

        })
        val userPoliticsRef = database.getReference("users").child(userId).child("politicsPreference")
        userPoliticsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Here, you just get the value of "drinking"
                selectedPolitics = dataSnapshot.getValue<String>()
                tvPolitics.text = "${selectedPolitics} >"


            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
                Log.w("EditPreferenceActivity", "loadPost:onCancelled", databaseError.toException())
            }

        })
        val userGenderRef = database.getReference("users").child(userId).child("genderPreference")
        userGenderRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Here, you just get the value of "drinking"
                selectedInterested = dataSnapshot.getValue<String>()
                tvInterest.text = "${selectedInterested} >"


            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
                Log.w("EditPreferenceActivity", "loadPost:onCancelled", databaseError.toException())
            }

        })
        val userEthnicityRef = database.getReference("users").child(userId).child("ethnicityPreference")
        userEthnicityRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Here, you just get the value of "drinking"
                selectedEthnicity = dataSnapshot.getValue<String>()
                tvEthnicity.text = "${selectedEthnicity} >"


            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
                Log.w("EditPreferenceActivity", "loadPost:onCancelled", databaseError.toException())
            }

        })
        val userSmokingRef = database.getReference("users").child(userId).child("smokingPreference")
        userSmokingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Here, you just get the value of "drinking"
                selectedSmoking = dataSnapshot.getValue<String>()
                tvSmoking.text = "${selectedSmoking} >"


            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
                Log.w("EditPreferenceActivity", "loadPost:onCancelled", databaseError.toException())
            }

        })
        val userReligionRef = database.getReference("users").child(userId).child("religionPreference")
        userReligionRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Here, you just get the value of "drinking"
                selectedReligion = dataSnapshot.getValue<String>()
                tvReligion.text = "${selectedReligion} >"


            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
                Log.w("EditPreferenceActivity", "loadPost:onCancelled", databaseError.toException())
            }

        })
        val userDistanceRef = database.getReference("users").child(userId).child("maxDistancePreference")
        userDistanceRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Here, you just get the value of "drinking"
                selectedDistance = dataSnapshot.getValue<Int>() ?: 0
                tvDistance.text = getString(R.string.selected_distance, selectedDistance)  // Update the TextView to display the selected distance.
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                val userData = userDataRepository.userData ?: UserDataModel().apply {

                    maxDistancePreference = selectedDistance

                }
                val userDataMap = mapOf(
                    "maxDistancePreference" to userData.maxDistancePreference,

                )
                // Update Firebase with the new userData
                userRef.updateChildren(userDataMap)
                    .addOnSuccessListener {
                        Log.d("UpdateFirebase", "Successfully updated user data in Firebase.")
                        // Handle success, perhaps by showing a toast or navigating
                    }
                    .addOnFailureListener { e ->
                        Log.w("UpdateFirebase", "Failed to update user data in Firebase.", e)
                        // Handle failure, perhaps by showing an error message
                    }


            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
                Log.w("EditPreferenceActivity", "loadPost:onCancelled", databaseError.toException())
            }

        })
        val userMinAgeRef = database.getReference("users").child(userId).child("minAgePreference")
        userMinAgeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                selectedMinAge = dataSnapshot.getValue<Int>() ?: 0
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                val userData = userDataRepository.userData ?: UserDataModel().apply {

                    minAgePreference = selectedMinAge

                }
                val userDataMap = mapOf(

                    "minAgePreference" to userData.minAgePreference,


                )
                // Update Firebase with the new userData
                userRef.updateChildren(userDataMap)
                    .addOnSuccessListener {
                        Log.d("UpdateFirebase", "Successfully updated user data in Firebase.")
                        // Handle success, perhaps by showing a toast or navigating
                    }
                    .addOnFailureListener { e ->
                        Log.w("UpdateFirebase", "Failed to update user data in Firebase.", e)
                        // Handle failure, perhaps by showing an error message
                    }
                updateAgeTextView()
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("EditPreferenceActivity", "loadPost:onCancelled", databaseError.toException())
            }
        })

        val userMaxAgeRef = database.getReference("users").child(userId).child("maxAgePreference")
        userMaxAgeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                selectedMaxAge = dataSnapshot.getValue<Int>() ?: 0
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                val userData = userDataRepository.userData ?: UserDataModel().apply {
                    maxAgePreference = selectedMaxAge
                }
                val userDataMap = mapOf(

                    "maxAgePreference" to userData.maxAgePreference

                )
                // Update Firebase with the new userData
                userRef.updateChildren(userDataMap)
                    .addOnSuccessListener {
                        Log.d("UpdateFirebase", "Successfully updated user data in Firebase.")
                        // Handle success, perhaps by showing a toast or navigating
                    }
                    .addOnFailureListener { e ->
                        Log.w("UpdateFirebase", "Failed to update user data in Firebase.", e)
                        // Handle failure, perhaps by showing an error message
                    }
                updateAgeTextView()
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("EditPreferenceActivity", "loadPost:onCancelled", databaseError.toException())
            }
        })
        val userDrugsRef = database.getReference("users").child(userId).child("drugsPreference")
        userDrugsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Here, you just get the value of "drinking"
                selectedDrugs = dataSnapshot.getValue<String>()
                tvDrugs.text = "${selectedDrugs} >"



            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
                Log.w("EditPreferenceActivity", "loadPost:onCancelled", databaseError.toException())
            }

        })





    }
    private fun updateAgeTextView() {
        if (selectedMinAge != null && selectedMaxAge != null) {
            runOnUiThread {
                tvAge.text = "$selectedMinAge - $selectedMaxAge years old"
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@runOnUiThread

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                val userData = userDataRepository.userData ?: UserDataModel().apply {
                    minAgePreference = selectedMinAge
                    maxAgePreference = selectedMaxAge
                }
                val userDataMap = mapOf(
                    "minAgePreference" to userData.minAgePreference,
                    "maxAgePreference" to userData.maxAgePreference

                )
                // Update Firebase with the new userData
                userRef.updateChildren(userDataMap)
                    .addOnSuccessListener {
                        Log.d("UpdateFirebase", "Successfully updated user data in Firebase.")
                        // Handle success, perhaps by showing a toast or navigating
                    }
                    .addOnFailureListener { e ->
                        Log.w("UpdateFirebase", "Failed to update user data in Firebase.", e)
                        // Handle failure, perhaps by showing an error message
                    }
            }
        }
    }
}
