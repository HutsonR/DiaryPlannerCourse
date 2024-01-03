package com.example.diarycourse.features.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.diarycourse.R
import com.example.diarycourse.databinding.FragmentAddBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class AddDialogFragment(private val layoutResourceId: Int) : DialogFragment() {
    private val TAG = "debugTag"
    private lateinit var binding: FragmentAddBinding
    private var title: String = ""
    private var text: String = ""
    private var date: String = ""
    private var timeStart: String  = ""
    private var timeEnd: String  = ""
    private lateinit var titleEditText: TextView
    private lateinit var textEditText: TextView
    private lateinit var dialogListener: DialogListener
    private lateinit var saveButton: ImageButton
    private lateinit var cancelButton: ImageButton

    //  проверка, что активити, вызывающая DialogFragment, реализует интерфейс DialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            dialogListener = parentFragment as DialogListener
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
        saveButton = binding.addSave
        cancelButton = binding.addClose
        saveButton.setOnClickListener {
            handleSaveButtonClicked()
            dismiss()
        }
        cancelButton.setOnClickListener {
            dismiss()
        }

        titleEditText()
        deskEditText()
        binding.datePicker.setOnClickListener { showDatePicker() }
        binding.timeStartPicker.setOnClickListener { showTimePickerForStart() }
        binding.timeEndPicker.setOnClickListener { showTimePickerForEnd() }

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
        val isTitleFilled = title.isNotEmpty()
        val isTextFilled = text.isNotEmpty()
        val isDateFilled = date.isNotEmpty()
        val isTimeStartFilled = timeStart.isNotEmpty()
        val isTimeEndFilled = timeEnd.isNotEmpty()

        val isEnabled = isTitleFilled && isTextFilled && isDateFilled && isTimeStartFilled && isTimeEndFilled

        saveButton.isEnabled = isEnabled
        saveButton.alpha = if (isEnabled) 1.0f else 0.5f
    }


    private fun handleSaveButtonClicked() {
        dialogListener.onConfirmAddDialogResult(title, text, date, timeStart, timeEnd)
    }

    private fun titleEditText() {
        titleEditText = binding.addTitleTask
        titleEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                title = s.toString()
                updateSaveButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun deskEditText() {
        textEditText = binding.addDeskTask
        textEditText.addTextChangedListener(object : TextWatcher {
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

        val datePicker = builder.build()

        datePicker.addOnPositiveButtonClickListener { selectedTimestamp ->
            val selectedDate = Calendar.getInstance()
            selectedDate.timeInMillis = selectedTimestamp
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            val formattedDate = selectedDate.time.let { dateFormat.format(it) }
            date = formattedDate
            binding.datePickerText.text = formattedDate
            updateSaveButtonState()
        }

        datePicker.show(childFragmentManager, datePicker.toString())
    }

    private fun showTimePickerForStart() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setTitleText("Выберите время начала")
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute

            val selectedTimeForStart = Calendar.getInstance()
            selectedTimeForStart.set(Calendar.HOUR_OF_DAY, selectedHour)
            selectedTimeForStart.set(Calendar.MINUTE, selectedMinute)

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTime = selectedTimeForStart.time.let { it1 -> timeFormat.format(it1) }
            timeStart = formattedTime
            binding.timeStartPickerText.text = formattedTime
            updateSaveButtonState()
        }

        timePicker.show(childFragmentManager, timePicker.toString())
    }
    private fun showTimePickerForEnd() {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setTitleText("Выберите время окончания")
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute

            var selectedTimeForEnd = Calendar.getInstance()
            selectedTimeForEnd.set(Calendar.HOUR_OF_DAY, selectedHour)
            selectedTimeForEnd.set(Calendar.MINUTE, selectedMinute)

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formattedTime = selectedTimeForEnd.time.let { it1 -> timeFormat.format(it1) }
            timeEnd = formattedTime
            binding.timeEndPickerText.text = formattedTime
            updateSaveButtonState()
        }

        timePicker.show(childFragmentManager, timePicker.toString())
    }


}