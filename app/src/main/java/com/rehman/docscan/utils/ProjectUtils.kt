package com.rehman.docscan.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.format.Formatter

object ProjectUtils {
    fun getImagesFromMediaStore(context: Context): ArrayList<Uri> {
        val imageUris = arrayListOf<Uri>()
        val contentResolver = context.contentResolver
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

    fun getFileNameFromUri(context: Context, uri: Uri): String? {
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

    private fun getFileSizeFromUri(context: Context, uri: Uri): Long {
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

    fun processFileName(context: Context, item: Uri): Pair<String, String>? {
        val fileName = getFileNameFromUri(context, item) ?: return null

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
        val fileSize = formatFileSize(getFileSizeFromUri(context,item))
        val description = "$formattedTime, $fileSize, ${ext.uppercase()} image"

        return Pair(newFileName, description)
    }

    fun deleteFileFromUri(context: Context, uri: Uri): Boolean {
        val contentResolver = context.contentResolver
        return try {
            val rowsDeleted = contentResolver.delete(uri, null, null)
            rowsDeleted > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}