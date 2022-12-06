package com.project24.animexapp.ui.community

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.project24.animexapp.ui.community.Events
import com.project24.animexapp.ui.community.Feed
import com.project24.animexapp.ui.community.Games
import com.project24.animexapp.ui.community.News

internal class CommunityTabAdapter(var context: Context, fm: FragmentManager, var totalTabs: Int): FragmentPagerAdapter(fm) {


    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> {
                Feed()
            }

            1 -> {
                News()
            }

            2 -> {
                Events()
            }

            3 -> {
                Games()
            }
            else -> getItem(position)
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }

}