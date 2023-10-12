package com.tiptop.presentation.screens.home.my_dictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.tiptop.R
import com.tiptop.databinding.ScreenUserAllDictionaryBinding
import com.tiptop.presentation.screens.BaseFragment
import com.tiptop.presentation.screens.document_view.pdf.Dictionary
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ScreenUserDictionary : BaseFragment(R.layout.screen_user_all_dictionary) {

    private lateinit var binding: ScreenUserAllDictionaryBinding
    private val vm by viewModels<DictViewModelImpl>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ScreenUserAllDictionaryBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserves()
    }

    private fun initObserves() {
        val adapter = DictAdapter(listener = object : DictAdapter.PageNumberClickListener {
            override fun onClick(word: Dictionary) {
                showWordDialog(word) { modifiedWord ->
                  //  vm.updateBaseWord(modifiedWord)
                }
            }
        })
        binding.rvDictAll.adapter = adapter.withLoadStateHeader(
            DictLoadStateAdapter()
        )

        lifecycleScope.launch {
            vm.dictionaryUser.collectLatest { words ->
                adapter.submitData(words)
            }
        }
    }
}