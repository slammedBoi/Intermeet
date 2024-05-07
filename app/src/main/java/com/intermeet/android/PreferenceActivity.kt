// Specify the package name for the class.
package com.intermeet.android

// Import necessary Android and Kotlin libraries.
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.intermeet.android.helperFunc.getUserDataRepository

// Define the PreferenceActivity class that extends AppCompatActivity and implements listener interfaces.
class PreferenceActivity : AppCompatActivity(), DistanceFragment.OnDistanceSelectedListener,
    AgeFragment.OnAgeSelectedListener {

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
        setContentView(R.layout.activity_preference) // Sets the UI layout for this Activity.

        userDataRepository = getUserDataRepository()


        // Linking variables with their respective view components in the layout.
        backButton = findViewById(R.id.next_button)
        tvInterest = findViewById(R.id.tvInterested)
        tvInterest.setOnClickListener { showInterestedPicker() }

        tvDistance = findViewById(R.id.tvDistance)
        tvDistance.setOnClickListener {
            // Create an instance of the DistanceFragment and set the listener.
            val fragment = DistanceFragment().also {
                it.setDistanceListener(this)
                backButton.visibility = View.GONE // Hide the back button when the fragment is shown.
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
                backButton.visibility = View.GONE // Hide the back button when the fragment is shown.
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

        // Setting the backButton's onClickListener to navigate to the DescriptionActivity.
        backButton.setOnClickListener {
            val intent = Intent(this, DescriptionActivity::class.java)
            startActivity(intent)
        }
    }

    // Method to display a picker dialog for selecting religion preference.
    private fun showReligionPicker() {
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
                backButton.visibility = View.VISIBLE

                // Update preference
                val userData = UserDataRepository.userData ?: UserDataModel()
                userData.religionPreference = selectedReligion
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

        AlertDialog.Builder(this).apply {
            setTitle("Is there someone who you are looking for specifically?")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedInterested = interested[numberPicker.value]
                tvInterest.text = "${interested[numberPicker.value]} >"
                backButton.visibility = View.VISIBLE

                // Update preference
                val userData = UserDataRepository.userData ?: UserDataModel()
                userData.genderPreference = selectedInterested
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

        AlertDialog.Builder(this).apply {
            setTitle("Who are you comfortable with?")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedEthnicity = ethnicity[numberPicker.value]
                tvEthnicity.text = "${ethnicity[numberPicker.value]} >"
                backButton.visibility = View.VISIBLE

                // Update preference
                val userData = UserDataRepository.userData ?: UserDataModel()
                userData.ethnicityPreference = selectedEthnicity
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

        AlertDialog.Builder(this).apply {
            setTitle("Mind if they drink?")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedDrink = drinking[numberPicker.value]
                tvDrink.text = "${drinking[numberPicker.value]} >"
                backButton.visibility = View.VISIBLE

                // Update preference
                val userData = UserDataRepository.userData ?: UserDataModel()
                userData.drinkingPreference = selectedDrink
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

        AlertDialog.Builder(this).apply {
            setTitle("Care if they take anything interesting?")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedDrugs = drugs[numberPicker.value]
                tvDrugs.text = "${drugs[numberPicker.value]} >"
                backButton.visibility = View.VISIBLE

                // Update preference
                val userData = UserDataRepository.userData ?: UserDataModel()
                userData.drugsPreference = selectedDrugs
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

        AlertDialog.Builder(this).apply {
            setTitle("Do you mind if they smoke?")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedSmoking = smoking[numberPicker.value]
                tvSmoking.text = "${smoking[numberPicker.value]} >"
                backButton.visibility = View.VISIBLE

                // Update preference
                val userData = UserDataRepository.userData ?: UserDataModel()
                userData.smokingPreference = selectedDrugs
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

        AlertDialog.Builder(this).apply {
            setTitle("Care what side of the political spectrum they're on?")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedPolitics = politics[numberPicker.value]
                tvPolitics.text = "${politics[numberPicker.value]} >"
                backButton.visibility = View.VISIBLE

                // Update preference
                val userData = UserDataRepository.userData ?: UserDataModel()
                userData.politicsPreference = selectedPolitics
            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    // Implementation of the OnDistanceSelectedListener interface.
    override fun onDistanceSelected(distance: Int) {
        selectedDistance = distance  // Assign the selected distance to the variable.
        tvDistance.text = getString(R.string.selected_distance, distance)  // Update the TextView to display the selected distance.
        backButton.visibility = View.VISIBLE  // Make the back button visible again.

        // Update preference
        val userData = UserDataRepository.userData ?: UserDataModel()
        userData.maxDistancePreference = selectedDistance
    }

    // Implementation of the OnAgeSelectedListener interface.
    override fun onAgeSelected(minAge: Int, maxAge: Int) {
        tvAge.text = "$minAge - $maxAge years old"  // Update the TextView to display the selected age range.
        selectedMinAge = minAge  // Assign the selected minimum age to the variable.
        selectedMaxAge = maxAge  // Assign the selected maximum age to the variable.
        backButton.visibility = View.VISIBLE  // Make the back button visible again.

        // Update preference
        val userData = UserDataRepository.userData ?: UserDataModel()
        userData.minAgePreference = selectedMinAge
        userData.maxAgePreference = selectedMaxAge
    }
}
