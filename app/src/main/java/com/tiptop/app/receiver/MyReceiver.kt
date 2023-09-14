package com.tiptop.app.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tiptop.app.common.Constants.KEY_JSON_CLEAR_CASH
import com.tiptop.app.common.SharedPrefSimple
import java.io.File


class MyReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
//        if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
//            (context.getSystemService(ACTIVITY_SERVICE) as ActivityManager)
//                .clearApplicationUserData()
//        }
        val pref = SharedPrefSimple(context)
        val stringJson = pref.getString(KEY_JSON_CLEAR_CASH) ?: ""
        val typeToken = object : TypeToken<List<String>>() {}.type
        val files = ArrayList<String>()
        if (stringJson != "") {
            try {
                files.addAll(Gson().fromJson<List<String>>(stringJson, typeToken))
            }catch (e:Exception){
                pref.saveString(KEY_JSON_CLEAR_CASH, "")
                e.printStackTrace()
            }
        }
        Log.d("tag", "onReceive")
        if (!files.isEmpty()) {
         files.forEach {
             val file = context.getFileStreamPath(it)
             Log.d("tag", "file deleted :${file.name}")
             file.delete()
         }

            pref.saveString(KEY_JSON_CLEAR_CASH, "")
        }
//        for (file:File in context.cacheDir?.listFiles()!!){
//            Log.d("tag","delete file${file.name}")
//            file.delete()
//        }
//        try {
//            context.cacheDir.deleteRecursively()
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }
//
}