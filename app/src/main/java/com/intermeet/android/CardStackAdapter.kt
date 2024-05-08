package com.intermeet.android


import InterestsAdapter
import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.setMargins
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.intermeet.android.helperFunc.calculateAgeWithCalendar
import java.io.IOException

class
CardStackAdapter(private val context: Context, private var users: MutableList<UserDataModel>) :
    RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {
    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(user: UserDataModel) {

            // Bind user data to the views
            val textViewName: TextView = view.findViewById(R.id.textViewName)
            textViewName.text = "${user.firstName}, ${calculateAgeWithCalendar(user.birthday)}"
            val textViewHeight: TextView = view.findViewById(R.id.tvHeight)
            textViewHeight.text = user.height
            val tvEducation: TextView = view.findViewById(R.id.tvEducation)
            tvEducation.text = user.school

            // Translate coordinates into city and state
            val geocoder = Geocoder(view.context)
            val tvLocation: TextView = view.findViewById(R.id.tvLocation)
            try {
                val addresses = user.latitude?.let {
                    user.longitude?.let { longitude ->
                        geocoder.getFromLocation(it, longitude, 1)
                    }
                }
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val city = address.locality
                    val state = address.adminArea
                    val stateAbbreviation = getStateAbbreviation(state)
                    tvLocation.text = if (city != null && stateAbbreviation != null) {
                        "$city, $stateAbbreviation"
                    } else {
                        "Unknown location"
                    }
                } else {
                    tvLocation.text = "Location not found"
                }
            } catch (e: IOException) {
                tvLocation.text = "Error getting location"
                Log.e("GeocoderError", "Failed to get location", e)
            }

            // Pronouns
            val tvPronouns: TextView = view.findViewById(R.id.tvPronouns)
            tvPronouns.text = user.pronouns

            // Gender
            val tvGender: TextView = view.findViewById(R.id.tvGender)
            tvGender.text = user.gender

            // About Me Header and Content
            val tvAboutMeHeader: TextView = view.findViewById(R.id.tvAboutMeHeader)
            val tvAboutMe: TextView = view.findViewById(R.id.tvAboutMe)
            if (user.aboutMeIntro.isNullOrEmpty()) {
                tvAboutMeHeader.visibility = View.GONE
                tvAboutMe.visibility = View.GONE
            } else {
                tvAboutMeHeader.visibility = View.VISIBLE
                tvAboutMe.visibility = View.VISIBLE
                tvAboutMe.text = user.aboutMeIntro
            }

            // Ethnicity
            val tvEthnicity: TextView = view.findViewById(R.id.tvEthnicity)
            tvEthnicity.text = user.ethnicity

            // Interests RecyclerView
            val recyclerViewInterests: RecyclerView = view.findViewById(R.id.rvInterests)
            recyclerViewInterests.adapter = InterestsAdapter(user.interests)
            recyclerViewInterests.layoutManager = LinearLayoutManager(view.context, RecyclerView.HORIZONTAL, false)

            val relativeLayout : RelativeLayout = view.findViewById(R.id.relativeLayout)

            // Load main profile image
            val imageView1: ImageView = view.findViewById(R.id.imageView1)
            Glide.with(view.context)
                .load(user.photoDownloadUrls.firstOrNull())
                .centerCrop()
                .into(imageView1)


            val imageUrls = user.photoDownloadUrls.drop(1) // Drops the first element

            var belowId = R.id.cardView2 // Initialize with cardView2's ID

            imageUrls.forEachIndexed { index, imageUrl ->
                val newCardView = createCardView(imageUrl)
                val layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(RelativeLayout.BELOW, belowId)
                    setMargins(view.context.resources.getDimensionPixelSize(R.dimen.card_margin))
                }
                newCardView.layoutParams = layoutParams
                newCardView.id = View.generateViewId()
                relativeLayout.addView(newCardView)
                belowId = newCardView.id

                if (user.prompts.size > index) {
                    val textCardView = createTextCardView(user.prompts[index])
                    val textLayoutParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        addRule(RelativeLayout.BELOW, belowId)
                        setMargins(view.context.resources.getDimensionPixelSize(R.dimen.card_margin))
                    }
                    textCardView.layoutParams = textLayoutParams
                    textCardView.id = View.generateViewId()
                    relativeLayout.addView(textCardView)
                    belowId = textCardView.id
                }
            }
        }

        private fun createCardView(imageUrl: String): CardView {
            val cardView = CardView(view.context).apply {
                radius = view.resources.getDimension(R.dimen.card_corner_radius)
                cardElevation = view.resources.getDimension(R.dimen.card_elevation)
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    val margin = view.resources.getDimensionPixelSize(R.dimen.card_margin)
                    setMargins(margin, margin, margin, margin)
                }
            }

            val imageView = ImageView(view.context).apply {
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    view.resources.getDimensionPixelSize(R.dimen.image_view_height)
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            Glide.with(view.context).load(imageUrl).into(imageView)
            cardView.addView(imageView)
            return cardView
        }

        private fun createTextCardView(prompt: String): CardView {
            val cardView = CardView(view.context).apply {
                radius = view.resources.getDimension(R.dimen.card_corner_radius)
                cardElevation = view.resources.getDimension(R.dimen.card_elevation)
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(view.resources.getDimensionPixelSize(R.dimen.card_margin))
                }
            }

            val textView = TextView(view.context).apply {
                text = prompt
                textSize = view.resources.getDimension(R.dimen.text_size)
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
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_user_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.alpha = 0f  // Start fully transparent
        holder.bind(users[position])
    }
    fun removeUserAtPosition(position: Int) {
        if (position >= 0 && position < users.size) {
            users.removeAt(position)
            notifyItemRemoved(position)

        }
    }
    fun getUserIdAtPosition(position: Int): String {
        return users.getOrNull(position)?.userId ?: throw IllegalStateException("User at position $position not found")
    }

    override fun getItemCount(): Int = users.size
    fun setUsers(newUsers: List<UserDataModel>) {
        this.users = newUsers.toMutableList()
        notifyDataSetChanged()
    }
}

