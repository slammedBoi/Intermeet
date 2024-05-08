
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.intermeet.android.CardStackAdapter
import com.intermeet.android.ChatActivity
import com.intermeet.android.DiscoverViewModel
import com.intermeet.android.LikeAnimation
import com.intermeet.android.PassAnimation
import com.intermeet.android.R
import com.intermeet.android.UserDataModel
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction

class LikesFragment : Fragment() {
    private val viewModel: DiscoverViewModel by viewModels()
    private lateinit var cardStackView: CardStackView
    private lateinit var adapter: CardStackAdapter
    private lateinit var noUsersTextView: TextView
    private lateinit var btnRefresh: TextView
    private lateinit var returnButton: View
    private lateinit var progressBar: ProgressBar
    private lateinit var manager: CardStackLayoutManager
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
        setupCardStackView()
        addLikeAnimationFragment()
        addPassAnimationFragment()

        /*val userRef = FirebaseDatabase.getInstance().getReference("users").child(passedUserId!!).child("likes")
        userRef.child(passedUserId!!).get().addOnSuccessListener { snapshot ->
                if(snapshot.exists())
                {
                    val designatedUser = snapshot.getValue(UserDataModel::class.java)

                    var likedUser : MutableList<UserDataModel> = mutableListOf()
                    if (designatedUser != null) {
                        likedUser.add(designatedUser)
                    }

                    updateAdapter(likedUser)

                    fetchUsers(autoRefresh = false)
                }
            }*/

