package com.example.audio

import android.content.Context
import android.net.Uri
import coil.map.Mapper
import coil.request.Options
import com.example.data.model.Song

/**
 * Custom Coil Mapper that resolves a Song to its cover art URI.
 *
 * Usage in Coil ImageRequest:
 *   .data(song)
 *   .mapper(CoverArtMapper(context))
 */
class CoverArtMapper(
    private val context: Context
) : Mapper<Song, Uri> {

    override fun map(data: Song, options: Options): Uri {
        return data.coverUri?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
            ?: Uri.parse("android.resource://${context.packageName}/drawable/${data.coverDrawableName}")
    }
}

/**
 * Extension function to easily get a Coil-compatible URI for a Song's cover art.
 * This can be used with AsyncImage or rememberAsyncImagePainter.
 */
fun Song.coverArtUri(context: Context): Uri {
    return coverUri?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
        ?: Uri.parse("android.resource://${context.packageName}/drawable/${coverDrawableName}")
}
