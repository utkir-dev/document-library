package com.tiptop.app.common

import android.content.Context
import android.util.Log
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipFile

object UnzipUtils {
    fun unzip(context: Context, zipFilePath: File, destDirectory: String, function: () -> Unit) {
        try {
            File(destDirectory).run {
                Log.d("dict", "file: ${this}")
                Log.d("dict", "destDirectory: ${destDirectory}")
                if (!exists()) {
                    Log.d("dict", "destDirectory.exists(): ${exists()}")
                    mkdirs()
                }
            }
            ZipFile(zipFilePath).use { zip ->
                Log.d("dict", "zip: ${zip}")

                zip.entries().asSequence().forEach { entry ->
                    Log.d("dict", "entry: ${entry}")
                    Log.d("dict", "entry: ${entry.name}")
                    zip.getInputStream(entry).use { input ->
                        val filePath = context.getFileStreamPath(entry.name).absolutePath
                        if (!entry.isDirectory) {
                            Log.d("dict", "entry.isDirectory: ${entry.isDirectory}")

                            extractFile(input, filePath) {
                                Log.d("dict", "extractedFile : ${filePath}")

                                function()
                            }
                        } else {
                            val dir = File(filePath)
                            dir.mkdir()
                        }
                    }
                }

            }
        } catch (e: Exception) {
        }
    }


    private fun extractFile(inputStream: InputStream, destFilePath: String, function: () -> Unit) {
        try {
            val bos = BufferedOutputStream(FileOutputStream(destFilePath))
            val bytesIn = ByteArray(BUFFER_SIZE)
            Log.d("dict", "bytesIn : ${bytesIn}")

            var read: Int
            while (inputStream.read(bytesIn).also { read = it } != -1) {
                Log.d("dict", " bos.write : ${bytesIn}")

                bos.write(bytesIn, 0, read)
                Log.d("dict", " bos.writed !!! : ${bytesIn}")

            }
            bos.close()
            function()
        } catch (e: Exception) {
            Log.d("dict", "Exception : ${e.message}")
            e.printStackTrace()
        }

    }

    private const val BUFFER_SIZE = 4096

}