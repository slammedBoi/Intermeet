package com.intermeet.android

import DiscoverFragment
import android.annotation.SuppressLint import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.setMargins
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.intermeet.android.helperFunc.calculateAgeWithCalendar

class DiscoverActivity : AppCompatActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val view = inflater.inflate(R.layout.fragment_discover, container, false)
        setContentView(R.layout.fragment_discover)
        //var discoverView : View =

        textViewName = findViewById(R.id.textViewName)
        cardView = findViewById(R.id.cardView)
        cardView2 = findViewById(R.id.cardView2)
        tvEducation = findViewById(R.id.tvEducation)
        tvLocation = findViewById(R.id.tvLocation)
        tvPronouns = findViewById(R.id.tvPronouns)
        tvGender = findViewById(R.id.tvGender)
        tvAboutMeHeader = findViewById(R.id.tvAboutMeHeader)
        tvAboutMe = findViewById(R.id.tvAboutMe)
        tvInterestsHeader = findViewById(R.id.tvInterestsHeader)
        tvEthnicity = findViewById(R.id.tvEthnicity)

        relativeLayout = findViewById(R.id.relativeLayout)


        cardView.setOnClickListener {
            toggleCardViewsVisibility()
        }

        fetchDataFromFirebase()

        //return view
    }
    private fun createCardView(imageUrl: String): CardView {
        //val context = applicationContext
        val cardView = CardView(applicationContext).apply {
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

        val imageView = ImageView(applicationContext).apply {
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(R.dimen.image_view_height)
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        // Use Glide to load the image into the ImageView
        Glide.with(this@DiscoverActivity)
            .load(imageUrl)
            .into(imageView)

        // Add the ImageView to the CardView
        cardView.addView(imageView)

        return cardView
    }

    private fun createTextCardView(prompt: String): CardView {
        val cardView = CardView(applicationContext).apply {
            radius = resources.getDimension(R.dimen.card_corner_radius)
            cardElevation = resources.getDimension(R.dimen.card_elevation)
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(resources.getDimensionPixelSize(R.dimen.card_margin))
            }
        }

        val textView = TextView(applicationContext).apply {
            text = prompt
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
            }
        }

        cardView.addView(textView)

        return cardView
    }


    private fun fetchDataFromFirebase() {
        val userId = "a6TWxI1076ahgLhZFOaHmNPRbom2" // Replace with dynamic user ID as needed
        val dbRef = FirebaseDatabase.getInstance().getReference("users/$userId")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserDataModel::class.java)
                user?.let {
                    textViewName.text = "${it.firstName}, ${calculateAgeWithCalendar(it.birthday)}"
                    tvEducation.text = "${it.school}"
                    tvLocation.text = "${it.longitude}"
                    tvPronouns.text = "${it.pronouns}"
                    tvGender.text = "${it.gender}"
                    tvAboutMe.text = "${it.aboutMeIntro}"
                    tvEthnicity.text ="${it.ethnicity}"
                    tvLocation.text = "${it.latitude}, ${it.longitude}"

                    val imageView1 = findViewById<ImageView>(R.id.imageView1)
                    if (imageView1 != null) {
                        Glide.with(this@DiscoverActivity)
                            .load(it.photoDownloadUrls.first()) // Load the first image URL into the ImageView1
                            .centerCrop()
                            .into(imageView1)
                    }
                    val imageUrls = it.photoDownloadUrls.drop(1) // Drops the first element

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

                        if (it.prompts.size > index) {
                            val textCardView = createTextCardView(it.prompts[index])
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
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun toggleCardViewsVisibility() {
        var delayIncrement = 50L // Increment delay by 50ms for each card
        var delay = 0L // Initial delay for the first card

        for (i in 1 until relativeLayout.childCount) {
            val child = relativeLayout.getChildAt(i)
            if (child is CardView) {
                if (child.visibility == View.VISIBLE) {
                    child.postDelayed({
                        val slideOut = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_out)
                        child.startAnimation(slideOut)
                        child.visibility = View.GONE
                    }, delay)
                    delay += delayIncrement // Increment delay for the next card
                } else {
                    child.visibility = View.VISIBLE
                    val slideIn = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_in)
                    child.startAnimation(slideIn)
                }
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = DiscoverFragment()
    }
}
