package com.rehman.docscan.ui.containerActivity.fragments

import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.rehman.docscan.databinding.FragmentExploreBinding
import com.rehman.docscan.ui.DataViewModel
import com.rehman.docscan.ui.containerActivity.fragments.tabsFragments.ImagesFragment
import com.rehman.docscan.ui.containerActivity.fragments.tabsFragments.PdfFragment
import com.rehman.docscan.ui.containerActivity.fragments.tabsFragments.pagerTransformer.ZoomOutPageTransformer
import com.rehman.docscan.utils.ProjectUtils


private const val NUM_PAGES = 1
private const val KEY_CURRENT_POSITION = "com.rehman.docscan.ui.detailActivity.key.currentPosition"

class ExploreFragment : Fragment() {
    private lateinit var binding: FragmentExploreBinding
    private val dataViewModel: DataViewModel by activityViewModels()
    var currentPosition: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentExploreBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION, 0)
        }

        dataViewModel.imagesUriList.value = ProjectUtils.getImagesFromMediaStore(requireContext())
        binding.viewPager.adapter = ExplorePagerAdapter(requireActivity())
        binding.viewPager.setPageTransformer(ZoomOutPageTransformer())

        binding.tabBarLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.viewPager.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tabBarLayout.getTabAt(position)!!.select()
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_POSITION, currentPosition)

    }

    private inner class ExplorePagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = NUM_PAGES

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ImagesFragment()
                else -> PdfFragment()
            }

        }
    }


}