package com.easyflow.diarycourse.features.feature_home.task

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.core.utils.collectOnStart
import com.easyflow.diarycourse.databinding.FragmentFastTaskBinding
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.Priority
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.TaskColor
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

    private var currentTask: ScheduleItem = createScheduleItem()

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
        currentTask = viewModel.getCurrentTask() ?: createScheduleItem()
        binding.titleTask.requestFocus()
        setListeners()
    }

    private fun createScheduleItem(): ScheduleItem = ScheduleItem(
        text = "",
        description = "",
        date = "",
        startTime = "",
        endTime = ""
    )

    private fun setObservers() {
        viewModel.state.onEach(::handleState).collectOnStart(viewLifecycleOwner)
        viewModel.action.onEach(::handleActions).collectOnStart(viewLifecycleOwner)
    }

    private fun handleState(state: FastTaskViewModel.State) {
        state.task?.let {
            currentTask = it
            updatePriorityButton(it.priority)
        }
        setStyle(state.taskColor)
        updateSaveButtonState(state.isSaveButtonEnable)
    }

    private fun handleActions(action: FastTaskViewModel.Actions) {
        when (action) {
            is FastTaskViewModel.Actions.GoBack -> dismiss()
            is FastTaskViewModel.Actions.GoToDurationDialog -> showDurationDialog(action.task)
            is FastTaskViewModel.Actions.GoToPriorityDialog -> showPriorityMenu(binding.taskPriorityButton)
            is FastTaskViewModel.Actions.GoToColorDialog -> showColorMenu(binding.taskColorButton)
        }
    }

    private fun showDurationDialog(task: ScheduleItem?) {
        val durationFragment = TaskDurationDialog()

        val bundle = Bundle()
        bundle.putParcelable(TaskDurationDialog.KEY_TASK_ITEM, task)
        durationFragment.arguments = bundle

        durationFragment.show(childFragmentManager, TaskDurationDialog.TAG)
    }

    private fun showPriorityMenu(view: View) {
        val contextThemeWrapper = ContextThemeWrapper(requireContext(), R.style.CustomPopupMenuStyle)
        val popupMenu = PopupMenu(contextThemeWrapper, view)
        popupMenu.menuInflater.inflate(R.menu.priority_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.priorityBase -> {
                    viewModel.updateTask(currentTask.copy(priority = Priority.STANDARD))
                    true
                }
                R.id.priorityHigh -> {
                    viewModel.updateTask(currentTask.copy(priority = Priority.IMPORTANT))
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
                    viewModel.updateTask(currentTask.copy(taskColor = TaskColor.BLUE))
                    true
                }
                R.id.colorGreen -> {
                    viewModel.updateTask(currentTask.copy(taskColor = TaskColor.GREEN))
                    true
                }
                R.id.colorRed -> {
                    viewModel.updateTask(currentTask.copy(taskColor = TaskColor.RED))
                    true
                }
                R.id.colorPurple -> {
                    viewModel.updateTask(currentTask.copy(taskColor = TaskColor.PURPLE))
                    true
                }
                R.id.colorPink -> {
                    viewModel.updateTask(currentTask.copy(taskColor = TaskColor.PINK))
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun setStyle(taskColor: TaskColor) {
        val taskColorStateList = setColorStateList(taskColor)

        with(binding) {
            saveButton.backgroundTintList = taskColorStateList

            taskDurationButton.chipIconTint = taskColorStateList
            taskPriorityButton.chipIconTint = taskColorStateList
            taskColorButton.chipIconTint = taskColorStateList

            taskDurationButton.chipStrokeColor = taskColorStateList
            taskPriorityButton.chipStrokeColor = taskColorStateList
            taskColorButton.chipStrokeColor = taskColorStateList

        }
    }

    private fun setColorStateList(taskColor: TaskColor): ColorStateList {
        val color = when (taskColor) {
            TaskColor.BLUE -> R.color.blue
            TaskColor.GREEN -> R.color.green
            TaskColor.RED -> R.color.redDialog
            TaskColor.PURPLE -> R.color.purple
            TaskColor.PINK -> R.color.pink
        }
        return ColorStateList.valueOf(ContextCompat.getColor(requireContext(), color))
    }

    private fun updateSaveButtonState(state: Boolean) {
        binding.saveButton.apply {
            isEnabled = state
            alpha = if (state) 1.0f else 0.6f
        }
    }

    private fun updatePriorityButton(priority: Priority) {
        binding.taskPriorityButton.text = when (priority) {
            Priority.STANDARD -> getString(R.string.main_low_priority)
            Priority.IMPORTANT -> getString(R.string.main_high_priority)
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