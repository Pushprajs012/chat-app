package com.talk.walk.Adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.talk.walk.Activities.MainActivity
import com.talk.walk.Fragments.ChatFragment
import com.talk.walk.Fragments.MoreFragment
import com.talk.walk.Fragments.SearchFragment

class BottomViewPagerAdapter(mainActivity: MainActivity) : FragmentStateAdapter(mainActivity) {

    override fun getItemCount(): Int {
        return 3

    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> return ChatFragment()
            1 -> return SearchFragment()
            2 -> return MoreFragment()
            else -> ChatFragment()
        }
    }

}