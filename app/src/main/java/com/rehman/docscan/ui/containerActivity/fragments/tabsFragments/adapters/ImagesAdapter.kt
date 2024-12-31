//package com.rehman.docscan.ui.containerActivity.fragments.tabsFragments.adapters
//
//import android.net.Uri
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.appcompat.widget.PopupMenu
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.bumptech.glide.RequestManager
//import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
//import com.rehman.docscan.R
//import com.rehman.docscan.databinding.ItemImagesBinding
//import com.rehman.docscan.ui.containerActivity.fragments.tabsFragments.ImagesFragment
//import com.rehman.docscan.ui.containerActivity.fragments.tabsFragments.interfaces.ClickInterface
//import com.rehman.docscan.utils.ProjectUtils.processFileName
//
//
//class ImagesAdapter(
//    private val fragment: ImagesFragment,
//    private val imageList: ArrayList<Uri>,
//    private val clickInterface: ClickInterface,
//) : RecyclerView.Adapter<ImagesAdapter.ImagesViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
//        val layoutInflater = LayoutInflater.from(parent.context)
//        val itemBinding = ItemImagesBinding.inflate(layoutInflater, parent, false)
//        return ImagesViewHolder(itemBinding)
//    }
//
//    override fun getItemCount(): Int = imageList.size
//
//    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
//        val item = imageList[position]
//        holder.bind(item,position)
//    }
//
//    inner class ImagesViewHolder(private val itemBinding: ItemImagesBinding) :
//        RecyclerView.ViewHolder(itemBinding.root) {
//        fun bind(uri: Uri, position: Int) {
//
//            val fileDetails = processFileName(fragment.requireContext(), uri)
//            fileDetails?.let { (title, description) ->
//                itemBinding.itemTitle.text = title
//                itemBinding.itemDesc.text = description
//            } ?: run {
//                itemBinding.itemTitle.text = fragment.requireContext().getString(R.string.unknown)
//                itemBinding.itemDesc.text = fragment.requireContext().getString(R.string.unknown)
//            }
//
//            Glide.with(fragment.requireContext()).load(uri)
//                .transition(DrawableTransitionOptions.withCrossFade())
//                .into(itemBinding.itemImage)
//
//
//            itemBinding.itemMore.setOnClickListener {
//                menuClicked(itemBinding.itemMore, uri, position)
//            }
//
//            itemBinding.root.setOnClickListener {
//                clickInterface.itemClick(itemBinding.itemImage,uri)
//            }
//
//        }
//
//
//    }
//
//
//    private fun menuClicked(it: View?, uri: Uri, position: Int) {
//        val popupMenu = PopupMenu(fragment.requireContext(), it!!)
//        popupMenu.menu.add("Share")
//        popupMenu.menu.add("Delete")
//
//        popupMenu.setOnMenuItemClickListener { item ->
//            when (item?.title!!.toString()) {
//                "Share" -> clickInterface.shareClick(uri)
//                "Delete" -> clickInterface.deleteClick(position,uri)
//            }
//
//            false
//        }
//        popupMenu.show()
//    }
//
//
//}

package com.rehman.docscan.ui.containerActivity.fragments.tabsFragments.adapters

import android.graphics.drawable.Drawable
import android.net.Uri
import android.transition.TransitionSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.rehman.docscan.R
import com.rehman.docscan.databinding.ItemImagesBinding
import com.rehman.docscan.ui.containerActivity.fragments.ExploreFragment
import com.rehman.docscan.ui.containerActivity.fragments.tabsFragments.ImagesFragment
import com.rehman.docscan.ui.containerActivity.fragments.tabsFragments.interfaces.ClickInterface
import com.rehman.docscan.ui.detailsActivity.ImagePagerFragment
import com.rehman.docscan.utils.ProjectUtils.processFileName
import java.util.concurrent.atomic.AtomicBoolean

class ImagesAdapter(
    private val fragment: ImagesFragment,
    private val imageList: ArrayList<Uri>,
    private val clickInterface: ClickInterface,
) : RecyclerView.Adapter<ImagesAdapter.ImagesViewHolder>() {



    private val requestManager: RequestManager = Glide.with(fragment)
    private val viewHolderListener: ViewHolderListener = ViewHolderListenerImpl(fragment,imageList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemBinding = ItemImagesBinding.inflate(layoutInflater, parent, false)
        return ImagesViewHolder(itemBinding, requestManager, viewHolderListener)
    }

    override fun getItemCount(): Int = imageList.size

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        val item = imageList[position]
        holder.bind(item, position)
    }

    inner class ImagesViewHolder(
        private val itemBinding: ItemImagesBinding,
        private val requestManager: RequestManager,
        private val viewHolderListener: ViewHolderListener
    ) : RecyclerView.ViewHolder(itemBinding.root), View.OnClickListener {

        private val image = itemBinding.itemImage


        init {
            itemBinding.itemMore.setOnClickListener(this)
            itemBinding.root.setOnClickListener(this)
        }

        fun bind(uri: Uri, position: Int) {
            val fileDetails = processFileName(fragment.requireContext(), uri)
            fileDetails?.let { (title, description) ->
                itemBinding.itemTitle.text = title
                itemBinding.itemDesc.text = description
            } ?: run {
                itemBinding.itemTitle.text = fragment.requireContext().getString(R.string.unknown)
                itemBinding.itemDesc.text = fragment.requireContext().getString(R.string.unknown)
            }

            requestManager
                .load(uri)
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        viewHolderListener.onLoadCompleted(image, position)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        viewHolderListener.onLoadCompleted(image, position)
                        return false
                    }
                })
                .into(image)

            image.transitionName = uri.toString()
        }

        override fun onClick(view: View) {
            when (view) {
                itemBinding.itemMore -> {
                    menuClicked(view, imageList[adapterPosition], adapterPosition)
                }
                itemBinding.root -> {
                    viewHolderListener.onItemClicked(view, adapterPosition)
                }
            }
        }
    }

    private fun menuClicked(view: View, uri: Uri, position: Int) {
        val popupMenu = PopupMenu(fragment.requireContext(), view)
        popupMenu.menu.add("Share")
        popupMenu.menu.add("Delete")

        popupMenu.setOnMenuItemClickListener { item ->
            when (item?.title!!.toString()) {
                "Share" -> clickInterface.shareClick(uri)
                "Delete" -> clickInterface.deleteClick(position, uri)
            }
            false
        }
        popupMenu.show()
    }

    interface ViewHolderListener {
        fun onLoadCompleted(view: ImageView, adapterPosition: Int)
        fun onItemClicked(view: View, adapterPosition: Int)
    }

    private class ViewHolderListenerImpl(
        private val fragment: Fragment,
        private val imageList: ArrayList<Uri>
    ) : ViewHolderListener {

        private val enterTransitionStarted = AtomicBoolean()


        override fun onLoadCompleted(view: ImageView, adapterPosition: Int) {
            if (ExploreFragment().currentPosition != adapterPosition) return
            if (enterTransitionStarted.getAndSet(true)) return
            fragment.startPostponedEnterTransition()
        }

        override fun onItemClicked(view: View, adapterPosition: Int) {
            ExploreFragment().currentPosition = adapterPosition
            (fragment.exitTransition as? TransitionSet)?.excludeTarget(view, true)
            val transitioningView = view.findViewById<ImageView>(R.id.itemImage)
            fragment.parentFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .addSharedElement(transitioningView, transitioningView.transitionName)
                .replace(R.id.fragment_container, ImagePagerFragment(imageList), ImagePagerFragment::class.java.simpleName)
                .addToBackStack(null)
                .commit()
        }
    }
}
