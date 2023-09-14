package com.tiptop.presentation.screens.document_view.text

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.tiptop.R
import com.tiptop.databinding.ScreenTextViewBinding
import com.tiptop.presentation.screens.BaseFragment
import com.tiptop.presentation.screens.document_view.DocumentViewModelIml
import com.tiptop.presentation.screens.document_view.pdf.ScreenPdfView
import dagger.hilt.android.AndroidEntryPoint


const val ARG_PARAM_TEXT = "arg_param_text"

@AndroidEntryPoint
class ScreenTextView : BaseFragment(R.layout.screen_text_view) {
//    private var popup: PopupWindow? = null
//    private var searchingText = ""
//    private val TAG = "FragmentPdf"
//    private var booklist = ArrayList<DocumentLocal>()
//    private var adapterDict: AdapterDict? = null
//    private var strLastBooks = ""

    private val vm by viewModels<DocumentViewModelIml>()
    private var _binding: ScreenTextViewBinding? = null
    private val binding get() = _binding!!
//    private var pageNumber = 0
//    private var boo = true
//    private var nightMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val id = it.getString(ARG_PARAM_TEXT) ?: ""
            if (id.isNotEmpty()) {
                vm.setDocument(id)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ScreenTextViewBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showText()
    }

    private fun showText() {
        vm.documentBytes.observe(viewLifecycleOwner) { bytes ->

        }

    }

