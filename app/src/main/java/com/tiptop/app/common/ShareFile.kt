package com.tiptop.app.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tiptop.app.common.Constants.KEY_JSON_CLEAR_CASH
import com.tiptop.app.common.Constants.TYPE_IMAGE
import com.tiptop.app.common.Constants.TYPE_PDF
import com.tiptop.app.receiver.MyReceiver
import com.tiptop.data.models.local.DocumentLocal


@SuppressLint("SuspiciousIndentation")
fun share(document: DocumentLocal, bytes: ByteArray, context: Activity) {
    try {
        val newFile = context.getFileStreamPath(document.nameDecrypted())
        newFile.writeBytes(bytes!!)
        val pref = SharedPrefSimple(context)
        val stringJson = pref.getString(KEY_JSON_CLEAR_CASH)
        val typeToken = object : TypeToken<List<String>>() {}.type
        val files = ArrayList<String>()
        if (stringJson != "") {
            try {
                files.addAll(Gson().fromJson<List<String>>(stringJson, typeToken))
            } catch (e: Exception) {
                pref.saveString(KEY_JSON_CLEAR_CASH, "")
            }
        }
        if (!files.contains(newFile.name)) {
            files.add(newFile.name ?: "")
        }
        pref.saveString(KEY_JSON_CLEAR_CASH, Gson().toJson(files))
        if (newFile.exists()) {
            //----------------------------------------------------
            try {
                val time = SystemClock.elapsedRealtime() + 600_000
                val intentReceiver = Intent(context, MyReceiver::class.java)
                val pendingIntent =
                    PendingIntent.getBroadcast(
                        context, 1010, intentReceiver, FLAG_IMMUTABLE
                        //PendingIntent.FLAG_IMMUTABLE
                    )
                val alarmManager =
                    context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
                alarmManager.set(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    time,
                    pendingIntent
                )
            } catch (_: Exception) {

            }

            //-------------------------------------------------
            val uri = FileProvider.getUriForFile(
                context,
                "com.tiptop.fileprovider",
                newFile
            )

            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(Intent.EXTRA_STREAM, uri)
                type = when (document.type) {
                    TYPE_PDF -> "application/pdf"
                    TYPE_IMAGE -> "image/*"
                    else -> "text/*"
                }
            }
            context.startActivity(Intent.createChooser(shareIntent, null))

//                val share = Intent.createChooser(Intent().apply {
//                    action = Intent.ACTION_SEND
//                    putExtra(Intent.EXTRA_TEXT, newFile.name)
//                    // (Optional) Here you're setting the title of the content
//                   //  putExtra(Intent.EXTRA_TITLE, "Introducing content previews")
//
//                    // (Optional) Here you're passing a content URI to an image to be displayed
//                    data = uri
//                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//                }, null)
//                context.startActivity(share)


        }
    } catch (e: java.lang.Exception) {
        Log.d("Share", "Exception : ${e.message}")

    }
}
