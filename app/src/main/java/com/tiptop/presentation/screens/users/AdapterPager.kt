package com.tiptop.presentation.screens.users

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tiptop.presentation.screens.users.accounts.PageAccounts
import com.tiptop.presentation.screens.users.devices.PageDevices

class AdapterPager(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount() = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> PageAccounts()
            1 -> PageDevices.newInstance(1)
            2 -> PageDevices.newInstance(2)
            3 -> PageDevices.newInstance(3)
            else -> PageAccounts()
        }
    }
}