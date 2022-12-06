package com.project24.animexapp.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.project24.animexapp.databinding.FragmentCommunityBinding
import com.project24.animexapp.ui.CommunityTabAdapter


class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        val root: View = binding.root

        tabLayout = binding.communityTabLayout
        viewPager = binding.communityViewPager

        tabLayout.addTab(tabLayout.newTab().setText("Feed"))
        tabLayout.addTab(tabLayout.newTab().setText("News"))
        tabLayout.addTab(tabLayout.newTab().setText("Events"))
        tabLayout.addTab(tabLayout.newTab().setText("Games"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        val adapter = CommunityTabAdapter(requireContext(), childFragmentManager, tabLayout.tabCount)
        viewPager.adapter = adapter

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                viewPager.currentItem = tab!!.position
            }

        })


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}