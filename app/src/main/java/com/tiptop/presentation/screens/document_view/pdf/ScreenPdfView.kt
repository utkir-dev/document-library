package com.tiptop.presentation.screens.document_view.pdf

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.barteksc.pdfviewer.listener.OnErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.tiptop.R
import com.tiptop.app.common.Constants.KEY_LAST_PAGE
import com.tiptop.app.common.Constants.KEY_SCREEN_TIMER
import com.tiptop.app.common.SharedPrefSimple
import com.tiptop.app.common.isInternetAvailable
import com.tiptop.app.common.share
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.databinding.DialogScreenTimerBinding
import com.tiptop.databinding.PopupGoToPageBinding
import com.tiptop.databinding.PopupLastSeenDocumentsBinding
import com.tiptop.databinding.ScreenDocumentViewBinding
import com.tiptop.presentation.screens.BaseFragment
import com.tiptop.presentation.screens.document_view.AdapterLastSeenDocuments
import dagger.hilt.android.AndroidEntryPoint


const val ARG_PARAM_DOCUMENT = "arg_param_document"

@AndroidEntryPoint
class ScreenPdfView : BaseFragment(R.layout.screen_document_view) {
    //    private var adapterDict: AdapterDict? = null
    private var alert: AlertDialog? = null

    //    private var popup: PopupWindow? = null
//    private var searchingText = ""
//    private val TAG = "FragmentPdf"
    private var lastSeenDocuments = ArrayList<DocumentLocal>()
    private var currentBytes: ByteArray? = null
    private var currentDocument: DocumentLocal? = null
    private var popup: PopupWindow? = null
    private var currentId: String = ""
    private var currentPage = 0

