package com.tiptop.presentation.screens.sign_in

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.tiptop.R
import com.tiptop.app.common.MyDevice
import com.tiptop.app.common.Status
import com.tiptop.app.common.hideKeyboard
import com.tiptop.app.common.isInternetAvailable
import com.tiptop.databinding.ScreenSignInBinding
import com.tiptop.presentation.MainActivity
import com.tiptop.presentation.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScreenSignIn : BaseFragment(R.layout.screen_sign_in) {
    private val vm by viewModels<SignInViewModelImpl>()
    private lateinit var binding: ScreenSignInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ScreenSignInBinding.inflate(layoutInflater, container, false)
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
                    showSnackBar("Xatolik sodir bo'ldi !")

                }

                Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    val mIntent = requireActivity().intent
                    requireActivity().finish()
                    startActivity(mIntent)
//
//                    (activity as MainActivity).initCurrentUser()
//                    val controller = findNavController()
//                    controller.navigate(route = "home") {
//                        popUpTo(controller.graph.id) {
//                            inclusive = true
//                        }
//                    }
                }

                Status.DEFAULT -> {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun setClickListeners() {
        binding.btnSignin.setOnClickListener {
            binding.btnSignin.hideKeyboard()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            if (!isInternetAvailable(requireContext())) {
                showSnackBar("Internet yo'q")
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                showSnackBar("Email kiritilmadi")
            } else if (!(email.contains("@") && email.contains("."))) {
                showSnackBar("Email noto'g'ri kiritildi")
            } else if (password.isEmpty()) {
                showSnackBar("Parol kiritilmadi")
            } else if (password.length < 6) {
                showSnackBar("Parol uzunligi 6 ta harfdan kam")
            } else {
                vm.signIn(email, password)
            }

        }
        binding.tvSignUp.setOnClickListener {
            binding.etEmail.setText("")
            binding.etPassword.setText("")
            findNavController().navigate(R.id.action_screenSignIn_to_screenSignUp)
        }
    }
}