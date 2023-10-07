package com.tiptop.presentation.screens.users.accounts

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.tiptop.R
import com.tiptop.app.common.DebouncingQueryTextListener
import com.tiptop.app.common.Encryptor
import com.tiptop.app.common.Status
import com.tiptop.app.common.Utils
import com.tiptop.app.common.encryption
import com.tiptop.app.common.huminize
import com.tiptop.app.common.isInternetAvailable
import com.tiptop.data.models.local.DeviceLocal
import com.tiptop.data.models.local.UserLocal
import com.tiptop.databinding.DialogEditDeviceBinding
import com.tiptop.databinding.DialogEditUserBinding
import com.tiptop.databinding.PageAccountsDevicesBinding
import com.tiptop.databinding.PopupAccountBinding
import com.tiptop.presentation.screens.BaseFragment
import com.tiptop.presentation.screens.users.devices.AdapterDevices
import com.tiptop.presentation.screens.users.devices.AdapterDevices.DeviceClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PageAccounts : BaseFragment(R.layout.page_accounts_devices) {
    private lateinit var binding: PageAccountsDevicesBinding
    private var searchText = ""
    private val vm: AccountsViewModelImpl by viewModels()
    private var isUserInfoShown = false
    private var isDeviceInfoShown = false
    private var isCheckUserShown = false
    private var editedUser: UserLocal? = null
    private var editedDevice: DeviceLocal? = null
    private var mapInnerAdapters = HashMap<String, AdapterDevices>()
    private var observer: Observer<Map<UserLocal, List<DeviceLocal>>>? = null
    private var adapter: AdapterAccounts? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = PageAccountsDevicesBinding.inflate(layoutInflater, container, false)
        vm.observeDevices()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
        adapter = AdapterAccounts(
            listener = object : AdapterAccounts.UserClickListener {
                override fun onClickMore(user: UserLocal, v: View) {
                    editedUser = user
                    popupAccount(user, v)
                }

                override fun onClickCheck(user: UserLocal) {
                    editedUser = user
                    isCheckUserShown = false
                    checkUser(user)
                }

                override fun onClickRv(user: UserLocal, rv: RecyclerView, positionUser: Int) {
                    editedUser = user
                    val adapterInner =
                        AdapterDevices(listener = object : DeviceClickListener {
                            override fun onClick(
                                device: DeviceLocal,
                                v: View,
                                positionDevice: Int
                            ) {
                                editedUser = user
                                editedDevice = device
                                popupDevice(device, v)
                            }
                        })
                    mapInnerAdapters[user.id] = adapterInner
                    //  adapterInner.setHasStableIds(true)
                    rv.adapter = adapterInner
                    observer?.let {
                        vm.userAndDevices.removeObserver(it)
                    }
                    observer = Observer { devices ->
                        adapterInner.submitList(devices[user]?.toList())
                    }
                    vm.userAndDevices.observe(viewLifecycleOwner, observer!!)
                }
            })
        //  adapter.setHasStableIds(true)
        binding.rv.adapter = adapter

        vm.userAndDevices.observe(viewLifecycleOwner) { mapUsers ->
            val listUsers = mapUsers.keys
            listUsers.forEach { it.searchText = searchText }
            adapter?.submitList(listUsers.toList())
        }
    }

    override fun onResume() {
        super.onResume()
        val searchView = requireActivity().findViewById<SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(DebouncingQueryTextListener(
            requireActivity().lifecycle
        ) {
            searchText = it ?: ""
            if (searchText.isNotEmpty()) {
                vm.searchUser(searchText)
            } else {
                vm.observeDevices()
            }
        })
    }

    private fun setObservers() {
        vm.resultPassword.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.progress.visibility = View.VISIBLE
                }

                Status.SUCCESS -> {
                    binding.progress.visibility = View.GONE
                    val password = it.data ?: "Parol olishda xatolik !"
                    showAllertDialog(title = "Parol: $password") {}
                }

                Status.DEFAULT -> {
                    binding.progress.visibility = View.GONE
                }

                Status.ERROR -> {
                    binding.progress.visibility = View.GONE
                    showAllertDialog(title = "Parol olishda xatolik sodir bo'ldi !") {}
                }
            }
        }
        vm.resultUpdateDevice.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.progress.visibility = View.VISIBLE
                }

                Status.SUCCESS -> {
                    binding.progress.visibility = View.GONE
                    editedUser?.let { user ->
                        editedDevice?.let { device ->
                            val index =
                                mapInnerAdapters[user.id]?.currentList?.indexOf(mapInnerAdapters[user.id]?.currentList?.find { it.id == device.id })
                            mapInnerAdapters[user.id]?.notifyItemChanged(index ?: 0)
                        }
                    }
                    //  showSnackBar("Muvaffaqiyatli yangilandi !")
                }

                Status.DEFAULT -> {
                    binding.progress.visibility = View.GONE
                }

                Status.ERROR -> {
                    binding.progress.visibility = View.GONE
                    showSnackBar("Xatolik sodir bo'ldi !")
                }
            }
        }
        vm.resultUpdateUser.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.progress.visibility = View.VISIBLE
                }

                Status.SUCCESS -> {
                    binding.progress.visibility = View.GONE
                    editedUser?.let { user ->
                        val indexParent =
                            adapter?.currentList?.indexOf(adapter?.currentList?.find { it.id == user.id })
                        adapter?.notifyItemChanged(indexParent ?: 0)
                    }
                    //  showSnackBar("Muvaffaqiyatli yangilandi !")
                }

                Status.DEFAULT -> {
                    binding.progress.visibility = View.GONE
                }

                Status.ERROR -> {
                    binding.progress.visibility = View.GONE
                    showSnackBar("Xatolik sodir bo'ldi !")
                }
            }
        }
        vm.resultDelete.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.progress.visibility = View.VISIBLE
                }

                Status.SUCCESS -> {
                    binding.progress.visibility = View.GONE
                    showSnackBar("Muvaffaqiyatli o'chirildi !")
                }

                Status.DEFAULT -> {
                    binding.progress.visibility = View.GONE
                }

                Status.ERROR -> {
                    binding.progress.visibility = View.GONE
                    showSnackBar("Xatolik sodir bo'ldi !")
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun popupAccount(user: UserLocal, v: View) {
        val view = PopupAccountBinding.inflate(layoutInflater)
        val popupRight = PopupWindow(
            view.root, // Custom view to show in popup window
            LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT // Window height
        )

        view.tvPasswordUser.setOnClickListener {
            popupRight.dismiss()
            getPasswordUser(user)
        }
        view.tvEditUser.setOnClickListener {
            popupRight.dismiss()
            editUser(user)
        }
        view.tvInfoUser.setOnClickListener {
            popupRight.dismiss()
            isUserInfoShown = false
            getInfoUser(user)
        }
        view.tvDeleteUser.setOnClickListener {
            popupRight.dismiss()
            deleteUser(user)
        }
        popupRight.isOutsideTouchable = true
        popupRight.elevation = 20.0F
        val slideIn = Slide()
        slideIn.slideEdge = Gravity.END
        popupRight.enterTransition = slideIn

        val slideOut = Slide()
        slideOut.slideEdge = Gravity.END
        popupRight.exitTransition = slideOut

        TransitionManager.beginDelayedTransition(binding.root)
        val location = IntArray(2)
        v.getLocationOnScreen(location)
        popupRight.showAtLocation(
            binding.root, Gravity.NO_GRAVITY, // root, Gravity.NO_GRAVITY,
            location[0] + v.measuredWidth,
            location[1] + v.measuredHeight
        )
    }

    private fun checkUser(user: UserLocal) {
        vm.userAndDevices.observe(viewLifecycleOwner) { mapUserAndDevices ->
            if (mapUserAndDevices?.isNotEmpty() == true) {
                if (!isCheckUserShown) {
                    isCheckUserShown = true
                    val device = mapUserAndDevices.values
                        .map { it.find { it.id == user.deviceId } }
                        .find { it?.id == user.deviceId }
                    val users = mapUserAndDevices.keys.filter { it.deviceId == user.deviceId }

                    var emails = " "
                    val comma = ", "
                    users.let { list ->
                        if (list.size == 1) {
                            emails = list[0].email
                        } else if (list.size > 1) {
                            list.map {
                                emails =
                                    "${it.email}ni ${it.dateAdded.huminize()}da${if (emails.isBlank()) emails else comma + emails}"
                            }
                        }
                    }

                    val additionalInfo =
                        if (emails.isBlank()) emails else "Qo'shimcha ma'lumot: ${emails}ochgan"
                    val telegram =
                        if (user.telegramUser.isEmpty()) "" else "telegram: ${user.telegramDecrypted()},\n"
                    val message =
                        "$telegram${device?.name} qurilma egasi a'zo bo'lishni so'ramoqda. $additionalInfo"
                    showConfirmDialog(
                        title = user.email,
                        message = message
                    ) { response ->
                        if (response) {
                            user.permitted = true
                            user.date = System.currentTimeMillis()
                            vm.updateUser(user)
                        } else {
                            vm.deleteUser(user)
                        }

                    }

                }
            }
        }
    }

    private fun getPasswordUser(user: UserLocal) {
        if (isInternetAvailable(requireContext())) {
            vm.getUserPassword(userId = user.id)
        } else {
            showSnackBar("Internet yo'q")
        }
    }

    private fun getInfoUser(user: UserLocal) {
        vm.userAndDevices.observe(viewLifecycleOwner) { mapUserAndDevices ->
            if (mapUserAndDevices?.isNotEmpty() == true) {
                if (!isUserInfoShown) {
                    isUserInfoShown = true
                    val device = mapUserAndDevices.values.map { it.find { it.userId == user.id } }
                        .find { it?.userId == user.id }
                    val deviceName =
                        if (device == null) "o'chirib yuborilgan" else "nomi ${device.name}"
                    val isPermitted =
                        if (user.permitted) "Kirishga ruxsat berilgan" else "Kirishga ruxsat yuq"
                    val telegram =
                        if (user.telegramUser.isEmpty()) "Telegram nomi kiritilmagan" else "Telegram nomi ${user.telegramDecrypted()}"
                    val message =
                        "Bu email ${user.dateAdded.huminize()} da yaratilgan. Yaratgan qurilma $deviceName. $isPermitted. $telegram. Oxirgi kirgan sana ${user.date.huminize()}"
                    showAllertDialog(title = user.email, message = message) {
                    }
                }
            }
        }
    }

    private fun editUser(user: UserLocal) {
        val view = DialogEditUserBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext(), R.style.MyDialogStyle).apply {
            setView(view.root)
                .setCancelable(true)
                .create()
        }
        val allert = dialog.show()
        val firstCheckedState = user.permitted
        val firstTelegramName = user.telegramDecrypted()
        view.tvEmail.text = user.email
        view.etTelegramUser.setText(firstTelegramName)
        view.chbPermission.isChecked = firstCheckedState
        view.btnConfirm.setOnClickListener {
            if (isInternetAvailable(requireContext())) {
                val lastCheckedState = view.chbPermission.isChecked
                val lastTelegramName = view.etTelegramUser.text.toString().trim()
                if (firstCheckedState != lastCheckedState || firstTelegramName != lastTelegramName) {
                    user.permitted = lastCheckedState
                    user.telegramUser = lastTelegramName.encryption(user.dateAdded)
                    user.date = System.currentTimeMillis()
                    vm.updateUser(user)
                }
            } else {
                showSnackBar("Internet yo'q")
            }
            allert.cancel()
        }
    }

    private fun deleteUser(user: UserLocal) {
        showConfirmDialog(
            title = "Diqqat !",
            message = "${user.email} ni o'chirishga ishonchingiz komilmi ?"
        ) { response ->
            if (response) {
                if (isInternetAvailable(requireContext())) {
                    vm.deleteUser(user = user)
                } else {
                    showSnackBar("Internet yo'q")
                }
            }
        }
    }

    fun popupDevice(device: DeviceLocal, v: View) {

        showPopup(v, binding.root) { viewId ->
            when (viewId) {
                R.id.tv_edit_device -> {
                    editDevice(device)
                }

                R.id.tv_info_device -> {
                    isDeviceInfoShown = false
                    getInfoDevice(device)
                }

                R.id.tv_delete_device -> {
                    deleteDevice(device)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun editDevice(device: DeviceLocal) {
        val view = DialogEditDeviceBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext(), R.style.MyDialogStyle).apply {
            setView(view.root)
                .setCancelable(true)
                .create()
        }
        val allert = dialog.show()
        val firstAdminState = device.admin
        val firstBlockState = device.blocked
        view.tvDeviceName.text = device.name
        view.tvDeviceId.text = "id: " + device.id

        view.chbMakeAdmin.isChecked = firstAdminState
        view.chbBlock.isChecked = firstBlockState
        view.btnConfirm.setOnClickListener {
            if (isInternetAvailable(requireContext())) {
                val lastAdminState = view.chbMakeAdmin.isChecked
                val lastBlockState = view.chbBlock.isChecked
                if (firstAdminState != lastAdminState || firstBlockState != lastBlockState) {
                    device.admin = lastAdminState
                    device.blocked = lastBlockState
                    device.date = System.currentTimeMillis()
                    vm.updateDevice(device)
                }
            } else {
                showSnackBar("Internet yo'q")
            }
            allert.cancel()
        }
    }

    private fun getInfoDevice(device: DeviceLocal) {
        vm.userAndDevices.observe(viewLifecycleOwner) { mapUserAndDevices ->
            if (mapUserAndDevices?.isNotEmpty() == true) {
                if (!isDeviceInfoShown) {
                    isDeviceInfoShown = true
                    val createdUsersByDevice =
                        mapUserAndDevices.keys.filter { it.deviceId == device.id }.map { it.email }
                    val userName =
                        if (createdUsersByDevice.size > 1) createdUsersByDevice.huminize() + " lar" else createdUsersByDevice[0] + " "
                    val isAdmin =
                        if (device.admin) "Admin" else ""
                    val isBlocked =
                        if (device.blocked) "Taqiqlangan" else ""
                    val message =
                        "Bu qurilma ${device.dateAdded.huminize()} da yaratilgan. ${userName}ni yaratgan. Oxirgi kirgan sana ${device.date.huminize()}. $isAdmin$isBlocked"
                    showAllertDialog(title = device.name, message = message) {}
                }
            }
        }
    }

    private fun deleteDevice(device: DeviceLocal) {
        showConfirmDialog(
            title = device.name,
            message = "Ushbu qurilmani o'chirishga ishinchingiz komilmi ?"
        ) { response ->
            if (response) {
                if (isInternetAvailable(requireContext())) {
                    vm.deleteDevice(device.id)
                } else {
                    showSnackBar("Internet yo'q")
                }
            }
        }
    }
}