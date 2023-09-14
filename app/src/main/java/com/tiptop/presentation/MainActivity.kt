package com.tiptop.presentation

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
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
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tiptop.R
import com.tiptop.app.common.DarkMode
import com.tiptop.app.common.Variables.CURRENT_DEVICE_ID
import com.tiptop.app.common.Variables.CURRENT_USER_ID
import com.tiptop.databinding.ActivityMainBinding
import com.tiptop.databinding.DialogConfirmBinding
import com.tiptop.presentation.screens.BaseViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    lateinit var drawer: DrawerLayout
    private var ivAvatar: ImageView? = null
    private var ivNightMode: ImageView? = null
    private var tvUserHeader: TextView? = null
    private var tvAdminTitle: TextView? = null
    private val vm by viewModels<BaseViewModel>()
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    //    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

//        hideSystemUI()
//        val actionBarToggle = ActionBarDrawerToggle(this, drawer, 0, 0)
//        drawer.addDrawerListener(actionBarToggle)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        actionBarToggle.syncState()
    }

    override fun onStart() {
        super.onStart()
        initFuns()
    }

    private fun initFuns() {
        drawer = binding.drawerLayout
        binding.navView.setNavigationItemSelectedListener(this)
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
                                    binding.navView.menu.findItem(R.id.screenAddEditDocuments).isVisible =true
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
                                    if (currentRoute != "home") {
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
private fun fakeEntreance(){
    tvAdminTitle?.visibility = View.VISIBLE
    binding.navView.menu.findItem(R.id.screenUsers).isVisible = true
    binding.navView.menu.findItem(R.id.screenAddEditDocuments).isVisible =true
    initDrawer(true)
    binding.appBarMain.navHost.visibility = View.VISIBLE
    binding.navView.visibility = View.VISIBLE
    binding.animBlock.visibility = View.GONE
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d("onNavigationItemSelected", "item.id = ${item.itemId}")
        val controller = findNavController(R.id.nav_host)
        when (item.itemId) {
            R.id.screenHome -> {
                controller.popBackStack(R.id.screenHome, true)
                controller.navigate(R.id.screenHome)
            }

            R.id.screenAddEditDocuments -> {
                controller.popBackStack(R.id.screenAddEditDocuments, true)
                controller.navigate(R.id.screenAddEditDocuments)
            }
            R.id.screenUsers -> {
                controller.popBackStack(R.id.screenUsers, true)
                controller.navigate(R.id.screenUsers)
            }
            R.id.screenSettings -> {
                controller.popBackStack(R.id.screenSettings, true)
                controller.navigate(R.id.screenSettings)
            }

            R.id.sign_out -> {
                showConfirmDialog { response ->
                    if (response) {
                        Firebase.auth.signOut()
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                }
            }
        }
        drawer.closeDrawer(GravityCompat.START)
        return true
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
            if (blocked|| unPermitted){
                finish()
            }
            findNavController(R.id.nav_host).popBackStack()
        }
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.appBarMain.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        this.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    companion object {
        var blocked = false
        var unPermitted = false
    }
}