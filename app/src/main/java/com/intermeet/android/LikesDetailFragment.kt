package com.intermeet.android

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide

//import com.intermeet.android.helperFunc.calculateAgeWithCalendar

class LikesDetailFragment : Fragment() {
    private lateinit var textViewName: TextView
    private lateinit var cardView: CardView
    private lateinit var cardView2: CardView
    private lateinit var tvEducation: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvPronouns: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvAboutMeHeader: TextView
    private lateinit var tvAboutMe: TextView
    private lateinit var tvInterestsHeader: TextView
    private lateinit var tvEthnicity: TextView
    private lateinit var relativeLayout: RelativeLayout
    private val viewModel: DiscoverViewModel by viewModels()

    private var userId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_likes_detail, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textViewName = view.findViewById(R.id.textViewName)
        cardView = view.findViewById(R.id.cardView)
        cardView2 = view.findViewById(R.id.cardView2)
        tvEducation = view.findViewById(R.id.tvEducation)
        tvLocation = view.findViewById(R.id.tvLocation)
        tvPronouns = view.findViewById(R.id.tvPronouns)
        tvGender = view.findViewById(R.id.tvGender)
        tvAboutMeHeader = view.findViewById(R.id.tvAboutMeHeader)
        tvAboutMe = view.findViewById(R.id.tvAboutMe)
        tvInterestsHeader = view.findViewById(R.id.tvInterestsHeader)
        tvEthnicity = view.findViewById(R.id.tvEthnicity)
        relativeLayout = view.findViewById(R.id.relativeLayout)

        userId?.let { id ->
            viewModel.fetchUserData(id)
            viewModel.userData.observe(viewLifecycleOwner) { userData ->
                userData?.let { updateUserUI(it) }
            }
        }
        val returnButton = view.findViewById<View>(R.id.return_button)
        returnButton.setOnClickListener {
            // Use fragmentManager for older versions of the support library,
            // use parentFragmentManager for newer versions (since Fragment 1.3.0-alpha04)
            parentFragmentManager.popBackStack()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateUserUI(user: UserDataModel) {
        textViewName.text = "${user.firstName}, ${calculateAge(user.birthday)}"
        tvEducation.text = user.school
        tvLocation.text = "${user.latitude}, ${user.longitude}"
        tvPronouns.text = user.pronouns
        tvGender.text = user.gender
        tvAboutMeHeader.visibility = if (user.aboutMeIntro?.isNotEmpty() == true) View.VISIBLE else View.GONE
        tvAboutMe.text = user.aboutMeIntro
        tvEthnicity.text = user.ethnicity

        val imageView1 = view?.findViewById<ImageView>(R.id.imageView1)
        Glide.with(this)
            .load(user.photoDownloadUrls.firstOrNull())
            .centerCrop()
            .into(imageView1 ?: return)

        val imageUrls = user.photoDownloadUrls.drop(1) // Drops the first element

        var belowId = R.id.cardView2 // Initialize with cardView2's ID

        imageUrls.forEachIndexed { index, imageUrl ->
            val newCardView = createCardView(imageUrl)
            val layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.BELOW, belowId)
                setMargins(resources.getDimensionPixelSize(R.dimen.card_margin))
            }
            newCardView.layoutParams = layoutParams
            relativeLayout.addView(newCardView)

            belowId = View.generateViewId()
            newCardView.id = belowId

            if (user.prompts.size > index) {
                val textCardView = createTextCardView(user.prompts[index])
                relativeLayout.addView(textCardView)

                // Set positioning for the text CardView
                val textLayoutParams = textCardView.layoutParams as RelativeLayout.LayoutParams
                textLayoutParams.addRule(RelativeLayout.BELOW, belowId)
                textCardView.layoutParams = textLayoutParams

                // Update belowId for the next CardView
                belowId = View.generateViewId()
                textCardView.id = belowId
            }
        }
    }

    private fun createCardView(imageUrl: String): CardView {
        val context = requireContext()
        val cardView = CardView(context).apply {
            radius = resources.getDimension(R.dimen.card_corner_radius)
            cardElevation = resources.getDimension(R.dimen.card_elevation)

            val layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // Set similar margins as the first CardView
                val margin = resources.getDimensionPixelSize(R.dimen.card_margin)
                setMargins(margin, margin, margin, margin)
            }
            this.layoutParams = layoutParams
        }

        val imageView = ImageView(context).apply {
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.image_view_height)
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        // Use Glide to load the image into the ImageView
        Glide.with(this@LikesDetailFragment)
            .load(imageUrl)
            .into(imageView)

        // Add the ImageView to the CardView
        cardView.addView(imageView)

        return cardView
    }

    private fun createTextCardView(prompt: String): CardView {
        val cardView = CardView(requireContext()).apply {
            radius = resources.getDimension(R.dimen.card_corner_radius)
            cardElevation = resources.getDimension(R.dimen.card_elevation)
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(resources.getDimensionPixelSize(R.dimen.card_margin))
            }
        }

        val textView = TextView(requireContext()).apply {
            text = prompt
            textSize = resources.getDimension(R.dimen.text_size)
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setPadding(16, 16, 16, 16)
            }
        }

        cardView.addView(textView)

        return cardView
    }
    @RequiresApi(Build.VERSION_CODES.O)
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

    companion object {
        const val ARG_USER_ID = "user_id"

        fun newInstance(userId: String) =
            LikesDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                }
            }
    }

}

