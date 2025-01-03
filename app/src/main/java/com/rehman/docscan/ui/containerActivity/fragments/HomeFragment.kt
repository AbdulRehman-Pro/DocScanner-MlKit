package com.rehman.docscan.ui.containerActivity.fragments

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.IntentSender
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.rehman.docscan.R
import com.rehman.docscan.databinding.FragmentHomeBinding
import com.rehman.docscan.local_db.TinyDB
import com.rehman.utilities.Utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val FULL_MODE = "FULL"
private const val BASIC_MODE = "BASE"
private const val BASIC_MODE_WITH_FILTER = "BASE_WITH_FILTER"

private const val SINGLE_PAGE = "1"
private const val MULTI_PAGE = ""

class HomeFragment : Fragment() {

    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var binding: FragmentHomeBinding
    private var result: GmsDocumentScanningResult? = null
    private lateinit var tinyDB: TinyDB
    private var isGalleryImport: Boolean = false

    private var selectedMode = FULL_MODE
    private var selectedPage = MULTI_PAGE


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        tinyDB = TinyDB(requireContext())
        isGalleryImport = tinyDB.getBoolean("Import_Switch")
        when (tinyDB.getInt("Mode_Radio")) {
            R.id.basicModeRadio -> selectedMode = BASIC_MODE
            R.id.basicModeFilterRadio -> selectedMode = BASIC_MODE_WITH_FILTER
            R.id.advanceModeRadio -> selectedMode = FULL_MODE
        }
        when (tinyDB.getInt("Limit_Radio")) {
            R.id.singleModeRadio -> selectedPage = SINGLE_PAGE
            R.id.burstModeRadio -> selectedPage = MULTI_PAGE
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        scannerLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                handleActivityResult(result)
            }


        binding.scannerBtn.setOnClickListener {


            val options =
                GmsDocumentScannerOptions.Builder()
                    .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
                    .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
                    .setGalleryImportAllowed(isGalleryImport)

            when (selectedMode) {
                FULL_MODE -> options.setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
                BASIC_MODE -> options.setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
                BASIC_MODE_WITH_FILTER -> options.setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE_WITH_FILTER)
                else -> Log.e(HomeFragment::class.java.name, "Unknown selectedMode: $selectedMode")
            }

            if (selectedPage == SINGLE_PAGE) {
                options.setPageLimit(SINGLE_PAGE.toInt())
            }

            GmsDocumentScanning.getClient(options.build())
                .getStartScanIntent(requireActivity())
                .addOnSuccessListener { intentSender: IntentSender ->
                    scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                }
                .addOnFailureListener() { e: Exception ->
                    Log.wtf("Scanner", "Exception -> $e")
                }
        }

    }


    private fun handleActivityResult(activityResult: ActivityResult) {
        val resultCode = activityResult.resultCode
        val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
        if (resultCode == Activity.RESULT_OK && result != null) {

            Log.wtf("Scanner", "Scan Result -> $result")


            lifecycleScope.launch(Dispatchers.IO) {
                val pages = result.pages
                if (pages!!.isNotEmpty()) {
                    pages.forEach {
                        val bitmap = getBitmapFromUri(it.imageUri)
                        if (bitmap != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                saveImageToMediaStore(bitmap)
                            } else {
                                saveImageToExternalStorage(bitmap)
                            }

                        } else {
                            Log.wtf("Scanner", "Failed to get bitmap from URI")
                            showToast("Failed to get image")
                        }
                    }

                    withContext(Dispatchers.Main) {
                        if (pages.size > 1) {
                            showToast("Images saved successfully.")
                        } else {
                            showToast("Image saved successfully.")
                        }
                    }

                }
            }


        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.wtf("Scanner", "Scan Result -> Cancelled...")
            showToast("Cancelled...")
        } else {
            Log.wtf("Scanner", "Scan Result -> Failed...")
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        val contentResolver: ContentResolver = requireContext().contentResolver
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun saveImageToMediaStore(bitmap: Bitmap) {


        val contentResolver = requireContext().contentResolver
        val contentValues = ContentValues().apply {
            put(
                MediaStore.MediaColumns.DISPLAY_NAME, "IMG-${
                    getCurrentDateAndTime()
                }"
            )
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/DocScanner/"
            )
        }

        val uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
            }
        }

    }

    private fun saveImageToExternalStorage(bitmap: Bitmap) {
        val imageDirectory = File(Environment.getExternalStorageDirectory().path + "/DocScanner/")
        if (!imageDirectory.exists()) {
            imageDirectory.mkdirs()
        }

        val imageFile =
            File(imageDirectory, "IMG-${getCurrentDateAndTime()}")
        val outputStream: OutputStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()

        // Refresh the MediaScanner to make the image available in the gallery
        MediaScannerConnection.scanFile(
            context,
            arrayOf(imageFile.absolutePath),
            null,
            null
        )
    }

    private fun getCurrentDateAndTime(): String {
        val format = SimpleDateFormat("yyyyMMdd-hhmmssa", Locale.getDefault())
        val dateTime = format.format(Calendar.getInstance().time)
        return if (dateTime.contains("am")) {
            dateTime.replace("am", "1")
        } else {
            dateTime.replace("pm", "2")
        }

    }


}