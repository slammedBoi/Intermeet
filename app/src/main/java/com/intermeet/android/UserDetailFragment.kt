
package com.intermeet.android

import InterestsAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

import com.intermeet.android.helperFunc.calculateAgeWithCalendar

class UserDetailFragment : Fragment() {
    private lateinit var height : TextView
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
    private lateinit var recyclerViewInterests: RecyclerView
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
        return inflater.inflate(R.layout.fragment_user_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        height = view.findViewById(R.id.tvHeight)
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
        recyclerViewInterests = view.findViewById(R.id.rvInterests)
        val layoutManager = FlexboxLayoutManager(context).apply {
            justifyContent = JustifyContent.CENTER
        }
        recyclerViewInterests.layoutManager = layoutManager

    }

    private fun updateUserUI(user: UserDataModel) {
        height.text = user.height
        textViewName.text = "${user.firstName}, ${calculateAgeWithCalendar(user.birthday)}"
        tvEducation.text = user.school
        tvLocation.text = "${user.latitude}, ${user.longitude}"
        tvPronouns.text = user.pronouns
        tvGender.text = user.gender
        tvAboutMeHeader.visibility =
            if (user.aboutMeIntro?.isNotEmpty() == true) View.VISIBLE else View.GONE
        tvAboutMe.text = user.aboutMeIntro
        tvEthnicity.text = user.ethnicity

        recyclerViewInterests.adapter = InterestsAdapter(user.interests)

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
        Glide.with(this@UserDetailFragment)
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

    companion object {
        private const val ARG_USER_ID = "user_id"

        fun newInstance(userId: String) =
            UserDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                }
            }
    }
}
