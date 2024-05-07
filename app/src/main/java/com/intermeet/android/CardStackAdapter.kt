package com.intermeet.android


import InterestsAdapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.setMargins
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.intermeet.android.helperFunc.calculateAgeWithCalendar

class CardStackAdapter(private val context: Context, private var users: MutableList<UserDataModel>) :
    RecyclerView.Adapter<CardStackAdapter.ViewHolder>() {
    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(user: UserDataModel) {

            // Bind user data to the views
            val textViewName: TextView = view.findViewById(R.id.textViewName)
            textViewName.text = "${user.firstName}, ${calculateAgeWithCalendar(user.birthday)}"

            val tvEducation: TextView = view.findViewById(R.id.tvEducation)
            tvEducation.text = user.school
            val tvLocation: TextView = view.findViewById(R.id.tvLocation)
            tvLocation.text = "${user.latitude}, ${user.longitude}"

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

    override fun getItemCount(): Int = users.size
    fun setUsers(newUsers: List<UserDataModel>) {
        this.users = newUsers.toMutableList()
        notifyDataSetChanged()


    }
}