    private val vm by viewModels<PdfViewModelIml>()
    private var _binding: ScreenDocumentViewBinding? = null
    private var pref: SharedPrefSimple? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentId = it.getString(ARG_PARAM_DOCUMENT) ?: ""
            setDocument()
        }
    }

    private fun setDocument() {
        if (currentId.isNotEmpty()) {
            vm.setDocument(currentId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ScreenDocumentViewBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pref = SharedPrefSimple(requireActivity())
        showPdf()
        initClickListeners()
        initObservers()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (popup?.isShowing == true) {
                popup?.dismiss()
            } else {
                findNavController().popBackStack()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        var timer = pref?.getInt(KEY_SCREEN_TIMER) ?: 0
        timer = if (timer == 0) 15 else timer
        vm.updateTimer(timer)
        vm.getLastSeenDocuments()
    }

    private fun showPdf() {
        vm.documentBytes.observe(viewLifecycleOwner) { bytes ->
            currentBytes = bytes
            binding.pdfView
                .fromBytes(bytes)
                .defaultPage(pref?.getInt(KEY_LAST_PAGE + currentId) ?: 0)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .enableAntialiasing(true)
                .enableAnnotationRendering(false)
                .spacing(1)
                .nightMode(false)
                .onPageChange { page, pageCount ->
                    vm.setCurrentPage(page)
                    vm.updateTimer()
                }
                .onDraw { canvas, pageWidth, pageHeight, displayedPage ->

                    val ratioX = pageWidth / canvas.width
                    val ratioY = pageHeight / canvas.height

                    val cx = pageWidth / 2
                    val cy = pageHeight / 2

                    val radius = if (pageWidth > pageHeight) {
                        pageHeight / 4
                    } else {
                        pageWidth / 4
                    }
                    val paint = Paint()
                    paint.color = resources.getColor(
                        R.color.grey_clear,
                        null
                    )// Color.GREEN // установим зелёный цвет
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(cx, cy, radius, paint)
                    val rect = RectF().apply {
                        left = pageWidth * 0.2F
                        right = pageWidth * 0.8F
                        top = pageHeight * 0.6F
                        bottom = pageHeight * 0.9F
                    }

                    canvas.drawRect(rect, paint)
                }
                .pageFitPolicy(FitPolicy.WIDTH)
                .scrollHandle(DefaultScrollHandle(requireContext()))
                .onError(OnErrorListener {
                    showSnackBar("Xatolik, fayl ochilmadi !")
                }
                ).load()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initObservers() {
        vm.currentPage.observe(viewLifecycleOwner) { pageNumber ->
            currentPage = pageNumber
            binding.tvPageCount.text = "${binding.pdfView.pageCount}/${pageNumber + 1}"
            binding.tvGotoPage.text = (pageNumber + 1).toString()
        }
        vm.currentDocument.observe(viewLifecycleOwner) { currentDocument ->
            this.currentDocument = currentDocument
            binding.tvBookName.text = currentDocument.name.substringBeforeLast(".")
        }
        vm.nightMode.observe(viewLifecycleOwner) { nightMode ->
            binding.pdfView.setNightMode(nightMode)
            if (nightMode) {
                binding.ivDayNight.setImageResource(R.drawable.ic_sun_yellow)
            } else {
                binding.ivDayNight.setImageResource(R.drawable.ic_moon)
            }
            binding.pdfView.loadPages()
        }
        vm.fullScreen.observe(viewLifecycleOwner) { fullScreen ->
            if (fullScreen) {
                binding.lTop.visibility = View.GONE
                binding.lBottom.visibility = View.GONE
            } else {
                binding.lTop.visibility = View.VISIBLE
                binding.lBottom.visibility = View.VISIBLE
            }
        }
        vm.screenBlockState.observe(viewLifecycleOwner) { blocked ->
            if (blocked) {
                blockScreen()
            }
        }

        vm.lastDocuments.observe(viewLifecycleOwner) { listDocument ->
            if (listDocument.isNotEmpty()) {
                lastSeenDocuments = listDocument as ArrayList<DocumentLocal>
            }
        }
    }

    private fun initClickListeners() {
        // top
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.pdfView.setOnClickListener {
            vm.changeFullScreen()
        }
        binding.ivLastBooks.setOnClickListener {
            showLastSeenDocuments(binding.ivLastBooks)

        }
        binding.cardBushro.setOnClickListener {
            showBushro()
        }
        binding.cardJavohir.setOnClickListener {
            // showJavohir()
        }
        binding.ivScreenOrientation.setOnClickListener {
            changeScreenOriantation()
        }

        // bottom
        binding.tvUp10.setOnClickListener {
            binding.pdfView.jumpTo(binding.pdfView.currentPage - 10)
        }
        binding.tvDown10.setOnClickListener {
            binding.pdfView.jumpTo(binding.pdfView.currentPage + 10)
        }
        binding.ivShare.setOnClickListener {
            currentBytes?.let { bytes ->
                currentDocument?.let { doc ->
                    isLoading = true
                    if (isInternetAvailable(requireContext())) {
                        share(doc, bytes, requireActivity())
                    } else {
                        showSnackBarNoConnection()
                    }
                }
            }
        }
        binding.ivDayNight.setOnClickListener {
            vm.changeNightMode()
        }
        binding.tvGotoPage.setOnClickListener {
            showPopupSelectPage(it)
        }
        binding.cardGotoPage.setOnClickListener {
            showPopupSelectPage(it)
        }
        binding.ivTimer.setOnClickListener {
            showTimer()
        }

    }

    private fun showBushro() {
        val launchIntent =
            requireContext().packageManager.getLaunchIntentForPackage("com.samples.bushro")
        if (launchIntent != null) {
            isLoading = true
            startActivity(launchIntent)
        } else {
            showSnackBar("'Bushro' ilovasi topilmadi, uni tortib o'rnating")
        }
    }

    private fun showLastSeenDocuments(v: View) {
        val viewPop = PopupLastSeenDocumentsBinding.inflate(layoutInflater)
        val slideIn = Slide()
        popup = PopupWindow(
            viewPop.root, // Custom view to show in popup window
            LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT // Window height
        ).apply {
            isOutsideTouchable = true
            elevation = 20.0F
            slideIn.slideEdge = Gravity.TOP
            enterTransition = slideIn
            val slideOut = Slide()
            slideOut.slideEdge = Gravity.TOP
            exitTransition = slideOut
        }
        viewPop.rvLastBooks.adapter =
            AdapterLastSeenDocuments(
                lastSeenDocuments,
                object : AdapterLastSeenDocuments.PageNumberClickListener {
                    override fun onClick(document: DocumentLocal) {
                        vm.setDocument(document.id)
                        popup?.dismiss()
                    }
                })

        viewPop.tvCancel.setOnClickListener {
            popup?.dismiss()
        }
        TransitionManager.beginDelayedTransition(binding.root)
        val location = IntArray(2)
        v.getLocationOnScreen(location)
        popup?.showAtLocation(
            binding.root, Gravity.NO_GRAVITY, // root, Gravity.NO_GRAVITY,
            binding.root.x.toInt()+v.measuredWidth / 2, //location[0] - 400,
            location[1]// + v.measuredHeight / 2
        )
    }

    private fun showPopupSelectPage(v: View) {
        val viewPop = PopupGoToPageBinding.inflate(layoutInflater)
        val slideIn = Slide()
        popup = PopupWindow(
            viewPop.root, // Custom view to show in popup window
            LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT // Window height
        ).apply {
            isOutsideTouchable = true
            elevation = 20.0F
            slideIn.slideEdge = Gravity.BOTTOM
            enterTransition = slideIn
            val slideOut = Slide()
            slideOut.slideEdge = Gravity.BOTTOM
            exitTransition = slideOut
        }



        viewPop.rvPageNumbers.adapter =
            AdapterPageNumbers(getNumbers(), object : AdapterPageNumbers.PageNumberClickListener {
                override fun onClick(value: Int) {
                    binding.pdfView.jumpTo(value)
                    popup?.dismiss()
                }
            })

        viewPop.rvPageNumbers.scrollToPosition(binding.pdfView.currentPage)
        viewPop.btnCancel.setOnClickListener {
            popup?.dismiss()
        }
        TransitionManager.beginDelayedTransition(binding.root)
        val location = IntArray(2)
        v.getLocationOnScreen(location)
        popup?.showAtLocation(
            binding.root, Gravity.NO_GRAVITY, // root, Gravity.NO_GRAVITY,
            location[0],// + v.measuredWidth/2,
            location[1] + v.measuredHeight / 2
        )
    }

    private fun getNumbers(): List<String> {
        val list = ArrayList<String>()
        for (i in 0 until binding.pdfView.pageCount) {
            list.add((i + 1).toString())

        }
        return list
    }
    private fun showTimer() {
        var currentTimer = pref?.getInt(KEY_SCREEN_TIMER) ?: 15
        val viewDialog = DialogScreenTimerBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setCancelable(true)
            .setView(viewDialog.root)
        alert = dialog.create()
        alert?.show()
        when (currentTimer) {
            0, 15 -> {
                viewDialog.rg.check(viewDialog.rb15.id)
                currentTimer = 15
            }

            30 -> {
                viewDialog.rg.check(viewDialog.rb30.id)
                //viewDialog.rb30.isChecked = true
            }

            60 -> {
                viewDialog.rg.check(viewDialog.rb1.id)
                //viewDialog.rb1.isChecked = true
            }

            120 -> {

                viewDialog.rg.check(viewDialog.rb2.id)
            }

            180 -> {
                viewDialog.rg.check(viewDialog.rb3.id)
            }

            240 -> {
                viewDialog.rg.check(viewDialog.rb4.id)
            }

            300 -> {
                viewDialog.rg.check(viewDialog.rb5.id)
            }
        }
        viewDialog.rg.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                viewDialog.rb15.id -> {
                    currentTimer = 15
                }

                viewDialog.rb30.id -> {
                    currentTimer = 30
                }

                viewDialog.rb1.id -> {
                    currentTimer = 60
                }

                viewDialog.rb2.id -> {
                    currentTimer = 120
                }

                viewDialog.rb3.id -> {
                    currentTimer = 180
                }

                viewDialog.rb4.id -> {
                    currentTimer = 240
                }

                viewDialog.rb5.id -> {
                    currentTimer = 300
                }
            }

        }
        viewDialog.btnConfirmTimer.setOnClickListener {
            pref?.saveInt(KEY_SCREEN_TIMER, currentTimer)
            vm.updateTimer(currentTimer)
            alert?.cancel()
        }
    }


    override fun onPause() {
        super.onPause()
        alert?.dismiss()
        popup?.dismiss()
        pref?.saveInt(KEY_LAST_PAGE + currentId, currentPage)
        vm.updateDocument()
        vm.cancelTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null

    }

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

}