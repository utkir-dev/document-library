package com.tiptop.presentation.screens


import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.SystemClock
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shockwave.pdfium.BuildConfig
import com.tiptop.R
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.databinding.DialogAllertBinding
import com.tiptop.databinding.DialogConfirmBinding
import com.tiptop.databinding.PopupDeviceBinding
import java.io.File
import java.io.InputStream


abstract class BaseFragment(layoutId: Int) : Fragment(layoutId) {

    override fun onResume() {
        super.onResume()
        val timeLimit = if (isLoading) 60_000 else 3000
        if (System.currentTimeMillis() - timeOut > timeLimit && !isBlocked) {
            blockScreen()
        } else {
            isBlocked = false
            isLoading = false
        }
    }

     fun blockScreen() {
         isBlocked = true
         BlockScreenDialogFragment().show(
            parentFragmentManager,
            BlockScreenDialogFragment.TAG
        )
    }

    override fun onPause() {
        super.onPause()
        timeOut = System.currentTimeMillis()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun changeScreenOriantation() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
          //  activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
       // activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    fun showSnackBar(text: String) {
        val rootView: View = requireActivity().window.decorView.rootView
        Snackbar.make(rootView, text, Snackbar.LENGTH_LONG).show()
    }

    fun showSnackBarNoConnection() {
        val rootView: View = requireActivity().window.decorView.rootView
        Snackbar.make(rootView, "Interntet yo'q", Snackbar.LENGTH_LONG).show()
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


    companion object {
        private var timeOut: Long = 0
        private var isBlocked: Boolean = false
        var isLoading: Boolean = false
    }
}