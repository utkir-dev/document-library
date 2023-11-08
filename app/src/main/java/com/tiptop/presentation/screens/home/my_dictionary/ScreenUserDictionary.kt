package com.tiptop.presentation.screens.home.my_dictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.tiptop.R
import com.tiptop.data.models.local.ArabUzUser
import com.tiptop.data.models.local.ArabUzUserForDictionaryScreen
import com.tiptop.databinding.ScreenUserAllDictionaryBinding
import com.tiptop.presentation.screens.BaseFragment
import com.tiptop.presentation.screens.document_view.pdf.Dictionary
import com.tiptop.presentation.screens.document_view.pdf.ScreenPdfView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ScreenUserDictionary : BaseFragment(R.layout.screen_user_all_dictionary) {

    private lateinit var binding: ScreenUserAllDictionaryBinding
    private val vm by viewModels<DictViewModelImpl>()
    private var adapter: DictAdapter? = null
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
        binding.cardBackMyDict.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun initObserves() {
        adapter = DictAdapter(listener = object : DictAdapter.PageNumberClickListener {
            override fun onClick(word: Dictionary) {
                showWordDialog(word) { modifiedWord ->
                    //  vm.updateBaseWord(modifiedWord)
                }
            }

            override fun onPageClick(word: ArabUzUserForDictionaryScreen) {
                ScreenPdfView.word = word
                findNavController().navigate(R.id.action_screenUserDictionary_to_screenDocument)

            }

            override fun onDeleteClick(word: Dictionary, position: Int) {
                showConfirmDialog(
                    title = (word as ArabUzUserForDictionaryScreen).c0arab,
                    message = "Ushbu so'zni shaxsiy lug'atingizdan chiqarishga ishonchingiz komilmi ?"
                ) {
                    if (it) {
                        vm.deleteWord(word)
                        notifyItemDelete(position)
                        //  findNavController().popBackStack()
                    }
                }
            }
        })
        binding.rvDictAll.adapter = adapter?.withLoadStateFooter(
            DictLoadStateAdapter()
        )

        lifecycleScope.launch {
            vm.dictionaryUser.collectLatest { words ->
                adapter?.submitData(words)
            }
        }
    }

    private fun notifyItemDelete(position: Int) {
        adapter?.updateItem(position)
    }
}