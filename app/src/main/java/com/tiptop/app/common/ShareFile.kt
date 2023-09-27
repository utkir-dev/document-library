package com.tiptop.app.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
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
                files.add(newFile.name?:"")
            }
            pref.saveString(KEY_JSON_CLEAR_CASH, Gson().toJson(files))

            if (newFile.exists()) {
                //----------------------------------------------------
                val time = SystemClock.elapsedRealtime() + 600_000
                val intentReceiver = Intent(context, MyReceiver::class.java)
                val pendingIntent =
                    PendingIntent.getBroadcast(
                        context, 1010, intentReceiver,0
                        //PendingIntent.FLAG_IMMUTABLE
                    )
                val alarmManager =
                    context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
                alarmManager.set(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    time,
                    pendingIntent
                )
                //-------------------------------------------------
                val uri = FileProvider.getUriForFile(
                    context,
                    "com.tiptop.provider",
                    //BuildConfig.APPLICATION_ID + ".provider",
                    newFile
                )

                val intent = Intent(Intent.ACTION_SEND)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                //intent.type = "*/*"
                intent.type =if (document.type== TYPE_PDF) "application/pdf"
                else if (document.type== TYPE_IMAGE) "image/*" else "text/plain"
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
    } catch (_: java.lang.Exception) {}
}
