package com.tiptop.presentation.screens

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.tiptop.R
import com.tiptop.app.common.Constants.KEY_SCREEN_BLOCK
import com.tiptop.app.common.SharedPrefSimple
import com.tiptop.app.common.Utils
import com.tiptop.app.common.encryption
import com.tiptop.databinding.ScreenPincodeBinding
import com.tiptop.presentation.MainActivity.Companion.IS_ENTERED
import com.tiptop.presentation.MainActivity.Companion.TEMPORARY_OUT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BlockScreenDialogFragment() : DialogFragment() {
    @Inject
    lateinit var shared: SharedPreferences
    private var code = ""
    private var countStar = 0
    private var countRegister = 0
    private var mask = ""
    private var imageClickSymbols = ""
    private var pass = ""
    private var b: ScreenPincodeBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = false
        b = ScreenPincodeBinding.inflate(layoutInflater)
        return b?.root
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPrefMask()
        initListeners()
    }

    private fun initListeners() {
        b!!.layoutMaskImage.setOnClickListener {
            mask = ""
        }
        b!!.ivMaskStar.setOnClickListener {
            countRegister = 0
            countStar++
            mask = "${mask}s${countStar}"
            if (mask == imageClickSymbols) {
                b!!.layoutMaskImage.visibility = View.GONE
            }
        }
        b!!.ivMaskRegister.setOnClickListener {
            countStar = 0
            countRegister++
            mask = "${mask}r${countRegister}"
            if (mask == imageClickSymbols) {
                b!!.layoutMaskImage.visibility = View.GONE
            }
        }

        b!!.card1.setOnClickListener { pass += "1";printCode() }
        b!!.card2.setOnClickListener { pass += "2";printCode() }
        b!!.card3.setOnClickListener { pass += "3";printCode() }
        b!!.card4.setOnClickListener { pass += "4";printCode() }
        b!!.card5.setOnClickListener { pass += "5";printCode() }
        b!!.card6.setOnClickListener { pass += "6";printCode() }
        b!!.card7.setOnClickListener { pass += "7";printCode() }
        b!!.card8.setOnClickListener { pass += "8";printCode() }
        b!!.card9.setOnClickListener { pass += "9";printCode() }
        b!!.card0.setOnClickListener { pass += "0";printCode() }
        b!!.cardOk.setOnClickListener { check() }
        b!!.cardClear.setOnClickListener {
            if (pass.length > 1) {
                pass = pass.substring(0, pass.length - 1)
            } else {
                pass = ""
            }
            printCode()
        }
    }

    private fun printCode() {
        b!!.tvEnteredPass.text = pass
    }

    private fun check() {
        val actialCode = shared.getString(
            Utils().getBlockCodeKey(),
            Utils().getDefaultBlockCode().encryption()
        ) ?: Utils().getDefaultBlockCode().encryption()

        if (pass.encryption() == actialCode) {
            IS_ENTERED = true
            TEMPORARY_OUT = false
            SharedPrefSimple(requireContext()).saveBoolean(KEY_SCREEN_BLOCK, false)
            dismiss()
        }
    }

    fun initPrefMask() {
        val count1 = shared.getInt(Utils().getBlockSpinnerKey1(), 0) ?: 0
        val count2 = shared.getInt(Utils().getBlockSpinnerKey2(), 0) ?: 0
        val count3 = shared.getInt(Utils().getBlockSpinnerKey3(), 0) ?: 0
        if (count1 != 0) {
            for (i in 1..count1) {
                imageClickSymbols = "${imageClickSymbols}s${i}"
            }
        }
        if (count2 != 0) {
            for (r in 1..count2) {
                imageClickSymbols = "${imageClickSymbols}r${r}"
            }
        }

        if (count3 != 0) {
            for (k in 1..count3) {
                imageClickSymbols = "${imageClickSymbols}s${k}"
            }
        }
        if (imageClickSymbols == "") {
            imageClickSymbols = Utils().getDefaultBlockImageCode()
        }
    }

    companion object {
        val TAG = "BlockScreenDialogFragment"
    }
}