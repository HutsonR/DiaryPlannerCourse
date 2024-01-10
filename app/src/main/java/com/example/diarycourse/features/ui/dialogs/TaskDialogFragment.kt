package com.example.diarycourse.features.ui.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.diarycourse.R
import com.example.diarycourse.databinding.FragmentAddBinding
import com.example.diarycourse.domain.models.ScheduleItem
import com.example.diarycourse.domain.util.Resource
import com.example.diarycourse.features.ui.NoteViewModel
import com.example.diarycourse.features.ui.utils.Color
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


class TaskDialogFragment(private val layoutResourceId: Int, private val viewModel: NoteViewModel) : DialogFragment() {
    private val TAG = "debugTag"
    private lateinit var binding: FragmentAddBinding
    private var dataList: MutableList<ScheduleItem> = mutableListOf()
    private var sortedDataByDate: MutableList<ScheduleItem> = mutableListOf()
    private var parcelItem: ScheduleItem? = null
    private var previousTitle: String = ""
    private var previousText: String = ""
    private var previousDate: String = ""
    private var previousTimeStart: String = ""
    private var previousTimeEnd: String = ""
    private var previousColor: Color = Color.BLUE
    private var title: String = ""
    private var text: String = ""
    private var date: String = ""
    private var timeStart: String  = ""
    private var timeEnd: String = ""
    private var color: Color = Color.BLUE
    private lateinit var titleEditTV: TextView
    private lateinit var textEditTV: TextView
    private lateinit var datePickerTV: TextView
    private lateinit var timeStartPickerTV: TextView
    private lateinit var timeEndPickerTV: TextView
    private lateinit var dialogListener: DialogListener
    private lateinit var saveButton: ImageButton
    private lateinit var cancelButton: ImageButton

    //  проверка, что активити, вызывающая DialogFragment, реализует интерфейс DialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            dialogListener = requireParentFragment() as DialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("Parent fragment must implement DialogListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAddBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        titleEditTV = binding.addTitleTask
        textEditTV = binding.addDeskTask
        datePickerTV = binding.datePickerText
        timeStartPickerTV = binding.timeStartPickerText
        timeEndPickerTV = binding.timeEndPickerText

        saveButton = binding.addSave
        cancelButton = binding.addClose
        saveButton.setOnClickListener {
            handleSaveButtonClicked()
        }
        cancelButton.setOnClickListener {
            dismiss()
        }

//        fetchData()
        titleEditText()
        deskEditText()
        binding.datePicker.setOnClickListener { showDatePicker() }
        binding.timeStartPicker.setOnClickListener { showTimePickerForStart() }
        colorPicker()
        binding.timeEndPicker.alpha = 0.5f

        // Если имеется модель (фрагмент открыт для редактирования)
        parcelItem = arguments?.getParcelable("scheduleItem")
        if (parcelItem != null) {
            binding.titleAddFragment.text = getString(R.string.add_title_edit)

            title = parcelItem!!.text
            text = parcelItem!!.description
            date = parcelItem!!.date
            timeStart = parcelItem!!.startTime
            timeEnd = parcelItem!!.endTime
            color = parcelItem!!.color

            previousTitle = title
            previousText = text
            previousDate = date
            previousTimeStart = timeStart
            previousTimeEnd = timeEnd
            previousColor = color

            titleEditTV.text = parcelItem!!.text
            textEditTV.text = parcelItem!!.description
            Log.d(TAG, "date $date")
            datePickerTV.text = formatDate(parcelItem!!.date)
            Log.d(TAG, "formatDate ${datePickerTV.text}")
            timeStartPickerTV.text = parcelItem!!.startTime
            timeEndPickerTV.text = parcelItem!!.endTime.ifEmpty { getString(R.string.add_date_time_blank) }
            setColor()
            // Для активации кнопки конца времени
            checkTime()
        }

        // Изначально деактивируем кнопку "Сохранить"
        updateSaveButtonState()
    }


    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        dialog!!.window?.setLayout(width, height)
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

            val isEnabled = (isTitleChanged || isDateChanged || isTimeStartChanged || isTextChanged || isTimeEndChanged || isColorChanged) &&
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
                    startTime = timeStart,
                    endTime = timeEnd,
                    duration = calculateDuration(timeStart, timeEnd),
                    color = color,
                    isCompleteTask = parcelItem!!.isCompleteTask
                )
