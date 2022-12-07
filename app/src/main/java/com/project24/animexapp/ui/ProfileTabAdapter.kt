package com.project24.animexapp.ui

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.project24.animexapp.ui.community.Clubs
import com.project24.animexapp.ui.profile.PlaylistsFragment

internal class ProfileTabAdapter(var context: Context, fm: FragmentManager, var totalTabs: Int): FragmentPagerAdapter(fm) {


    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> {
                PlaylistsFragment()
            }
            1 -> {
                Clubs()
            }

            else -> getItem(position)
        }
    }

    override fun getCount(): Int {
        return totalTabs
    }

}