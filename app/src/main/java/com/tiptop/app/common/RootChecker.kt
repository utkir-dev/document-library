package com.tiptop.app.common

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.tiptop.presentation.MainActivity
import java.io.File
import java.util.Scanner

class RootChecker(private val context: Context) {

    val isRooted =
        /* checkRootFilesAndPackages() ||  checkSUPaths() || */
        checkCustomOS() || checkOTACerts() || checkForDangerousProps() || detectRootCloakingApps() || detectPotentiallyDangerousApps()

//    // check packages
//    private val knownRootAppsPackages = listOf(
//        "com.noshufou.android.su",
//        "com.noshufou.android.su.elite",
//        "eu.chainfire.supersu",
//        "com.koushikdutta.superuser",
//        "com.thirdparty.superuser",
//        "com.yellowes.su",
//        "com.topjohnwu.magisk",
//        "com.kingroot.kinguser",
//        "com.kingo.root",
//        "com.smedialink.oneclickroot",
//        "com.zhiqupk.root.global",
//        "com.alephzain.framaroot"
//    )
//
//    private fun checkRootFilesAndPackages(): Boolean {
//        val result = false
//        knownRootAppsPackages.forEach {
//            if (isPackageInstalled(it)) {
//                return true
//            }
//        }
//        return result
//    }
//
//    private fun isPackageInstalled(packagename: String): Boolean {
//        val pm = context.packageManager as PackageManager
//        return try {
//            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES)
//            true
//        } catch (e: PackageManager.NameNotFoundException) {
//            false
//        }
//    }


//    // check paths
//    private val knownSUPaths = listOf(
//        "/system/bin/su",
//        "/system/xbin/su",
//        "/sbin/su",
//        "/system/su",
//        "/system/bin/.ext/.su",
//        "/system/usr/we-need-root/su-backup",
//        "/system/xbin/mu"
//    )
//
//    private fun checkSUPaths(): Boolean {
//        var result = false
//        for (string1 in knownSUPaths) {
//            val f = File(string1)
//            val fileExists = f.exists()
//            if (fileExists) {
//                result = true
//            }
//        }
//        return result
//    }

    // check TAGS (A)
    private fun checkCustomOS(): Boolean {
        val buildTags = android.os.Build.TAGS
        var result = false
        if (buildTags != null && buildTags.contains("test-keys")) {
            result = true
        }
        Log.d("rooted", "checkCustomOS : $result")
        return result
    }

    // check TAGS (B)
    private fun checkOTACerts(): Boolean {
        val OTAPath = "/etc/security/otacerts.zip"
        val f = File(OTAPath)
        Log.d("rooted", "checkOTACerts : ${!f.exists()}")

        return !f.exists()
    }

    // check Dangerous Props
    private fun propsReader(): List<String>? {
        try {
            val inputstream = Runtime.getRuntime().exec("getprop").inputStream ?: return null
            val propVal = Scanner(inputstream).useDelimiter("\\A").next()
            return propVal.split("\n")
        } catch (e: Exception) {
            return null
        }
    }

    private fun checkForDangerousProps(): Boolean {
        val dangerousProps = HashMap<String, String>()
        dangerousProps.put("ro.debuggable", "1")
        dangerousProps.put("ro.secure", "0")
        var result = false
        val lines = propsReader() ?: return false
        for (line in lines) {
            for (key in dangerousProps.keys) {
                if (line.contains(key)) {
                    var badValue = dangerousProps[key]
                    badValue = "[$badValue]"
                    if (line.contains(badValue)) {
                        result = true
                    }
                }
            }
        }
        Log.d("rooted", "checkForDangerousProps : ${result}")
        return result
    }


    //check applications that are commonly found on a rooted device

    private val knownDangerousAppsPackages = listOf(
        "com.koushikdutta.rommanager",
        "com.koushikdutta.rommanager.license",
        "com.dimonvideo.luckypatcher",
        "com.chelpus.lackypatch",
        "com.ramdroid.appquarantine",
        "com.ramdroid.appquarantinepro",
        "com.android.vending.billing.InAppBillingService.COIN",
        "com.android.vending.billing.InAppBillingService.LUCK",
        "com.chelpus.luckypatcher",
        "com.blackmartalpha",
        "org.blackmart.market",
        "com.allinone.free",
        "com.repodroid.app",
        "org.creeplays.hack",
        "com.baseappfull.fwd",
        "com.zmapp",
        "com.dv.marketmod.installer",
        "org.mobilism.android",
        "com.android.wp.net.log",
        "com.android.camera.update",
        "cc.madkite.freedom",
        "com.solohsu.android.edxp.manager",
        "org.meowcat.edxposed.manager",
        "com.xmodgame",
        "com.cih.game_cih",
        "com.charles.lpoqasert",
        "catch_.me_.if_.you_.can_"
    )

    private fun detectPotentiallyDangerousApps(): Boolean {
        val result = isAnyPackageFromListInstalled(knownDangerousAppsPackages)
        Log.d("rooted", "checkForDangerousProps : ${result}")
        return result
    }

    // check detect root cloaking apps installed on the device.

    private val knownRootCloakingPackages = listOf(
        "com.devadvance.rootcloak",
        "com.devadvance.rootcloakplus",
        "de.robv.android.xposed.installer",
        "com.saurik.substrate",
        "com.zachspong.temprootremovejb",
        "com.amphoras.hidemyroot",
        "com.amphoras.hidemyrootadfree",
        "com.formyhm.hiderootPremium",
        "com.formyhm.hideroot"
    )

    private fun detectRootCloakingApps(): Boolean {
        val result = isAnyPackageFromListInstalled(knownRootCloakingPackages)
        Log.d("rooted", "checkForDangerousProps : ${result}")
        return result

    }

    private fun isAnyPackageFromListInstalled(packages: List<String>): Boolean {
        var result = false
        val pm = context.packageManager
        for (packageName in packages) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(MATCH_UNINSTALLED_PACKAGES.toLong())
                    )
                } else {
                    pm.getPackageInfo(
                        packageName,
                        0
                    )
                }
                result = true
            } catch (e: PackageManager.NameNotFoundException) {
                return result
            }
        }
        return result
    }
}
