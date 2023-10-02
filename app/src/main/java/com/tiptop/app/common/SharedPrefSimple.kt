package com.tiptop.app.common

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.tiptop.app.App
import com.tiptop.presentation.MainActivity

class SharedPrefSimple(val ctx: Context) {
    private val sharedPref = ctx.getSharedPreferences("SharedPreferences",Context.MODE_PRIVATE)
    fun cleaAllData() {
        sharedPref.edit().clear().apply()
    }

    fun saveInt(key: String, n: Int) {
        sharedPref.edit().putInt(key, n).apply()
    }

    fun getInt(key: String): Int {
        return sharedPref.getInt(key, 0)
    }

    fun saveBoolean(key: String, n: Boolean) {
        sharedPref.edit().putBoolean(key, n).apply()
    }

    fun getBoolean(key: String): Boolean {
        return sharedPref.getBoolean(key, false)
    }

    fun saveLong(key: String, n: Long) {
        sharedPref.edit().putLong(key, n).apply()
    }

    fun getLong(key: String): Long {
        return sharedPref.getLong(key,0L)
    }
    fun saveString(key: String, n: String) {
        sharedPref.edit().putString(key, n).apply()
    }

    fun getString(key: String): String {
        return sharedPref.getString(key,"")?:""
    }
}