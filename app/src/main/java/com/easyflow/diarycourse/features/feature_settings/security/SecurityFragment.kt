package com.easyflow.diarycourse.features.feature_settings.security

import android.content.Context
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.core.BaseFragment
import com.easyflow.diarycourse.core.utils.SharedPreferencesHelper
import com.easyflow.diarycourse.core.utils.fingerprint.common.BiometricAuthListener
import com.easyflow.diarycourse.core.utils.fingerprint.common.TouchIdRandomGenerator
import com.easyflow.diarycourse.core.utils.fingerprint.utils.BiometricUtils
import com.easyflow.diarycourse.core.utils.fingerprint.utils.CryptoUtils
import com.easyflow.diarycourse.databinding.FragmentSettingsSecurityBinding
import dagger.Lazy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class SecurityFragment : BaseFragment(), BiometricAuthListener {
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
        if (BiometricUtils.isBiometricReady(requireContext())) {
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

        val encoded = CryptoUtils().encode(touchId.bytes)
        Log.d("debugTag", "FRAGMENT setFingerprints encoded(base64) $encoded")
        encoded?.let { SharedPreferencesHelper.setTouchId(requireContext(), it) }
        prepareSensor()
    }

    private fun prepareSensor() {
        Toast.makeText(requireContext(), "use fingerprint to login", Toast.LENGTH_LONG).show()
        BiometricUtils.showBiometricPrompt(
            activity = requireActivity(),
            listener = this,
            cryptoObject = CryptoUtils().getCryptoObject(),
        )
    }

    override fun onBiometricAuthenticateError(error: Int, errMsg: String) {
        when (error) {
            BiometricPrompt.ERROR_USER_CANCELED -> showBiometricAuthenticateError()
            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> showBiometricAuthenticateError()
        }
    }

    override fun onBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult) {
        showBiometricAuthenticateSuccess(result)
    }

    private fun showBiometricAuthenticateError() {
        binding.settingFingerprintSwitchButton.isChecked = false
        Toast.makeText(
            requireContext(),
            "Отмена использования отпечатков пальцев",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showBiometricAuthenticateSuccess(result: BiometricPrompt.AuthenticationResult) {
        val cipher = result.cryptoObject?.cipher
        Log.d("debugTag", "FRAGMENT showBiometricAuthenticateSuccess - cipher $cipher")
        val encoded: String? = SharedPreferencesHelper.getTouchId(requireContext())
        Log.d("debugTag", "FRAGMENT showBiometricAuthenticateSuccess - encoded(base64) $encoded")
        val decoded: String? = cipher?.let { CryptoUtils().decode(encoded, it) }
        Log.d("debugTag", "FRAGMENT showBiometricAuthenticateSuccess - decoded $decoded")

        val bytes = Base64.decode(encoded, Base64.NO_WRAP)
        Log.d("debugTag", "FRAGMENT showBiometricAuthenticateSuccess - decoded(base64) ${bytes.joinToString { it.toString() }}")
        Toast.makeText(context, "success", Toast.LENGTH_SHORT).show()

        binding.settingFingerprintSwitchButton.isChecked = true
        Toast.makeText(
            requireContext(),
            "Отпечатки пальцев зарегистированы",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}