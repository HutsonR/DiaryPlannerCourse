package com.easyflow.diarycourse.features.feature_calendar.task

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.PorterDuff
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
import com.easyflow.diarycourse.domain.util.Resource
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.Color
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.Priority
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.PriorityAdapter
import com.easyflow.diarycourse.features.feature_calendar.task.dialogs.ReminderDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.Lazy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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
    private var parcelItem: ScheduleItem? = null
    private var previousTitle: String = ""
    private var previousText: String = ""
    private var previousDate: String = ""
    private var previousPriority: Priority = Priority.STANDARD
    private var previousTimeStart: String = ""
    private var previousTimeEnd: String = ""
    private var previousColor: Color = Color.BLUE

    private var title: String = ""
    private var text: String = ""
    private var date: String = ""
    private var priority: Priority = Priority.STANDARD
    private var timeStart: String = ""
    private var timeEnd: String = ""
    private var color: Color = Color.BLUE

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
        updateSaveButtonState()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
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
                updateCollect(state.update)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun updateCollect(result: Resource?) {
        result?.let {
            when (it) {
                is Resource.Success<*> -> viewModel.goBack()
                is Resource.Failed -> onFailed()
            }
        }
    }

    private fun observeActions() {
        viewModel
            .action
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { action ->
                when (action) {
                    is TaskViewModel.Actions.GoBack -> dismiss()
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
            handleSaveButtonClicked()
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

        setTaskStyle(color)
        binding.timeEndPicker.alpha = 0.5f

        taskIconBackground.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Выбор иконки в разработке...",
                Toast.LENGTH_SHORT
            ).show()
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
        parcelItem = arguments?.getParcelable("scheduleItem")
        parcelItem?.let { parcelItem ->
            binding.taskToolbar.toolbar.title = getString(R.string.task_edit_title)
            saveButtonTV.text = getString(R.string.task_button_edit)

            title = parcelItem.text
            text = parcelItem.description
            date = parcelItem.date
            priority = parcelItem.priority
            timeStart = parcelItem.startTime
            timeEnd = parcelItem.endTime
            color = parcelItem.color

            previousTitle = title
            previousText = text
            previousDate = date
            previousPriority = priority
            previousTimeStart = timeStart
            previousTimeEnd = timeEnd
            previousColor = color

            titleEditTV.text = parcelItem.text
            textEditTV.text = parcelItem.description
            datePickerTV.text = parcelItem.date
            timeStartPickerTV.text = parcelItem.startTime
            timeEndPickerTV.text = parcelItem.endTime.ifEmpty { getString(R.string.task_time_blank) }
            setColor()
            setTaskStyle(parcelItem.color)
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

    private fun updateReminderSwitchState(isChecked: Boolean) {
        binding.reminderSwitchButton.isChecked = isChecked
    }

    private fun updateSaveButtonState() {
        if (parcelItem != null) {
            // Для редактирования элемента
            val isTitleChanged = title != previousTitle
            val isDateChanged = date != previousDate
            val isTimeStartChanged = timeStart != previousTimeStart
            val isTextChanged = text != previousText
            val isTimeEndChanged = timeEnd != previousTimeEnd
            val isColorChanged = color != previousColor
            val isPriorityChanged = priority != previousPriority

            val isEnabled =
                (isTitleChanged || isDateChanged || isTimeStartChanged || isTextChanged || isTimeEndChanged || isColorChanged || isPriorityChanged) &&
                        title.isNotEmpty() && date.isNotEmpty() && timeStart.isNotEmpty()

            saveButton.isEnabled = isEnabled
            saveButtonTV.alpha = if (isEnabled) 1.0f else 0.6f
        } else {
            // По умолчанию обычное добавление элемента
            val isTitleFilled = title.isNotEmpty()
            val isDateFilled = date.isNotEmpty()
            val isTimeStartFilled = timeStart.isNotEmpty()

            val isEnabled = isTitleFilled && isDateFilled && isTimeStartFilled

            saveButton.isEnabled = isEnabled
            saveButtonTV.alpha = if (isEnabled) 1.0f else 0.6f
        }
    }

    private fun handleSaveButtonClicked() {
        if (parcelItem != null) {
            // Для редактирования элемента
            lifecycleScope.launch {
                val updatedItem = parcelItem!!.copy(
                    text = title,
                    description = text,
                    date = date,
                    priority = priority,
                    startTime = timeStart,
                    endTime = timeEnd,
                    duration = viewModel.calculateDuration(timeStart, timeEnd),
                    color = color,
                    isCompleteTask = parcelItem!!.isCompleteTask
                )
                sendTaskItem(updatedItem)
                viewModel.goBack()
            }
        } else {
            // По умолчанию обычное добавление элемента
            val taskItem = ScheduleItem(
                text = title,
                description = text,
                date = date,
                priority = priority,
                startTime = timeStart,
                endTime = timeEnd,
                duration = viewModel.calculateDuration(timeStart, timeEnd),
                color = color,
                isCompleteTask = false
            )
            sendTaskItem(taskItem)
            setReminder()
            viewModel.goBack()
        }
    }

    private fun onFailed() {
        Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_SHORT).show()
        viewModel.goBack()
    }

    private fun setReminder() {
        Log.d("debugTag", "chosenHour $chosenHour, chosenMin $chosenMin")
        if (chosenHour != 0 && chosenMin != 0) {
            val userSelectedDateTime = Calendar.getInstance()
            userSelectedDateTime.set(chosenYear, chosenMonth, chosenDay, chosenHour , chosenMin)

            val todayDateTime = Calendar.getInstance()
            val delayInSeconds = (userSelectedDateTime.timeInMillis/1000L) - (todayDateTime.timeInMillis/1000L)
            createWorkRequest(title, timeStart, delayInSeconds)
        }
    }

    private fun sendTaskItem(item: ScheduleItem) {
        val bundle = Bundle().apply {
            putParcelable(FRAGMENT_TASK_ITEM, item)
        }
        if (parcelItem != null) {
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
            binding.addClearTimeTV.visibility = View.GONE
            timeStart = ""
            timeStartPickerTV.text = getString(R.string.task_time_blank)
            timeEnd = ""
            timeEndPickerTV.text = getString(R.string.task_time_blank)
            binding.timePickerTitle.text = getString(R.string.task_time_title_add)
            isStartTimeAfterEndTime()
            updateSaveButtonState()
        }
    }

    private fun isStartTimeAfterEndTime() {
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
        if (timeStart.isNotEmpty() && timeEnd.isNotEmpty()) {
            val currentStartTime = LocalTime.parse(timeStart, timeFormat)
            val currentEndTime = LocalTime.parse(timeEnd, timeFormat)
            //  Проверка между двумя временами в окне добавления
            if (currentStartTime.isAfter(currentEndTime)) {
                Toast.makeText(requireContext(), "Начальное время не может быть больше конечного", Toast.LENGTH_SHORT).show()
                timeStart = ""
                timeStartPickerTV.text = getString(R.string.task_time_blank)
                timeEnd = ""
                timeEndPickerTV.text = getString(R.string.task_time_blank)
                binding.addClearTimeTV.visibility = View.GONE
            } else if (currentStartTime == currentEndTime) {
                Toast.makeText(requireContext(), "Если начальное время одинаково с конечным, то конечное можно не писать", Toast.LENGTH_SHORT).show()
                timeEnd = ""
                timeEndPickerTV.text = getString(R.string.task_time_blank)
            }
        }

        if (timeStart.isNotEmpty()) {
            binding.timeEndPicker.alpha = 1f
            binding.timeEndPicker.setOnClickListener { endTimePickerListener() }
        } else {
            binding.timeEndPicker.alpha = 0.5f
            binding.timeEndPicker.setOnClickListener { }
        }

        updateSaveButtonState()
    }

    private fun checkDate() {
        val today = Calendar.getInstance()
        val formattedToday = formatDate(today)

        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
        val formattedTomorrow = formatDate(tomorrow)

        if (date == formattedToday) {
            binding.datePickerTodayChecked.visibility = View.VISIBLE
        } else {
            binding.datePickerTodayChecked.visibility = View.INVISIBLE
        }
        if (date == formattedTomorrow) {
            binding.datePickerTomorrowChecked.visibility = View.VISIBLE
        } else {
            binding.datePickerTomorrowChecked.visibility = View.INVISIBLE
        }
        if (date.isNotEmpty()) {
            binding.datePickerText.visibility = View.VISIBLE
            binding.datePickerTitle.text = getString(R.string.task_date_title)
        }
    }

    private fun setTaskStyle(color: Color) {
        val colorStateList = when (color) {
            Color.BLUE -> ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.blue
                )
            )

            Color.GREEN -> ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.green
                )
            )

            Color.RED -> ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.redDialog
                )
            )

            Color.PURPLE -> ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.purple
                )
            )

            Color.PINK -> ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.pink
                )
            )
        }
        // Задний фон иконки задачи
        taskIconBackground.backgroundTintList = colorStateList
        // Цвет нижней черты у заголовка задачи
        val drawable = binding.addTitleTask.background
        DrawableCompat.setTintList(drawable, colorStateList)
        binding.addTitleTask.background = drawable
        // Цвет кнопки добавления задачи
        binding.taskConfirmTV.backgroundTintList = colorStateList
        // Устанавливаем цвет для переключателя
        binding.reminderSwitchButton.thumbTintList = colorStateList
        binding.reminderSwitchButton.trackTintList = colorStateList
        binding.repeatSwitchButton.thumbTintList = colorStateList
        binding.repeatSwitchButton.trackTintList = colorStateList
        // Галочки для даты
        binding.datePickerTodayChecked.setColorFilter(colorStateList.defaultColor, PorterDuff.Mode.SRC_IN)
        binding.datePickerTomorrowChecked.setColorFilter(colorStateList.defaultColor, PorterDuff.Mode.SRC_IN)
    }

    private fun setColor() {
        val colorToSet = parcelItem?.color ?: Color.BLUE

        // Пройти по всем RadioButton в группе и установить isChecked для соответствующего цвета
        for (i in 0 until binding.colorPicker.childCount) {
            val radioButton = binding.colorPicker.getChildAt(i) as? RadioButton
            val colorTag = radioButton?.tag as? String
            val colorEnum = colorTag?.let { viewModel.getColorEnum(it) }

            if (colorEnum == colorToSet) {
                radioButton.isChecked = true
                break
            }
        }
    }

    // Private Function to create the OneTimeWorkRequest
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
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                title = s.toString()
                updateSaveButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun descriptionListener() {
        textEditTV.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                text = s.toString()
                updateSaveButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
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

                date = formattedDate
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

            date = formattedDate
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

            date = formattedDate
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
                timeStart = formattedTime
                timeStartPickerTV.text = timeStart
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
            timeEnd = formattedTime
            timeEndPickerTV.text = timeEnd
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

        if (parcelItem != null) {
            val position = items.indexOf(viewModel.getPriorityString(priority))
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
                priority = selectedPriority
                updateSaveButtonState()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun colorPickerListener() {
        binding.colorPicker.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton: RadioButton =
                view?.findViewById(checkedId) ?: return@setOnCheckedChangeListener
            val selectedColorTag: String = selectedRadioButton.tag as String

            val selectedColor: Color = viewModel.getColorEnum(selectedColorTag)
            color = selectedColor
            setTaskStyle(color)

            updateSaveButtonState()
        }
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