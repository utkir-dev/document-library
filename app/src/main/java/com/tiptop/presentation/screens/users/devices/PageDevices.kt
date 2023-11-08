package com.tiptop.presentation.screens.users.devices

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.tiptop.R
import com.tiptop.app.common.DebouncingQueryTextListener
import com.tiptop.app.common.Status
import com.tiptop.app.common.huminize
import com.tiptop.app.common.isInternetAvailable
import com.tiptop.data.models.local.DeviceLocal
import com.tiptop.databinding.DialogEditDeviceBinding
import com.tiptop.databinding.PageAccountsDevicesBinding
import com.tiptop.presentation.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

private const val ARG_PARAM = "param"

@AndroidEntryPoint
class PageDevices : BaseFragment(R.layout.page_accounts_devices) {
    private var param: Int? = null
    private var searchText = ""
    private var device: DeviceLocal? = null
    private lateinit var binding: PageAccountsDevicesBinding
    private var vm: DevicesViewModel? = null
    private val vmAllDevices by viewModels<DevicesViewModelImpl>()
    private val vmAdminDevices by viewModels<AdminDevicesViewModelImpl>()
    private val vmBlockedDevices by viewModels<BlockedDevicesViewModelImpl>()
    private var isDeviceInfoShown = false
    private var adapter: AdapterDevices? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param = it.getInt(ARG_PARAM)
            vm = when (param) {
                1 -> vmAllDevices
                2 -> vmAdminDevices
                else -> vmBlockedDevices
            }
            vm?.observeDevices()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PageAccountsDevicesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = AdapterDevices(listener = object : AdapterDevices.DeviceClickListener {
            override fun onClick(deviceLocal: DeviceLocal, v: View, positionDevice: Int) {
                device = deviceLocal
                popupDevice(deviceLocal, v)
            }
        })
        // adapter.setHasStableIds(true)
        binding.rv.adapter = adapter
        vm?.devices?.observe(viewLifecycleOwner) { devices ->
            devices.forEach { it.searchText = searchText }
            if (devices.isEmpty()) {
                binding.tvBlockedDecices.visibility = View.VISIBLE
            } else {
                binding.tvBlockedDecices.visibility = View.GONE
            }
            adapter?.submitList(devices)
        }
        setObservers()
    }

    override fun onResume() {
        super.onResume()
        val searchView = requireActivity().findViewById<SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(DebouncingQueryTextListener(
            requireActivity().lifecycle
        ) {
            searchText = it ?: ""
            if (searchText.isNotEmpty()) {
                vm?.searchDevice(searchText)
            } else {
                vm?.observeDevices()
            }
        })
    }

    private fun setObservers() {
        vm?.resultUpdate?.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    binding.progress.visibility = View.VISIBLE
                }

                Status.SUCCESS -> {
                    binding.progress.visibility = View.GONE
                    device?.let { d ->
                        val index =
                            adapter?.currentList?.indexOf(adapter?.currentList?.find { it.id == d.id })
                        adapter?.notifyItemChanged(index ?: 0)

                    }
                    showSnackBar("Muvaffaqiyatli yangilandi !")
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

        vm?.resultDelete?.observe(viewLifecycleOwner) {
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
        vm?.users?.observe(viewLifecycleOwner) { users ->
            try {
                if (!isDeviceInfoShown) {
                    isDeviceInfoShown = true
                    var userName = ""
                    if (users?.isNotEmpty() == true) {
                        val createdUsersByDevice = users.map { it.email }
                        if (createdUsersByDevice.isNotEmpty()) {
                            userName =
                                if (createdUsersByDevice.size > 1) createdUsersByDevice.huminize() + " lar" else createdUsersByDevice[0] + " "
                            userName = "${userName}ni yaratgan. "
                        }
                    }
                    val isAdmin =
                        if (device?.admin == true) "Admin" else ""
                    val isBlocked =
                        if (device?.blocked == true) "Taqiqlangan" else ""
                    val message =
                        "Bu qurilma ${device?.dateAdded?.huminize()} da yaratilgan. ${userName}Oxirgi kirgan sana ${device?.date?.huminize()}. $isAdmin$isBlocked"
                    showAllertDialog(title = device?.name ?: "", message = message) {
                    }
                }
            } catch (_: Exception) {
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
                    vm?.updateDevice(device)
                }
            } else {
                showSnackBar("Internet yo'q")
            }
            allert.cancel()
        }
    }

    private fun getInfoDevice(d: DeviceLocal) {
        device = d
        vm?.searchUsersByDeviceId(deviceId = d.id)
    }

    private fun deleteDevice(device: DeviceLocal) {
        showConfirmDialog(
            title = device.name,
            message = "Ushbu qurilmani o'chirishga ishinchingiz komilmi ?"
        ) { response ->
            if (response) {
                if (isInternetAvailable(requireContext())) {
                    vm?.deleteDevice(device.id)
                } else {
                    showSnackBar("Internet yo'q")
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param: Int) =
            PageDevices().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM, param)
                }
            }
    }
}