package com.intermeet.android

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatAdapter(context: Context, private val userIds: List<String>,  private val onItemClick: (String) -> Unit) : ArrayAdapter<String>(context, 0, userIds) {
    private val inflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.chat_users, parent, false)
        val imageView = view.findViewById<ImageView>(R.id.user_image)
        val textView = view.findViewById<TextView>(R.id.user_name)
        val userId = getItem(position)

        view.tag = userId  // Tag the view with the current user ID

        if (userId != null) {
            fetchUserDetails(userId) { user ->
                // Check if the view is still supposed to show data for this user
                if (view.tag == userId) {
                    Log.d("ChatAdapter", "${user.firstName} ${user.lastName}")
                    textView.text = "${user.firstName} ${user.lastName}"
                    if (user.photoDownloadUrls.isNotEmpty()) {
                        Glide.with(context).load(user.photoDownloadUrls[0]).into(imageView)
                    }
                }
            }
        }
        view.setOnClickListener {
            if (userId != null) {
                onItemClick(userId)
            }
        }
        return view
    }


    private fun fetchUserDetails(userId: String, callback: (User) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val firstName = snapshot.child("firstName").getValue(String::class.java) ?: ""
                val lastName = snapshot.child("lastName").getValue(String::class.java) ?: ""
                val photoDownloadUrls = snapshot.child("photoDownloadUrls").children.mapNotNull { it.getValue(String::class.java) }
                callback(User(userId, firstName, lastName, photoDownloadUrls))
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserAdapter", "Failed to fetch user details: ${error.message}")
            }
        })
    }

    data class User(val id: String, val firstName: String, val lastName: String, val photoDownloadUrls: List<String>)
}