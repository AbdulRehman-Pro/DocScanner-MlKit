package com.rehman.docscan.ui.fragments.tabsFragments.adapters

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.rehman.docscan.R
import com.rehman.docscan.databinding.ItemImagesBinding
import com.rehman.docscan.ui.fragments.tabsFragments.interfaces.ClickInterface

class ImagesAdapter(
    private val context: Context,
    private val imageList: ArrayList<Uri>,
    private val clickInterface: ClickInterface,
) :
    RecyclerView.Adapter<ImagesAdapter.ImagesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemBinding = ItemImagesBinding.inflate(layoutInflater, parent, false)
        return ImagesViewHolder(itemBinding)
    }

    override fun getItemCount(): Int = imageList.size

    override fun onBindViewHolder(holder: ImagesViewHolder, position: Int) {
        val item = imageList[position]
        holder.bind(item,position)
    }

    inner class ImagesViewHolder(private val itemBinding: ItemImagesBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(uri: Uri, position: Int) {

            val fileDetails = processFileName(uri)
            fileDetails?.let { (title, description) ->
                itemBinding.itemTitle.text = title
                itemBinding.itemDesc.text = description
            } ?: run {
                itemBinding.itemTitle.text = context.getString(R.string.unknown)
                itemBinding.itemDesc.text = context.getString(R.string.unknown)
            }

            Glide.with(context).load(uri)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(itemBinding.itemImage)


            itemBinding.itemMore.setOnClickListener {
                menuClicked(itemBinding.itemMore, uri, position)
            }

            itemBinding.root.setOnClickListener {
                clickInterface.itemClick(uri)
            }

        }


    }

    fun processFileName(item: Uri): Pair<String, String>? {
        val fileName = getFileNameFromUri(item) ?: return null

        val parts = fileName.split("-")
        if (parts.size < 3) return null

        val date = parts[1]
        val timePart = parts[2].split(".")
        if (timePart.size < 2) return null

        val cleanTime = timePart[0]
        val ext = timePart[1]
        val amPm = when (cleanTime.last()) {
            '1' -> "AM"
            '2' -> "PM"
            else -> ""
        }

        if (cleanTime.length < 4) return null

        val hours = cleanTime.substring(0, 2)
        val minutes = cleanTime.substring(2, 4)
        val formattedTime = "${hours}:${minutes} $amPm"

        val newFileName = "${parts[0]}-$date-DS${cleanTime.substring(2, 6)}.$ext"
        val fileSize = formatFileSize(getFileSizeFromUri(item))
        val description = "$formattedTime, $fileSize, ${ext.uppercase()} image"

        return Pair(newFileName, description)
    }


    private fun menuClicked(it: View?, uri: Uri, position: Int) {
        val popupMenu = PopupMenu(context, it!!)
        popupMenu.menu.add("Share")
        popupMenu.menu.add("Delete")

        popupMenu.setOnMenuItemClickListener { item ->
            when (item?.title!!.toString()) {
                "Share" -> clickInterface.shareClick(uri)
                "Delete" -> clickInterface.deleteClick(position,uri)
            }

            false
        }
        popupMenu.show()
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        val contentResolver: ContentResolver = context.contentResolver
        val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        var fileName: String? = null

        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                fileName = cursor.getString(nameIndex)
            }
        }

        return fileName
    }

    private fun getFileSizeFromUri(uri: Uri): Long {
        val contentResolver: ContentResolver = context.contentResolver
        val projection = arrayOf(MediaStore.Images.Media.SIZE)
        var fileSize: Long = 0

        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                fileSize = cursor.getLong(sizeIndex)
            }
        }

        return fileSize
    }

    private fun formatFileSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> String.format("%d bytes", size)
        }
    }
}