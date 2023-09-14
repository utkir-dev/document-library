package com.tiptop.presentation.screens.settings

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.tiptop.R
import com.tiptop.app.common.Utils
import com.tiptop.app.common.encrypt
import com.tiptop.app.common.hideKeyboard
import com.tiptop.databinding.ScreenSettingsBinding
import com.tiptop.presentation.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ScreenSettings : BaseFragment(R.layout.screen_settings) {
    @Inject
    lateinit var shared: SharedPreferences
    private var _binding: ScreenSettingsBinding? = null
    private var valueSpinner1: Int? = null
    private var valueSpinner2: Int? = null
    private var valueSpinner3: Int? = null
    private val b get() = _binding!!
    private val list_of_spinner = arrayOf(
        " 1 ",
        " 2 ",
        " 3 ",
        " 4 ",
        " 5 "
    )
    private val list_of_spinner1 = arrayOf(
        " 0 ",
        " 1 ",
        " 2 ",
        " 3 ",
        " 4 ",
        " 5 "
    )

    @SuppressLint("CommitPrefEdits")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ScreenSettingsBinding.inflate(inflater, container, false)
        b.btnConfirm.setOnClickListener {
            b.btnConfirm.hideKeyboard()
            val passCurrent = b.etKodeCurrent.text.toString().trim()
            val pass1 = b.etKode1.text.toString().trim()
            val pass2 = b.etKode2.text.toString().trim()
            if (passCurrent.isBlank()) return@setOnClickListener
            if (pass1.isBlank()) return@setOnClickListener
            if (pass2.isBlank()) return@setOnClickListener


            val actialCode = shared.getString(
                Utils().getBlockCodeKey(),
                Utils().getDefaultBlockCode().encrypt()
            ) ?: Utils().getDefaultBlockCode().encrypt()

            if (passCurrent.encrypt() != actialCode) {
                showSnackBar("Hozirgi kod noto'g'ri kiritildi")
            } else if (pass1 != pass2) {
                showSnackBar("Kodlar mos kelmadi")
            } else if (pass1 == pass2) {
                shared.edit().putString(Utils().getBlockCodeKey(), pass1.encrypt()).apply()
                showSnackBar("Kod yangilandi")
            }
        }
// spinner1

        val orderMask = shared.getInt(Utils().getOrderMaskKey(), 0) ?: 0
        val n = if (orderMask != 0) {
            orderMask
        } else {
            1
        }
        when (n) {
            1 -> {
                b.star1.setImageResource(R.drawable.star1)
                b.star2.setImageResource(R.drawable.star1)
            }

            2 -> {
                b.star1.setImageResource(R.drawable.star2)
                b.star2.setImageResource(R.drawable.star2)
            }

            3 -> {
                b.star1.setImageResource(R.drawable.star3)
                b.star2.setImageResource(R.drawable.star3)
            }

            4 -> {
                b.star1.setImageResource(R.drawable.star4_cloud)
                b.star2.setImageResource(R.drawable.star4_cloud)
            }

            5 -> {
                b.star1.setImageResource(R.drawable.star5)
                b.star2.setImageResource(R.drawable.star5)
            }

            6 -> {
                b.star1.setImageResource(R.drawable.star6)
                b.star2.setImageResource(R.drawable.star6)
            }

            7 -> {
                b.star1.setImageResource(R.drawable.star7)
                b.star2.setImageResource(R.drawable.star7)
            }

            8 -> {
                b.star1.setImageResource(R.drawable.star8)
                b.star2.setImageResource(R.drawable.star8)
            }

            9 -> {
                b.star1.setImageResource(R.drawable.star9)
                b.star2.setImageResource(R.drawable.star9)
            }

            10 -> {
                b.star1.setImageResource(R.drawable.star10)
                b.star2.setImageResource(R.drawable.star10)
            }
        }

        b.spinner1.adapter = ArrayAdapter(
            requireContext(),
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            list_of_spinner
        )
        var coun1 = shared.getInt(Utils().getBlockSpinnerKey1(), 0) ?: 0
        if (coun1 == 0) coun1 = 3
        b.spinner1.setSelection(coun1 - 1)
        b.spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                valueSpinner1 = list_of_spinner[position].trim().toInt()
            }
        }

        // spinner2
        b.spinner2.adapter = ArrayAdapter(
            requireContext(),
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            list_of_spinner
        )
        var coun2 = shared.getInt(Utils().getBlockSpinnerKey2(), 0) ?: 0
        if (coun2 == 0) coun2 = 2
        b.spinner2.setSelection(coun2 - 1)
        b.spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                valueSpinner2 = list_of_spinner[position].trim().toInt()
            }
        }

        // spinner3
        b.spinner3.adapter = ArrayAdapter(
            requireContext(),
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            list_of_spinner1
        )
        b.spinner3.setSelection(shared.getInt(Utils().getBlockSpinnerKey3(), 0) ?: 0)
        b.spinner3.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                valueSpinner3 = list_of_spinner1[position].trim().toInt()
            }
        }

        b.btnConfirm2.setOnClickListener {
            if (valueSpinner1 != null && valueSpinner2 != null) {
                shared.edit().putInt(Utils().getBlockSpinnerKey1(), valueSpinner1!!).apply()
                shared.edit().putInt(Utils().getBlockSpinnerKey2(), valueSpinner2!!).apply()
                valueSpinner3?.let {
                    shared.edit().putInt(Utils().getBlockSpinnerKey3(), valueSpinner3!!).apply()
                }
                showSnackBar("Kod yangilandi")
            }
        }
        return b.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}