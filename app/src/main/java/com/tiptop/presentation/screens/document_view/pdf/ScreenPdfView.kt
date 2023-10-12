package com.tiptop.presentation.screens.document_view.pdf

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.barteksc.pdfviewer.listener.OnErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.tiptop.R
import com.tiptop.app.common.CanvasData
import com.tiptop.app.common.Constants.KEY_LAST_PAGE
import com.tiptop.app.common.Constants.KEY_SCREEN_TIMER
import com.tiptop.app.common.DebouncingQueryTextListener
import com.tiptop.app.common.MyColor
import com.tiptop.app.common.SharedPrefSimple
import com.tiptop.app.common.hideKeyboard
import com.tiptop.app.common.isInternetAvailable
import com.tiptop.app.common.share
import com.tiptop.data.models.local.ArabUzBase
import com.tiptop.data.models.local.ArabUzUser
import com.tiptop.data.models.local.DocumentLocal
import com.tiptop.data.models.local.UzArabBase
import com.tiptop.databinding.DialogAllertBinding
import com.tiptop.databinding.DialogScreenTimerBinding
import com.tiptop.databinding.DialogWordBinding
import com.tiptop.databinding.PopupGoToPageBinding
import com.tiptop.databinding.PopupLastSeenDocumentsBinding
import com.tiptop.databinding.ScreenDocumentViewBinding
import com.tiptop.presentation.MainActivity
import com.tiptop.presentation.MainActivity.Companion.TEMPORARY_OUT
import com.tiptop.presentation.screens.BaseFragment
import com.tiptop.presentation.screens.document_view.AdapterLastSeenDocuments
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ScreenPdfView : BaseFragment(R.layout.screen_document_view) {
    private var canvasMap = HashMap<Int, List<CanvasData>>()
    private var alert: AlertDialog? = null
    private var lastSeenDocuments = ArrayList<DocumentLocal>()
    private var currentDocument: DocumentLocal? = null
    private var popup: PopupWindow? = null
    private var currentPage = 0
    private val vm by viewModels<PdfViewModelIml>()
    private var _binding: ScreenDocumentViewBinding? = null
    private var pref: SharedPrefSimple? = null
    private val binding get() = _binding!!


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
        pref = SharedPrefSimple(requireContext())
        setDocument()
        initObservers()
        initClickListeners()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            var isBackAvialable = true
            if (popup?.isShowing == true) {
                popup?.dismiss()
                isBackAvialable = false
            }
            if (binding.lDictionary.rootDictionary.visibility != View.GONE) {
                isBackAvialable = false
                closeDictionary()
            }
            if (binding.myCanvas.rootCustomCanvas.visibility == View.VISIBLE) {
                isBackAvialable = false
                binding.myCanvas.rootCustomCanvas.visibility = View.GONE
            }
            if (isBackAvialable) {
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


    @SuppressLint("SetTextI18n")
    private fun initObservers() {
        vm.documentBytes.observe(viewLifecycleOwner) { bytes ->
            bytes?.let {
                currentBytes = it
                showPdf()
            }
        }
        vm.currentPage.observe(viewLifecycleOwner) { pageNumber ->
            currentPage = pageNumber
            binding.tvPageCount.text = "${binding.pdfView.pageCount}/${pageNumber + 1}"
            binding.tvGotoPage.text = (pageNumber + 1).toString()
        }
        vm.currentDocument.observe(viewLifecycleOwner) { currentDocument ->
            this.currentDocument = currentDocument
            binding.tvBookName.text = currentDocument.nameDecrypted().substringBeforeLast(".")
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
                openFullScreen()

            } else {
                closeFullScreen()

            }
        }
        vm.screenBlockState.observe(viewLifecycleOwner) { blocked ->
            if (blocked) {
                (activity as MainActivity).blockScreen()
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
            showJavohir()
        }
        binding.ivScreenOrientation.setOnClickListener {
            changeScreenOriantation()
        }

        // bottom
        binding.tvUp10.setOnClickListener {
            binding.pdfView.jumpTo(binding.pdfView.currentPage - 10, true)
        }
        binding.tvDown10.setOnClickListener {
            binding.pdfView.jumpTo(binding.pdfView.currentPage + 10, true)
        }
        binding.ivShare.setOnClickListener {
            currentBytes?.let { bytes ->
                currentDocument?.let { doc ->
                    TEMPORARY_OUT = true
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

    private fun setDocument() {
        if (currentId.isNotEmpty()) {
            vm.setDocument(currentId)
        }
    }

    private fun showPdf() {
        binding.pdfView
            .fromBytes(currentBytes ?: byteArrayOf())
            .defaultPage(pref?.getInt(KEY_LAST_PAGE + currentId) ?: 0)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .enableAntialiasing(true)
            .enableAnnotationRendering(false)
            .spacing(0)
            .nightMode(false)
            .onPageChange { page, pageCount ->
                vm.setCurrentPage(page)
                vm.updateTimer()
            }
            .pageFitPolicy(FitPolicy.WIDTH)
            .scrollHandle(DefaultScrollHandle(requireContext()))
            .onError(OnErrorListener {
                showSnackBar("Xatolik, fayl ochilmadi !")
            }
            ).load()
    }

    private fun showBushro() {
        val launchIntent =
            requireContext().packageManager.getLaunchIntentForPackage("com.samples.bushro")
        if (launchIntent != null) {
            TEMPORARY_OUT = true
            startActivity(launchIntent)
        } else {
            showSnackBar("'Bushro' ilovasi topilmadi, uni tortib o'rnating")
        }
    }

    private fun showJavohir() {
        openDictionary()
        vm.getWords(currentId)
        var modeAr = true
        var searchText = ""
        val adpaterWords = AdapterDictionary(
            searchText,
            object : AdapterDictionary.PageNumberClickListener {
                override fun onClick(word: Dictionary) {
                    binding.lDictionary.etSearchWord.clearFocus()
                    binding.lDictionary.etSearchWord.hideKeyboard()
                    showWordDialog(word) { modifiedWord ->
                        binding.lDictionary.etSearchWord.setQuery("", true)
                        vm.updateBaseWord(modifiedWord, currentPage, currentDocument?.id ?: "")
                    }
                }

                override fun onClickPage(page: Int) {
                    closeDictionary()
                    if (page != currentPage) {
                        binding.pdfView.jumpTo(page, true)
                    }
                }
            })

        binding.lDictionary.rvDict.adapter = adpaterWords

        vm.words.observe(viewLifecycleOwner) {
            adpaterWords.submitList(it)
        }
        vm.closestPage.observe(viewLifecycleOwner) { index ->
            binding.lDictionary.rvDict.scrollToPosition(index)
        }
        binding.lDictionary.etSearchWord.setOnQueryTextListener(DebouncingQueryTextListener(
            requireActivity().lifecycle
        ) {
            searchText = it ?: ""
            if (searchText.isNotEmpty()) {
                vm.getSearchedBaseWords(searchText)
            } else {
                vm.getWords(currentId)
            }
        })
        binding.lDictionary.ivChange.setOnClickListener {
            modeAr = !modeAr
            if (modeAr) {
                binding.lDictionary.tvAr.text = "Arabcha"
                binding.lDictionary.tvUz.text = "O'zbekcha"
            } else {
                binding.lDictionary.tvAr.text = "O'zbekcha"
                binding.lDictionary.tvUz.text = "Arabcha"
            }
        }
        binding.lDictionary.rvDict.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )

        binding.lDictionary.btnClose.setOnClickListener {
            closeDictionary()
        }
    }

    private fun openFullScreen() {
        val animationSlideToUp =
            AnimationUtils.loadAnimation(requireContext(), R.anim.slide_to_up)
        val animationSlideBottomToDown =
            AnimationUtils.loadAnimation(requireContext(), R.anim.slide_bottom_to_down)
        binding.lTop.visibility = View.GONE
        binding.lTop.startAnimation(animationSlideToUp)

        binding.lBottom.visibility = View.GONE
        binding.lBottom.startAnimation(animationSlideBottomToDown)
        (activity as MainActivity).hideSystemUI()
    }

    private fun closeFullScreen() {
        val animationSlideToDown =
            AnimationUtils.loadAnimation(requireContext(), R.anim.slide_to_down)
        val animationSlideBottomToUp =
            AnimationUtils.loadAnimation(requireContext(), R.anim.slide_bottom_to_up)
        binding.lTop.visibility = View.VISIBLE
        binding.lTop.startAnimation(animationSlideToDown)
        binding.lBottom.visibility = View.VISIBLE
        binding.lBottom.startAnimation(animationSlideBottomToUp)
        (activity as MainActivity).showSystemUI()
    }

    private fun openDictionary() {
        val animationSlideToLeft =
            AnimationUtils.loadAnimation(requireContext(), R.anim.slide_to_left)
        binding.lDictionary.rootDictionary.visibility = View.VISIBLE
        binding.lDictionary.rootDictionary.startAnimation(animationSlideToLeft)
    }

    private fun closeDictionary() {
        binding.lDictionary.etSearchWord.setQuery("", true)
        val animationSlideToRight =
            AnimationUtils.loadAnimation(requireContext(), R.anim.slide_to_right)
        binding.lDictionary.rootDictionary.visibility = View.GONE
        binding.lDictionary.rootDictionary.startAnimation(animationSlideToRight)
    }

    private fun initColorList(iv: ImageView, colorList: List<ImageView>) {
        colorList.forEach {
            if (it == iv) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.GONE
            }
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
                lastSeenDocuments.filter { it.id != currentId },
                object : AdapterLastSeenDocuments.PageNumberClickListener {
                    @SuppressLint("CommitTransaction", "DetachAndAttachSameFragment")
                    override fun onClick(document: DocumentLocal) {
                        closeCurrentDocument()
                        currentId = document.id
                        setDocument()
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
            binding.root.x.toInt() + v.measuredWidth / 2, //location[0] - 400,
            binding.root.y.toInt() + v.measuredHeight / 2
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
                    binding.pdfView.jumpTo(value, true)
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
            }

            60 -> {
                viewDialog.rg.check(viewDialog.rb1.id)
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

    private fun closeCurrentDocument() {
        pref?.saveInt(KEY_LAST_PAGE + currentId, currentPage)
        vm.updateDocument()
    }

    override fun onStop() {
        super.onStop()
        alert?.dismiss()
        popup?.dismiss()
        closeCurrentDocument()
        closeDictionary()
        vm.cancelTimer()
    }


    override fun onDestroy() {
        super.onDestroy()
        currentBytes = null
        _binding = null
    }

    companion object {
        var currentId: String = ""
        private var currentBytes: ByteArray? = null
    }

    private fun showDrawingScreen() {
        binding.pdfView.resetZoomWithAnimation()
        vm.changeFullScreen()
        val canvasLayout = binding.myCanvas
        val canvas = canvasLayout.drawSquareCanvas
        canvasMap[currentPage]?.let {
            canvas.setData(it)
        }

        canvasLayout.rootCustomCanvas.visibility = View.VISIBLE
        canvasLayout.tvCancel.setOnClickListener {
            canvas.clear()
            canvasLayout.rootCustomCanvas.visibility = View.GONE
        }
        canvasLayout.tvSave.setOnClickListener {
            canvasLayout.rootCustomCanvas.visibility = View.GONE
            canvas.save { canvasDataList ->
                canvasMap[currentPage] = canvasDataList
                binding.pdfView.loadPages()
                //  canvas.clear()
            }
        }
        canvasLayout.icUndo.setOnClickListener {
            canvas.undo()
        }
        canvasLayout.icClear.setOnClickListener {
            canvas.clear()
        }

        val colorList = listOf(
            canvasLayout.ivRed,
            canvasLayout.ivYellow,
            canvasLayout.ivGreen,
            canvasLayout.ivBlue
        )
        canvasLayout.rlRed.setOnClickListener {
            canvas.setColor(MyColor.RED)
            initColorList(canvasLayout.ivRed, colorList)
        }
        canvasLayout.rlYellow.setOnClickListener {
            canvas.setColor(MyColor.YELLOW)
            initColorList(canvasLayout.ivYellow, colorList)
        }
        canvasLayout.rlGreen.setOnClickListener {
            canvas.setColor(MyColor.GREEN)
            initColorList(canvasLayout.ivGreen, colorList)
        }
        canvasLayout.rlBlue.setOnClickListener {
            canvas.setColor(MyColor.BLUE)
            initColorList(canvasLayout.ivBlue, colorList)

        }
    }
}