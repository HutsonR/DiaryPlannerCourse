package com.easyflow.diarycourse.features.feature_home.schedule.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.features.feature_home.schedule.utils.Priority
import com.easyflow.diarycourse.features.feature_home.task.TaskFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar

class ScheduleItemBottomSheetFragment : BottomSheetDialogFragment() {
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

        parcelItem?.let {
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
                sendItemToDelete(parcelItem)
                dismiss()
            }
            completeButton.setOnClickListener {
                val updatedItem = parcelItem.copy(isCompleteTask = !parcelItem.isCompleteTask)
                sendItemToUpdate(updatedItem)
                dismiss()
            }
            editButton.setOnClickListener {
                val taskFragment = TaskFragment()
                val bundle = Bundle()
                bundle.putParcelable("scheduleItem", parcelItem)
                taskFragment.arguments = bundle

                taskFragment.show(parentFragmentManager, "taskFragment")
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

    private fun sendItemToDelete(item: ScheduleItem) {
        val bundle = Bundle().apply {
            putParcelable(FRAGMENT_TASK_ITEM, item)
        }
        activity?.supportFragmentManager?.setFragmentResult(KEY_BOTTOM_SHEET_RESULT_DEL, bundle)
    }

    private fun sendItemToUpdate(item: ScheduleItem) {
        val bundle = Bundle().apply {
            putParcelable(FRAGMENT_TASK_ITEM, item)
        }
        activity?.supportFragmentManager?.setFragmentResult(KEY_BOTTOM_SHEET_RESULT_UPD, bundle)
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

    companion object {
        const val KEY_BOTTOM_SHEET_RESULT_DEL = "KEY_BOTTOM_SHEET_RESULT_DEL"
        const val KEY_BOTTOM_SHEET_RESULT_UPD = "KEY_FRAGMENT_RESULT_UPD"

        const val FRAGMENT_TASK_ITEM = "taskItem"
    }

}
