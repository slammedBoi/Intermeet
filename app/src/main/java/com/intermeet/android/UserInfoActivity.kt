package com.intermeet.android

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.activity.viewModels
import com.intermeet.android.helperFunc.getUserDataRepository

// UserInfoActivity class inherits AppCompatActivity and implements listeners from fragments.
class UserInfoActivity : AppCompatActivity(), OccupationFragment.OccupationListener,
    PronounFragment.PronounListener, TagsFragment.OnTagsSelectedListener {

    // ViewModel instance for shared data across the app's components.
    val sharedViewModel: SharedViewModel by viewModels()

    // UI components declared to be initialized later.
    private lateinit var backButton: Button
    private lateinit var tvGender: TextView
    private lateinit var tvHeight: TextView
    private lateinit var tvReligion: TextView
    private lateinit var tvEthnicity: TextView
    private lateinit var tvJob: TextView
    private lateinit var tvSex: TextView
    private lateinit var tvPronoun: TextView
    private lateinit var tagsDisplay: TextView
    private lateinit var tvDrink: TextView
    private lateinit var tvDrugs: TextView
    private lateinit var tvSmoking: TextView
    private lateinit var tvPolitics: TextView
    private lateinit var btnNavigateFragment: TextView
    private lateinit var userDataRepository: UserDataRepository

    // Variables to store user-selected values.
    private var selectedTags: List<String> = listOf()
    private var selectedGender: String? = null
    private var selectedHeight: String? = null
    private var selectedReligion: String? = null
    private var selectedEthnicity: String? = null
    private var selectedJob: String? = null
    private var selectedSex: String? = null
    private var selectedPronoun: String? = null
    private var selectedDrink: String? = null
    private var selectedDrugs: String? = null
    private var selectedSmoking: String? = null
    private var selectedPolitics: String? = null

    // Arrays containing options for various user attributes.
    private val genders = arrayOf("Male", "Female", "Nonbinary", "Trans", "Other")
    // Array containing height options. Each element represents a possible height value the user can pick.
    private val heights = arrayOf(
        "3'0\"", "3'1\"", "3'2\"", "3'3\"", "3'4\"", "3'5\"", "3'6\"",
        "3'7\"", "3'8\"", "3'9\"", "3'10\"", "3'11\"", "4'0\"", "4'1\"",
        "4'2\"", "4'3\"", "4'4\"", "4'5\"", "4'6\"", "4'7\"", "4'8\"",
        "4'9\"", "4'10\"", "4'11\"", "5'0\"", "5'1\"", "5'2\"", "5'3\"",
        "5'4\"", "5'5\"", "5'6\"", "5'7\"", "5'8\"", "5'9\"", "5'10\"",
        "5'11\"", "6'0\"", "6'1\"", "6'2\"", "6'3\"", "6'4\"", "6'5\"",
        "6'6\"", "6'7\"", "6'8\"", "6'9\"", "6'10\"", "6'11\"", "7'0\""
    ) // These values are typically used in a picker to allow the user to select their height.

    // Array containing religion options. Each element is a string representing a religious affiliation or lack thereof.
    private val religion = arrayOf(
        "Agnostic", "Atheist", "Buddhist", "Catholic", "Christian",
        "Hindu", "Jewish", "Muslim", "Sikh", "Spiritual", "Other",
        "Prefer not to say"
    ) // The user can select their religion or spiritual belief from these options, or choose to not specify.

    // Array containing ethnicity options. Each string represents a different ethnic group the user can identify with.
    private val ethnicity = arrayOf(
        "Black/African Descent", "East Asian", "Hispanic/Latino",
        "Middle Eastern", "Native American", "Pacific Islander",
        "South Asian", "Southeast Asian", "White/Caucasian", "Other",
        "Prefer not to say"
    ) // Users can select from these options to indicate their ethnicity, providing an option for those who prefer not to say or identify as 'Other'.

    // Array containing sexuality options. It represents various sexual orientations a user can identify with.
    private val sexuality = arrayOf(
        "Straight", "Gay", "Lesbian", "Bisexual", "Asexual",
        "Pansexual", "Queer", "Other", "Figuring it out", "Prefer not to say"
    ) // This array allows users to pick a label that best fits their sexual orientation, including an option for those who are still exploring or prefer not to disclose this information.

    private val drinking = arrayOf("Yes", "No", "Prefer not to say")
    private val drugs = arrayOf("Yes", "No", "Prefer not to say")
    private val smoking = arrayOf("Yes", "No", "Prefer not to say")
    private val politics = arrayOf("Liberal", "Moderate", "Conservative", "Not Political", "Other", "Prefer not to say")

    // The onCreate method is called when the activity is starting.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info) // Sets the UI layout for this Activity.
        btnNavigateFragment = findViewById(R.id.addTagButton)
        // Linking variables with their respective view components in the layout.
        backButton = findViewById(R.id.next_button)

        userDataRepository = getUserDataRepository()

        tvGender = findViewById(R.id.tvGender)
        tvHeight = findViewById(R.id.tvHeight)
        tvReligion = findViewById(R.id.tvReligion)
        tvEthnicity = findViewById(R.id.tvEthnicity)
        tvJob = findViewById(R.id.tvJob)
        tvSex = findViewById(R.id.tvSex)
        tvPronoun = findViewById(R.id.tvPronoun)
        tvDrink = findViewById(R.id.tvDrink)
        tvDrugs = findViewById(R.id.tvDrugs)
        tvSmoking = findViewById(R.id.tvSmoking)
        tvPolitics = findViewById(R.id.tvPolitics)
        btnNavigateFragment = findViewById(R.id.addTagButton)

        // Setting onClick listeners to show dialogs for selecting user attributes.
        tvGender.setOnClickListener { showGenderPicker() }
        tvHeight.setOnClickListener { showHeightPicker() }
        tvReligion.setOnClickListener { showReligionPicker() }
        tvEthnicity.setOnClickListener { showEthnicityPicker() }
        tvJob.setOnClickListener { navigateToOccupationFragment() }
        tvSex.setOnClickListener { showSexualityPicker() }
        tvPronoun.setOnClickListener { navigateToPronounFragment() }
        tvDrink.setOnClickListener { showDrinkPicker() }
        tvDrugs.setOnClickListener { showDrugsPicker() }
        tvSmoking.setOnClickListener { showSmokingPicker() }
        tvPolitics.setOnClickListener { showPoliticsPicker() }

        // Setting an onClick listener for the button to navigate to the PreferenceActivity.
        backButton.setOnClickListener {
            val intent = Intent(this, PreferenceActivity::class.java)
            startActivity(intent)
        }

        // Setting an onClick listener for navigating to the TagsFragment.
        btnNavigateFragment.setOnClickListener {
            navigateToTagsFragment()
            backButton.visibility = View.GONE // Hide the back button when navigating to the fragment.
        }
    }

    // Implementing the method from OccupationListener to handle the occupation entered in the fragment.
    override fun onOccupationEntered(occupation: String) {
        updateOccupationUI(occupation)
    }

    // Implementing the method from PronounListener to handle the pronoun entered in the fragment.
    override fun onPronounEntered(pronoun: String) {
        updatePronounUI(pronoun)
    }

    // Implementing the method from TagsSelectionListener to handle the tags selected in the fragment.
    override fun onTagsSelected(tags: List<String>) {
        updateTagsUI(tags)
    }

    // Method for showing a dialog to pick gender.
    private fun showGenderPicker() {
        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = genders.size - 1
            displayedValues = genders
            wrapSelectorWheel = true
        }

        AlertDialog.Builder(this).apply {
            setTitle("Select Your Gender")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedGender = genders[numberPicker.value]
                tvGender.text = "${genders[numberPicker.value]} >"

                // Update user data
                val userData = UserDataRepository.userData ?: UserDataModel()
                userData.gender = selectedGender
            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    // Method for showing a dialog to pick height.
    private fun showHeightPicker() {
        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = heights.size - 1
            displayedValues = heights
            wrapSelectorWheel = true
        }

        AlertDialog.Builder(this).apply {
            setTitle("Select Your Height")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedHeight = heights[numberPicker.value]
                tvHeight.text = "${heights[numberPicker.value]} >"

                // Update user data
                val userData = UserDataRepository.userData ?: UserDataModel()
                userData.height = selectedHeight
            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    // Method for showing a dialog to pick religion.
    private fun showReligionPicker() {
        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = religion.size - 1
            displayedValues = religion
            wrapSelectorWheel = true
        }

        AlertDialog.Builder(this).apply {
            setTitle("Select Your Religion")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedReligion = religion[numberPicker.value]
                tvReligion.text = "${religion[numberPicker.value]} >"

                // Update user data
                val userData = UserDataRepository.userData ?: UserDataModel()
                userData.religion = selectedReligion
            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    // Method for showing a dialog to pick ethnicity.
    private fun showEthnicityPicker() {
        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = ethnicity.size - 1
            displayedValues = ethnicity
            wrapSelectorWheel = true
        }

        AlertDialog.Builder(this).apply {
            setTitle("Select Your Ethnicity")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedEthnicity = ethnicity[numberPicker.value]
                tvEthnicity.text = "${ethnicity[numberPicker.value]} >"

                // Update user data
                val userData = UserDataRepository.userData ?: UserDataModel()
                userData.ethnicity = selectedEthnicity
            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    // Method for showing a dialog to pick sexual orientation.
    private fun showSexualityPicker() {
        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = sexuality.size - 1
            displayedValues = sexuality
            wrapSelectorWheel = true
        }

        AlertDialog.Builder(this).apply {
            setTitle("Select Your Sexuality")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedSex = sexuality[numberPicker.value]
                tvSex.text = "${sexuality[numberPicker.value]} >"

                // Update user data
                val userData = UserDataRepository.userData ?: UserDataModel()
                userData.sexuality = selectedSex
            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    // Method for showing a dialog to pick drinking preference.
    private fun showDrinkPicker() {
        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = drinking.size - 1
            displayedValues = drinking
            wrapSelectorWheel = true
        }

        AlertDialog.Builder(this).apply {
            setTitle("Do you drink?")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedDrink = drinking[numberPicker.value]
                tvDrink.text = "${drinking[numberPicker.value]} >"

                // Update user data
                val userData = UserDataRepository.userData ?: UserDataModel()
                userData.drinking = selectedDrink
            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    // Method for showing a dialog to pick drug usage preference.
    private fun showDrugsPicker() {
        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = drugs.size - 1
            displayedValues = drugs
            wrapSelectorWheel = true
        }

        AlertDialog.Builder(this).apply {
            setTitle("Do you use recreational drugs?")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedDrugs = drugs[numberPicker.value]
                tvDrugs.text = "${drugs[numberPicker.value]} >"

                // Update user data
                val userData = UserDataRepository.userData ?: UserDataModel()
                userData.drugs = selectedDrugs
            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    // Method for showing a dialog to pick smoking preference.
    private fun showSmokingPicker() {
        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = smoking.size - 1
            displayedValues = smoking
            wrapSelectorWheel = true
        }

        AlertDialog.Builder(this).apply {
            setTitle("Do you smoke?")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedSmoking = smoking[numberPicker.value]
                tvSmoking.text = "${smoking[numberPicker.value]} >"

                // Update user data
                val userData = UserDataRepository.userData ?: UserDataModel()
                userData.smoking = selectedSmoking
            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    // Method for showing a dialog to pick political views.
    private fun showPoliticsPicker() {
        val numberPicker = NumberPicker(this).apply {
            minValue = 0
            maxValue = politics.size - 1
            displayedValues = politics
            wrapSelectorWheel = true
        }

        AlertDialog.Builder(this).apply {
            setTitle("What are your political views?")
            setView(numberPicker)
            setPositiveButton("OK") { _, _ ->
                selectedPolitics = politics[numberPicker.value]
                tvPolitics.text = "${politics[numberPicker.value]} >"

                // Update user data
                val userData = UserDataRepository.userData ?: UserDataModel()
                userData.politics = selectedPolitics
            }
            setNegativeButton("Cancel", null)
        }.show()
    }

    // Navigate to the OccupationFragment and set up necessary listeners.
    private fun navigateToOccupationFragment() {
        val occupationFragment = OccupationFragment().also {
            it.setOccupationListener(this)  // Setting the current activity as the listener for the fragment.
            backButton.visibility = View.GONE  // Hides the back button when the fragment is displayed.
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container1, occupationFragment)  // Replaces the content of the container with the OccupationFragment.
            .addToBackStack(null)  // Adds the transaction to the back stack, allowing user navigation back.
            .commit()  // Commits the transaction.
    }

    // Navigate to the PronounFragment and set up necessary listeners.
    private fun navigateToPronounFragment() {
        val pronounFragment = PronounFragment().also {
            it.setPronounListener(this)  // Setting the current activity as the listener for the fragment.
            backButton.visibility = View.GONE  // Hides the back button when the fragment is displayed.
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container2, pronounFragment)  // Replaces the content of the container with the PronounFragment.
            .addToBackStack(null)  // Adds the transaction to the back stack.
            .commit()  // Commits the transaction.
    }

    // Navigate to the TagsFragment.
    private fun navigateToTagsFragment() {
        val tagsFragment = TagsFragment().also {
            backButton.visibility = View.GONE  // Hides the back button when the fragment is displayed.
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container3, tagsFragment)  // Replaces the content of the container with the TagsFragment.
            .addToBackStack(null)  // Adds the transaction to the back stack.
            .commit()  // Commits the transaction.
    }

    // Updates the UI based on the selected occupation.
    private fun updateOccupationUI(occupation: String) {
        tvJob.text = "$occupation >"  // Sets the text of the job TextView to the selected occupation.
        selectedJob = occupation  // Updates the selectedJob variable with the chosen occupation.
        tvJob.visibility = View.VISIBLE  // Makes the job TextView visible.
        backButton.visibility = View.VISIBLE  // Ensures the back button is visible.

        // Update user data
        val userData = UserDataRepository.userData ?: UserDataModel()
        userData.occupation = selectedJob
    }

    // Updates the UI based on the entered pronoun.
    private fun updatePronounUI(pronoun: String) {
        tvPronoun.text = "$pronoun >"  // Sets the text of the pronoun TextView to the selected pronoun.
        selectedPronoun = pronoun  // Updates the selectedPronoun variable with the chosen pronoun.
        tvPronoun.visibility = View.VISIBLE  // Makes the pronoun TextView visible.
        backButton.visibility = View.VISIBLE  // Ensures the back button is visible.

        // Update user data
        val userData = UserDataRepository.userData ?: UserDataModel()
        userData.pronouns = selectedPronoun
    }

    // Updates the UI to display the selected tags.
    private fun updateTagsUI(tags: List<String>) {
        selectedTags = tags
        val tagsContainer: GridLayout = findViewById(R.id.customTagsContainer)  // Retrieves the GridLayout where tags will be displayed.
        tagsContainer.removeAllViews()  // Clears any existing views (tags) in the container.

        // Iterates over the list of selected tags and creates TextViews for each.
        tags.forEach { tag ->
            val textView = TextView(this).apply {
                text = tag  // Sets the text of the TextView to the current tag.
                gravity = Gravity.CENTER  // Centers the text within the TextView.
                textSize = 14f  // Sets the text size.
                setTextColor(Color.BLACK)  // Sets the text color.
                background = ContextCompat.getDrawable(context, R.drawable.tag_background)  // Sets the background drawable.
                setPadding(10, 10, 10, 10)  // Adds padding inside the TextView.

                layoutParams = GridLayout.LayoutParams().apply {
                    setMargins(20, 20, 20, 20)  // Sets margins around the TextView.
                }
            }
            tagsContainer.addView(textView)  // Adds the TextView to the GridLayout.
        }
        sharedViewModel.setSelectedTags(tags)  // Updates the ViewModel with the selected tags.
        backButton.visibility = View.VISIBLE  // Ensures the back button is visible.

        // Update user data
        val userData = UserDataRepository.userData ?: UserDataModel()
        userData.interests = selectedTags
    }

    // Callback method triggered when the activity resumes from the paused state.
    override fun onResume() {
        super.onResume()
        backButton.visibility = View.VISIBLE  // Ensures the back button is visible when the activity resumes.
    }
}

// Interfaces for fragment-to-activity communication.
interface OccupationListener {
    fun onOccupationEntered(occupation: String)  // Callback for when an occupation is entered.
}

interface PronounListener {
    fun onPronounEntered(pronoun: String)  // Callback for when a pronoun is entered.
}

interface OnTagsSelectedListener {
    fun onTagsSelected(tags: List<String>)  // Callback for when tags are selected.
}
