package com.intermeet.android

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.slider.RangeSlider

class AgeFragment : Fragment() {
    var listener: OnAgeSelectedListener? = null

    private lateinit var rangeSlider: RangeSlider
    private lateinit var tvSelectedRange: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_age, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the RangeSlider and TextView
        rangeSlider = view.findViewById(R.id.range_slider)
        tvSelectedRange = view.findViewById<TextView>(R.id.tvRange)

        // Set initial values for the TextView based on RangeSlider's initial values
        val initialValues = rangeSlider.values
        tvSelectedRange.text = "Selected age range: ${initialValues[0].toInt()} - ${initialValues[1].toInt()}"

        // Add a listener to update the TextView as the slider value changes
        rangeSlider.addOnChangeListener { slider, _, _ ->
            // Get the values of the two thumbs
            val values = slider.values
            // Update the TextView with the selected range
            tvSelectedRange.text = "Selected age range: ${values[0].toInt()} - ${values[1].toInt()}"
        }
        val btnConfirm = view.findViewById<Button>(R.id.backButton)
        btnConfirm.setOnClickListener {
            // Call the listener and pass the selected age range
            val selectedValues = rangeSlider.values
            listener?.onAgeSelected(selectedValues[0].toInt(), selectedValues[1].toInt())

            // Navigate back by popping the back stack
            fragmentManager?.popBackStack()
        }
    }
    interface OnAgeSelectedListener {
        fun onAgeSelected(minAge: Int, maxAge: Int)
    }
    fun setAgeListener(listener: PreferenceActivity) {
        this.listener = listener
    }
    fun setAgeListener(listener: EditPreference) {
        this.listener = listener
    }



}