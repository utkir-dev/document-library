package com.tiptop.presentation.screens.sign_up

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.tiptop.R
import com.tiptop.app.common.MyDevice
import com.tiptop.app.common.Status
import com.tiptop.app.common.isInternetAvailable
import com.tiptop.databinding.ScreenPincodeBinding
import com.tiptop.databinding.ScreenSignInBinding
import com.tiptop.databinding.ScreenSignUpBinding
import com.tiptop.presentation.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScreenSignUp : BaseFragment(R.layout.screen_sign_up) {

    private val vm by viewModels<SignUpViewModelImpl>()
    private lateinit var binding: ScreenSignUpBinding
    private var email = ""
    var password1 = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ScreenSignUpBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.setDeviceId(MyDevice(requireContext()).getUniquieId())
        vm.setDeviceIsTablet(resources.getBoolean(R.bool.is_tablet))
        setClickListeners()
        setObservers()
    }

    private fun setObservers() {
        vm.result.observe(viewLifecycleOwner) {

            when (it.status) {
                Status.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                Status.ERROR -> {
                    binding.progressBar.visibility = View.GONE
                    showSnackBar("Xatolik sodir bo'ldi!")
                }

                Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    showSnackBar("Ro'yxatdan muvaffaqiyatli o'tdingiz!")

                    showAllertDialog(
                        "☝️  Diqqat !",
                        "email: $email\nparol: $password1\nBularni esdan chiqarmang! Akkauntga shular orqali kirasiz"
                    ) {
                        findNavController().popBackStack()
                    }

                }

                Status.DEFAULT -> {
                    binding.progressBar.visibility = View.GONE
                }
            }

        }
    }

    private fun setClickListeners() {
        binding.btnSignup.setOnClickListener {
            if (!isInternetAvailable(requireContext())) {
                showSnackBar("Internet yo'q")
                return@setOnClickListener
            }
            email = binding.etEmail.text.toString().trim()
            val telegramUser = binding.etTelegramUser.text.toString().trim()
            password1 = binding.etPassword1.text.toString().trim()
            val password2 = binding.etPassword2.text.toString().trim()
            if (email.isEmpty()) {
                showSnackBar("Email kiritilmadi")
            } else if (!(email.contains("@") && email.contains("."))) {
                showSnackBar("Email noto'g'ri kiritildi")
            } else if (password1.isEmpty()) {
                showSnackBar("Parol kiritilmadi")
            } else if (password1.length < 6) {
                showSnackBar("Parol uzunligi 6 ta harfdan kam")
            } else if (password2.isEmpty()) {
                showSnackBar("Parol takror kiritilmadi")
            } else if (password1 != password2) {
                showSnackBar("Parollar mos kelmadi")
            } else {
                vm.signUp(email, password1, telegramUser)
            }
        }
        binding.cardBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

}