package com.rehman.docscan.ui.detailsActivity


import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.rehman.docscan.R


/**
 * A fragment for displaying an image.
 */
class ImageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_image, container, false)
        val arguments: Bundle? = arguments
         val imageRes : Uri? = arguments!!.getParcelable(KEY_IMAGE_RES)

        // Just like we do when binding views at the grid, we set the transition name to be the string
        // value of the image res.
        view.findViewById<View>(R.id.image).transitionName = imageRes.toString()

        // Load the image with Glide to prevent OOM error when the image drawables are very large.
        Glide.with(this)
            .load(imageRes)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean,
                ): Boolean {
                    // The postponeEnterTransition is called on the parent ImagePagerFragment, so the
                    // startPostponedEnterTransition() should also be called on it to get the transition
                    // going in case of a failure.
                    parentFragment!!.startPostponedEnterTransition()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable?>,
                    dataSource: DataSource,
                    isFirstResource: Boolean,
                ): Boolean {
                    // The postponeEnterTransition is called on the parent ImagePagerFragment, so the
                    // startPostponedEnterTransition() should also be called on it to get the transition
                    // going when the image is ready.
                    parentFragment!!.startPostponedEnterTransition()
                    return false
                }
            })
            .into(view.findViewById<View>(R.id.image) as ImageView)
        return view
    }

    companion object {
        private const val KEY_IMAGE_RES = "com.delaroystudios.viewpagertogrid.key.imageRes"
        fun newInstance(drawableRes: Uri): ImageFragment {
            val fragment = ImageFragment()
            val argument = Bundle()
            argument.putParcelable(KEY_IMAGE_RES, drawableRes)
            fragment.arguments = argument
            return fragment
        }
    }
}