        viewModel.userData.observe(viewLifecycleOwner){user ->
            var likedUser : MutableList<UserDataModel> = mutableListOf()
            if (user != null) {
                likedUser.add(user)
                updateAdapter(likedUser)
            }
            else{
                displayNoUsers()
            }
        }
    }

    private fun setupViews(view: View) {
        cardStackView = view.findViewById(R.id.usersCardStackView)
        noUsersTextView = view.findViewById(R.id.tvNoUsers)
        btnRefresh = view.findViewById(R.id.btnRefresh)
        returnButton = view.findViewById(R.id.return_button)
        progressBar = view.findViewById(R.id.loadingProgressBar)

        adapter = CardStackAdapter(requireContext(), mutableListOf())
        cardStackView.adapter = adapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners() {
        /*viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                userId?.let { viewModel.markAsSeen(it) }
            }
        })*/

        /*btnLike.setOnClickListener {
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
        }*/

        returnButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun addMatch(likedUserId : String)
    {
        //val userId = FirebaseAuth.getInstance().currentUser?.uid
        val likedUserDB = FirebaseDatabase.getInstance().getReference("users/$likedUserId/matches")
        val currentUserDB = FirebaseDatabase.getInstance().getReference("users/$currentUser/matches")


        likedUserDB.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    var listSize = snapshot.childrenCount.toInt()
                    listSize+=1
                    var userField = "user" + listSize.toString()
                    likedUserDB.updateChildren(mapOf(userField to currentUser))
                }
                else
                {
                    val userField = "user1"
                    likedUserDB.updateChildren(mapOf(userField to currentUser))
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

    private fun removeLikedUser(likedUserId : String)
    {
        val currentUserDB = FirebaseDatabase.getInstance().getReference("users/$currentUser/likes")
        val updates = HashMap<String, Any?>()
        updates[likedUserId] = null

        currentUserDB.updateChildren(updates)
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

    //@RequiresApi(Build.VERSION_CODES.O)
    /*private fun displayUserList(userIds: List<String>) {
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
    }*/

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayNoUsers(autoRefresh: Boolean = false) {
        Log.d("DiscoverFragment", "displayNoUsers executed with autoRefresh=$autoRefresh")
        progressBar.visibility = View.GONE  // Ensure the progress bar is hidden
        cardStackView.visibility = View.GONE  // Hide the card stack view

        Handler().postDelayed({
            // Delayed changes in view after 2 seconds
            noUsersTextView.visibility = View.VISIBLE
            btnRefresh.visibility = View.VISIBLE

            if (autoRefresh) {
                fetchUsers(autoRefresh = true)
            }
        }, 1500) // 2000 milliseconds = 2 seconds
    }

    private fun setupCardStackView() {
        // Initialize the manager before attaching any listeners that might use it.
        manager = CardStackLayoutManager(context, object : CardStackListener {
            override fun onCardDragging(direction: Direction, ratio: Float) {
                // Handle card dragging
            }

            override fun onCardSwiped(direction: Direction) {
                val position = manager.topPosition - 1
                // Log the swipe direction and position
                Log.d("DiscoverFragment", "Card swiped at position: ${manager.topPosition - 1}")

                // Trigger animations and user removal based on swipe direction
                when (direction) {
                    Direction.Right -> {
                        val likedUserId = passedUserId//adapter.getUserId(viewPager.currentItem)
                        Log.d(TAG, "Liked user: " + likedUserId)
                        if (likedUserId != null) {
                            removeLikedUser(likedUserId)
                            //addMatch(likedUserId)
                            triggerLikeAnimation(likedUserId)
                        }
                        //triggerLikeAnimation()
                        //removeUserFromAdapter(manager.topPosition - 1)
                    }
                    Direction.Left -> {
                        val likedUserId = passedUserId//adapter.getUserId(viewPager.currentItem)
                        Log.d(TAG, "Did not like user: " + likedUserId)
                        if (likedUserId != null) {
                            removeLikedUser(likedUserId)
                            //addMatch(likedUserId)
                            triggerPassAnimation()
                        }
                    }
                    else -> {}
                }
            }

            override fun onCardRewound() {
                // Handle card rewind
            }

            override fun onCardCanceled() {
                // Handle card cancel
            }

            override fun onCardAppeared(view: View, position: Int) {
                // Handle card appearance
                Handler(Looper.getMainLooper()).postDelayed({
                    view.animate().alpha(1.0f).setDuration(100).start()
                }, 500)
            }

            override fun onCardDisappeared(view: View, position: Int) {
                // Handle card disappearance
            }
        }).apply {
            // Set swipe directions and scrolling behavior
            setDirections(Direction.HORIZONTAL)
            setCanScrollVertical(false)
            setCanScrollHorizontal(true)
        }

        // Set the layout manager and adapter to the CardStackView
        cardStackView.layoutManager = manager
        cardStackView.adapter = adapter
    }

    private fun triggerLikeAnimation(likedUserId: String) {
        Log.d("DiscoverFragment", "triggerLikeAnimation executed")
        val likeAnimationFragment =
            childFragmentManager.findFragmentByTag("LikeAnimationFragment") as? LikeAnimation
        likeAnimationFragment?.let {
            it.animateLike()
            it.toggleBackgroundAnimation()
        }

        // Get the current top card's position and pass it to the remove method
        //val positionToRemove = manager.topPosition - 1
        //removeUserFromAdapter(positionToRemove)
        addMatch(likedUserId)
    }

    private fun addLikeAnimationFragment() {
        val transaction = childFragmentManager.beginTransaction()
        val likeFragment = LikeAnimation()
        transaction.add(R.id.like_animation_container, likeFragment, "LikeAnimationFragment")
        transaction.commit()
        Log.d("DiscoverFragment", "Like animation fragment added")
    }

    private fun triggerPassAnimation() {
        Log.d("DiscoverFragment", "triggerPassAnimation executed")
        val passAnimationFragment =
            childFragmentManager.findFragmentByTag("PassAnimationFragment") as? PassAnimation
        passAnimationFragment?.let {
            it.animatePass()
            it.toggleBackgroundAnimation()
        }

        // Get the current top card's position and pass it to the remove method
        //val positionToRemove = manager.topPosition - 1
        //removeUserFromAdapter(positionToRemove)
        parentFragmentManager.popBackStack()
    }

    /*private fun removeUserFromAdapter(position: Int) {
        if (position >= 0 && position < adapter.itemCount) {
            adapter.removeUserAtPosition(position)
        }

        // Now check if the adapter is empty
        if (adapter.itemCount == 0) {
            Log.d("DiscoverFragment", "Adapter is empty after swipe")
            displayNoUsers()
        }
    }*/

    private fun addPassAnimationFragment() {
        val transaction = childFragmentManager.beginTransaction()
        val passFragment = PassAnimation()
        transaction.add(R.id.like_animation_container, passFragment, "PassAnimationFragment")
        transaction.commit()
        Log.d("DiscoverFragment", "Pass animation fragment added")
    }

    private fun updateAdapter(users: List<UserDataModel>) {
        Log.d("DiscoverFragment", "updateAdapter executed with user count: ${users.size}")
        adapter.setUsers(users)
        if (users.isEmpty()) {
            displayNoUsers()
        } else {
            cardStackView.visibility = View.VISIBLE
            noUsersTextView.visibility = View.GONE
            progressBar.visibility = View.GONE
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