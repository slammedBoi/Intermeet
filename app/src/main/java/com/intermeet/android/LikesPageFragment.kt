//import com.intermeet.android.DiscoverFragment
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.intermeet.android.DiscoverActivity
import com.intermeet.android.R
import com.intermeet.android.UserDetailAdapter

class LikesPageFragment(private var recyclerDataArrayList: List<String>) : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var userDetailAdapter: UserDetailAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //var recyclerDataArrayList: List<String>
        val view = inflater.inflate(R.layout.fragment_likespage, container, false)
        recyclerView = view.findViewById(R.id.idCourseRV)

        //add users that like current users account and send to adapter
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId.toString()).child("likes")
        if (userId != null) {
            fetchLikedUsers(userId) { users ->
                val adapter = RecyclerViewAdapter(users, this, DiscoverActivity())
                recyclerView.adapter = adapter

                // setting grid layout manager to implement grid view.
                // in this method '2' represent/s number of columns to be displayed in grid view.
                val layoutManager = GridLayoutManager(requireContext().applicationContext, 2)

                // at last set adapter to recycler view.
                recyclerView.layoutManager = layoutManager
                recyclerView.adapter = adapter
                userDetailAdapter = UserDetailAdapter(this)


                adapter.setOnClickListener(object : RecyclerViewAdapter.OnClickDetect {
                    override fun onClickDetect(position: Int, userId: String) {
                        Log.d(TAG, "Clicking on userId: " + userId)
                        val likesDetailFragment = LikesFragment.newInstance(userId)
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, likesDetailFragment)
                            .addToBackStack(null)  // Optional: Add transaction to the back stack
                            .commit()
                    }
                })
            }
        }

        return view
    }

    private fun fetchLikedUsers(userID: String, callback: (List<String>) -> Unit) {
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userID).child("likes")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val likedUserIds = snapshot.children.mapNotNull { it.key }
                callback(likedUserIds)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchLikedUsers", "Error fetching liked user IDs: ${error.message}")
                callback(emptyList()) // Return an empty list in case of error
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance() = LikesPageFragment(emptyList())
    }
}