package com.tiptop.app.common

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.tiptop.R
import java.io.File

class DownloadController(
    val context: Context,
    private val apkName: String,
    private val downloadUrl: String
) {
    companion object {
        private const val FILE_BASE_PATH = "file://"
        private const val MIME_TYPE = "application/vnd.android.package-archive"
        private const val APP_INSTALL_PATH = "\"application/vnd.android.package-archive\""
    }

    fun enqueueDownload() {
        var destination =
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        destination += apkName
        val uri = Uri.parse("$FILE_BASE_PATH$destination")
        val downloadUri = Uri.parse(downloadUrl)
        val file = File(destination)
        if (file.exists()) file.delete()
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(downloadUri)
        request.setMimeType(MIME_TYPE)
        request.setTitle("$apkName yuklanmoqda")
        request.setDescription(context.getString(R.string.downloading))
        // set destination
        request.setDestinationUri(uri)
        showInstallOption(destination)
        // Enqueue a new download and same the referenceId
        downloadManager.enqueue(request)
        Toast.makeText(context, context.getString(R.string.downloading), Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun showInstallOption(
        destination: String
    ) {
        Log.d("DownloadController", "showInstallOption")

        // set BroadcastReceiver to install app when .apk is downloaded
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                Log.d("DownloadController", "onReceive")

                val contentUri = FileProvider.getUriForFile(
                    context,
                    "com.tiptop.provider",
                    File(destination)
                )
                Log.d("DownloadController", "contentUri :$contentUri")

                val install = Intent(Intent.ACTION_VIEW)
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                install.data = contentUri
                Log.d("DownloadController", "installed install.data")

                context.startActivity(install)
                Log.d("DownloadController", "satarted startActivity")

                context.unregisterReceiver(this)
                Log.d("DownloadController", "unregisterReceiver")

                // finish()
            }
        }
        context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
}