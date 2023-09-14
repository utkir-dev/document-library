package com.tiptop.app.common

import android.content.res.Resources
import java.math.BigInteger
import java.security.MessageDigest
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()

val Float.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()
fun Long.huminize(): String {
    val diff = System.currentTimeMillis() - this
    val formatHour = "dd/MM/yy  HH:mm"
    val formatDay = "dd/MM/yyyy"
    val formatted = if (diff < 180_000) {
        "hozirgina"
    } else if (diff < 3600_000) {
        "${diff / 60_000} min oldin"
    } else if (diff < 86_400_000) {
        "${diff / 3600_000} soat oldin"
    } else if (diff < 30 * 86_400_000L) {
        SimpleDateFormat(formatHour, Locale.getDefault()).format(Date(this))
    } else {
        SimpleDateFormat(formatDay, Locale.getDefault()).format(Date(this))
    }
    return formatted
}

fun ByteArray.huminize(): String {
    val kbyte = 1024
    val mbyte = 1024 * 1024
    val gbyte = 1024 * 1024 * 1024
    val value = if (this.size < kbyte) "${this} byte"
    else if (this.size < 700 * kbyte) "${(this.size.toDouble() / kbyte).roundTen()} Kb"
    else if (this.size < 700 * mbyte) "${(this.size.toDouble() / mbyte).roundTen()} Mb"
    else "${(this.size.toDouble() / gbyte).roundTen()} Gb"
    return value
}
fun Long.validateFileSize():String{
    val kbyte = 1024
    val mbyte = 1024 * 1024
    val gbyte = 1024 * 1024 * 1024
    val value = if (this < kbyte) "${this} byte"
    else if (this < 700 * kbyte) "${(this.toDouble() / kbyte).roundTen()} Kb"
    else if (this < 700 * mbyte) "${(this.toDouble() / mbyte).roundTen()} Mb"
    else "${(this.toDouble() / gbyte).roundTen()} Gb"
    return value
}
fun Double.roundTen(): String {
    val dec =
        DecimalFormat("###,###,###,###,###.0", DecimalFormatSymbols(Locale.ENGLISH))
    return dec.format(this).replace(",", " ")
}

fun Double.round() = Math.round(this * 100.0) / 100.0
fun String.encrypt(): String {
    val md = MessageDigest.getInstance("MD5")
    val digest = md.digest(toByteArray())
    val hexString = BigInteger(1, digest).toString(16)
    return hexString.padStart(32, '0')
}

fun List<String>.huminize(): String {
    var str = ""
    this.forEach {
        str = "$it, $str"
    }
    return str.trim().substring(0, str.trim().length - 1)
}