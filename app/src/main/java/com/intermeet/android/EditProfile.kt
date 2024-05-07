package com.intermeet.android

//import CustomAdapter
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.ListView
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.intermeet.android.helperFunc.getUserDataRepository

// UserInfoActivity class inherits AppCompatActivity and implements listeners from fragments.
class EditProfile : AppCompatActivity(),  EditTagsFragments.OnTagsSelectedListener,
    EditPronounFragment.PronounListener, EditOccupation.editOccupationListener,
    editPronounListener {
    companion object {
        private const val TAG = "EditProfile"

    }

    // ViewModel instance for shared data across the app's components.
    val sharedViewModel: SharedViewModel by viewModels()

    // UI components declared to be initialized later.

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
    private lateinit var promptsListView: ListView
    private lateinit var promptsCustomAdapter: CustomAdapter
    private lateinit var deleteCustomAdapter: CustomAdapter
    private var promptsList: ArrayList<String> = arrayListOf()
    private var userPrompts: MutableList<String> = mutableListOf()
    private lateinit var promptTextbox: EditText
    private lateinit var enterPromptImage: ImageView
    private lateinit var promptDropdown: Spinner
    private lateinit var  deleteButton: Button
    private lateinit var imageViews: List<ImageView>
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var userPhotoUrlsRef: DatabaseReference
    private lateinit var storageReference: StorageReference



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
    private var aboutMeIntroText: String? = null
    private var currentImageIndex = -1
    private val imageUris = arrayOfNulls<Uri>(5)



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
            setContentView(R.layout.activity_edit_profile) // Sets the UI layout for this Activity.
            btnNavigateFragment = findViewById(R.id.addTagButton)
            // Linking variables with their respective view components in the layout.

            promptTextbox = findViewById(R.id.enter_prompt)
            enterPromptImage = findViewById(R.id.add)
            promptDropdown = findViewById(R.id.prompt_spinner)
            promptsListView = findViewById(R.id.listView)





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
            promptsListView = findViewById(R.id.listView) // Replace with your actual ListView ID
            promptsCustomAdapter = CustomAdapter(this, promptsList, promptsListView)

            promptsListView.adapter = promptsCustomAdapter


            val isEditMode = intent.getBooleanExtra("isEditMode", false)
            if (isEditMode) {
                loadUserPrompts()

                promptsListView.setOnItemClickListener { _, _, position, _ ->
                    val promptWithResponse = userPrompts[position]
                    val parts = promptWithResponse.split("... ")
                    val prompt = parts[0] + "..."
                    val response = parts.getOrElse(1) { "" }

                    showEditPromptDialog(prompt, response, position)

                }
                //initializeImageViews()
                //firebaseStorage = FirebaseStorage.getInstance()
                loadUserPhotoUrls()
                loadUserIntro()
                loadUserPreferences()
            }
            enterPromptImage.setOnClickListener {
                val text = promptTextbox.text.toString()
                val selectedPrompt = promptDropdown.selectedItem.toString()
                val combinedText = "$selectedPrompt $text"

                if (text.isEmpty()) {
                    Toast.makeText(this, "Please fill in the prompt.", Toast.LENGTH_SHORT).show()
                } else {
                    // Hide the keyboard
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(promptTextbox.windowToken, 0)

                    // Add the combined text to your list and update the adapter
                    promptsList.add(combinedText)
                    promptsCustomAdapter.notifyDataSetChanged()
                    setListViewHeightBasedOnChildren(promptsListView)

                    // Clear the text box for the next entry
                    promptTextbox.setText("")
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                    val database = Firebase.database
                    val userRef = database.getReference("users").child(userId)
                    val userDataMap = mapOf(
                        "prompts" to promptsList,

                        )
                    userRef.updateChildren(userDataMap)

                }
            }
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            firebaseStorage = FirebaseStorage.getInstance()
            userPhotoUrlsRef = Firebase.database.getReference("users").child(userId).child("photoDownloadUrls")
            storageReference = firebaseStorage.getReference("users/$userId/photos")
            imageViews = listOf(
                findViewById(R.id.imageView1),
                findViewById(R.id.imageView2),
                findViewById(R.id.imageView3),
                findViewById(R.id.imageView4),
                findViewById(R.id.imageView5)
            )
            initializeImagePickerLauncher()
            setupCloseIconListeners()

            imageViews.forEachIndexed { index, imageView ->
                imageView.setOnClickListener {
                    currentImageIndex = index // Set the current index to know which ImageView to update
                    imagePickerLauncher.launch("image/*") // Launch the image picker
                }
            }
            val toolbar: Toolbar = findViewById(R.id.toolbar)




            // Setting an onClick listener for the button to navigate to the PreferenceActivity.
            toolbar.setNavigationOnClickListener {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setNavigationOnClickListener

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)
                val introEditText: EditText = findViewById(R.id.IntroText)
                aboutMeIntroText = introEditText.text.toString()
                val userData = userDataRepository.userData ?: UserDataModel().apply{
                    aboutMeIntro = aboutMeIntroText




                }
                val userDataMap = mapOf(
                    "aboutMeIntro" to userData.aboutMeIntro
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
                val intent = Intent(this, MainActivity::class.java).apply {
                    // Clear all activities on top of MainActivity and bring it to the top
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                val text = promptTextbox.text.toString()
                val selectedPrompt = promptDropdown.selectedItem.toString()

                val combinedText = "$selectedPrompt\n$text"

                val promptsRef = database.getReference("users").child(userId).child("prompts")


                startActivity(intent)

            }

            // Setting an onClick listener for navigating to the TagsFragment.
            btnNavigateFragment.setOnClickListener {
                navigateToTagsFragment()

            }
    }
    private fun initializeImagePickerLauncher() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null && currentImageIndex != -1) {
                imageViews[currentImageIndex].setImageURI(uri) // Display the selected image immediately
                uploadImageToFirebaseStorage(uri)  // Trigger uploading to Firebase Storage
            } else {
                Toast.makeText(this, "Error: Please select an image.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun uploadImageToFirebaseStorage(uri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val photoRef = storageReference.child("images/${userId}/${System.currentTimeMillis()}")

        photoRef.putFile(uri).addOnSuccessListener {
            photoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val newUrl = downloadUri.toString()
                imageUris[currentImageIndex] = Uri.parse(newUrl)  // Update local URI list
                updatePhotoDownloadUrlInFirebase(newUrl, currentImageIndex)  // Pass currentImageIndex here
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updatePhotoDownloadUrlInFirebase(newUrl: String?, index: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (newUrl == null) {
            // If newUrl is null, remove the value from Firebase
            userPhotoUrlsRef.child(index.toString()).removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "Photo URL removed successfully from Firebase.")
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to remove photo URL from Firebase.", it)
                }
        } else {
            // If newUrl is not null, update Firebase with the new URL
            userPhotoUrlsRef.child(index.toString()).setValue(newUrl)
                .addOnSuccessListener {
                    Log.d(TAG, "Photo URL updated successfully in Firebase.")
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to update photo URL in Firebase.", it)
                }
        }
    }






    private fun storeSelectedUris() {
        val userDataRepository = getUserDataRepository()
        userDataRepository.userData?.photoUris = imageUris.filterNotNull().toMutableList()

        // Save/update the list of image URIs in Firebase if needed
        updateFirebaseWithImageUris()
    }

    private fun updateFirebaseWithImageUris() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = Firebase.database.getReference("users/$userId/photoDownloadUrls")

        // Filter non-null URIs and map them to strings for Firebase storage
        val uriStrings = imageUris.filterNotNull().map { it.toString() }

        userRef.setValue(uriStrings)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully updated image URIs in Firebase.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update image URIs in Firebase.", e)
            }
    }





    private fun loadUserPhotoUrls() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userPhotoUrlsRef = Firebase.database.getReference("users").child(userId).child("photoDownloadUrls")

        userPhotoUrlsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val urls = dataSnapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: return
                displayUserPhotos(urls)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadUserPhotoUrls:onCancelled", databaseError.toException())
            }
        })
    }
    private fun displayUserPhotos(urls: List<String>) {
        urls.forEachIndexed { index, url ->
            if (index < imageViews.size) {
                Glide.with(this)
                    .load(url)
                    .into(imageViews[index])
            }
        }
    }
    private fun setupCloseIconListeners() {
        val closeIcons = listOf(
            findViewById<ImageView>(R.id.closeImageView1),
            findViewById<ImageView>(R.id.closeImageView2),
            findViewById<ImageView>(R.id.closeImageView3),
            findViewById<ImageView>(R.id.closeImageView4),
            findViewById<ImageView>(R.id.closeImageView5),
            // Add all the corresponding close icons...
        )

        imageViews.forEachIndexed { index, imageView ->
            closeIcons[index].setOnClickListener {
                removeImageUrlAt(index)
            }
        }
    }
    private fun fetchPhotoUrls(completion: (List<String>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userPhotoUrlsRef = Firebase.database.getReference("users/$userId/photoDownloadUrls")

        userPhotoUrlsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val urls = dataSnapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                completion(urls ?: listOf())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "fetchPhotoUrls:onCancelled", databaseError.toException())
            }
        })
    }

    private fun updateUserPhotoUrlsInFirebase(urls: List<String>) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userPhotoUrlsRef = Firebase.database.getReference("users/$userId/photoDownloadUrls")

        userPhotoUrlsRef.setValue(urls)
            .addOnSuccessListener {
                Log.d(TAG, "Photo URLs updated successfully in Firebase.")
                // Use the current value of isEditMode when restarting
                val isEditMode = intent.getBooleanExtra("isEditMode", false)
                restartActivity(isEditMode)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update photo URLs in Firebase.", e)
            }
    }



    private fun removeImageUrlAt(index: Int) {
        fetchPhotoUrls { urls ->
            // Ensure the index is within the list size
            if (index < urls.size) {
                // Create a new list with the URL at the index removed
                val updatedUrls = urls.toMutableList().apply {
                    removeAt(index)
                }

                // Update Firebase with the new list of URLs
                updateUserPhotoUrlsInFirebase(updatedUrls)
            }
        }
    }
    private fun restartActivity(isEditMode: Boolean) {
        val intent = Intent(this, EditProfile::class.java)
        intent.putExtra("isEditMode", isEditMode)  // Re-pass the isEditMode flag
        finish() // Call this to finish the current activity
        startActivity(intent) // Start a new instance of the current activity with the flag
    }



    // Update Firebase with new image URIs and provide a callback for when it succeeds
    private fun updateUserPhotoUrlsInFirebase(uriStrings: List<String>, onSuccess: () -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userPhotoUrlsRef = Firebase.database.getReference("users/$userId/photoDownloadUrls")

        userPhotoUrlsRef.setValue(uriStrings)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully updated image URIs in Firebase.")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update image URIs in Firebase.", e)
            }
    }

    private fun updateUiAfterImageRemoval() {
        imageViews.forEachIndexed { index, imageView ->
            val uri = imageUris[index]
            if (uri == null) {
                imageView.setImageResource(R.drawable.image_placeholder) // Set placeholder if no image is present
            } else {
                Glide.with(this)
                    .load(uri)
                    .into(imageView) // Load the image from the URI
            }
        }
    }



    private fun removeImageAt(index: Int) {
        imageUris[index] = null  // Clear the URI
        imageViews[index].setImageResource(R.drawable.image_placeholder)  // Set back to placeholder
        // Optionally, remove the URL from Firebase or handle other clean-up tasks
        updatePhotoDownloadUrlInFirebase(null, index)
    }

    private fun loadUserPrompts() {
        // Retrieve prompts from Firebase
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userPromptsRef = Firebase.database.getReference("users").child(userId).child("prompts")
        userPromptsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                promptsList.clear()
                for (promptSnapshot in dataSnapshot.children) {
                    val prompt = promptSnapshot.getValue<String>()
                    if (prompt != null) {
                        promptsList.add(prompt)
                    }
                }
                promptsCustomAdapter.notifyDataSetChanged()
                setListViewHeightBasedOnChildren(promptsListView)



            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadUserPrompts:onCancelled", databaseError.toException())
            }
        })
    }
    private fun loadUserIntro() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userAboutRef = Firebase.database.getReference("users").child(userId).child("aboutMeIntro")
        userAboutRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                aboutMeIntroText = dataSnapshot.getValue((String::class.java))
                val introEditText: EditText = findViewById(R.id.IntroText)
                introEditText.setText(aboutMeIntroText)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "loadUserIntro:onCancelled", databaseError.toException())
            }
        })
    }

    private fun showEditPromptDialog(prompt: String, response: String, position: Int) {
        val input = EditText(this).apply {
            setText(response)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Prompt")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newResponse = "$prompt ${input.text.toString()}"
                userPrompts[position] = newResponse
                promptsCustomAdapter.notifyDataSetChanged()
                updatePromptInFirebase(position, newResponse)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    fun setListViewHeightBasedOnChildren(listView: ListView) {
        val listAdapter = listView.adapter ?: return
        var totalHeight = 0
        val desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.width, View.MeasureSpec.AT_MOST)

        for (i in 0 until listAdapter.count) {
            val listItem = listAdapter.getView(i, null, listView)
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
            totalHeight += listItem.measuredHeight
        }

        val layoutParams = listView.layoutParams
        layoutParams.height = totalHeight + (listView.dividerHeight * (listAdapter.count - 1))
        listView.layoutParams = layoutParams
        listView.requestLayout()
    }


    private fun updatePromptInFirebase(position: Int, newResponse: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = Firebase.database

        val userRef = database.getReference("users").child(userId)
        val userDataMap = mapOf(
            "prompts" to promptsList,

            )
        userRef.updateChildren(userDataMap)

            .addOnSuccessListener {
                Toast.makeText(this, "Prompt updated successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update prompt.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserPreferences() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = Firebase.database
        val userGenderRef = database.getReference("users").child(userId).child("gender")
        userGenderRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Here, you just get the value of "drinking"
                selectedGender = dataSnapshot.getValue<String>()
                tvGender.text = "${selectedGender} >"


            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
                Log.w("EditPreferenceActivity", "loadPost:onCancelled", databaseError.toException())
            }

        })
        val userHeightRef = database.getReference("users").child(userId).child("height")
        userHeightRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Here, you just get the value of "drinking"
                selectedHeight = dataSnapshot.getValue<String>()
                tvHeight.text = "${selectedHeight} >"


            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
                Log.w("EditPreferenceActivity", "loadPost:onCancelled", databaseError.toException())
            }

        })
        val userReligionRef = database.getReference("users").child(userId).child("religion")
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
        val userEthnicityRef = database.getReference("users").child(userId).child("ethnicity")
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
        val userOccupationRef = database.getReference("users").child(userId).child("occupation")
        userOccupationRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Here, you just get the value of "drinking"
                selectedJob = dataSnapshot.getValue<String>()
                tvJob.text = "${selectedJob} >"


            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
                Log.w("EditPreferenceActivity", "loadPost:onCancelled", databaseError.toException())
            }

        })
        val userSexualityRef = database.getReference("users").child(userId).child("sexuality")
        userSexualityRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Here, you just get the value of "drinking"
                selectedSex = dataSnapshot.getValue<String>()
                tvSex.text = "${selectedSex} >"


            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
                Log.w("EditPreferenceActivity", "loadPost:onCancelled", databaseError.toException())
            }

        })
        val userPronounRef = database.getReference("users").child(userId).child("pronouns")
        userPronounRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Here, you just get the value of "drinking"
                selectedPronoun = dataSnapshot.getValue<String>()
                tvPronoun.text = "${selectedPronoun} >"


            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
                Log.w("EditPreferenceActivity", "loadPost:onCancelled", databaseError.toException())
            }

        })
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
        val userDrugsRef = database.getReference("users").child(userId).child("drugs")
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
        val userSmokingRef = database.getReference("users").child(userId).child("smoking")
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
        val userPoliticsRef = database.getReference("users").child(userId).child("politics")
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
        val userInterestRef = database.getReference("users").child(userId).child("interests")
        userInterestRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Here, you just get the value of "drinking"
                val selectedTags: List<String>? = dataSnapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                if (selectedTags != null) {
                    updateTagsUI(selectedTags)
                }


            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
                Log.w("EditPreferenceActivity", "loadPost:onCancelled", databaseError.toException())
            }

        })


    }

    // Implementing the method from OccupationListener to handle the occupation entered in the fragment.
    override fun onEditOccupationEntered(occupation: String) {
        updateOccupationUI(occupation)
    }


    // Implementing the method from PronounListener to handle the pronoun entered in the fragment.
    override fun onEditPronounEntered(pronoun: String) {
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
                val userData = userDataRepository.userData ?: UserDataModel()
                userData.gender = selectedGender

                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                userDataRepository.userData ?: UserDataModel().apply{
                    gender = selectedGender
                }
                val userDataMap = mapOf(
                    "gender" to userData.gender
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
                val userData = userDataRepository.userData ?: UserDataModel()
                userData.height = selectedHeight
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                userDataRepository.userData ?: UserDataModel().apply{
                    height = selectedHeight
                }
                val userDataMap = mapOf(
                    "height" to userData.height
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
                val userData = userDataRepository.userData ?: UserDataModel()
                userData.religion = selectedReligion
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                userDataRepository.userData ?: UserDataModel().apply{
                    religion = selectedReligion
                }
                val userDataMap = mapOf(
                    "religion" to userData.religion
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
                val userData = userDataRepository.userData ?: UserDataModel()
                userData.ethnicity = selectedEthnicity
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                userDataRepository.userData ?: UserDataModel().apply{
                    ethnicity = selectedEthnicity
                }
                val userDataMap = mapOf(
                    "ethnicity" to userData.ethnicity
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
                val userData = userDataRepository.userData ?: UserDataModel()
                userData.sexuality = selectedSex
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                userDataRepository.userData ?: UserDataModel().apply{
                    sexuality = selectedSex
                }
                val userDataMap = mapOf(
                    "sexuality" to userData.sexuality
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
                val userData = userDataRepository.userData ?: UserDataModel()
                userData.drinking = selectedDrink
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                userDataRepository.userData ?: UserDataModel().apply{
                    drinking = selectedDrink
                }
                val userDataMap = mapOf(
                    "drinking" to userData.drinking
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
                val userData = userDataRepository.userData ?: UserDataModel()
                userData.drugs = selectedDrugs
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                userDataRepository.userData ?: UserDataModel().apply{
                    drugs = selectedDrugs
                }
                val userDataMap = mapOf(
                    "drugs" to userData.drugs
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
                val userData = userDataRepository.userData ?: UserDataModel()
                userData.smoking = selectedSmoking
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                userDataRepository.userData ?: UserDataModel().apply{
                    smoking = selectedSmoking
                }
                val userDataMap = mapOf(
                    "smoking" to userData.smoking
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
                val userData = userDataRepository.userData ?: UserDataModel()
                userData.politics = selectedPolitics
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton

                val database = Firebase.database
                val userRef = database.getReference("users").child(userId)

                userDataRepository.userData ?: UserDataModel().apply{
                    politics = selectedPolitics
                }
                val userDataMap = mapOf(
                    "politics" to userData.politics
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

    // Navigate to the OccupationFragment and set up necessary listeners.
    private fun navigateToOccupationFragment() {
        val editOccupationFragment = EditOccupation().also {
            it.setEditOccupationListener(this)  // Setting the current activity as the listener for the fragment.

            val toolbar: Toolbar = findViewById(R.id.toolbar)
            toolbar.visibility = View.GONE
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container1, editOccupationFragment)  // Replaces the content of the container with the OccupationFragment.
            .addToBackStack(null)  // Adds the transaction to the back stack, allowing user navigation back.
            .commit()  // Commits the transaction.
    }

    // Navigate to the PronounFragment and set up necessary listeners.
    private fun navigateToPronounFragment() {
        val editPronounFragment = EditPronounFragment().also {
            it.editPronounListener(this)  // Setting the current activity as the listener for the fragment.

            val toolbar: Toolbar = findViewById(R.id.toolbar)
            toolbar.visibility = View.GONE
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container2, editPronounFragment)  // Replaces the content of the container with the PronounFragment.
            .addToBackStack(null)  // Adds the transaction to the back stack.
            .commit()  // Commits the transaction.
    }

    // Navigate to the TagsFragment.
    private fun navigateToTagsFragment() {
        val tagsFragment = EditTagsFragments().also {

            val toolbar: Toolbar = findViewById(R.id.toolbar)
            toolbar.visibility = View.GONE
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container3, tagsFragment)  // Replaces the content of the container with the TagsFragment.
            .addToBackStack(null)  // Adds the transaction to the back stack.
            .commit()  // Commits the transaction.
    }

    // Updates the UI based on the selected occupation.
    private fun updateOccupationUI(job: String) {
        tvJob.text = "$job >"  // Sets the text of the job TextView to the selected occupation.
        selectedJob = job  // Updates the selectedJob variable with the chosen occupation.
        tvJob.visibility = View.VISIBLE  // Makes the job TextView visible.

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.visibility = View.VISIBLE

        // Update user data
        val userData = userDataRepository.userData ?: UserDataModel()
        userData.occupation = selectedJob
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val database = Firebase.database
        val userRef = database.getReference("users").child(userId)

        userDataRepository.userData ?: UserDataModel().apply{
            occupation = selectedJob
        }
        val userDataMap = mapOf(
            "occupation" to userData.occupation
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

    // Updates the UI based on the entered pronoun.
    private fun updatePronounUI(pronoun: String) {
        tvPronoun.text = "$pronoun >"  // Sets the text of the pronoun TextView to the selected pronoun.
        selectedPronoun = pronoun  // Updates the selectedPronoun variable with the chosen pronoun.
        tvPronoun.visibility = View.VISIBLE  // Makes the pronoun TextView visible.

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.visibility = View.VISIBLE

        // Update user data
        val userData = userDataRepository.userData ?: UserDataModel()
        userData.pronouns = selectedPronoun
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val database = Firebase.database
        val userRef = database.getReference("users").child(userId)

        userDataRepository.userData ?: UserDataModel().apply{
            pronouns = selectedPronoun
        }
        val userDataMap = mapOf(
            "pronouns" to userData.pronouns
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

    // Updates the UI to display the selected tags.
    private fun updateTagsUI(tags: List<String>) {
        selectedTags = tags
        Log.d(TAG, "test: $tags")
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

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.visibility = View.VISIBLE
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val database = Firebase.database
        val userRef = database.getReference("users").child(userId)

        val userData = userDataRepository.userData ?: UserDataModel().apply{
            interests = selectedTags
        }
        val userDataMap = mapOf(
            "interests" to userData.interests,
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


        // Update user data
        //val userData = userDataRepository.userData ?: UserDataModel()
        //userData.interests = selectedTags
    }

    // Callback method triggered when the activity resumes from the paused state.
    override fun onResume() {
        super.onResume()

    }

}

// Interfaces for fragment-to-activity communication.
interface editOccupationListener {
    fun onOccupationEntered(occupation: String)  // Callback for when an occupation is entered.
}

interface editPronounListener {
    fun onEditPronounEntered(pronoun: String)  // Callback for when a pronoun is entered.
}

interface editOnTagsSelectedListener {
    fun onTagsSelected(tags: List<String>)  // Callback for when tags are selected.
}
