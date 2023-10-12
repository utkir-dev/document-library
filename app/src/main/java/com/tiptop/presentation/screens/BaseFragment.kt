package com.tiptop.presentation.screens


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.tiptop.R
import com.tiptop.app.common.SharedPrefSimple
import com.tiptop.data.models.local.ArabUzBase
import com.tiptop.data.models.local.ArabUzUser
import com.tiptop.data.models.local.ArabUzUserForDictionaryScreen
import com.tiptop.data.models.local.UzArabBase
import com.tiptop.databinding.DialogAllertBinding
import com.tiptop.databinding.DialogConfirmBinding
import com.tiptop.databinding.DialogWordBinding
import com.tiptop.databinding.PopupDeviceBinding
import com.tiptop.presentation.screens.document_view.pdf.Dictionary


abstract class BaseFragment(layoutId: Int) : Fragment(layoutId) {

    private var shared: SharedPrefSimple? = null

    @SuppressLint("SourceLockedOrientationActivity")
    fun changeScreenOriantation() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //  activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    fun showSnackBar(text: String) {
        val rootView: View = requireActivity().window.decorView.rootView
        Snackbar.make(rootView, text, Snackbar.LENGTH_LONG).show()
    }

    fun showSnackBarNoConnection() {
        val rootView: View = requireActivity().window.decorView.rootView
        Snackbar.make(rootView, "Interntet yo'q", Snackbar.LENGTH_LONG).show()
    }
    fun showWordDialog(word: Dictionary, function: (Dictionary) -> Unit) {
        val b = DialogWordBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext(), R.style.MyDialogStyle).apply {
            setView(b.root)
                .setCancelable(true)
                .create()
        }
        val allert = dialog.show()
        if (word is ArabUzBase) {
            b.tvSearchedWord.text = word.c0arab
            b.tvRus.text = word.c3rus
            b.tvUz.text = word.c2uzbek
            if (word.saved) {
                b.ivSavedWord.setImageResource(R.drawable.star1)
            } else {
                b.ivSavedWord.setImageResource(R.drawable.ic_star_unselected)
            }
            b.ivSavedWord.setOnClickListener {
                function(word.copy(saved = !word.saved))
                allert.cancel()
            }
        } else if (word is ArabUzUser) {
            b.tvSearchedWord.text = word.c0arab
            b.tvRus.text = word.c3rus
            b.tvUz.text = word.c2uzbek
            b.ivSavedWord.setImageResource(R.drawable.ic_delete)
            b.ivSavedWord.setOnClickListener {
                function(word)
                allert.cancel()
            }
        }else if (word is ArabUzUserForDictionaryScreen) {
            b.tvSearchedWord.text = word.c0arab
            b.tvRus.text = word.c3rus
            b.tvUz.text = word.c2uzbek
            b.ivSavedWord.visibility = View.GONE
        } else if (word is UzArabBase) {
            b.tvSearchedWord.text = word.c0uzbek
            b.tvUzTitle.text = "arabchasi"
            b.tvUz.text = word.c1arab
            b.tvRusTitle.visibility = View.GONE
            b.tvRus.visibility = View.GONE
        }

        b.btnOk.setOnClickListener {
            allert.cancel()
        }
    }

    @SuppressLint("InflateParams")
    fun showPopup(v: View, root: ViewGroup, function: (Int) -> Unit) {
        val view = PopupDeviceBinding.inflate(layoutInflater)
        val popupRight = PopupWindow(
            view.root, // Custom view to show in popup window
            LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT // Window height
        )
        view.tvDeleteDevice.setOnClickListener {
            popupRight.dismiss()
            function(view.tvDeleteDevice.id)
        }
        view.tvEditDevice.setOnClickListener {
            popupRight.dismiss()
            function(view.tvEditDevice.id)
        }
        view.tvInfoDevice.setOnClickListener {
            popupRight.dismiss()
            function(view.tvInfoDevice.id)
        }

        popupRight.isOutsideTouchable = true
        popupRight.elevation = 20.0F
        val slideIn = Slide()
        slideIn.slideEdge = Gravity.END
        popupRight.enterTransition = slideIn

        val slideOut = Slide()
        slideOut.slideEdge = Gravity.END
        popupRight.exitTransition = slideOut

        TransitionManager.beginDelayedTransition(root)
        val location = IntArray(2)
        v.getLocationOnScreen(location)
        popupRight.showAtLocation(
            root, Gravity.NO_GRAVITY, // root, Gravity.NO_GRAVITY,
            location[0] + v.measuredWidth,
            location[1] + v.measuredHeight
        )
    }

    fun showConfirmDialog(title: String, message: String, response: (Boolean) -> Unit) {
        val view = DialogConfirmBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext(), R.style.MyDialogStyle).apply {
            setView(view.root)
                .setCancelable(true)
                .create()
        }
        val allert = dialog.show()
        view.tvTitle.text = title
        view.tvMessage.text = message
        view.btnConfirm.setOnClickListener { response(true);allert.cancel() }
        view.btnCancel.setOnClickListener { response(false);allert.cancel() }
    }

    fun showAllertDialog(title: String = "", message: String = "", function: () -> Unit) {
        val view = DialogAllertBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext(), R.style.MyDialogStyle).apply {
            setView(view.root)
                .setCancelable(true)
                .create()
        }
        val allert = dialog.show()
        view.tvTitle.text = title
        view.tvMessage.text = message
        if (message.isEmpty()) {
            view.tvMessage.visibility = View.GONE
        }
        view.btnOk.setOnClickListener {
            function()
            allert.cancel()
        }
    }

    fun requestPermissions(vararg permissions: String, function: (Boolean) -> Unit) {
        Dexter.withContext(requireContext())
            .withPermissions(
                *permissions
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
                            function(true)
                        } else {
                            showSettingsDialog(*permissions)
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .check()
    }

    private fun showSettingsDialog(vararg permissions: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Ruxsat kerak !")
        builder.setMessage("${getPermissionNames(*permissions)}larga ruxsat talab etiladi!")
        builder.setPositiveButton("Sozlamalar") { dialog, _ ->
            dialog.cancel()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireActivity().packageName, null)
            intent.data = uri
            startActivityForResult(intent, 101)
        }
        builder.setNegativeButton("Bekor") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun getPermissionNames(vararg permissions: String): String {
        var names = permissions.toString()
        permissions.forEach {
            when (it) {
                Manifest.permission.CAMERA -> names = "kamera, $names"
                Manifest.permission.REQUEST_INSTALL_PACKAGES -> names = "dastur o'rnatish, $names"
                Manifest.permission.READ_EXTERNAL_STORAGE -> names = "xotiradan o'qish, $names"
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> names = "xotiraga yozish, $names"
                Manifest.permission.ACCESS_COARSE_LOCATION -> names = "geo lokaciya, $names"
                Manifest.permission.ACCESS_FINE_LOCATION -> names = "geo lokaciya, $names"
            }
        }
        return if (names.length > 2) names.trim().substring(0, names.length - 2) else ""
    }

}