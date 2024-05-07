
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
    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            userId = it.getString(ARG_USER_ID)
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
        adapter = LikesPageAdapter(this, userId!!)
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
            val likedUserId = userId//adapter.getUserId(viewPager.currentItem)
            Log.d(TAG, "Liked user: " + likedUserId)
            if (likedUserId != null) {
                addMatch(likedUserId)
            }
            //need to implement to remove from someones discover list and then add to chats

        }

        btnPass.setOnClickListener {
            //navigateToNextUser()

            //need to implement to remove someone from the list
        }

        returnButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun addMatch(likedUserId : String)
    {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val likedUserDB = FirebaseDatabase.getInstance().getReference("users/$likedUserId/matches")
        val currentUserDB = FirebaseDatabase.getInstance().getReference("users/$userId/matches")


        likedUserDB.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    var listSize = snapshot.childrenCount.toInt()
                    listSize+=1
                    var userField = "user" + listSize.toString()
                    likedUserDB.updateChildren(mapOf(userField to userId))
                }
                else
                {
                    val userField = "user1"
                    likedUserDB.updateChildren(mapOf(userField to userId))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    var listSize = snapshot.childrenCount.toInt()
                    listSize+=1
                    var userField = "user" + listSize.toString()
                    currentUserDB.updateChildren(mapOf(userField to likedUserId))
                }
                else
                {
                    val userField = "user1"
                    currentUserDB.updateChildren(mapOf(userField to likedUserId))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        val intent = Intent(requireContext(), ChatActivity::class.java)
        intent.putExtra("userId", likedUserId)
        startActivity(intent)
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