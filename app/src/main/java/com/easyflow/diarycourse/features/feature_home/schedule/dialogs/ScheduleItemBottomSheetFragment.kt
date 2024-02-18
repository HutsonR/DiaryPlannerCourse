package com.easyflow.diarycourse.features.feature_home.schedule.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.domain.util.Resource
import com.easyflow.diarycourse.features.feature_home.schedule.ScheduleViewModel
import com.easyflow.diarycourse.features.feature_home.schedule.utils.Priority
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Calendar

class ScheduleItemBottomSheetFragment(private val viewModel: ScheduleViewModel, private val fragmentManager: FragmentManager) : BottomSheetDialogFragment() {
    private val TAG = "debugTag"
    private lateinit var title: String
    private lateinit var dayOfWeek: String
    private lateinit var startTime: String
    private lateinit var description: String
    private lateinit var priority: String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).appComponent.inject(this)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule_item_bottom_sheet, container, false)

        lifecycleScope.launch {
            observeState()
        }

        val titleTV: TextView = view.findViewById(R.id.schedule_sheet_title)
        val descriptionTV: TextView = view.findViewById(R.id.schedule_sheet_description)
        val startTimeTV: TextView = view.findViewById(R.id.schedule_sheet_timeStart)
        val dayOfWeekTV: TextView = view.findViewById(R.id.schedule_sheet_day_of_week)
        val priorityTV: TextView = view.findViewById(R.id.priorityText)
        val priorityIcon: ImageView = view.findViewById(R.id.priorityIcon)

        val deleteButton: Button = view.findViewById(R.id.schedule_sheet_buttonDelete)
        val completeButton: Button = view.findViewById(R.id.schedule_sheet_buttonComplete)
        val editButton: Button = view.findViewById(R.id.schedule_sheet_buttonEdit)

        val parcelItem = arguments?.getParcelable<ScheduleItem>("scheduleItem")

        if (parcelItem != null) {
            title = parcelItem.text
            description = parcelItem.description
            startTime = parcelItem.startTime
            dayOfWeek = parcelItem.date
            priority = getPriorityString(parcelItem.priority)

            if (parcelItem.priority == Priority.IMPORTANT) {
                val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
                val flagActive = ContextCompat.getDrawable(requireContext(), R.drawable.ic_flag_small_active)
                priorityTV.setTextColor(primaryColor)
                priorityIcon.setImageDrawable(flagActive)
            }

            if (parcelItem.isCompleteTask)
                completeButton.text = getString(R.string.button_uncomplete)

            deleteButton.setOnClickListener {
                parcelItem.id?.let {
                    lifecycleScope.launch {
                        viewModel.deleteItem(parcelItem.id)
                    }
                }
            }
            completeButton.setOnClickListener {
                lifecycleScope.launch {
                    val updatedItem = parcelItem.copy(isCompleteTask = !parcelItem.isCompleteTask)
                    viewModel.updateData(data = updatedItem)
                }
            }
            editButton.setOnClickListener {
                val taskDialogFragment = TaskDialogFragment(R.layout.fragment_task, viewModel)
                // Передайте всю модель в аргументы
                val args = Bundle()
                args.putParcelable("scheduleItem", parcelItem)
                taskDialogFragment.arguments = args

                taskDialogFragment.show(fragmentManager, "add fragment")
                dismiss()
            }

        }

        titleTV.text = title
        descriptionTV.text = description
        startTimeTV.text = startTime
        dayOfWeekTV.text = "${setDayOfWeek(dayOfWeek)},"
        priorityTV.text = priority

        return view
    }

    private suspend fun observeState() {
        viewModel
            .state
            .onEach { state ->
                resultCollect(state.result)
                updateCollect(state.update)
            }.collect()
    }

    private fun resultCollect(result: Resource?) {
        result?.let {
            when (it) {
                is Resource.Success -> dismiss()
                is Resource.Empty.Failed -> onFailed()
            }
        }
    }

    private fun updateCollect(result: Resource?) {
        result?.let {
            when (it) {
                is Resource.Success -> dismiss()
                is Resource.Empty.Failed -> onFailed()
            }
        }
    }

    private fun getPriorityString(priority: Priority): String {
        return when (priority) {
            Priority.STANDARD -> "Обычное"
            Priority.IMPORTANT -> "Важное"
        }
    }

    private fun setDayOfWeek(day: String): String {
        val dayOfMonth = day.substring(0, 2).toInt()
        val month = day.substring(3, 5).toInt() - 1
        val year = day.substring(6).toInt()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, "20$year".toInt())
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }

        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Воскресенье"
            Calendar.MONDAY -> "Понедельник"
            Calendar.TUESDAY -> "Вторник"
            Calendar.WEDNESDAY -> "Среда"
            Calendar.THURSDAY -> "Четверг"
            Calendar.FRIDAY -> "Пятница"
            Calendar.SATURDAY -> "Суббота"
            else -> "Неизвестно"
        }
    }

    private fun onFailed() {
        Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_SHORT).show()
    }

}
