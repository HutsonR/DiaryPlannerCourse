package com.easyflow.diarycourse.features.feature_settings.security

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.core.BaseFragment
import com.easyflow.diarycourse.core.utils.SharedPreferencesHelper
import com.easyflow.diarycourse.core.utils.fingerprint.CryptoUtils
import com.easyflow.diarycourse.core.utils.fingerprint.FingerprintHelper
import com.easyflow.diarycourse.core.utils.fingerprint.FingerprintUtils
import com.easyflow.diarycourse.core.utils.fingerprint.TouchIdRandomGenerator
import com.easyflow.diarycourse.databinding.FragmentSettingsSecurityBinding
import dagger.Lazy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class SecurityFragment : BaseFragment() {
    private var _binding: FragmentSettingsSecurityBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var securityViewModelFactory: Lazy<SecurityViewModel.SecurityViewModelFactory>
    private val viewModel: SecurityViewModel by viewModels {
        securityViewModelFactory.get()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsSecurityBinding.inflate(inflater, container, false)
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
        binding.securityToolbar.toolbar.title = getString(R.string.settings_security)
        binding.securityToolbar.toolbar.setNavigationOnClickListener {
            viewModel.goBack()
        }
    }

    private fun setClickListeners() {
        binding.settingFingerprintSwitchButton.setOnCheckedChangeListener { _, _ ->
            onFingerprintSwitchButtonClicked()
        }
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
                    is SecurityViewModel.Actions.GoBack -> popBackStack()
                    is SecurityViewModel.Actions.SwitchFingerprint -> setFingerprints()
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun onFingerprintSwitchButtonClicked() {
        val manager = FingerprintManagerCompat.from(requireContext())

        if (manager.isHardwareDetected && manager.hasEnrolledFingerprints()) {
            viewModel.makeFingerprint()
        } else {
            Toast.makeText(
                requireContext(),
                "На вашем устройстве не поддерживается разблокировка по отпечатку пальца",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setFingerprints() {
        CryptoUtils().deleteInvalidKey()
        val touchId = TouchIdRandomGenerator().generateTouchId()
        CryptoUtils().encode(touchId.bytes)?.let { SharedPreferencesHelper.setTouchId(requireContext(), it) }
        prepareSensor()
    }

    private fun prepareSensor() {
        if (FingerprintUtils().isSensorStateAt(FingerprintUtils.SensorState.READY, requireContext())) {
            val cryptoObject: FingerprintManagerCompat.CryptoObject? = CryptoUtils().getCryptoObject()
            if (cryptoObject != null) {
                Toast.makeText(requireContext(), "use fingerprint to login", Toast.LENGTH_LONG).show()
                FingerprintHelper(requireContext()).startAuth(cryptoObject)
            } else {
                SharedPreferencesHelper.setTouchId(requireContext(), "")
                Toast.makeText(
                    requireContext(),
                    "new fingerprint enrolled. enter pin again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}