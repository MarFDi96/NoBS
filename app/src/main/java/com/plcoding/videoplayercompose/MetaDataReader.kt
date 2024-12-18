package com.plcoding.videoplayercompose

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
data class MetaData(
    val fileName: String
)

interface MetaDataReader {
    fun getMetaDataFromUri(contentUri: Uri): MetaData?
}

class MetaDataReaderImpl(
    private val app: Application
): MetaDataReader {

    override fun getMetaDataFromUri(contentUri: Uri): MetaData? {
        if(contentUri.scheme != "content") {
            return null
        }
        val fileName = app.contentResolver
            .query(
                contentUri,
                arrayOf(MediaStore.Audio.AudioColumns.DISPLAY_NAME),
                null,
                null,
                null,
            )
            ?.use { cursor ->
                val index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(index)
            }
        return fileName?.let { fullFileName ->
            MetaData(
                fileName = Uri.parse(fullFileName).lastPathSegment ?: return null
            )
        }
    }
}
/*
data class MetaData(
    val fileName: String?,
    val title: String?,
    val artist: String?,
    val album: String?,
    val albumArt: Bitmap?
)

interface MetaDataReader {
    fun getMetaDataFromUri(contentUri: Uri): MetaData?
}

class MetaDataReaderImpl(
    private val app: Application
): MetaDataReader {

    override fun getMetaDataFromUri(contentUri: Uri): MetaData? {
        if (contentUri.scheme != "content") {
            return null
        }
        val fileName = app.contentResolver
            .query(
                contentUri,
                arrayOf(MediaStore.Audio.AudioColumns.DISPLAY_NAME),
                null,
                null,
                null,
            )
            ?.use { cursor ->
                val index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(index)
            }


        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(app.applicationContext, contentUri)

        val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)

        val albumArt: Bitmap? = try {
            retriever.embeddedPicture?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        } catch (e: Exception) {
            Log.e("MetaDataReader", "Error getting embedded picture: ${e.message}")
            null
        }

        retriever.release()

        return MetaData(fileName, title, artist, album, albumArt)
    }
}*/