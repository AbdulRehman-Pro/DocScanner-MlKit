package com.rehman.docscan.ui.fragments.tabsFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rehman.docscan.R
import com.rehman.docscan.databinding.FragmentHomeBinding
import com.rehman.docscan.databinding.FragmentImagesBinding

class ImagesFragment : Fragment() {

    private lateinit var binding: FragmentImagesBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentImagesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


}