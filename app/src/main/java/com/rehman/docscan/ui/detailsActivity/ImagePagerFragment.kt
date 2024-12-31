package com.rehman.docscan.ui.detailsActivity

import android.net.Uri
import android.os.Bundle
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.SharedElementCallback
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.rehman.docscan.R
import com.rehman.docscan.databinding.FragmentImagePagerBinding
import com.rehman.docscan.ui.containerActivity.fragments.ExploreFragment


class ImagePagerFragment(private val imageList: ArrayList<Uri>) : Fragment() {

    private lateinit var binding: FragmentImagePagerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentImagePagerBinding.inflate(layoutInflater, container, false)
        binding.viewPager.adapter = ImagePagerAdapter(this,imageList)
        // Set the current position and add a listener that will update the selection coordinator when
        // paging the images.
        // Set the current position and add a listener that will update the selection coordinator when
        // paging the images.
        binding.viewPager.currentItem = ExploreFragment().currentPosition
        binding.viewPager.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                ExploreFragment().currentPosition = position
            }
        })

        prepareSharedElementTransition()

        // Avoid a postponeEnterTransition on orientation change, and postpone only of first creation.
        if (savedInstanceState == null) {
            postponeEnterTransition()
        }
        return binding.root
    }


    private fun prepareSharedElementTransition() {
        val transition: Transition = TransitionInflater.from(context)
            .inflateTransition(R.transition.image_shared_element_transition)
        sharedElementEnterTransition = transition

        // A similar mapping is set at the GridFragment with a setExitSharedElementCallback.
        setEnterSharedElementCallback(
            object : SharedElementCallback() {
                override fun onMapSharedElements(
                    names: List<String?>,
                    sharedElements: MutableMap<String?, View?>,
                ) {
                    // Locate the image view at the primary fragment (the ImageFragment that is currently
                    // visible). To locate the fragment, call instantiateItem with the selection position.
                    // At this stage, the method will simply return the fragment at the position and will
                    // not create a new one.
                    val currentFragment = binding.viewPager.adapter!!
                        .instantiateItem(binding.viewPager, ExploreFragment().currentPosition) as Fragment
                    val view = currentFragment.view ?: return

                    // Map the first shared element name to the child ImageView.
                    sharedElements[names[0]] = view.findViewById<View>(R.id.image)
                }
            })
    }

}