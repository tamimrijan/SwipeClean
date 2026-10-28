package com.swipeclean.app.data

import android.app.RecoverableSecurityException
import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads photos from MediaStore and deletes them through the system-approved paths.
 * Everything runs locally; nothing is uploaded anywhere.
 */
class PhotoRepository(context: Context) {

    private val resolver = context.applicationContext.contentResolver

    private val collection: Uri =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

    @Suppress("DEPRECATION")
    suspend fun loadPhotos(): List<Photo> = withContext(Dispatchers.IO) {
        val pathColumn =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.MediaColumns.RELATIVE_PATH
            else MediaStore.MediaColumns.DATA
        val projection = buildList {
            add(MediaStore.Images.Media._ID)
            add(MediaStore.Images.Media.DISPLAY_NAME)
            add(MediaStore.Images.Media.SIZE)
            add(MediaStore.Images.Media.WIDTH)
            add(MediaStore.Images.Media.HEIGHT)
            add(MediaStore.Images.Media.DATE_ADDED)
            add(MediaStore.Images.ImageColumns.DATE_TAKEN)
            add(MediaStore.Images.ImageColumns.BUCKET_ID)
            add(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)
            add(pathColumn)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) add(MediaStore.MediaColumns.IS_FAVORITE)
        }.toTypedArray()

        val photos = ArrayList<Photo>()
        val cursor = resolver.query(
            collection, projection, null, null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC"
        ) ?: throw IllegalStateException("The device's media library is unavailable right now.")

        cursor.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val widthCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val addedCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val takenCol = c.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN)
            val bucketIdCol = c.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_ID)
            val bucketNameCol = c.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)
            val pathCol = c.getColumnIndex(pathColumn)
            val favCol =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) c.getColumnIndex(MediaStore.MediaColumns.IS_FAVORITE)
                else -1

            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val name = c.getString(nameCol).orEmpty()
                val bucket = c.getString(bucketNameCol) ?: "Other"
                val path = if (pathCol >= 0) c.getString(pathCol).orEmpty() else ""
                val taken = c.getLong(takenCol)
                val lowerBucket = bucket.lowercase()
                val lowerPath = "/" + path.lowercase()

                photos += Photo(
                    id = id,
                    uri = ContentUris.withAppendedId(collection, id),
                    displayName = name,
                    date = if (taken > 0) taken else c.getLong(addedCol) * 1000L,
                    size = c.getLong(sizeCol),
                    width = c.getInt(widthCol),
                    height = c.getInt(heightCol),
                    bucketId = c.getLong(bucketIdCol),
                    bucketName = bucket,
                    isScreenshot = "screenshot" in lowerBucket ||
                        "/screenshots/" in lowerPath ||
                        name.lowercase().startsWith("screenshot"),
                    isDownload = lowerBucket == "download" || lowerBucket == "downloads" ||
                        "/download/" in lowerPath || "/downloads/" in lowerPath,
                    isFavorite = favCol >= 0 && c.getInt(favCol) == 1,
                )
            }
        }
        photos
    }

    /** True if the item is still present in MediaStore. */
    suspend fun exists(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            resolver.query(uri, arrayOf(MediaStore.Images.Media._ID), null, null, null)
                ?.use { it.count > 0 } ?: false
        }.getOrDefault(false)
    }

    /** Android 11+: one system dialog asks the user to approve deleting all [uris]. */
    @RequiresApi(Build.VERSION_CODES.R)
    fun createDeleteRequest(uris: List<Uri>): IntentSender =
        MediaStore.createDeleteRequest(resolver, uris).intentSender

    class LegacyDeleteResult(
        val deleted: List<Photo>,
        /** Android 10: system consent needed before the next item can be deleted. */
        val consent: IntentSender?,
        val error: String?,
    )

    /** Android 10 and below: delete directly (the app already holds storage permission). */
    suspend fun deleteLegacy(photos: List<Photo>): LegacyDeleteResult = withContext(Dispatchers.IO) {
        val deleted = ArrayList<Photo>()
        for (photo in photos) {
            try {
                if (resolver.delete(photo.uri, null, null) > 0) deleted += photo
            } catch (e: SecurityException) {
                val consent = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) consentFor(e) else null
                return@withContext if (consent != null) {
                    LegacyDeleteResult(deleted, consent, null)
                } else {
                    LegacyDeleteResult(deleted, null, "Android didn't allow SwipeClean to delete \"${photo.displayName}\".")
                }
            }
        }
        LegacyDeleteResult(deleted, null, null)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun consentFor(e: SecurityException): IntentSender? =
        (e as? RecoverableSecurityException)?.userAction?.actionIntent?.intentSender
}
