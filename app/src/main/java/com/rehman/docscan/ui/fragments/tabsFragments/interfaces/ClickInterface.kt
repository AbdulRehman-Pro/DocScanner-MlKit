package com.rehman.docscan.ui.fragments.tabsFragments.interfaces

import android.net.Uri
import java.text.FieldPosition

interface ClickInterface {
    fun itemClick(item: Uri)
    fun shareClick(uri: Uri)

    fun deleteClick(position: Int, uri: Uri)
}