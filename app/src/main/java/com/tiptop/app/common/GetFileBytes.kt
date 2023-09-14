package com.tiptop.app.common

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.net.toUri
import java.io.File

@SuppressLint("Recycle")
fun getBytes(id: String, context: Context): ByteArray? {
    val file: File = context.getFileStreamPath(id)
    val bytes =
        file?.let { context.contentResolver.openInputStream(it.toUri())?.readBytes() }
    return bytes
}
