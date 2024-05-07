
import android.os.Build
import android.os.Bundle
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
import com.intermeet.android.DiscoverViewModel
import com.intermeet.android.R
import com.intermeet.android.UsersPagerAdapter

class DiscoverFragment : Fragment() {
    private val viewModel: DiscoverViewModel by viewModels()
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: UsersPagerAdapter
    private lateinit var noUsersTextView: TextView
    private lateinit var btnRefresh: Button
    private lateinit var btnLike: Button
    private lateinit var btnPass: Button
    private lateinit var returnButton: View
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discover, container, false)
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
        adapter = UsersPagerAdapter(this)
        viewPager.adapter = adapter
        noUsersTextView = view.findViewById(R.id.tvNoUsers)
        progressBar = view.findViewById(R.id.loadingProgressBar)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners() {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.markAsSeen(adapter.getUserId(position))
            }
        })

        btnLike.setOnClickListener {
            returnButton.setBackground(resources.getDrawable(R.drawable.arrow_return))
            val likedUserId = adapter.getUserId(viewPager.currentItem)
            viewModel.addLike(likedUserId)
            navigateToNextUser()
        }

        btnPass.setOnClickListener {
            returnButton.setBackground(resources.getDrawable(R.drawable.arrow_return_black))
            navigateToNextUser()
        }

        returnButton.setOnClickListener {
            returnButton.setBackground(resources.getDrawable(R.drawable.arrow_return))
            navigateToPreviousUser()
        }

        btnRefresh.setOnClickListener {
            fetchUsers(autoRefresh = false)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun navigateToNextUser() {
        if (viewPager.currentItem < adapter.itemCount - 1) {
            viewPager.currentItem += 1
        } else {
            displayNoUsers()
        }
    }

    private fun navigateToPreviousUser() {
        if (viewPager.currentItem > 0) {
            viewPager.currentItem -= 1
        }
    }

    companion object {
        fun newInstance() = DiscoverFragment()
    }
}