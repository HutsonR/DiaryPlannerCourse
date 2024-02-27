package com.easyflow.diarycourse.features.feature_home.task

import android.content.Context
import android.content.res.ColorStateList
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.BaseFragment
import com.easyflow.diarycourse.core.utils.formatDate
import com.easyflow.diarycourse.databinding.FragmentTaskBinding
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.domain.util.Resource
import com.easyflow.diarycourse.features.feature_home.schedule.utils.Color
import com.easyflow.diarycourse.features.feature_home.schedule.utils.Priority
import com.easyflow.diarycourse.features.feature_home.schedule.utils.PriorityAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.Lazy
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class TaskFragment : BottomSheetDialogFragment() {
    private val TAG = "debugTag"
    private var _binding: FragmentTaskBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var taskViewModelFactory: Lazy<TaskViewModel.TaskViewModelFactory>
    private val taskViewModel: TaskViewModel by viewModels {
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
    private lateinit var taskIconBackground: LinearLayout
    private lateinit var titleEditTV: TextView
    private lateinit var textEditTV: TextView
    private lateinit var datePickerTV: TextView
    private lateinit var timeStartPickerTV: TextView
    private lateinit var timeEndPickerTV: TextView
    private lateinit var saveButton: LinearLayout
    private lateinit var saveButtonTV: TextView
    private lateinit var cancelButton: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        _binding = FragmentTaskBinding.inflate(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parcelItem = arguments?.getParcelable("scheduleItem")

        lifecycleScope.launch {
            setObservers()
        }
        initialize()
        parcelInitialize()

        // Изначально деактивируем кнопку "Сохранить"
        updateSaveButtonState()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private suspend fun setObservers() {
        observeState()
    }

    private suspend fun observeState() {
        taskViewModel
            .state
            .onEach { state ->
                updateCollect(state.update)
            }.collect()
    }

    private fun updateCollect(result: Resource?) {
        result?.let {
            when (it) {
                is Resource.Success -> dismiss()
                is Resource.Empty.Failed -> onFailed()
            }
        }
    }

    private fun initialize() {
        taskIconBackground = binding.taskIconBackground
        titleEditTV = binding.addTitleTask
        textEditTV = binding.addDeskTask
        datePickerTV = binding.datePickerText
        timeStartPickerTV = binding.timeStartPickerText
        timeEndPickerTV = binding.timeEndPickerText
        saveButtonTV = binding.taskConfirm

        saveButton = binding.addSave
        cancelButton = binding.addClose
        saveButton.setOnClickListener {
            handleSaveButtonClicked()
        }
        cancelButton.setOnClickListener {
            dismiss()
        }

        titleEditText()
        deskEditText()
        binding.datePicker.setOnClickListener { showDatePicker() }
        dateFastPicker()
        binding.timeStartPicker.setOnClickListener { showTimePickerForStart() }
        binding.timeEndPicker.alpha = 0.5f
        colorPicker()
        setBackgroundIconColor(color)

        taskIconBackground.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "В разработке...",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun parcelInitialize() {
        if (parcelItem != null) {
            binding.titleAddFragment.text = getString(R.string.task_edit_title)
            saveButtonTV.text = getString(R.string.task_button_edit)

            title = parcelItem!!.text
            text = parcelItem!!.description
            date = parcelItem!!.date
            priority = parcelItem!!.priority
            timeStart = parcelItem!!.startTime
            timeEnd = parcelItem!!.endTime
            color = parcelItem!!.color

            previousTitle = title
            previousText = text
            previousDate = date
            previousPriority = priority
            previousTimeStart = timeStart
            previousTimeEnd = timeEnd
            previousColor = color

            titleEditTV.text = parcelItem!!.text
            textEditTV.text = parcelItem!!.description
            datePickerTV.text = parcelItem!!.date
            timeStartPickerTV.text = parcelItem!!.startTime
            timeEndPickerTV.text =
                parcelItem!!.endTime.ifEmpty { getString(R.string.task_time_blank) }
            setColor()
            setBackgroundIconColor(parcelItem!!.color)
            // Для активации кнопки конца времени
            checkTime()
            timePicked()
            checkDate()
        }
        showPriorityPicker()
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
            saveButton.alpha = if (isEnabled) 1.0f else 0.5f
        } else {
            // По умолчанию обычное добавление элемента
            val isTitleFilled = title.isNotEmpty()
            val isDateFilled = date.isNotEmpty()
            val isTimeStartFilled = timeStart.isNotEmpty()

            val isEnabled = isTitleFilled && isDateFilled && isTimeStartFilled

            saveButton.isEnabled = isEnabled
            saveButton.alpha = if (isEnabled) 1.0f else 0.5f
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
                    duration = calculateDuration(timeStart, timeEnd),
                    color = color,
                    isCompleteTask = parcelItem!!.isCompleteTask
                )
                sendTaskItem(updatedItem)
                dismiss()
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
                duration = calculateDuration(timeStart, timeEnd),
                color = color,
                isCompleteTask = false
            )
            sendTaskItem(taskItem)
            dismiss()
        }
    }

    private fun onFailed() {
        Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_SHORT).show()
        dismiss()
    }

    private fun sendTaskItem(item: ScheduleItem) {
        val bundle = Bundle().apply {
            putParcelable(FRAGMENT_TASK_ITEM, item)
        }
        Log.d("debugTag", "==========================================================================")
        Log.d("debugTag", "sendTaskItem item: $item bundle: $bundle")
        if (parcelItem != null) {
            activity?.supportFragmentManager?.setFragmentResult(KEY_TASK_FRAGMENT_RESULT_UPD, bundle)
        } else activity?.supportFragmentManager?.setFragmentResult(KEY_TASK_FRAGMENT_RESULT_ADD, bundle)
    }

    private fun calculateDuration(startTime: String, endTime: String): String {
        if (endTime.isEmpty()) {
            return "бессрочно"
        }

        val startParts = startTime.split(":")
        val endParts = endTime.split(":")

        val startHours = startParts[0].toInt()
        val startMinutes = startParts[1].toInt()

        val endHours = endParts[0].toInt()
        val endMinutes = endParts[1].toInt()

        val durationMinutes = (endHours * 60 + endMinutes) - (startHours * 60 + startMinutes)

        val durationHours = durationMinutes / 60
        val remainingMinutes = durationMinutes % 60

        return when {
            durationHours > 0 && remainingMinutes > 0 -> "$durationHours ч. $remainingMinutes мин."
            durationHours > 0 -> "$durationHours ч."
            remainingMinutes > 0 -> "$remainingMinutes мин."
            else -> "0 мин."
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
            checkTime()
            updateSaveButtonState()
        }
    }

    private fun checkTime() {
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
        if (timeStart.isNotEmpty() && timeEnd.isNotEmpty()) {
            val currentStartTime = LocalTime.parse(timeStart, timeFormat)
            val currentEndTime = LocalTime.parse(timeEnd, timeFormat)
//            Проверка между двумя временами в окне добавления
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
            binding.timeEndPicker.setOnClickListener { showTimePickerForEnd() }
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

    private fun setBackgroundIconColor(color: Color) {
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

            Color.YELLOW -> ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.yellow
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
        taskIconBackground.backgroundTintList = colorStateList
    }

    private fun setColor() {
        val colorToSet = parcelItem?.color ?: Color.BLUE

        // Пройти по всем RadioButton в группе и установить isChecked для соответствующего цвета
        for (i in 0 until binding.colorPicker.childCount) {
            val radioButton = binding.colorPicker.getChildAt(i) as? RadioButton
            val colorTag = radioButton?.tag as? String
            val colorEnum = colorTag?.let { getColorEnum(it) }

            if (colorEnum == colorToSet) {
                radioButton.isChecked = true
                break
            }
        }
    }

    //    Listeners
    private fun titleEditText() {
        titleEditTV.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                title = s.toString()
                updateSaveButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun deskEditText() {
        textEditTV.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                text = s.toString()
                updateSaveButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun dateFastPicker() {
        binding.datePickerToday.setOnClickListener {
            val day = Calendar.getInstance()

            val formattedDate = formatDate(day)

            date = formattedDate
            datePickerTV.text = formattedDate
            checkDate()
            checkTime()
        }
        binding.datePickerTomorrow.setOnClickListener {
            val day = Calendar.getInstance()
            day.add(Calendar.DAY_OF_YEAR, 1)

            val formattedDate = formatDate(day)

            date = formattedDate
            datePickerTV.text = formattedDate
            checkDate()
            checkTime()
        }
    }

    private fun showDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Выберите дату")
        builder.setTheme(R.style.MaterialCalendarPickerTheme)

        val datePicker = builder.build()

        datePicker.addOnPositiveButtonClickListener { selectedTimestamp ->
            val selectedDate = Calendar.getInstance()
            selectedDate.timeInMillis = selectedTimestamp

            val formattedDate = formatDate(selectedDate)

            date = formattedDate
            datePickerTV.text = formattedDate
            checkDate()
            checkTime()
        }

        datePicker.show(childFragmentManager, datePicker.toString())
    }

    private fun showPriorityPicker() {
        val items = resources.getStringArray(R.array.priority_array).toList()
        val adapter = PriorityAdapter(requireContext(), items)

        val prioritySpinner = binding.prioritySpinner
        prioritySpinner.adapter = adapter

        if (parcelItem != null) {
            val position = items.indexOf(getPriorityString(priority))
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
                val selectedPriority: Priority = getPriorityEnum(currentPriority)
                priority = selectedPriority
                updateSaveButtonState()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun getPriorityEnum(priorityString: String): Priority {
        return when (priorityString) {
            "Обычный приоритет" -> Priority.STANDARD
            "Высокий приоритет" -> Priority.IMPORTANT
            else -> Priority.STANDARD
        }
    }

    private fun getPriorityString(priority: Priority): String {
        return when (priority) {
            Priority.STANDARD -> "Обычный приоритет"
            Priority.IMPORTANT -> "Высокий приоритет"
        }
    }

    private fun showTimePickerForStart() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setTitleText("Время начала задачи")
            .setTheme(R.style.MaterialTimePickerTheme)
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute

            val selectedTimeForStart = Calendar.getInstance()
            selectedTimeForStart.set(Calendar.HOUR_OF_DAY, selectedHour)
            selectedTimeForStart.set(Calendar.MINUTE, selectedMinute)

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTime = selectedTimeForStart.time.let { timeFormat.format(it) }
            timeStart = formattedTime
            timeStartPickerTV.text = timeStart
            timePicked()
            checkTime()
        }

        timePicker.show(childFragmentManager, timePicker.toString())
    }

    private fun showTimePickerForEnd() {
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
            checkTime()
        }

        timePicker.show(childFragmentManager, timePicker.toString())
    }

    private fun colorPicker() {
        binding.colorPicker.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton: RadioButton =
                view?.findViewById(checkedId) ?: return@setOnCheckedChangeListener
            val selectedColorTag: String = selectedRadioButton.tag as String

            val selectedColor: Color = getColorEnum(selectedColorTag)
            color = selectedColor
            setBackgroundIconColor(color)

            updateSaveButtonState()
        }
    }

    private fun getColorEnum(colorString: String): Color {
        return try {
            Color.valueOf(colorString)
        } catch (e: IllegalArgumentException) {
            Color.BLUE
        }
    }

    companion object {
        const val KEY_TASK_FRAGMENT_RESULT_ADD = "KEY_TASK_FRAGMENT_RESULT_ADD"
        const val KEY_TASK_FRAGMENT_RESULT_UPD = "KEY_FRAGMENT_RESULT_UPD"

        const val FRAGMENT_TASK_ITEM = "taskItem"
    }
}