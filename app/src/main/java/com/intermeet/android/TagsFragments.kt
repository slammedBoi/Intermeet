package com.intermeet.android

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import android.util.Log

class TagsFragment : Fragment() {
    val sharedViewModel: SharedViewModel by activityViewModels()
    private val selectedTags = mutableListOf<String>()

    private var tagsSelectedListener: OnTagsSelectedListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tags_fragments, container, false)

        // Setup predefined tags (example for food tags)
        val foodTagsContainer = view.findViewById<GridLayout>(R.id.foodTagsContainer)
        val foodTags = resources.getStringArray(R.array.food_tags)
        foodTags.forEach { tag ->
            val tagView = createTagView(tag)
            tagView.setOnClickListener { toggleTagSelection(tag) }
            foodTagsContainer.addView(tagView)
        }

        val entertainTagsContainer = view.findViewById<GridLayout>(R.id.entertainmentTagsContainer)
        val entertainTags = resources.getStringArray(R.array.show_tags)
        entertainTags.forEach { tag ->
            val tagView = createTagView(tag)
            tagView.setOnClickListener { toggleTagSelection(tag) }
            entertainTagsContainer.addView(tagView)
        }

        val socializeTagsContainer = view.findViewById<GridLayout>(R.id.socialTagsContainer)
        val socializeTags = resources.getStringArray(R.array.social)
        socializeTags.forEach { tag ->
            val tagView = createTagView(tag)
            tagView.setOnClickListener { toggleTagSelection(tag) }
            socializeTagsContainer.addView(tagView)
        }

        val creativeTagsContainer = view.findViewById<GridLayout>(R.id.creativityTagsContainer)
        val creativeTags = resources.getStringArray(R.array.Creativity)
        creativeTags.forEach { tag ->
            val tagView = createTagView(tag)
            tagView.setOnClickListener { toggleTagSelection(tag) }
            creativeTagsContainer.addView(tagView)
        }

        val songTagsContainer = view.findViewById<GridLayout>(R.id.musicTagsContainer)
        val songTags = resources.getStringArray(R.array.music)
        songTags.forEach { tag ->
            val tagView = createTagView(tag)
            tagView.setOnClickListener { toggleTagSelection(tag) }
            songTagsContainer.addView(tagView)
        }

        val valueTagsContainer = view.findViewById<GridLayout>(R.id.valuesTagsContainer)
        val valueTags = resources.getStringArray(R.array.values)
        valueTags.forEach { tag ->
            val tagView = createTagView(tag)
            tagView.setOnClickListener { toggleTagSelection(tag) }
            valueTagsContainer.addView(tagView)
        }

        // Custom tag input
        val customTagInput = view.findViewById<EditText>(R.id.customTagInput)
        customTagInput.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val tagText = v.text.toString().trim()
                if (tagText.isNotEmpty()) {
                    if (!selectedTags.contains(tagText) && selectedTags.size < 6) {
                        selectedTags.add(tagText)
                        createTag(tagText)
                        v.text = ""  // Corrected line
                        Log.d("TagsFragment", "Custom tag added: $tagText")
                    }
                }
                true  // Indicate that the action has been handled
            } else {
                false  // Let the system handle the event
            }
        }

        // Handle 'Done' button logic
        val backButton = view.findViewById<Button>(R.id.btnBackToActivity)
        backButton.setOnClickListener {
            sendSelectedTagsBack()
            parentFragmentManager.popBackStack()
        }

        setupButtonAnimations(backButton)
        return view
    }

    private fun createTagView(tag: String): View {
        val tagView = TextView(context).apply {
            text = tag
            gravity = Gravity.CENTER
            setTextColor(Color.BLACK)
            textSize = 14f
            background = ContextCompat.getDrawable(context, R.drawable.tag_background)
            setPadding(20, 30, 30, 20)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                if (this is ViewGroup.MarginLayoutParams) {
                    setMargins(30, 40, 40, 30)
                }
            }
        }

        setupButtonAnimations(tagView)
        return tagView
    }

    private fun toggleTagSelection(tag: String) {
        if (selectedTags.contains(tag)) {
            selectedTags.remove(tag)
            Log.d("TagsFragment", "Tag removed: $tag")
        } else {
            if (selectedTags.size < 6) {
                selectedTags.add(tag)
                Log.d("TagsFragment", "Tag added: $tag")
            } else {
                Log.d("TagsFragment", "Cannot add more tags. Limit reached.")
            }
        }
    }

    private fun createTag(tagText: String) {
        if (!selectedTags.contains(tagText)) {
            selectedTags.add(tagText)

            val customTagsContainer = view?.findViewById<LinearLayout>(R.id.customTagsContainer)

            val tagView = TextView(context).apply {
                text = tagText
                textSize = 14f
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.BLACK)
                setPadding(10, 10, 10, 10)
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(20, 20, 20, 20)
                }
                this.layoutParams = layoutParams
                setOnClickListener {
                    customTagsContainer?.removeView(this)
                    selectedTags.remove(tagText)
                    sharedViewModel.setSelectedTags(selectedTags)
                }
            }

            setupButtonAnimations(tagView)
            customTagsContainer?.addView(tagView)

            Log.d("TagsFragment", "Custom tag added and displayed: $tagText")
        } else {
            Log.d("TagsFragment", "Tag is already selected: $tagText")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupButtonAnimations(button: View) {
        button.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(200).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                }
            }
            false
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        tagsSelectedListener = context as? OnTagsSelectedListener
    }

    fun sendSelectedTagsBack() {
        tagsSelectedListener?.onTagsSelected(selectedTags)
        parentFragmentManager.popBackStack()
        sharedViewModel.setSelectedTags(selectedTags)
    }

    interface OnTagsSelectedListener {
        fun onTagsSelected(tags: List<String>)
    }

    companion object {
        @JvmStatic
        fun newInstance() = TagsFragment()
    }
}
