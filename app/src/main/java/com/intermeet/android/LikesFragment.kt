
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.intermeet.android.ChatActivity
import com.intermeet.android.DiscoverViewModel
import com.intermeet.android.LikesPageAdapter
import com.intermeet.android.R

class LikesFragment : Fragment() {
    private val viewModel: DiscoverViewModel by viewModels()
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: LikesPageAdapter
    private lateinit var noUsersTextView: TextView
    private lateinit var btnRefresh: Button
    private lateinit var btnLike: Button
    private lateinit var btnPass: Button
    private lateinit var returnButton: View
    private lateinit var progressBar: ProgressBar
    private var passedUserId: String? = null
    private val currentUser = FirebaseAuth.getInstance().currentUser?.uid


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            passedUserId = it.getString(ARG_USER_ID)
        }
        return inflater.inflate(R.layout.fragment_likes_prepage, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupListeners()

        viewModel.filteredUserIdsLiveData.observe(viewLifecycleOwner) { userIds ->
            progressBar.visibility = View.GONE
            if (userIds.isNotEmpty()) {
                displayUserList(userIds)
            } else {
                displayNoUsers()
            }
        }

        fetchUsers(autoRefresh = false)
    }

    private fun setupViews(view: View) {
        btnRefresh = view.findViewById(R.id.btnRefresh)
        btnLike = view.findViewById(R.id.btnLike)
        btnPass = view.findViewById(R.id.btnPass)
        returnButton = view.findViewById(R.id.retrieve_lastuser)
        viewPager = view.findViewById(R.id.usersViewPager)
        viewPager.isUserInputEnabled = false
        adapter = LikesPageAdapter(this, passedUserId!!)
        viewPager.adapter = adapter
        noUsersTextView = view.findViewById(R.id.tvNoUsers)
        progressBar = view.findViewById(R.id.loadingProgressBar)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners() {
        /*viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                userId?.let { viewModel.markAsSeen(it) }
            }
        })*/

        btnLike.setOnClickListener {
            val likedUserId = passedUserId//adapter.getUserId(viewPager.currentItem)
            Log.d(TAG, "Liked user: " + likedUserId)
            if (likedUserId != null) {
                addMatch(likedUserId)
                removeLikedUser(likedUserId)
            }
            //need to implement to remove from someones discover list and then add to chats
        }

        btnPass.setOnClickListener {
            //navigateToNextUser()
            parentFragmentManager.popBackStack()

            //need to implement to remove someone from the list
        }

        returnButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun addMatch(likedUserId : String)
    {
        //val userId = FirebaseAuth.getInstance().currentUser?.uid
        val likedUserDB = FirebaseDatabase.getInstance().getReference("users/$likedUserId/matches")
        val currentUserDB = FirebaseDatabase.getInstance().getReference("users/$currentUser/matches")

        currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isAlreadyMatched = false
                for (match in snapshot.children) {
                    val matchedUserId = match.getValue(String::class.java)
                    if (matchedUserId == likedUserId) {
                        isAlreadyMatched = true
                        break
                    }
                }

                if (!isAlreadyMatched) {
                    // Add current user to liked user's matches
                    likedUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(likedSnapshot: DataSnapshot) {
                            val listSize = likedSnapshot.childrenCount + 1
                            val userField = "user$listSize"
                            likedUserDB.child(userField).setValue(currentUser)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "Error adding match to liked user's database", error.toException())
                        }
                    })

                    // Add liked user to current user's matches
                    currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(currentSnapshot: DataSnapshot) {
                            val listSize = currentSnapshot.childrenCount + 1
                            val userField = "user$listSize"
                            currentUserDB.child(userField).setValue(likedUserId)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "Error adding match to current user's database", error.toException())
                        }
                    })

                    // Navigate to chat activity
                    val intent = Intent(requireContext(), ChatActivity::class.java)
                    intent.putExtra("userId", likedUserId)
                    startActivity(intent)
                } else {
                    Log.d(TAG, "User $likedUserId is already in matches list")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error checking matches in current user's database", error.toException())
            }
        })

        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra("userId", likedUserId)
        startActivity(intent)
    }

    private fun removeLikedUser(likedUserId : String)
    {
        val currentUserDB = FirebaseDatabase.getInstance().getReference("users/$currentUser/likes")
        val likedUserDb = FirebaseDatabase.getInstance().getReference("user/$likedUserId/likes")

        val updates = HashMap<String, Any?>()
        updates[likedUserId] = null

        val likedUpdates = HashMap<String, Any?>()
        likedUpdates[currentUser!!] = null

        currentUserDB.updateChildren(updates)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully removed")
            }
            .addOnFailureListener{
                Log.d(TAG, "Cannot remove")
            }

        likedUserDb.updateChildren(likedUpdates)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully removed")
            }
            .addOnFailureListener{
                Log.d(TAG, "Cannot remove")
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchUsers(autoRefresh: Boolean) {
        progressBar.visibility = View.VISIBLE
        noUsersTextView.visibility = View.GONE
        btnRefresh.visibility = View.GONE
        viewModel.clearSeenUsers()
        viewModel.fetchAndFilterUsers()

        if (autoRefresh) {
            viewModel.filteredUserIdsLiveData.observe(viewLifecycleOwner) { userIds ->
                if (userIds.isEmpty()) {
                    displayNoUsers(autoRefresh = false)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayUserList(userIds: List<String>) {
        if (userIds.isEmpty()) {
            fetchUsers(autoRefresh = true)
        } else {
            noUsersTextView.visibility = View.GONE
            viewPager.visibility = View.VISIBLE
            btnRefresh.visibility = View.GONE
            btnLike.visibility = View.VISIBLE
            btnPass.visibility = View.VISIBLE
            returnButton.visibility = View.VISIBLE
            adapter.setUserIds(userIds)
            adapter.notifyDataSetChanged()
            viewPager.currentItem = 0
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayNoUsers(autoRefresh: Boolean = false) {
        if (autoRefresh) {
            fetchUsers(autoRefresh = true)
        } else {
            noUsersTextView.visibility = View.VISIBLE
            viewPager.visibility = View.GONE
            btnRefresh.visibility = View.VISIBLE
            btnLike.visibility = View.GONE
            btnPass.visibility = View.GONE
            returnButton.visibility = View.GONE
        }
    }

    companion object {
        const val ARG_USER_ID = "user_id"
        fun newInstance(userId: String) =
            LikesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_USER_ID, userId)
                }
            }
    }
}