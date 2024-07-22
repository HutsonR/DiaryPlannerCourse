package com.easyflow.diarycourse.features.feature_home.task.dialogs

import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.core.models.AlertData
import com.easyflow.diarycourse.core.utils.collectOnStart
import com.easyflow.diarycourse.core.utils.formatDate
import com.easyflow.diarycourse.databinding.FragmentTaskDurationDialogBinding
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.TaskColor
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.Lazy
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class TaskDurationDialog: BottomSheetDialogFragment() {
    private var _binding: FragmentTaskDurationDialogBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var taskDurationViewModelFactory: Lazy<TaskDurationViewModel.TaskDurationViewModelFactory>
    private val viewModel: TaskDurationViewModel by viewModels {
        taskDurationViewModelFactory.get()
    }

    private var currentTask: ScheduleItem = createScheduleItem()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setBottomDialogStyle()
        _binding = FragmentTaskDurationDialogBinding.inflate(inflater)
        return _binding?.root
    }

    private fun setBottomDialogStyle() {
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        dialog?.setOnShowListener { dialog ->
            val layout: FrameLayout? = (dialog as BottomSheetDialog).
            findViewById(com.google.android.material.R.id.design_bottom_sheet)
            layout?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false
                behavior.skipCollapsed = true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    private fun initialize() {
        setListeners()
        setObservers()
        initializeParcel()
    }

    private fun setObservers() {
        viewModel.state.onEach(::handleState).collectOnStart(viewLifecycleOwner)
        viewModel.action.onEach(::handleActions).collectOnStart(viewLifecycleOwner)
    }

    private fun handleState(state: TaskDurationViewModel.State) {
        state.task?.let {
            currentTask = it
            binding.durationTime.text = it.duration
            isStartTimeAfterEndTime()
        }
        updateSaveButtonState(state.isSaveButtonEnable)
        setStyle(state.taskColor)
    }

    private fun handleActions(action: TaskDurationViewModel.Actions) {
        when (action) {
            is TaskDurationViewModel.Actions.ShowAlert -> showAlert(
                AlertData(
                    title = R.string.task_duration_alert_title,
                    message = R.string.task_duration_alert_text,
                    positiveButton = R.string.task_duration_alert_ok_button,
                    isNegativeButtonNeeded = true,
                    navigate = { viewModel.goBack() }
                )
            )
            is TaskDurationViewModel.Actions.GoBack -> dismiss()
            is TaskDurationViewModel.Actions.GoBackWithItem -> {
                val bundle = Bundle().apply {
                    putParcelable(KEY_TASK_ITEM, action.item)
                }
                activity?.supportFragmentManager?.setFragmentResult(REQ_KEY_TASK_ITEM, bundle)
                viewModel.goBack()
            }
        }
    }

    private fun initializeParcel() {
        val parcelItem: ScheduleItem? = arguments?.getParcelable(KEY_TASK_ITEM)
        currentTask = parcelItem ?: createScheduleItem()
        viewModel.updateTask(currentTask)
        initializeDate()
        initializeTime()
    }

    private fun createScheduleItem(): ScheduleItem {
        return ScheduleItem(
            text = "",
            description = "",
            date = "",
            startTime = "",
            endTime = ""
        )
    }

    private fun initializeDate() {
        val date = currentTask.date
        if (date.isNotBlank()) {
            val dateParts = date.split(".")
            val calendar = Calendar.getInstance()
            calendar.set("20${dateParts[2]}".toInt(), dateParts[1].toInt() -1, dateParts[0].toInt())
            binding.calendar.date = calendar.timeInMillis
            checkPredefinedDates(calendar)
        }
    }

    private fun initializeTime() {
        val taskStartTime = currentTask.startTime
        val taskEndTime = currentTask.endTime
        if (taskStartTime.isNotBlank() && taskEndTime.isNotBlank()) {
            with(binding) {
                removeTime.visibility = View.VISIBLE
                taskTime.visibility = View.VISIBLE
                startTime.text = taskStartTime
                endTime.text = taskEndTime
                durationTime.visibility = View.VISIBLE
                durationTime.text = currentTask.duration
                timeAllDay.isChecked = currentTask.isAllDay
            }
        }
    }

    private fun getCurrentPairTime(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val startTime = formatTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        calendar.add(Calendar.MINUTE, 30)
        val endTime = formatTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        return startTime to endTime
    }

    private fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }

    private fun setStyle(taskColor: TaskColor) {
        val taskColorStateList = setColorStateList(taskColor)

        with(binding) {
            saveButton.backgroundTintList = taskColorStateList

            calendarToday.checkedIconTint = taskColorStateList
            calendarTomorrow.checkedIconTint = taskColorStateList
            calendarThreeDays.checkedIconTint = taskColorStateList

            timeAllDay.checkedIconTint = taskColorStateList
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

    private fun setListeners() {
        with(binding) {
            sheetClose.setOnClickListener { viewModel.showAlert() }
            saveButton.setOnClickListener { viewModel.saveTaskDuration() }
            // TIME
            setupTimeListeners()
            setupAllDayCheckboxListener()
            startTimePickerListener()
            endTimePickerListener()
            // DATE
            setupDateListeners()
        }
    }

    private fun setupDateListeners() {
        with(binding) {
            calendar.setOnDateChangeListener { view, year, month, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth)
                val formattedDate = formatDate(calendar)
                checkPredefinedDates(calendar)
                viewModel.updateDate(formattedDate)
            }
            calendarToday.setOnClickListener {
                val calendar = Calendar.getInstance()
                binding.calendar.date = calendar.timeInMillis
                val formattedDate = formatDate(calendar)
                viewModel.updateDate(formattedDate)
            }
            calendarTomorrow.setOnClickListener {
                val calendar = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
                binding.calendar.date = calendar.timeInMillis
                val formattedDate = formatDate(calendar)
                viewModel.updateDate(formattedDate)
            }
            calendarThreeDays.setOnClickListener {
                val calendar = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 3)
                }
                binding.calendar.date = calendar.timeInMillis
                val formattedDate = formatDate(calendar)
                viewModel.updateDate(formattedDate)
            }
        }
    }

    private fun checkPredefinedDates(selectedCalendar: Calendar) {
        with(binding) {
            calendarToday.isChecked = false
            calendarTomorrow.isChecked = false
            calendarThreeDays.isChecked = false

            val today = Calendar.getInstance()
            if (isSameDay(today, selectedCalendar)) {
                calendarToday.isChecked = true
            }

            val tomorrow = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1)
            }
            if (isSameDay(tomorrow, selectedCalendar)) {
                calendarTomorrow.isChecked = true
            }

            val threeDaysLater = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 3)
            }
            if (isSameDay(threeDaysLater, selectedCalendar)) {
                calendarThreeDays.isChecked = true
            }
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun setupTimeListeners() {
        with(binding) {
            addTime.setOnClickListener {
                addTime.text = getString(R.string.task_time_title)
                removeTime.visibility = View.VISIBLE
                taskTime.visibility = View.VISIBLE
                val time = getCurrentPairTime()
                startTime.text = time.first
                endTime.text = time.second
                durationTime.visibility = View.VISIBLE
                timeAllDay.isChecked = false

                viewModel.updateTask(currentTask.copy(
                    startTime = time.first,
                    endTime = time.second,
                    isAllDay = false
                ))
            }
            removeTime.setOnClickListener {
                addTime.text = getString(R.string.task_duration_time_button)
                removeTime.visibility = View.GONE
                taskTime.visibility = View.GONE

                viewModel.updateTask(currentTask.copy(
                    startTime = "",
                    endTime = "",
                    isAllDay = true
                ))
            }
        }
    }

    private fun setupAllDayCheckboxListener() {
        with(binding) {
            timeAllDay.setOnCheckedChangeListener { _, isChecked ->
                val task: ScheduleItem
                if (isChecked) {
                    startTime.text = "ꝏ"
                    endTime.text = "ꝏ"
                    durationTime.visibility = View.GONE

                    task = currentTask.copy(
                        startTime = "",
                        endTime = "",
                        isAllDay = true
                    )
                } else {
                    val time = getCurrentPairTime()
                    startTime.text = time.first
                    endTime.text = time.second
                    durationTime.visibility = View.VISIBLE

                    task = currentTask.copy(
                        startTime = time.first,
                        endTime = time.second,
                        isAllDay = false
                    )
                }

                viewModel.updateTask(task)
            }
        }
    }

    private fun startTimePickerListener() {
        binding.startTime.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Время начала задачи")
                .setTheme(R.style.MaterialTimePickerTheme)
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val selectedTimeForStart = Calendar.getInstance()
                selectedTimeForStart.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                selectedTimeForStart.set(Calendar.MINUTE, timePicker.minute)

                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val formattedTime = selectedTimeForStart.time.let { timeFormat.format(it) }

                viewModel.updateTask(currentTask.copy(startTime = formattedTime))
                binding.startTime.text = formattedTime
            }

            timePicker.show(childFragmentManager, timePicker.toString())
        }
    }

    private fun endTimePickerListener() {
        binding.endTime.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Время окончания задачи")
                .setTheme(R.style.MaterialTimePickerTheme)
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val selectedHour = timePicker.hour
                val selectedMinute = timePicker.minute

                val selectedTimeForEnd = Calendar.getInstance()
                selectedTimeForEnd.set(Calendar.HOUR_OF_DAY, selectedHour)
                selectedTimeForEnd.set(Calendar.MINUTE, selectedMinute)

                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val formattedTime = selectedTimeForEnd.time.let { timeFormat.format(it) }

                viewModel.updateTask(currentTask.copy(endTime = formattedTime))
                binding.endTime.text = formattedTime
            }

            timePicker.show(childFragmentManager, timePicker.toString())
        }
    }

    private fun isStartTimeAfterEndTime() {
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
        val startTime = currentTask.startTime
        val endTime = currentTask.endTime

        if (startTime.isNotEmpty() && endTime.isNotEmpty()) {
            val currentStartTime = LocalTime.parse(startTime, timeFormat)
            val currentEndTime = LocalTime.parse(endTime, timeFormat)
            //  Проверка между двумя временами в окне добавления
            if (currentStartTime.isAfter(currentEndTime)) {
                Toast.makeText(requireContext(), "Начальное время не может быть больше конечного", Toast.LENGTH_SHORT).show()
                binding.endTime.text = "ꝏ"
                viewModel.updateTask(currentTask.copy(endTime = ""))
            }
        }
    }

    private fun showAlert(alertData: AlertData) {
        val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
        builder.setTitle(getString(alertData.title))
        builder.setMessage(getString(alertData.message))
        builder.setPositiveButton(getString(alertData.positiveButton)) { dialog: DialogInterface, _: Int ->
            alertData.navigate?.invoke() ?: dialog.dismiss()
        }
        if (alertData.isNegativeButtonNeeded) {
            builder.setNegativeButton(getString(alertData.negativeButton)) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    companion object {
        const val TAG = "TaskDateDialog"
        const val KEY_TASK_ITEM = "SCHEDULE_ITEM"
        const val REQ_KEY_TASK_ITEM = "REQ_KEY_DURATION_TASK_ITEM"
    }
}