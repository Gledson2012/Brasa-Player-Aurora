package com.example.audio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Disk-based LRU cache for album art thumbnails.
 *
 * Extracts embedded artwork from audio files and caches resized thumbnails
 * on disk for ultra-fast loading in track lists. Uses an in-memory LRU cache
 * as a first-level cache and disk as second-level.
 */
class CoverArtCache(private val context: Context) {

    companion object {
        private const val TAG = "CoverArtCache"
        private const val CACHE_DIR_NAME = "cover_art_cache"
        private const val MAX_MEMORY_CACHE_SIZE = 50 * 1024 * 1024 // 50MB
        private const val MAX_DISK_CACHE_SIZE = 200 * 1024 * 1024 // 200MB
        private const val THUMBNAIL_WIDTH = 300
        private const val THUMBNAIL_HEIGHT = 300
        private const val JPEG_QUALITY = 85
    }

    // In-memory LRU cache for hot thumbnails
    private val memoryCache = object : LruCache<String, Bitmap>(MAX_MEMORY_CACHE_SIZE) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount
        }
    }

    private val cacheDir: File by lazy {
        File(context.cacheDir, CACHE_DIR_NAME).also { dir ->
            if (!dir.exists()) dir.mkdirs()
        }
    }

    private val mutex = Mutex()

    /**
     * Get album art bitmap for a song, checking memory cache, then disk, then extracting.
     * Returns null if no artwork is available.
     */
    suspend fun getAlbumArt(
        songUri: String?,
        songId: Long,
        fallbackDrawableName: String? = null
    ): Bitmap? {
        if (songUri.isNullOrBlank()) return null

        val cacheKey = "${songId}_${songUri.hashCode()}"

        // 1. Check memory cache
        memoryCache.get(cacheKey)?.let { return it }

        // 2. Check disk cache
        val diskFile = getDiskCacheFile(cacheKey)
        if (diskFile.exists()) {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeFile(diskFile.absolutePath)
                }
                if (bitmap != null) {
                    memoryCache.put(cacheKey, bitmap)
                    return bitmap
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load cached cover art from disk", e)
                diskFile.delete()
            }
        }

        // 3. Extract from audio file
        val bitmap = withContext(Dispatchers.IO) {
            extractEmbeddedArt(songUri)
        }

        if (bitmap != null) {
            // Cache to memory and disk
            memoryCache.put(cacheKey, bitmap)
            saveToDiskCache(diskFile, bitmap)
            return bitmap
        }

        return null
    }

    /**
     * Extract embedded artwork from an audio file.
     */
    private fun extractEmbeddedArt(uri: String): Bitmap? {
        return try {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(context, Uri.parse(uri))
                val artBytes = retriever.embeddedPicture ?: return null

                // Decode and resize to thumbnail
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size, options)

                // Calculate sample size for efficient decoding
                options.inSampleSize = calculateInSampleSize(
                    options.outWidth,
                    options.outHeight,
                    THUMBNAIL_WIDTH,
                    THUMBNAIL_HEIGHT
                )
                options.inJustDecodeBounds = false

                val bitmap = BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size, options)

                // Scale to exact thumbnail size if needed
                if (bitmap != null && (bitmap.width > THUMBNAIL_WIDTH * 2 || bitmap.height > THUMBNAIL_HEIGHT * 2)) {
                    Bitmap.createScaledBitmap(bitmap, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, true).also {
                        if (it != bitmap) bitmap.recycle()
                    }
                } else {
                    bitmap
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract embedded art from $uri", e)
            null
        }
    }

    /**
     * Calculate appropriate sample size for bitmap decoding.
     */
    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Save bitmap to disk cache.
     */
    private fun saveToDiskCache(file: File, bitmap: Bitmap) {
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save cover art to disk cache", e)
        }
    }

    /**
     * Get the disk cache file for a given key.
     */
    private fun getDiskCacheFile(key: String): File {
        return File(cacheDir, "${key.hashCode().toUInt()}.jpg")
    }

    /**
     * Clear all cached cover art (memory + disk).
     */
    suspend fun clearCache() = mutex.withLock {
        memoryCache.evictAll()
        withContext(Dispatchers.IO) {
            cacheDir.listFiles()?.forEach { it.delete() }
        }
    }

    /**
     * Get total cache size in bytes.
     */
    suspend fun getCacheSize(): Long = withContext(Dispatchers.IO) {
        cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    /**
     * Trim disk cache to stay under max size by deleting oldest files.
     */
    suspend fun trimDiskCache() = mutex.withLock {
        withContext(Dispatchers.IO) {
            val files = cacheDir.listFiles()?.sortedBy { it.lastModified() } ?: return@withContext
            var totalSize = files.sumOf { it.length() }

            for (file in files) {
                if (totalSize <= MAX_DISK_CACHE_SIZE) break
                totalSize -= file.length()
                file.delete()
            }
        }
    }
}
