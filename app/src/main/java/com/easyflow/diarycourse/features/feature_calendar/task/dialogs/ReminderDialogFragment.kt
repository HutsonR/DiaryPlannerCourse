package com.easyflow.diarycourse.features.feature_calendar.task.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.databinding.DialogFragmentReminderBinding
import dagger.Lazy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject


class ReminderDialogFragment : DialogFragment() {
    private var _binding: DialogFragmentReminderBinding? = null
    private val binding get() = _binding!!
    @Inject
    lateinit var reminderViewModelFactory: Lazy<ReminderDialogViewModel.ReminderViewModelFactory>
    private val viewModel: ReminderDialogViewModel by viewModels {
        reminderViewModelFactory.get()
    }
    private lateinit var saveButton: LinearLayout
    private lateinit var cancelButton: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DialogFragmentReminderBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initialize()
        setObservers()
    }

    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog!!.window?.setLayout(width, height)
    }

    private fun initialize() {
        saveButton = binding.btnSave
        cancelButton = binding.btnClose

        setListeners()
    }

    private fun setListeners() {
        saveButton.setOnClickListener {
            viewModel.sendItem()
        }
        cancelButton.setOnClickListener {
            viewModel.sendItem(true)
        }

        timeListener()
    }

    private fun setObservers() {
        observeState()
        observeActions()
    }

    private fun observeState() {
        viewModel
            .state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                timeCollect(state.time)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun timeCollect(time: String) {
        updateSaveButtonState(viewModel.saveButtonCheck())
    }

    private fun observeActions() {
        viewModel
            .action
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { action ->
                when (action) {
                    is ReminderDialogViewModel.Actions.GoBack -> sendItem(action.item)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun updateSaveButtonState(isEnabled: Boolean) {
        if (isEnabled) {
            saveButton.isEnabled = true
            saveButton.alpha = 1.0f
        } else {
            saveButton.isEnabled = false
            saveButton.alpha = 0.5f
        }
    }

    private fun sendItem(item: String) {
        val bundle = Bundle().apply {
            putString(FRAGMENT_REMIND_ITEM, item)
        }
        activity?.supportFragmentManager?.setFragmentResult(KEY_REMINDER_FRAGMENT_RESULT_SET, bundle)
        dismiss()
    }


//    Listeners
    private fun timeListener() {
        binding.timePickerReminder.setIs24HourView(true)
        binding.timePickerReminder.setOnTimeChangedListener { _, hourOfDay, minute ->
            val selectedTimeForStart = Calendar.getInstance()
            selectedTimeForStart.set(Calendar.HOUR_OF_DAY, hourOfDay)
            selectedTimeForStart.set(Calendar.MINUTE, minute)

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTime = selectedTimeForStart.time.let { timeFormat.format(it) }
            viewModel.setTime(formattedTime)
        }
    }


    companion object {
        const val KEY_REMINDER_FRAGMENT_RESULT_SET = "KEY_REMINDER_FRAGMENT_RESULT_SET"

        const val FRAGMENT_REMIND_ITEM = "REMIND_ITEM"
    }
}