    //
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        _binding = ScreenPdfViewBinding.inflate(inflater, container, false)
//        val view = b.root
//        APP.supportActionBar?.show()
//        BOOK1?.let {
//            openPDF()
//            initListeners()
//        }
//        return view
//    }
//
//
//    @SuppressLint("ClickableViewAccessibility")
//    private fun initListeners() {
//        b.pdfViewFargment.setOnClickListener {
//            APP.vm.updateTimer()
//            if (b.lLastBooks.visibility == View.VISIBLE) {
//                b.lLastBooks.visibility = View.GONE
//            }
//
//            if (boo) {
//                boo = false
//                b.lTop.visibility = View.VISIBLE
//                b.lBottom.visibility = View.VISIBLE
//
//            } else {
//                b.lTop.visibility = View.GONE
//                b.lBottom.visibility = View.GONE
//                boo = true
//            }
//        }
//        // top
//        b.ivBack.setOnClickListener { findNavController().popBackStack() }
//        b.ivLastBooks.setOnClickListener {
//            booklist.clear()
//            if (strLastBooks.isNotEmpty()) {
//                strLastBooks.split("#").forEach {
//                    if (it.isNotEmpty()) {
//                        booklist.add(DB_LOCAL.daoBook().getBookById(it))
//                    }
//                }
//            } else {
//                booklist.addAll(DB_LOCAL.daoBook().getLoadedBooks())
//            }
//            b.lLastBooks.visibility = View.VISIBLE
//            b.rvLastBooks.adapter =
//                AdapterOpenBooks(booklist,
//                    object : AdapterOpenBooks.PageNumberClickListener {
//                        override fun onClick(book: Book) {
//                            BOOK1 = book
//                            b.lLastBooks.visibility = View.GONE
//                            openPDF()
//                        }
//                    })
//            b.rvLastBooks.addItemDecoration(
//                DividerItemDecoration(
//                    APP,
//                    LinearLayoutManager.VERTICAL
//                )
//            )
//        }
//        b.tvCancel.setOnClickListener {
//            b.lLastBooks.visibility = View.GONE
//        }
//        b.cardBushro.setOnClickListener {
//            showBushro()
//        }
//        b.cardJavohir.setOnClickListener {
//            showJavohir()
//        }
//        b.ivScreenOrientation.setOnClickListener {
//            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
//            } else {
//                b.etGotoPage
//                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//            }
//        }
//
//        // bottom
//
//        b.tvUp10.setOnClickListener {
//            b.pdfViewFargment.jumpTo(b.pdfViewFargment.currentPage - 10)
//        }
//        b.tvDown10.setOnClickListener {
//            b.pdfViewFargment.jumpTo(b.pdfViewFargment.currentPage + 10)
//        }
//        b.ivShare.setOnClickListener {
//            share(BOOK1!!)
//        }
//        b.ivDayNight.setOnClickListener {
//            nightMode = !nightMode
//            b.pdfViewFargment.setNightMode(nightMode)
//            if (nightMode) {
//                b.ivDayNight.setImageResource(R.drawable.ic_sun_yellow)
//            } else {
//                b.ivDayNight.setImageResource(R.drawable.ic_moon)
//            }
//            b.pdfViewFargment.loadPages()
//        }
//        b.etGotoPage.setOnClickListener {
//            showPopupSelectPage(it)
//        }
//        b.cardGotoPage.setOnClickListener {
//            showPopupSelectPage(it)
//        }
//        b.ivTimer.setOnClickListener {
//            showTimer()
//        }
//    }
//
//    private fun showTimer() {
//        var currentTimer = PREF.getInt(KEY_SCREEN_OPEN_TIME) ?: 15
//        val viewDialog = DialogMakTimerBinding.inflate(layoutInflater)
//        val dialog = AlertDialog.Builder(APP)
//            .setCancelable(true)
//            .setView(viewDialog.root)
//        alert = dialog.create()
//        alert?.show()
//        when (currentTimer) {
//            0, 15 -> {
//                viewDialog.rg.check(viewDialog.rb15.id)
//            }
//            30 -> {
//                viewDialog.rg.check(viewDialog.rb30.id)
//                //viewDialog.rb30.isChecked = true
//            }
//            60 -> {
//                viewDialog.rg.check(viewDialog.rb1.id)
//                //viewDialog.rb1.isChecked = true
//            }
//            120 -> {
//
//                viewDialog.rg.check(viewDialog.rb2.id)
//            }
//            180 -> {
//                viewDialog.rg.check(viewDialog.rb3.id)
//            }
//            240 -> {
//                viewDialog.rg.check(viewDialog.rb4.id)
//            }
//            300 -> {
//                viewDialog.rg.check(viewDialog.rb5.id)
//            }
//        }
//        viewDialog.rg.setOnCheckedChangeListener { radioGroup, checkedId ->
//            when (checkedId) {
//                viewDialog.rb15.id -> {
//                    currentTimer = 15
//                }
//                viewDialog.rb3.id -> {
//                    currentTimer = 30
//                }
//                viewDialog.rb1.id -> {
//                    currentTimer = 60
//                }
//                viewDialog.rb2.id -> {
//                    currentTimer = 120
//                }
//                viewDialog.rb3.id -> {
//                    currentTimer = 180
//                }
//                viewDialog.rb4.id -> {
//                    currentTimer = 240
//                }
//                viewDialog.rb5.id -> {
//                    currentTimer = 300
//                }
//            }
//
//        }
//        viewDialog.btnConfirmTimer.setOnClickListener {
//            PREF.setInt(KEY_SCREEN_OPEN_TIME, currentTimer)
//            APP.vm.updateTimer(currentTimer)
//            alert?.cancel()
//        }
//    }
//
//    private fun showBushro() {
//        val launchIntent =
//            APP.packageManager.getLaunchIntentForPackage("com.samples.bushro")
//        if (launchIntent != null) {
//            TEMPORARY_OUT = true
//            startActivity(launchIntent)
//        } else {
//            "'Bushro' ilovasi topilmadi, uni tortib o'rnating".toToast()
//        }
//    }
//
//    @SuppressLint("SetTextI18n")
//    private fun showJavohir() {
//        val list = DB_DIC.daoAruz().getAruzList()
//        adapterDict =
//            AdapterDict(list, searchingText, object : AdapterDict.PageNumberClickListener {
//                override fun onClick(word: Dictionary) {
//                    val fr = FragmentDialogWord(word)
//                    fr.isCancelable = true
//                    fr.show(APP.supportFragmentManager, FragmentDialogWord.TAG)
//
//                }
//            })
//        b.lDict.ivChange.setOnClickListener {
//            modeAr = !modeAr
//            b.lDict.etSearchWord.setText("")
//            if (modeAr) {
//                b.lDict.tvAr.text = "Arabcha"
//                b.lDict.tvUz.text = "O'zbekcha"
//            } else {
//                b.lDict.tvAr.text = "O'zbekcha"
//                b.lDict.tvUz.text = "Arabcha"
//            }
//        }
//        b.lDict.btnClose.setOnClickListener {
//            b.lDict.lDictionary.visibility = View.GONE
//        }
//        b.lDict.lDictionary.visibility = View.VISIBLE
//        b.lDict.rvDict.adapter = adapterDict
//
//        b.lDict.rvDict.addItemDecoration(
//            DividerItemDecoration(
//                APP,
//                LinearLayoutManager.VERTICAL
//            )
//        )
//        b.lDict.etSearchWord.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//
//            }
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                searchingText = p0.toString()
//                if (searchingText.isNotEmpty()) {
//                    if (modeAr) {
//                        val searchText = "$searchingText%"
//                        val list2 = DB_DIC.daoAruz().searchWords(searchText)
//                        adapterDict =
//                            AdapterDict(
//                                list2,
//                                searchingText,
//                                object : AdapterDict.PageNumberClickListener {
//                                    override fun onClick(word: Dictionary) {
//                                        val fr = FragmentDialogWord(word)
//                                        fr.isCancelable = true
//                                        fr.show(APP.supportFragmentManager, FragmentDialogWord.TAG)
//                                    }
//                                })
//                    } else {
//                        val searchText = "$searchingText%"
//                        val list3 = DB_DIC.daoUzar().searchWords(searchText)
//                        adapterDict =
//                            AdapterDict(
//                                list3,
//                                searchingText,
//                                object : AdapterDict.PageNumberClickListener {
//                                    override fun onClick(word: Dictionary) {
//                                        val fr = FragmentDialogWord(word)
//                                        fr.isCancelable = true
//                                        fr.show(APP.supportFragmentManager, FragmentDialogWord.TAG)
//                                    }
//                                })
//                    }
//                    b.lDict.rvDict.adapter = adapterDict
//                } else {
//                    b.lDict.etSearchWord.hideKeyboard()
//                }
//
//                b.lDict.rvDict.addItemDecoration(
//                    DividerItemDecoration(
//                        APP,
//                        LinearLayoutManager.VERTICAL
//                    )
//                )
//            }
//
//            override fun afterTextChanged(p0: Editable?) {
//
//            }
//
//        })
////-----------------------------------------------------------------
////        val launchIntent =
////            APP.packageManager.getLaunchIntentForPackage("uz.arabic.dictionary")
////        if (launchIntent != null) {
////            TEMPORARY_OUT=true
////            APP.vm.updateTimer(5)
////            startActivity(launchIntent)
////        } else {
////            "'Javohir' ilovasi topilmadi, uni tortib o'rnating".toToast()
////        }
//    }
//
//    private fun openPDF() {
//        val bookName = BOOK1!!.name.replace(".pdf".toRegex(), "")
//
//        strLastBooks = PREF.getString(KEY_JSON_LAST_BOOKS) ?: ""
//        strLastBooks = strLastBooks.replace("${BOOK1!!.id}#".toRegex(), "")
//        strLastBooks = "${BOOK1!!.id}#$strLastBooks"
//        PREF.setString(KEY_JSON_LAST_BOOKS, strLastBooks)
//
//        b.tvBookName.text = bookName
//        APP.vm.updateTimer()
//
//        val file: File? = APP.getFileStreamPath(BOOK1!!.id)
//        val bytes = file?.readBytes()
//        val decodedBytes = EncryptorKtx().getDecryptedBytes(
//            BOOK1!!.keyStr,
//            BOOK1!!.specStr,
//            bytes as ByteArray
//        )
//        PREF.setString(KEY_JSON_LAST_BOOK, Gson().toJson(BOOK1))
//        pageNumber = PREF.getInt(BOOK1!!.id) ?: 0
//        setPageCount(pageNumber, "open")
//
//        b.pdfViewFargment
//            .fromBytes(decodedBytes)
//            .defaultPage(pageNumber)
//            .enableSwipe(true)
//            .swipeHorizontal(false)
//            .enableDoubletap(true)
//            .enableAntialiasing(true)
//            .enableAnnotationRendering(false)
//            .spacing(1)
//            .nightMode(false)
//            .onPageChange { page, pageCount ->
//                pageNumber = if (page == 0) {
//                    pageNumber
//                } else {
//                    page
//                }
//                setPageCount(pageNumber, "onPageChange")
//                PREF.setInt(BOOK1!!.id!!, pageNumber)
//                APP.vm.updateTimer()
//            }
//            .pageFitPolicy(FitPolicy.WIDTH)
//            .scrollHandle(DefaultScrollHandle(requireContext()))
//            .onError(OnErrorListener {
//                "Xatolik, fayl ochilmadi !".toToast()
//            }
//            ).load()
//
//    }
//
//
//    @SuppressLint("SetTextI18n")
//    private fun setPageCount(num: Int, dest: String = "") {
//        b.tvPageCount.text = "${num + 1}/${b.pdfViewFargment.pageCount}"
//        b.etGotoPage.text = "${num + 1}"
//    }
//
//    private fun showPopupSelectPage(v: View) {
//        val viewPop = PopupGoToPageBinding.inflate(layoutInflater)
//        popup = PopupWindow(
//            viewPop.root, // Custom view to show in popup window
//            LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
//            LinearLayout.LayoutParams.WRAP_CONTENT // Window height
//        )
//        popup!!.isOutsideTouchable = true
//        popup!!.elevation = 20.0F
//        val slideIn = Slide()
//        slideIn.slideEdge = Gravity.BOTTOM
//        popup!!.enterTransition = slideIn
//        val slideOut = Slide()
//        slideOut.slideEdge = Gravity.BOTTOM
//        popup?.exitTransition = slideOut
//
//        viewPop.rvPageNumbers.adapter =
//            AdapterPageNumbers(getNumbers(), object : AdapterPageNumbers.PageNumberClickListener {
//                override fun onClick(value: Int) {
//                    b.pdfViewFargment.jumpTo(value)
//                    setPageCount(value, "jumpTo $value")
//                    popup?.dismiss()
//                }
//            })
//
//        viewPop.rvPageNumbers.scrollToPosition(b.pdfViewFargment.currentPage)
//        viewPop.btnCancel.setOnClickListener {
//            popup?.dismiss()
//        }
//        TransitionManager.beginDelayedTransition(b.root)
//        val location = IntArray(2)
//        v.getLocationOnScreen(location)
//        popup?.showAtLocation(
//            b.root, Gravity.NO_GRAVITY, // root, Gravity.NO_GRAVITY,
//            location[0],// + v.measuredWidth/2,
//            location[1] + v.measuredHeight / 2
//        )
//    }
//
//    private fun getNumbers(): List<String> {
//        val list = ArrayList<String>()
//        for (i in 0 until b.pdfViewFargment.pageCount) {
//            list.add((i + 1).toString())
//
//        }
//        return list
//    }
//
//    override fun onPause() {
//        super.onPause()
//        alert?.dismiss()
//    }
//
//
//    companion object {
//        var alert: AlertDialog? = null
//        var modeAr: Boolean = true
//    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param: String) =
            ScreenPdfView().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM_TEXT, param)
                }
            }
    }
}