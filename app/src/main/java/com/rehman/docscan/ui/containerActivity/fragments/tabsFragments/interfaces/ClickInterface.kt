package com.rehman.docscan.ui.containerActivity.fragments.tabsFragments.interfaces

import android.net.Uri
import android.widget.ImageView

interface ClickInterface {
    fun itemClick(imageView: ImageView, uri: Uri)
    fun shareClick(uri: Uri)
    fun deleteClick(position: Int, uri: Uri)
}