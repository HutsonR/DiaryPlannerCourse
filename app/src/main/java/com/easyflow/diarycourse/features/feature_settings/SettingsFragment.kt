package com.easyflow.diarycourse.features.feature_settings

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import com.easyflow.diarycourse.core.BaseFragment
import com.easyflow.diarycourse.databinding.FragmentSettingsBinding

class SettingsFragment : BaseFragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
    }

    private fun initialize() {
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.settingTheme.setOnClickListener { themeSwitch() }
    }

    private fun themeSwitch() {
        if (isDarkTheme()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        requireActivity().recreate()
    }

    private fun isDarkTheme(): Boolean {
        val currentTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentTheme == Configuration.UI_MODE_NIGHT_YES
    }

}