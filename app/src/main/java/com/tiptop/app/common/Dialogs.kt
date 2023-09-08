package com.tiptop.app.common

import android.app.AlertDialog
import android.graphics.Color
import androidx.fragment.app.Fragment
import com.tiptop.R
import com.tiptop.databinding.DialogAddEditNameBinding

fun showEditNameDialog(
    fragment: Fragment,
    title: String,
    name: String = "",
    function: (String) -> Unit
) {
    val v = DialogAddEditNameBinding.inflate(fragment.layoutInflater)
   // v.root.setBackgroundColor(Color.TRANSPARENT)
    val dialog = AlertDialog.Builder(fragment.requireContext(), R.style.MyDialogStyle).apply {
        setView(v.root)
            .setCancelable(true)
            .create()
    }
    val alert = dialog.show()
    v.title.text = title
    if (name.isNotEmpty()) {
        v.etDocumentName.setText(name)
    }
    v.btnConfirm.setOnClickListener {
        function(v.etDocumentName.text.toString().trim())
        v.etDocumentName.hideKeyboard()
        alert.cancel()
    }
    v.btnCancel.setOnClickListener {
        v.etDocumentName.hideKeyboard()
        alert.cancel()
    }
}