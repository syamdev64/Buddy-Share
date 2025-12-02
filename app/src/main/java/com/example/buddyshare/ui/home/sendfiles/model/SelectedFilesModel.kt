package com.example.buddyshare.ui.home.sendfiles.model

import android.graphics.Bitmap
import android.net.Uri

data class SelectedFilesModel(
    val uri: Uri,
    val name: String,
    val size: Long
)
