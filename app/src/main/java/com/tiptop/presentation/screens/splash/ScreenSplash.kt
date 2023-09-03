package com.tiptop.presentation.screens.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.tiptop.R
import com.tiptop.databinding.ScreenPincodeBinding
import com.tiptop.presentation.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScreenSplash : BaseFragment(R.layout.screen_pincode) {
    private val vm by viewModels<SplashViewModelIml>()
    private lateinit var binding: ScreenPincodeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ScreenPincodeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        var head: ByteArray? = null
//        var body: ByteArray? = null
//        var bytes: ByteArray? = null
//
//        try {
//            bytes = requireActivity().assets.open("testPdf_100mb.pdf").readBytes()
//        } catch (e: OutOfMemoryError) {
//            Log.d("pdf", "Fayl hajmi kattaligi uchun ochib bo'lmadi")
//        }
//
//        head = bytes?.copyOfRange(0, 387)
//        body = bytes?.copyOfRange(387, bytes!!.size)
//        val headString = String(head!!)
//        var headStringBytes = headString.toByteArray(Charsets.UTF_8)
//
//        var middleByte = body!!.copyOfRange(0, 1)
//        ScreenHome.fileBytes = middleByte.plus(body)


        vm.state.observe(viewLifecycleOwner) { it ->
            val controller = findNavController()
            if (it == 0) {
                controller.navigate(route = "signIn") {
                    popUpTo(controller.graph.id) {
                        inclusive = true
                    }
                }
              //  findNavController().navigate(R.id.action_screenSplash_to_screenSignIn)
            } else if (it == 1) {
                controller.navigate(route = "home") {
                    popUpTo(controller.graph.id) {
                        inclusive = true
                    }
                }
               // findNavController().navigate(R.id.action_screenSplash_to_screenHome)
            }
        }
    }
}