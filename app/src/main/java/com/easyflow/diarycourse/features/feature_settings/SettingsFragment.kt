package com.easyflow.diarycourse.features.feature_settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.core.BaseFragment
import com.easyflow.diarycourse.databinding.FragmentSettingsBinding
import dagger.Lazy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class SettingsFragment : BaseFragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var settingsViewModelFactory: Lazy<SettingsViewModel.SettingsViewModelFactory>
    private val viewModel: SettingsViewModel by viewModels {
        settingsViewModelFactory.get()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).appComponent.inject(this)
    }

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
        setObservers()
    }

    private fun initialize() {
        setListeners()
    }

    private fun setListeners() {
        binding.settingTheme.setOnClickListener { viewModel.goToAppearance() }
        binding.settingFingerprint.setOnClickListener { viewModel.goToSecurity() }
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
                    is SettingsViewModel.Actions.GoToAppearance -> navigateTo(R.id.actionSettingsGoToAppearance)
                    is SettingsViewModel.Actions.GoToSecurity -> navigateTo(R.id.actionSettingsGoToSecurity)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

}