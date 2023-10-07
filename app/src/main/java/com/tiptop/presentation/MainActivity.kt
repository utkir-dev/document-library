package com.tiptop.presentation

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED
import androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNDEFINED
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tiptop.R
import com.tiptop.app.common.Constants
import com.tiptop.app.common.Constants.KEY_IS_LAUNCHER_ICON_INSTALLED
import com.tiptop.app.common.DarkMode
import com.tiptop.app.common.SharedPrefSimple
import com.tiptop.app.common.Variables.CURRENT_DEVICE_ID
import com.tiptop.app.common.Variables.CURRENT_USER_ID
import com.tiptop.databinding.ActivityMainBinding
import com.tiptop.databinding.DialogConfirmBinding
import com.tiptop.presentation.screens.BaseViewModel
import com.tiptop.presentation.screens.BlockScreenDialogFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawer: DrawerLayout
    private var ivAvatar: ImageView? = null
    private var ivNightMode: ImageView? = null
    private var tvUserHeader: TextView? = null
    private var tvAdminTitle: TextView? = null
    private var controller: NavController? = null
    val vm by viewModels<BaseViewModel>()
    private var shared: SharedPrefSimple? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideSystemUI()
        if (savedInstanceState != null) {
            val fr =
                supportFragmentManager.findFragmentByTag(BlockScreenDialogFragment.TAG) as BlockScreenDialogFragment?
            fr?.show(
                supportFragmentManager,
                BlockScreenDialogFragment.TAG
            )
        }
    }

    override fun onStart() {
        super.onStart()
        shared = SharedPrefSimple(this)
        initFuns()
        checkAppIcon()
    }

    override fun onResume() {
        super.onResume()
        setMask()
    }

    override fun onPause() {
        super.onPause()
        shared?.saveLong(Constants.KEY_MASK_TIME, System.currentTimeMillis())
    }

    private fun setMask() {
        val timeOut = shared?.getLong(Constants.KEY_MASK_TIME) ?: 0L
        val isScreenBlocked = shared?.getBoolean(Constants.KEY_SCREEN_BLOCK) ?: true
        if (!isScreenBlocked) {
            if (IS_ENTERED && TEMPORARY_OUT && System.currentTimeMillis() - timeOut < 60_000) {

            } else if (IS_ENTERED && System.currentTimeMillis() - timeOut < 2500) {

            } else {
                blockScreen()
            }
        } else {
            blockScreen()
        }
    }

    fun blockScreen() {
        IS_ENTERED = false
        val fr = BlockScreenDialogFragment()
        fr.show(
            supportFragmentManager,
            BlockScreenDialogFragment.TAG
        )
        if (masks.isNotEmpty()) {
            masks.forEach {
                try {
                    it.dismiss()
                } catch (_: Exception) {
                }
            }
            masks.clear()
        }
        masks.add(fr)
    }

    private fun initFuns() {
        drawer = binding.drawerLayout
        controller = findNavController(R.id.nav_host)
        //  binding.navView.setNavigationItemSelectedListener(this)
        initNavigationDrawerListener()
        initBackPressed()
        initHeader()
        initListeners()
        initCurrentUser()

        //fakeEntreance()
    }

    @SuppressLint("SetTextI18n")
    fun initCurrentUser() {
        vm.initObservers()
        vm.currentUser.asLiveData().observe(this) { currentUser ->
            vm.currentDevice.asLiveData().observe(this) { currentDevice ->
                if (currentUser != null) {
                    CURRENT_USER_ID = currentUser.id
                    if (currentUser.permitted) {
                        if (unPermitted) {
                            unPermitted = false
                            val mIntent = intent
                            finish()
                            startActivity(mIntent)
                        }
                        if (currentDevice != null) {
                            CURRENT_DEVICE_ID = currentDevice.id
                            tvUserHeader?.text =
                                "email: ${currentUser.email}\ndevice: ${currentDevice.name}"
                            if (currentDevice.blocked) {
                                blocked = true
                                initDrawer(false)
                                binding.navView.visibility = View.GONE
                                binding.appBarMain.navHost.visibility = View.GONE
                                binding.animBlock.visibility = View.VISIBLE
                            } else {
                                if (blocked) {
                                    blocked = false
                                    val mIntent = intent
                                    finish()
                                    startActivity(mIntent)
                                }

                                if (currentDevice.admin) {
                                    tvAdminTitle?.visibility = View.VISIBLE
                                    binding.navView.menu.findItem(R.id.screenUsers).isVisible = true
                                    binding.navView.menu.findItem(R.id.screenAddEditDocuments).isVisible =
                                        true
                                    initDrawer(true)
                                    binding.appBarMain.navHost.visibility = View.VISIBLE
                                    binding.navView.visibility = View.VISIBLE
                                    binding.animBlock.visibility = View.GONE

                                } else {
                                    tvAdminTitle?.visibility = View.INVISIBLE
                                    binding.navView.menu.findItem(R.id.screenUsers).isVisible =
                                        false
                                    binding.navView.menu.findItem(R.id.screenAddEditDocuments).isVisible =
                                        false
                                    initDrawer(true)
                                    binding.appBarMain.navHost.visibility = View.VISIBLE
                                    binding.navView.visibility = View.VISIBLE
                                    binding.animBlock.visibility = View.GONE

                                    val currentRoute =
                                        findNavController(R.id.nav_host).currentDestination?.route
                                    if (currentRoute == "users"
                                        || currentRoute == "screenAddEditDocuments"
                                        || currentRoute == "screenAddEditDocumentsChild1"
                                        || currentRoute == "screenAddEditDocumentsChild2"
                                    ) {
                                        findNavController(R.id.nav_host).popBackStack()
                                    }
                                }
                            }
                        } else {
                            initDrawer(false)
                            binding.navView.visibility = View.GONE
                            binding.appBarMain.navHost.visibility = View.GONE
                            binding.animBlock.visibility = View.VISIBLE
                        }
                    } else {
                        unPermitted = true
                        initDrawer(false)
                        binding.navView.visibility = View.GONE
                        binding.appBarMain.navHost.visibility = View.GONE
                        binding.animBlock.visibility = View.VISIBLE
                    }
                } else {
                    initDrawer(false)
                    binding.navView.visibility = View.GONE
                    val currentRoute =
                        findNavController(R.id.nav_host).currentDestination?.route
                    if (currentRoute == "splash" || currentRoute == "signIn" || currentRoute == "signUp") {
                        binding.appBarMain.navHost.visibility = View.VISIBLE
                        binding.animBlock.visibility = View.GONE
                    } else {
                        binding.appBarMain.navHost.visibility = View.GONE
                        binding.animBlock.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun initDrawer(isEnable: Boolean) {

        if (isEnable) {
            // drawer.setDrawerLockMode(LOCK_MODE_UNLOCKED)
            drawer.setDrawerLockMode(LOCK_MODE_UNDEFINED)
        } else {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START)
            }
            drawer.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
        }
    }

    fun openDrawer() {
        drawer.open()
    }

    @SuppressLint("SetTextI18n")
    private fun initHeader() {
        val header = binding.navView.getHeaderView(0)
        ivAvatar = header.findViewById(R.id.iv_avatar)
        ivNightMode = header.findViewById(R.id.iv_night_mode)
        tvUserHeader = header.findViewById(R.id.tv_user_header)
        tvAdminTitle = header.findViewById(R.id.tv_admin_title)

        if (DarkMode(this).isDarkModeOn()) {
            ivNightMode?.setImageResource(R.drawable.ic_sun)
        } else {
            ivNightMode?.setImageResource(R.drawable.ic_moon)
        }
        //  ivAvatar?.setImageResource(R.drawable.background_main)


        drawer.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {

            }

            override fun onDrawerClosed(drawerView: View) {}

            override fun onDrawerStateChanged(newState: Int) {}

        })
    }

    private fun initListeners() {
        ivNightMode?.setOnClickListener {

        }
    }

    private fun initNavigationDrawerListener() {
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            drawer.closeDrawer(GravityCompat.START)
            when (menuItem.itemId) {
                R.id.screenHome -> {
                    controller?.popBackStack(R.id.screenHome, true)
                    controller?.navigate(R.id.screenHome)
                    true
                }

                R.id.screenAddEditDocuments -> {
                    controller?.popBackStack(R.id.screenAddEditDocuments, true)
                    controller?.navigate(R.id.screenAddEditDocuments)
                    true
                }

                R.id.screenUsers -> {
                    controller?.popBackStack(R.id.screenUsers, true)
                    controller?.navigate(R.id.screenUsers)
                    true
                }

                R.id.screenSettings -> {
                    controller?.popBackStack(R.id.screenSettings, true)
                    controller?.navigate(R.id.screenSettings)
                    true
                }

                R.id.sign_out -> {
                    showConfirmDialog { response ->
                        if (response) {
                            Firebase.auth.signOut()
                            finish()
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                    }
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showConfirmDialog(response: (Boolean) -> Unit) {
        val view = DialogConfirmBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this, R.style.MyDialogStyle).apply {
            setView(view.root)
                .setCancelable(true)
                .create()
        }
        val allert = dialog.show()
        view.tvTitle.text = "☝️ Diqqat !"
        view.tvMessage.text =
            "Login va parolingiz esingizdami? Akkauntdan chiqib ketishga ishonchingiz komilmi ?"
        view.btnConfirm.setOnClickListener { response(true);allert.cancel() }
        view.btnCancel.setOnClickListener { response(false);allert.cancel() }
    }

    private fun initBackPressed() {
        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                backPressed()
            }
        } else {
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backPressed()
                }
            })
        }
    }

    private fun backPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            if (blocked || unPermitted) {
                finish()
            }
            val count = findNavController(R.id.nav_host).currentBackStack.value.size
            Log.d("currentBackStack", "count = $count")
            if (count < 2) {
                finish()
            } else {
                findNavController(R.id.nav_host).popBackStack()
            }
        }
    }

    fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
        }
        window?.addFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window?.addFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    private fun checkAppIcon() {
        val shared = SharedPrefSimple(this)
        if (!shared.getBoolean(KEY_IS_LAUNCHER_ICON_INSTALLED)) {
            packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            packageManager.setComponentEnabledSetting(
                ComponentName(
                    this@MainActivity,
                    aliases.random()
                ),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            shared.saveBoolean(KEY_IS_LAUNCHER_ICON_INSTALLED, true)
        }
    }

    //    private fun fakeEntreance() {
//        tvAdminTitle?.visibility = View.VISIBLE
//        binding.navView.menu.findItem(R.id.screenUsers).isVisible = true
//        binding.navView.menu.findItem(R.id.screenAddEditDocuments).isVisible = true
//        initDrawer(true)
//        binding.appBarMain.navHost.visibility = View.VISIBLE
//        binding.navView.visibility = View.VISIBLE
//        binding.animBlock.visibility = View.GONE
//    }
    companion object {
        var blocked = false
        var unPermitted = false
        private val masks = HashSet<BlockScreenDialogFragment>()
        private var blockScreenDialog: BlockScreenDialogFragment? = null
        var IS_ENTERED = false
        var TEMPORARY_OUT: Boolean = false
    }
}