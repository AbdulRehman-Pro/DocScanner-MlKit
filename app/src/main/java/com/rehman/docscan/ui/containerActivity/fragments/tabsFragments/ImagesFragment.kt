package com.rehman.docscan.ui.containerActivity.fragments.tabsFragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rehman.docscan.R
import com.rehman.docscan.databinding.FragmentImagesBinding
import com.rehman.docscan.ui.DataViewModel
import com.rehman.docscan.ui.containerActivity.fragments.ExploreFragment
import com.rehman.docscan.ui.containerActivity.fragments.tabsFragments.adapters.ImagesAdapter
import com.rehman.docscan.ui.containerActivity.fragments.tabsFragments.interfaces.ClickInterface
import com.rehman.docscan.ui.detailsActivity.DetailsActivity
import com.rehman.docscan.utils.ProjectUtils
import com.rehman.utilities.Utils.showToast


class ImagesFragment : Fragment(), ClickInterface {

    private lateinit var binding: FragmentImagesBinding
    private val imageUris = ArrayList<Uri>()
    private lateinit var imagesAdapter: ImagesAdapter
    private val dataViewModel: DataViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentImagesBinding.inflate(layoutInflater, container, false)
        observers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scrollToPosition()
    }

    private fun observers() {
        dataViewModel.imagesUriList.observe(viewLifecycleOwner) { uriList ->
            if (uriList.size > 0) {
                initRecycler(uriList)
                prepareTransitions()
                postponeEnterTransition()
            } else {
                binding.imagesRV.visibility = View.GONE
                binding.noFileLayout.visibility = View.VISIBLE
            }
        }
    }


    private fun initRecycler(list: ArrayList<Uri>) {
        binding.noFileLayout.visibility = View.GONE
        binding.imagesRV.visibility = View.VISIBLE
        binding.imagesRV.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }
        imageUris.clear()
        imageUris.addAll(list)
        imagesAdapter = ImagesAdapter(this, imageUris, this)
        binding.imagesRV.adapter = imagesAdapter

    }

    /**
     * Scrolls the recycler view to show the last viewed item in the grid. This is important when
     * navigating back from the grid.
     */
    private fun scrollToPosition() {
        binding.imagesRV.addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int,
            ) {
                binding.imagesRV.removeOnLayoutChangeListener(this)
                val layoutManager: RecyclerView.LayoutManager = binding.imagesRV.layoutManager!!
                val viewAtPosition =
                    layoutManager.findViewByPosition(ExploreFragment().currentPosition)
                // Scroll to position if the view for the current position is null (not currently part of
                // layout manager children), or it's not completely visible.
                if (viewAtPosition == null || layoutManager
                        .isViewPartiallyVisible(viewAtPosition, false, true)
                ) {
                    binding.imagesRV.post { layoutManager.scrollToPosition(ExploreFragment().currentPosition) }
                }
            }
        })
    }

    /**
     * Prepares the shared element transition to the pager fragment, as well as the other transitions
     * that affect the flow.
     */
    private fun prepareTransitions() {
        exitTransition = TransitionInflater.from(context)
            .inflateTransition(R.transition.grid_exit_transition)

        // A similar mapping is set at the ImagePagerFragment with a setEnterSharedElementCallback.
        setExitSharedElementCallback(
            object : SharedElementCallback() {
                override fun onMapSharedElements(
                    names: List<String?>,
                    sharedElements: MutableMap<String?, View?>,
                ) {
                    // Locate the ViewHolder for the clicked position.
                    val selectedViewHolder: RecyclerView.ViewHolder = binding.imagesRV
                        .findViewHolderForAdapterPosition(ExploreFragment().currentPosition)!!

                    // Map the first shared element name to the child ImageView.
                    sharedElements[names[0]] =
                        selectedViewHolder.itemView.findViewById(R.id.itemImage)
                }
            })
    }

    override fun itemClick(imageView: ImageView, uri: Uri) {
        showToast("Click : ${ProjectUtils.getFileNameFromUri(requireContext(), uri)}")

        val intent = Intent(context, DetailsActivity::class.java).apply {
            putParcelableArrayListExtra("itemList", dataViewModel.imagesUriList.value)
            putExtra("selectedItemPosition", dataViewModel.imagesUriList.value!!.indexOf(uri))
        }

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            requireActivity(),
            imageView,
            ViewCompat.getTransitionName(imageView) ?: ""
        )

        requireContext().startActivity(intent, options.toBundle())

//        StfalconImageViewer.Builder(context, imageUris) { view, uri ->
//            Glide.with(requireContext()).load(uri).into(view)
//        }.withHiddenStatusBar(false).show()
    }

    override fun shareClick(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Check out this image, scanned by DocScan.")
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Doc Share"))
    }

    override fun deleteClick(position: Int, uri: Uri) {
        if (ProjectUtils.deleteFileFromUri(requireContext(), uri)) {
            showToast("Image deleted successfully")
            imageUris.remove(uri)
            imagesAdapter.notifyItemRemoved(position)
            dataViewModel.imagesUriList.value =
                ProjectUtils.getImagesFromMediaStore(requireContext())
        } else {
            showToast("Failed to delete image")
        }
    }


}