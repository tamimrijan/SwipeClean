package com.swipeclean.app.data

import android.net.Uri

data class Photo(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    /** Milliseconds since epoch (date taken, or date added as a fallback). */
    val date: Long,
    val size: Long,
    val width: Int,
    val height: Int,
    val bucketId: Long,
    val bucketName: String,
    val isScreenshot: Boolean,
    val isDownload: Boolean,
    val isFavorite: Boolean,
)

data class Album(
    val id: Long,
    val name: String,
    val count: Int,
    val cover: Uri,
)
