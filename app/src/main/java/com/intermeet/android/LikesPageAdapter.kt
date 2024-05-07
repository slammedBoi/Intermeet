package com.intermeet.android

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class LikesPageAdapter(fragment: Fragment, userId: String) : FragmentStateAdapter(fragment) {
    private var userIds = emptyList<String>()
    private var likes_userId = userId

    fun setUserIds(newIds: List<String>) {
        userIds = newIds
    }

    override fun getItemCount(): Int = userIds.size

    override fun createFragment(position: Int): Fragment {
        return UserDetailFragment.newInstance(likes_userId)
    }

    fun getUserId(position: Int): String {
        return userIds[position]
    }
}