private fun getStateAbbreviation(stateName: String?): String? {
    val stateAbbreviations = mapOf(
        "Alabama" to "AL",
        "Alaska" to "AK",
        "Arizona" to "AZ",
        "Arkansas" to "AR",
        "California" to "CA",
        "Colorado" to "CO",
        "Connecticut" to "CT",
        "Delaware" to "DE",
        "Florida" to "FL",
        "Georgia" to "GA",
        "Hawaii" to "HI",
        "Idaho" to "ID",
        "Illinois" to "IL",
        "Indiana" to "IN",
        "Iowa" to "IA",
        "Kansas" to "KS",
        "Kentucky" to "KY",
        "Louisiana" to "LA",
        "Maine" to "ME",
        "Maryland" to "MD",
        "Massachusetts" to "MA",
        "Michigan" to "MI",
        "Minnesota" to "MN",
        "Mississippi" to "MS",
        "Missouri" to "MO",
        "Montana" to "MT",
        "Nebraska" to "NE",
        "Nevada" to "NV",
        "New Hampshire" to "NH",
        "New Jersey" to "NJ",
        "New Mexico" to "NM",
        "New York" to "NY",
        "North Carolina" to "NC",
        "North Dakota" to "ND",
        "Ohio" to "OH",
        "Oklahoma" to "OK",
        "Oregon" to "OR",
        "Pennsylvania" to "PA",
        "Rhode Island" to "RI",
        "South Carolina" to "SC",
        "South Dakota" to "SD",
        "Tennessee" to "TN",
        "Texas" to "TX",
        "Utah" to "UT",
        "Vermont" to "VT",
        "Virginia" to "VA",
        "Washington" to "WA",
        "West Virginia" to "WV",
        "Wisconsin" to "WI",
        "Wyoming" to "WY",
        "District of Columbia" to "DC",
        "American Samoa" to "AS",
        "Guam" to "GU",
        "Northern Mariana Islands" to "MP",
        "Puerto Rico" to "PR",
        "United States Minor Outlying Islands" to "UM",
        "Virgin Islands" to "VI"
    )
    return stateAbbreviations[stateName]
}
