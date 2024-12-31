package com.rehman.docscan.ui.detailsActivity

import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter


class ImagePagerAdapter(fragment: Fragment, private val imagesList: ArrayList<Uri>) :
    FragmentStatePagerAdapter(fragment.childFragmentManager) {
    override fun getCount(): Int {
        return imagesList.size
    }

    override fun getItem(position: Int): Fragment {
        return ImageFragment.newInstance(imagesList.get(position))
    }
}