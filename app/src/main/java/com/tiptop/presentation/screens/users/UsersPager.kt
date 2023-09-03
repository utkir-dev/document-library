package com.tiptop.presentation.screens.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tiptop.R
import com.tiptop.app.common.showKeyboard
import com.tiptop.databinding.ScreenUsersBinding
import com.tiptop.presentation.screens.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class UsersPager : BaseFragment(R.layout.screen_users) {
    private val tabItems = listOf("Akkauntlar", "Qurilmalar", "Adminlar", "Taqiqlanganlar")
    private lateinit var binding: ScreenUsersBinding
    private val vm by viewModels<UsersPagerViewModelImpl>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ScreenUsersBinding.inflate(layoutInflater, container, false)
        vm.init()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.ivBack.setOnClickListener { findNavController().popBackStack() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = binding.pagerUsers
        val tabLayout = binding.tabUsers

        val adapter = AdapterPager(childFragmentManager, lifecycle)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabItems[position]
        }.attach()
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                binding.searchView.visibility = View.GONE
                binding.searchView.setQuery("", true)
            }
        })
        binding.ivSearch.setOnClickListener {
            binding.searchView.visibility = View.VISIBLE
            binding.searchView.requestFocus()
            showKeyboard(requireContext(), binding.searchView)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (binding.searchView.visibility == View.VISIBLE) {
                binding.searchView.visibility = View.GONE
            } else {
                findNavController().popBackStack()
            }
        }
    }

}