//                Log.d(TAG, "text $title, description $text, date $date, timeEnd $timeEnd")
                viewModel.updateData(data = updatedItem)

                viewModel.update.collect { result: Resource ->
                    when (result) {
                        is Resource.Success -> dismiss()
                        is Resource.Empty.Failed -> onFailed()
                    }
                }
            }
        } else {
            // По умолчанию обычное добавление элемента
            dialogListener.onConfirmAddDialogResult(title, text, date, timeStart, timeEnd, color)
            Log.d(TAG, "color add $color")
            dismiss()
        }
    }

    private fun onFailed() {
        showCustomToast("Возникла ошибка, попробуйте позже", Toast.LENGTH_SHORT)
        dismiss()
    }

    private fun fetchData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.dataList.collect { scheduleItems: List<ScheduleItem> ->
                dataList.apply {
                    clear()
                    addAll(scheduleItems)
                }
            }
        }
    }

    private fun formatDate(inputDateString: String): String {
        val inputFormat = if (inputDateString.length == 6) {
            SimpleDateFormat("yyyyMd", Locale.getDefault())
        } else {
            SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        }

        return try {
            val date = inputFormat.parse(inputDateString)

            // Форматирование даты в требуемый формат
            val outputFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            outputFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            "Invalid Date"
        }
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

    private fun viewClearTime() {
        binding.addClearTimeTV.visibility = View.VISIBLE
        binding.addClearTime.setOnClickListener {
            binding.addClearTimeTV.visibility = View.GONE
            timeStart = ""
            timeStartPickerTV.text = getString(R.string.add_date_time_blank)
            timeEnd = ""
            timeEndPickerTV.text = getString(R.string.add_date_time_blank)
            checkTime()
            updateSaveButtonState()
        }
    }

    private fun checkTime() {
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
        if (timeStart.isNotEmpty() && timeEnd.isNotEmpty()) {
            Log.d(TAG, "dataList $dataList")
            val currentStartTime = LocalTime.parse(timeStart, timeFormat)
            val currentEndTime = LocalTime.parse(timeEnd, timeFormat)
//            Проверка между временами в выбранной дате
//            if (date.isNotEmpty()) {
//
//                dataList.forEach {
//                    if (it.date == date)
//                        sortedDataByDate.add(it)
//                }
//
//                val sortedDateByTime = sortedDataByDate.sortedBy { it.startTime }
//                var isComplete = false
//                var prevTask: ScheduleItem = sortedDateByTime.first()
//
//                sortedDateByTime.drop(1).forEach {
//                    val startTimeItem = LocalTime.parse(it.startTime, timeFormat)
//                    val endTimeItem = LocalTime.parse(prevTask.endTime, timeFormat)
//
//                    if (currentStartTime.isAfter(endTimeItem) && currentEndTime.isBefore(startTimeItem)) {
//                        timeEnd = ""
//                        timeEndPickerTV.text = getString(R.string.add_date_time_blank)
//                        timeStart = ""
//                        timeStartPickerTV.text = getString(R.string.add_date_time_blank)
//                        showCustomToast("Конечное время пересекается с задачей (начинается на $startTimeItem, оканчивается на $endTimeItem)", Toast.LENGTH_SHORT)
//                        isComplete = true
//                        return@forEach
//                    }
//                    prevTask = it
//                }
//                if (isComplete)
//                    showCustomToast("Все в порядке", Toast.LENGTH_SHORT)
//            }

//            Проверка между двумя временами в окне добавления
            if (currentStartTime.isAfter(currentEndTime)) {
                showCustomToast("Начальное время не может быть больше конечного", Toast.LENGTH_SHORT)
                timeStart = ""
                timeStartPickerTV.text = getString(R.string.add_date_time_blank)
                timeEnd = ""
                timeEndPickerTV.text = getString(R.string.add_date_time_blank)
                binding.addClearTimeTV.visibility = View.GONE
            } else if (currentStartTime == currentEndTime) {
                showCustomToast("Если начальное время одинаково с конечным, то конечное можно не писать", Toast.LENGTH_SHORT)
                timeEnd = ""
                timeEndPickerTV.text = getString(R.string.add_date_time_blank)
            }
        }

        if (timeStart.isNotEmpty()) {
            binding.timeEndPicker.alpha = 1f
            binding.timeEndPicker.setOnClickListener { showTimePickerForEnd() }
        } else {
            binding.timeEndPicker.alpha = 0.5f
            binding.timeEndPicker.setOnClickListener {  }
        }

        updateSaveButtonState()
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

    private fun showCustomToast(message: String, duration: Int) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.custom_toast, binding.root.findViewById(R.id.custom_toast_layout))

        val text = layout.findViewById<TextView>(R.id.customToastText)
        text.text = message

        val toast = Toast(requireContext())
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 80)
        toast.duration = duration
        toast.view = layout
        toast.show()
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

    private fun showDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Выберите дату")
        builder.setTheme(R.style.MaterialCalendarPickerTheme)

        val datePicker = builder.build()

        datePicker.addOnPositiveButtonClickListener { selectedTimestamp ->
            val selectedDate = Calendar.getInstance()
            selectedDate.timeInMillis = selectedTimestamp
            val dateFormat = SimpleDateFormat("yyyyMd", Locale.getDefault())
            val formattedDate = selectedDate.time.let { dateFormat.format(it) }
            val dateFormatForUser = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            val formattedDateForUser = selectedDate.time.let { dateFormatForUser.format(it) }
            date = formattedDate
            datePickerTV.text = formattedDateForUser
            checkTime()
        }

        datePicker.show(childFragmentManager, datePicker.toString())
    }

    private fun showTimePickerForStart() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setTitleText("Выберите время начала задачи")
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
            viewClearTime()
            checkTime()
        }

        timePicker.show(childFragmentManager, timePicker.toString())
    }
    private fun showTimePickerForEnd() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setTitleText("Выберите время окончания задачи")
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
            viewClearTime()
            checkTime()
        }

        timePicker.show(childFragmentManager, timePicker.toString())
    }

    private fun colorPicker() {
        binding.colorPicker.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton: RadioButton = view?.findViewById(checkedId) ?: return@setOnCheckedChangeListener
            val selectedColorTag: String = selectedRadioButton.tag as String

            val selectedColor: Color = getColorEnum(selectedColorTag)
            color = selectedColor

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

}