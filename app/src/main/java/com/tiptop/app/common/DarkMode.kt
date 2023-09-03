package com.tiptop.app.common

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.tiptop.presentation.MainActivity

class DarkMode(val ctx:Context) {
    fun setNightMode(mode: Boolean) {
        if (mode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            (ctx as MainActivity).delegate.applyDayNight()
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            (ctx as MainActivity).delegate.applyDayNight()
        }
    }

    fun isDarkModeOn(): Boolean {
        val currentNightMode = (ctx as MainActivity).resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}