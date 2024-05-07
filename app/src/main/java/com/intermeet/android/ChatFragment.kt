package com.intermeet.android

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatFragment : Fragment() {

    companion object {
        fun newInstance(): ChatFragment = ChatFragment()
    }

    private lateinit var listView: ListView
    private lateinit var searchEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        searchEditText = view.findViewById(R.id.searchbox)
        listView = view.findViewById(R.id.usersList)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val currentUser = getCurrentUserId()
        if (currentUser != null) {
            fetchMapUsers(currentUser) { users ->
                val adapter = ChatAdapter(requireContext(), users) { userId ->
                    startChatWithUser(userId)
                }
                listView.adapter = adapter
            }
        }
        // Set item click listener for list view
        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val userId = listView.getItemAtPosition(position) as String
            startChatWithUser(userId)
            //fetchLikedUsers(currentUser)
        }
    }

    private fun updateListView(userIds: List<String>) {
        if (isAdded) {  // Check if the fragment is currently added to its activity
            val adapter = ChatAdapter(requireContext(), userIds) { userId ->
                startChatWithUser(userId)
            }
            listView.adapter = adapter
            listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                val userId = adapter.getItem(position) as String
                startChatWithUser(userId)
            }
        } else {
            Log.d(TAG, "Fragment not attached to the context.")
        }
    }

    private fun fetchMapUsers(userID: String, callback: (List<String>) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userID).child("matches")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val likedUserIds = snapshot.children.mapNotNull { it.value.toString() }
                callback(likedUserIds)
    /*private fun fetchLikedUsers(userID: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userID).child("likes")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val likedUserIds = snapshot.children.mapNotNull { it.key }
                if (isAdded) {  // Ensure the fragment is still added
                    updateListView(likedUserIds)
                } else {
                    Log.d(TAG, "Fragment not attached when data received.")
                }*/
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatFragment", "Error fetching liked user IDs: ${error.message}")
            }
        })
    }


    private fun startChatWithUser(userId: String) {
        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }

    private fun getCurrentUserId(): String? = FirebaseAuth.getInstance().currentUser?.uid

    override fun onResume() {
        super.onResume()
        AppState.isChatFragmentActive = true
    }

    override fun onPause() {
        super.onPause()
        AppState.isChatFragmentActive = false
    }
}
