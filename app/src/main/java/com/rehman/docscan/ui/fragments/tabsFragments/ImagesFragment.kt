package com.rehman.docscan.ui.fragments.tabsFragments

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.rehman.docscan.databinding.FragmentImagesBinding
import com.rehman.docscan.ui.fragments.tabsFragments.adapters.ImagesAdapter
import com.rehman.docscan.ui.fragments.tabsFragments.interfaces.ClickInterface
import com.rehman.utilities.Utils.showToast
import com.stfalcon.imageviewer.StfalconImageViewer

class ImagesFragment : Fragment(), ClickInterface {

    private lateinit var binding: FragmentImagesBinding
    private val imageUris = ArrayList<Uri>()
    private lateinit var imagesAdapter: ImagesAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentImagesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (getImagesFromMediaStore().size > 0) {
            initRecycler(getImagesFromMediaStore())
        } else {
            binding.imagesRV.visibility = View.GONE
            binding.noFileLayout.visibility = View.VISIBLE
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
        imagesAdapter = ImagesAdapter(requireContext(), imageUris, this)
        binding.imagesRV.adapter = imagesAdapter

    }

    private fun getImagesFromMediaStore(): ArrayList<Uri> {
        val imageUris = arrayListOf<Uri>()
        val contentResolver = requireContext().contentResolver
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME)
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("%${Environment.DIRECTORY_PICTURES}/DocScanner/%")
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageUris.add(uri)
            }
        }

        return imageUris
    }

    private fun deleteFileFromUri(uri: Uri): Boolean {
        val contentResolver = requireContext().contentResolver
        return try {
            val rowsDeleted = contentResolver.delete(uri, null, null)
            rowsDeleted > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun itemClick(item: Uri) {
        StfalconImageViewer.Builder(context, imageUris) { view, uri ->
            Glide.with(requireContext()).load(uri).into(view)
        }.withHiddenStatusBar(false).show()
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
        if (deleteFileFromUri(uri)) {
            showToast("Image deleted successfully")
            imageUris.remove(uri)
            imagesAdapter.notifyItemRemoved(position)
        } else {
            showToast("Failed to delete image")
        }
    }


}