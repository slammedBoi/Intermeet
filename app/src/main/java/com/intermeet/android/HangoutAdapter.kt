package com.intermeet.android

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class HangoutAdapter(var hangouts: List<Hangout>, private val context: Context, private val onDeleteClick: (Hangout) -> Unit, private val onEditClick: (Hangout) -> Unit) : RecyclerView.Adapter<HangoutAdapter.HangoutViewHolder>() {

    // ViewHolder class to hold the item views
    class HangoutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val hangoutName: TextView = view.findViewById(R.id.hangoutName)
        val hangoutDate: TextView = view.findViewById(R.id.hangoutDate)
        val hangoutLocation: TextView = view.findViewById(R.id.hangoutLocation)
        val hangoutDescription: TextView = view.findViewById(R.id.hangoutDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HangoutViewHolder {
        // Inflate the item layout
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hangout_view, parent, false)
        return HangoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: HangoutViewHolder, position: Int) {
        val hangout = hangouts[position]
        // Set item views based on your views and data model
        holder.hangoutName.text = hangout.name
        holder.hangoutDate.text = "Date: ${hangout.beginTime} ${hangout.endTime}"
        holder.hangoutLocation.text = "Location: ${hangout.location}"
        holder.hangoutDescription.text = "Description: ${hangout.description}"

        holder.itemView.setOnClickListener {
            AlertDialog.Builder(context).apply {
                setTitle("Choose an action")
                setItems(arrayOf("Edit", "Delete")) { dialog, which ->
                    when (which) {
                        0 -> onEditClick(hangout)
                        1 -> onDeleteClick(hangout)
                    }
                }
                show()
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = hangouts.size
}
