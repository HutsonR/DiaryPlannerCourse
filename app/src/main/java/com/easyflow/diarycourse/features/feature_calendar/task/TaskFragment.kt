package com.easyflow.diarycourse.features.feature_calendar.task

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.core.utils.ReminderWorker
import com.easyflow.diarycourse.core.utils.formatDate
import com.easyflow.diarycourse.databinding.FragmentTaskBinding
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.Priority
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.PriorityAdapter
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.TaskColor
import com.easyflow.diarycourse.features.feature_calendar.task.dialogs.ReminderDialogFragment
import com.easyflow.diarycourse.features.feature_calendar.task.util.TaskType
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.Lazy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TaskFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentTaskBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var taskViewModelFactory: Lazy<TaskViewModel.TaskViewModelFactory>
    private val viewModel: TaskViewModel by viewModels {
        taskViewModelFactory.get()
    }
    private var currentTask = ScheduleItem(
        text = "",
        description = "",
        date = "",
        startTime = "",
        endTime = ""
    )

    private var purposeTask: TaskType = TaskType.ADD

    // For Reminder
    private var chosenYear = 0
    private var chosenMonth = 0
    private var chosenDay = 0
    private var chosenHour = 0
    private var chosenMin = 0

    private lateinit var taskIconBackground: LinearLayout
    private lateinit var titleEditTV: TextView
    private lateinit var textEditTV: TextView
    private lateinit var datePickerTV: TextView
    private lateinit var timeStartPickerTV: TextView
    private lateinit var timeEndPickerTV: TextView
    private lateinit var saveButton: LinearLayout
    private lateinit var saveButtonTV: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setStyle()
        _binding = FragmentTaskBinding.inflate(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setObservers()
        initialize()
        initializeParcel()
        initializeListeners()
        viewModel.updateSaveButtonState()
    }

    private fun setStyle() {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        dialog?.setOnShowListener { dialog ->
            val layout: FrameLayout? = (dialog as BottomSheetDialog).
            findViewById(com.google.android.material.R.id.design_bottom_sheet)
            layout?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
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
                updateItem(state.item)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun observeActions() {
        viewModel
            .action
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { action ->
                when (action) {
                    is TaskViewModel.Actions.GoBack -> dismiss()
                    is TaskViewModel.Actions.GoBackWithItem -> saveButtonClicked(action.item)
                    is TaskViewModel.Actions.ChangeButtonState -> updateSaveButtonState(action.state)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun initialize() {
        initializeNavBar()
        setFragmentListener()
        checkPermission()

        taskIconBackground = binding.taskIconBackground
        titleEditTV = binding.addTitleTask
        textEditTV = binding.addDeskTask
        datePickerTV = binding.datePickerText
        timeStartPickerTV = binding.timeStartPickerText
        timeEndPickerTV = binding.timeEndPickerText
        saveButtonTV = binding.taskConfirmTV

        saveButton = binding.taskConfirm
        saveButton.setOnClickListener {
            viewModel.onSaveButtonClicked()
        }

        titleEditTV.requestFocus()

        binding.reminderSwitchButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val reminderDialogFragment = ReminderDialogFragment()
                reminderDialogFragment.show(childFragmentManager, "reminder dialog")
            } else {
                chosenHour = 0
                chosenMin = 0

                binding.reminderPickerText.text = ""
            }
        }

        setTaskStyle(currentTask.taskColor)
        binding.timeEndPicker.alpha = 0.5f

        taskIconBackground.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Выбор иконки в разработке...",
                Toast.LENGTH_SHORT
            ).show()
            Log.d("debugTag", "currentTask $currentTask")
        }
    }

    private fun initializeNavBar() {
        binding.taskToolbar.toolbar.title = getString(R.string.task_main_title)
        binding.taskToolbar.toolbar.setNavigationOnClickListener {
            viewModel.goBack()
        }
    }

    private fun initializeListeners() {
        titleListener()
        descriptionListener()
        datePickerListener()
        dateFastPickerListener()
        startTimePickerListener()
        colorPickerListener()
    }

    private fun initializeParcel() {
        val parcelItem: ScheduleItem? = arguments?.getParcelable("scheduleItem")
        parcelItem?.let {
            binding.taskToolbar.toolbar.title = getString(R.string.task_edit_title)
            saveButtonTV.text = getString(R.string.task_button_edit)
            Log.d("debugTag", "initializeParcel")
            viewModel.setParcelItem(parcelItem)
            purposeTask = TaskType.CHANGE
            currentTask = parcelItem

            titleEditTV.text = parcelItem.text
            textEditTV.text = parcelItem.description
            datePickerTV.text = parcelItem.date
            timeStartPickerTV.text = parcelItem.startTime
            timeEndPickerTV.text = parcelItem.endTime.ifEmpty { getString(R.string.task_time_blank) }
            
            setColor()
            setTaskStyle(parcelItem.taskColor)
            // Для активации кнопки конца времени
            isStartTimeAfterEndTime()
            timePicked()
            checkDate()
        }
        priorityPickerListener()
    }

    private fun setFragmentListener() {
        // Из ReminderDialogFragment
        activity?.supportFragmentManager?.setFragmentResultListener(
            KEY_TASK_FRAGMENT_RESULT_SET,
            this
        ) { _, bundle ->
            val requestValue: String? = bundle.getString(FRAGMENT_REMIND_ITEM)
            requestValue?.let { time ->
                if (time.isNotEmpty()) {
                    Log.d("debugTag", "requestValue $requestValue")
                    val (hours, minutes) = requestValue.split(":").map { it.toInt() }
                    chosenHour = hours
                    chosenMin = minutes

                    binding.reminderPickerText.text = "в $requestValue"
                    updateReminderSwitchState(true)
                } else {
                    chosenHour = 0
                    chosenMin = 0

                    binding.reminderPickerText.text = ""
                    updateReminderSwitchState(false)
                }
            }
        }
    }

    private fun updateItem(item: ScheduleItem?) {
        item?.let { currentTask = it }
    }

    private fun updateReminderSwitchState(isChecked: Boolean) {
        binding.reminderSwitchButton.isChecked = isChecked
    }

    private fun updateSaveButtonState(state: Boolean) {
        if (state) {
            saveButton.isEnabled = true
            saveButtonTV.alpha = 1.0f
        } else {
            saveButton.isEnabled = true
            saveButtonTV.alpha = 0.6f
        }
    }

    private fun saveButtonClicked(item: ScheduleItem) {
        if (purposeTask == TaskType.ADD) {
            setReminder()
        }
        sendTaskItem(item)
        viewModel.goBack()
    }

    private fun sendTaskItem(item: ScheduleItem) {
        val bundle = Bundle().apply {
            putParcelable(FRAGMENT_TASK_ITEM, item)
        }
        if (purposeTask == TaskType.CHANGE) {
            activity?.supportFragmentManager?.setFragmentResult(KEY_TASK_FRAGMENT_RESULT_UPD, bundle)
        } else {
            activity?.supportFragmentManager?.setFragmentResult(KEY_TASK_FRAGMENT_RESULT_ADD, bundle)
        }
    }

    private fun checkPermission() {
        val notificationPermissionStatus = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.POST_NOTIFICATIONS
        )
        // Если разрешение еще не предоставлено, запросите его у пользователя
        if (notificationPermissionStatus != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                requireContext(),
                "Предоставьте, пожалуйста, разрешение для уведомлений",
                Toast.LENGTH_SHORT
            ).show()
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun timePicked() {
        binding.addClearTimeTV.visibility = View.VISIBLE
        binding.timePickerTitle.text = getString(R.string.task_time_title)
        binding.addClearTimeTV.setOnClickListener {
            viewModel.clearTime()
            binding.addClearTimeTV.visibility = View.GONE
            binding.timePickerTitle.text = getString(R.string.task_time_title_add)
            timeStartPickerTV.text = getString(R.string.task_time_blank)
            timeEndPickerTV.text = getString(R.string.task_time_blank)
            isStartTimeAfterEndTime()
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
                viewModel.clearTime()
                binding.addClearTimeTV.visibility = View.GONE
                timeStartPickerTV.text = getString(R.string.task_time_blank)
                timeEndPickerTV.text = getString(R.string.task_time_blank)
            }
        }

        if (currentTask.startTime.isNotEmpty()) {
            binding.timeEndPicker.alpha = 1f
            binding.timeEndPicker.setOnClickListener { endTimePickerListener() }
        } else {
            binding.timeEndPicker.alpha = 0.5f
            binding.timeEndPicker.setOnClickListener { }
        }

        viewModel.updateSaveButtonState()
    }

    private fun checkDate() {
        val today = Calendar.getInstance()
        val formattedToday = formatDate(today)

        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
        val formattedTomorrow = formatDate(tomorrow)

        if (currentTask.date.isNotEmpty() && currentTask.date == formattedToday) {
            binding.datePickerTodayChecked.visibility = View.VISIBLE
        } else {
            binding.datePickerTodayChecked.visibility = View.INVISIBLE
        }
        if (currentTask.date.isNotEmpty() && currentTask.date == formattedTomorrow) {
            binding.datePickerTomorrowChecked.visibility = View.VISIBLE
        } else {
            binding.datePickerTomorrowChecked.visibility = View.INVISIBLE
        }
        if (currentTask.date.isNotEmpty()) {
            binding.datePickerText.visibility = View.VISIBLE
            binding.datePickerTitle.text = getString(R.string.task_date_title)
        }
    }

    private fun setTaskStyle(taskColor: TaskColor) {
        val taskColorStateList = when (taskColor) {
            TaskColor.BLUE -> ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.blue
                )
            )
            TaskColor.GREEN -> ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.green
                )
            )
            TaskColor.RED -> ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.redDialog
                )
            )
            TaskColor.PURPLE -> ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.purple
                )
            )
            TaskColor.PINK -> ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.pink
                )
            )
        }
        // Задний фон иконки задачи
        taskIconBackground.backgroundTintList = taskColorStateList
        // Цвет нижней черты у заголовка задачи
        val drawable = binding.addTitleTask.background
        DrawableCompat.setTintList(drawable, taskColorStateList)
        binding.addTitleTask.background = drawable
        // Цвет кнопки добавления задачи
        binding.taskConfirmTV.backgroundTintList = taskColorStateList
        // Устанавливаем цвет для переключателя
        binding.reminderSwitchButton.thumbTintList = taskColorStateList
        binding.reminderSwitchButton.trackTintList = taskColorStateList
        binding.repeatSwitchButton.thumbTintList = taskColorStateList
        binding.repeatSwitchButton.trackTintList = taskColorStateList
        // Галочки для даты
        binding.datePickerTodayChecked.setColorFilter(taskColorStateList.defaultColor, PorterDuff.Mode.SRC_IN)
        binding.datePickerTomorrowChecked.setColorFilter(taskColorStateList.defaultColor, PorterDuff.Mode.SRC_IN)
        // Метки
        val backgroundDrawable = GradientDrawable().apply {
            setStroke(2, taskColorStateList)
            cornerRadius = 50F
        }
        binding.taskTagAdd.tagWrapper.background = backgroundDrawable
        binding.taskTagAdd.tagIcon.setColorFilter(taskColorStateList.defaultColor, PorterDuff.Mode.SRC_IN)
    }

    private fun setColor() {
        val taskColorToSet = currentTask.taskColor

        // Пройти по всем RadioButton в группе и установить isChecked для соответствующего цвета
        for (i in 0 until binding.colorPicker.childCount) {
            val radioButton = binding.colorPicker.getChildAt(i) as? RadioButton
            val colorTag = radioButton?.tag as? String
            val colorEnum = colorTag?.let { viewModel.getColorEnum(it) }

            if (colorEnum == taskColorToSet) {
                radioButton.isChecked = true
                break
            }
        }
    }

    private fun setReminder() {
        Log.d("debugTag", "chosenHour $chosenHour, chosenMin $chosenMin")
        if (chosenHour != 0 && chosenMin != 0) {
            val userSelectedDateTime = Calendar.getInstance()
            userSelectedDateTime.set(chosenYear, chosenMonth, chosenDay, chosenHour , chosenMin)

            val todayDateTime = Calendar.getInstance()
            val delayInSeconds = (userSelectedDateTime.timeInMillis/1000L) - (todayDateTime.timeInMillis/1000L)
            createWorkRequest(currentTask.text, currentTask.startTime, delayInSeconds)
        }
    }

    private fun createWorkRequest(title: String, message: String, timeDelayInSeconds: Long  ) {
        val myWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(timeDelayInSeconds, TimeUnit.SECONDS)
            .setInputData(workDataOf(
                "title" to title,
                "message" to message,
            )
            )
            .build()

        WorkManager.getInstance(requireContext()).enqueue(myWorkRequest)
    }


    //    Listeners
    private fun titleListener() {
        titleEditTV.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateTask(currentTask.copy(text = s.toString()))
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun descriptionListener() {
        textEditTV.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateTask(currentTask.copy(description = s.toString()))
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun datePickerListener() {
        binding.datePicker.setOnClickListener {
            val builder = MaterialDatePicker.Builder.datePicker()
            builder.setTitleText("Выберите дату")
            builder.setTheme(R.style.MaterialCalendarPickerTheme)

            val datePicker = builder.build()

            datePicker.addOnPositiveButtonClickListener { selectedTimestamp ->
                val selectedDate = Calendar.getInstance()
                selectedDate.timeInMillis = selectedTimestamp

                chosenYear = selectedDate.get(Calendar.YEAR)
                chosenMonth = selectedDate.get(Calendar.MONTH)
                chosenDay = selectedDate.get(Calendar.DAY_OF_MONTH)

                val formattedDate = formatDate(selectedDate)

                viewModel.updateTask(currentTask.copy(date = formattedDate))
                datePickerTV.text = formattedDate
                checkDate()
                isStartTimeAfterEndTime()
            }

            datePicker.show(childFragmentManager, datePicker.toString())
        }
    }

    private fun dateFastPickerListener() {
        binding.datePickerToday.setOnClickListener {
            val day = Calendar.getInstance()

            chosenYear = day.get(Calendar.YEAR)
            chosenMonth = day.get(Calendar.MONTH)
            chosenDay = day.get(Calendar.DAY_OF_MONTH)

            val formattedDate = formatDate(day)
            viewModel.updateTask(currentTask.copy(date = formattedDate))
            datePickerTV.text = formattedDate
            checkDate()
            isStartTimeAfterEndTime()
        }
        binding.datePickerTomorrow.setOnClickListener {
            val day = Calendar.getInstance()
            day.add(Calendar.DAY_OF_YEAR, 1)

            chosenYear = day.get(Calendar.YEAR)
            chosenMonth = day.get(Calendar.MONTH)
            chosenDay = day.get(Calendar.DAY_OF_MONTH)

            val formattedDate = formatDate(day)
            viewModel.updateTask(currentTask.copy(date = formattedDate))
            datePickerTV.text = formattedDate
            checkDate()
            isStartTimeAfterEndTime()
        }
    }

    private fun startTimePickerListener() {
        binding.timeStartPicker.setOnClickListener {
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
                timeStartPickerTV.text = formattedTime
                timePicked()
                isStartTimeAfterEndTime()
            }

            timePicker.show(childFragmentManager, timePicker.toString())
        }
    }

    private fun endTimePickerListener() {
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
            timeEndPickerTV.text = formattedTime
            timePicked()
            isStartTimeAfterEndTime()
        }

        timePicker.show(childFragmentManager, timePicker.toString())
    }

    private fun priorityPickerListener() {
        val items = resources.getStringArray(R.array.priority_array).toList()
        val adapter = PriorityAdapter(requireContext(), items)

        val prioritySpinner = binding.prioritySpinner
        prioritySpinner.adapter = adapter

        if (purposeTask == TaskType.CHANGE) {
            val position = items.indexOf(viewModel.getPriorityString(currentTask.priority))
            if (position != -1) {
                prioritySpinner.setSelection(position)
            }
        }

        prioritySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val currentPriority = parent?.getItemAtPosition(position).toString()
                val selectedPriority: Priority = viewModel.getPriorityEnum(currentPriority)
                viewModel.updateTask(currentTask.copy(priority = selectedPriority))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun colorPickerListener() {
        binding.colorPicker.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton: RadioButton =
                view?.findViewById(checkedId) ?: return@setOnCheckedChangeListener
            val selectedColorTag: String = selectedRadioButton.tag as String

            val selectedTaskColor: TaskColor = viewModel.getColorEnum(selectedColorTag)
            viewModel.updateTask(currentTask.copy(taskColor = selectedTaskColor))
            setTaskStyle(selectedTaskColor)

            viewModel.updateSaveButtonState()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val KEY_TASK_FRAGMENT_RESULT_ADD = "KEY_TASK_FRAGMENT_RESULT_ADD"
        const val KEY_TASK_FRAGMENT_RESULT_UPD = "KEY_FRAGMENT_RESULT_UPD"
        const val KEY_TASK_FRAGMENT_RESULT_SET = "KEY_REMINDER_FRAGMENT_RESULT_SET"
        const val PERMISSION_REQUEST_CODE = 1337

        const val FRAGMENT_TASK_ITEM = "taskItem"
        const val FRAGMENT_REMIND_ITEM = "REMIND_ITEM"
    }
}