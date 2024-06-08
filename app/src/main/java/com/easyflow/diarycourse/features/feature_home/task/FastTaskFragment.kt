package com.easyflow.diarycourse.features.feature_home.task

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.core.utils.collectOnStart
import com.easyflow.diarycourse.databinding.FragmentFastTaskBinding
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.features.feature_home.task.dialogs.TaskDurationDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.Lazy
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class FastTaskFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentFastTaskBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var fastTaskViewModelFactory: Lazy<FastTaskViewModel.FastTaskViewModelFactory>
    private val viewModel: FastTaskViewModel by viewModels {
        fastTaskViewModelFactory.get()
    }

    private var currentTask = ScheduleItem(
        text = "",
        description = "",
        date = "",
        startTime = "",
        endTime = ""
    )

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setBottomDialogStyle()
        _binding = FragmentFastTaskBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    private fun setBottomDialogStyle() {
        dialog?.setOnShowListener { dialog ->
            val layout: FrameLayout? = (dialog as BottomSheetDialog).
            findViewById(com.google.android.material.R.id.design_bottom_sheet)
            layout?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.isDraggable = false
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        setObservers()
    }

    private fun initialize() {
        viewModel.updateTask(currentTask)
        binding.titleTask.requestFocus()
        setListeners()
    }

    private fun setObservers() {
        viewModel.state.onEach(::handleState).collectOnStart(viewLifecycleOwner)
        viewModel.action.onEach(::handleActions).collectOnStart(viewLifecycleOwner)
    }

    private fun handleState(state: FastTaskViewModel.State) {
        updateItem(state.task)
    }

    private fun updateItem(item: ScheduleItem?) {
        item?.let { currentTask = it }
    }

    private fun handleActions(action: FastTaskViewModel.Actions) {
        when (action) {
            is FastTaskViewModel.Actions.GoBack -> dismiss()
            is FastTaskViewModel.Actions.GoToDurationDialog -> TaskDurationDialog().show(childFragmentManager, TaskDurationDialog.TAG)
            is FastTaskViewModel.Actions.GoToPriorityDialog -> showPriorityMenu(binding.taskPriorityButton)
            is FastTaskViewModel.Actions.GoToColorDialog -> showColorMenu(binding.taskColorButton)
            is FastTaskViewModel.Actions.UpdateSaveButtonState -> updateSaveButtonState(action.state)
        }
    }

    private fun showPriorityMenu(view: View) {
        val contextThemeWrapper = ContextThemeWrapper(requireContext(), R.style.CustomPopupMenuStyle)
        val popupMenu = PopupMenu(contextThemeWrapper, view)
        popupMenu.menuInflater.inflate(R.menu.priority_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.priorityBase -> {
                    Toast.makeText(requireContext(), "Приоритет 1", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.priorityHigh -> {
                    Toast.makeText(requireContext(), "Приоритет 2", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showColorMenu(view: View) {
        val contextThemeWrapper = ContextThemeWrapper(requireContext(), R.style.CustomPopupMenuStyle)
        val popupMenu = PopupMenu(contextThemeWrapper, view)
        popupMenu.inflate(R.menu.color_menu)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true)
        }

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.colorBlue -> {
                    Toast.makeText(requireContext(), "Синий", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.colorGreen -> {
                    Toast.makeText(requireContext(), "зелёный", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.colorRed -> {
                    Toast.makeText(requireContext(), "красный", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.colorPurple -> {
                    Toast.makeText(requireContext(), "фиолетовый", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.colorPink -> {
                    Toast.makeText(requireContext(), "розовый", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun updateSaveButtonState(state: Boolean) {
        Log.d("debugTag", "FRAGMENT updateSaveButtonState")
        binding.saveButton.apply {
            isEnabled = state
            alpha = if (state) 1.0f else 0.6f
        }
    }

    private fun setListeners() {
        binding.taskDurationButton.setOnClickListener { viewModel.openDurationDialog() }
        binding.taskPriorityButton.setOnClickListener { viewModel.openPriorityDialog() }
        binding.taskColorButton.setOnClickListener { viewModel.openColorDialog() }
        titleListener()
        descriptionListener()
    }

    private fun titleListener() {
        binding.titleTask.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateTask(currentTask.copy(text = s.toString()))
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun descriptionListener() {
        binding.descriptionTask.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateTask(currentTask.copy(description = s.toString()))
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}