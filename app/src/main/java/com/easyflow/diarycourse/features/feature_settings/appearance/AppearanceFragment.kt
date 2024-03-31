package com.easyflow.diarycourse.features.feature_settings.appearance

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.core.BaseFragment
import com.easyflow.diarycourse.databinding.FragmentSettingsAppearanceBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationBarView
import dagger.Lazy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class AppearanceFragment : BaseFragment() {
    private var _binding: FragmentSettingsAppearanceBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var appearanceViewModelFactory: Lazy<AppearanceViewModel.AppearanceViewModelFactory>
    private val viewModel: AppearanceViewModel by viewModels {
        appearanceViewModelFactory.get()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsAppearanceBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        setObservers()
    }

    private fun initialize() {
        initializeNavBar()
        setClickListeners()
    }

    private fun initializeNavBar() {
        binding.appearanceToolbar.toolbar.title = getString(R.string.settings_theme)
        binding.appearanceToolbar.toolbar.setNavigationOnClickListener {
            viewModel.goBack()
        }
    }

    private fun setClickListeners() {
        binding.settingThemeMode.setOnClickListener { openThemeSwitchDialog() }
        binding.settingThemeNavigation.setOnClickListener { openNavigationPanelSwitchDialog() }
    }

    private fun setObservers() {
        observeActions()
    }

    private fun observeActions() {
        viewModel
            .action
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { action ->
                when (action) {
                    is AppearanceViewModel.Actions.GoBack -> popBackStack()
                    is AppearanceViewModel.Actions.SwitchTheme -> themeSwitch(action.mode)
                    is AppearanceViewModel.Actions.SwitchNavigation -> navigationPanelSwitch(action.mode)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun themeSwitch(mode: Int) {
        when (mode) {
            0 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            1 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            2 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
        requireActivity().recreate()
    }

    private fun isDarkTheme(mode: Int): Boolean {
        val currentTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentTheme == Configuration.UI_MODE_NIGHT_YES
    }

    private fun navigationPanelSwitch(mode: Int) {
        val bottomNavigationView: BottomNavigationView = requireView().findViewById(R.id.bottomNavigation)
        when (mode) {
            0 -> {
                bottomNavigationView.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_LABELED
            }
            1 -> {
                bottomNavigationView.labelVisibilityMode = NavigationBarView.LABEL_VISIBILITY_UNLABELED
            }
        }
    }

    private fun openThemeSwitchDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.settings_theme_mode))
            .setSingleChoiceItems(
                arrayOf(
                    resources.getString(R.string.settings_theme_mode_auto),
                    resources.getString(R.string.settings_theme_mode_light),
                    resources.getString(R.string.settings_theme_mode_dark)
                ), 0
            ) { dialog, which ->
                viewModel.themeSwitchMode(which)
                dialog.dismiss()
            }
            .show()
    }

    private fun openNavigationPanelSwitchDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.settings_theme_navigation))
            .setSingleChoiceItems(
                arrayOf(
                    resources.getString(R.string.settings_theme_navigation_full),
                    resources.getString(R.string.settings_theme_navigation_compact)
                ), 0
            ) { dialog, which ->
                viewModel.switchNavigation(which)
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}