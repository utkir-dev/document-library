package com.tiptop.app.common

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings.Secure
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


@SuppressLint("HardwareIds")
class MyDevice(val context: Context) {
    fun getUniquieId(): String {
        val m_szAndroidID =
            Build.MODEL + Secure.getString(context.contentResolver, Secure.ANDROID_ID)
        return m_szAndroidID
        //        try {
//            // 1
//            val m_szDevIDShort = //we make this look like a valid IMEI
//                "35" + Build.BOARD.length % 10 + Build.BRAND.length % 10 + Build.DEVICE.length % 10 + Build.DISPLAY.length % 10 + Build.HOST.length % 10 + Build.ID.length % 10 + Build.MANUFACTURER.length % 10 + Build.MODEL.length % 10 + Build.PRODUCT.length % 10 + Build.TAGS.length % 10 + Build.TYPE.length % 10 + Build.USER.length % 10 //13 digits
//            // 3
//            val m_szWLANMAC = getEthMac()
//            // 4
//            val m_szLongID = m_szDevIDShort + m_szAndroidID + m_szWLANMAC
//            // compute md5
//            var m: MessageDigest? = null;
//            try {
//                m = MessageDigest.getInstance("MD5");
//            } catch (e: NoSuchAlgorithmException) {
//                e.printStackTrace();
//            }
//            m?.update(m_szLongID.toByteArray(), 0, m_szLongID.length);
//            // get md5 bytes
//            val p_md5Data = m?.digest();
//            // create a hex string from md5 bytes
//            val hexString = StringBuffer()
//            for (i in 0 until p_md5Data!!.size) hexString.append(
//                Integer.toHexString(0xFF and p_md5Data.get(i).toInt())
//            )
//            return hexString.toString()
//        } catch (e: Exception) {
//            return m_szAndroidID
//        }
    }

    private fun getEthMac(): String {
        var macAddress = "No_MAC_address"
        var br: BufferedReader? = null
        try {
            br = BufferedReader(FileReader("/sys/class/net/eth0/address"))
            macAddress = br.readLine().uppercase()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (br != null) {
                try {
                    br.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return macAddress
    }
}
