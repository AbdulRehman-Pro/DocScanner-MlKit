package com.rehman.docscan.ui

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DataViewModel : ViewModel() {
    val imagesUriList: MutableLiveData<ArrayList<Uri> > = MutableLiveData()
}