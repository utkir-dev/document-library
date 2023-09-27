package com.tiptop.presentation.screens.document_view.pdf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.tiptop.R
import com.tiptop.data.models.local.ArabUzBase
import com.tiptop.data.models.local.ArabUzUser
import com.tiptop.data.models.local.UzArabBase
import com.tiptop.databinding.DialogWordBinding


class DialogWord(
    val word: Dictionary,
    val vm: PdfViewModelIml,
    val currentPage: Int,
    val documentId: String
) : DialogFragment() {
    private var b: DialogWordBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = true
        b = DialogWordBinding.inflate(layoutInflater)
        return b?.root
    }

    override fun getTheme(): Int {
        return R.style.MyDialogStyle
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (word is ArabUzBase) {
            b?.tvSearchedWord?.text = word.c0arab
            b?.tvRus?.text = word.c3rus
            b?.tvUz?.text = word.c2uzbek
            if (word.saved) {
                b?.ivSavedWord?.setImageResource(R.drawable.star1)
            } else {
                b?.ivSavedWord?.setImageResource(R.drawable.ic_star_unselected)
            }
            b?.ivSavedWord?.setOnClickListener {
                if (word.saved) {
                    word.saved = false
                    b?.ivSavedWord?.setImageResource(R.drawable.ic_star_unselected)
                } else {
                    word.saved = true
                    b?.ivSavedWord?.setImageResource(R.drawable.star1)
                }
                vm.updateBaseWord(word, currentPage, documentId)
                dismiss()
            }
        } else if (word is ArabUzUser) {
            b?.tvSearchedWord?.text = word.c0arab
            b?.tvRus?.text = word.c3rus
            b?.tvUz?.text = word.c2uzbek
            b?.ivSavedWord?.setImageResource(R.drawable.ic_delete)
            b?.ivSavedWord?.setOnClickListener {
                vm.updateBaseWord(word, currentPage, documentId)
                dismiss()
            }
        } else if (word is UzArabBase) {
            b?.tvSearchedWord?.text = word.c0uzbek
            b?.tvUzTitle?.text = "arabchasi"
            b?.tvUz?.text = word.c1arab
            b?.tvRusTitle?.visibility = View.GONE
            b?.tvRus?.visibility = View.GONE
        }
        b?.btnOk?.setOnClickListener {
            dismiss()
        }
    }


    companion object {
        var TAG = "FragmentDialogWord"
    }

}
