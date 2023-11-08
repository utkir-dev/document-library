package com.tiptop.app.common

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import java.io.File
import java.io.InputStream

@SuppressLint("Recycle")
fun getBytes(id: String, context: Context): ByteArray? {
    return try {
        val inputStream: InputStream? = context.getFileStreamPath(id)
            ?.let { context.contentResolver.openInputStream(it.toUri()) }
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        bytes
    } catch (e: Exception) {
        null
    }
}
