package com.easyflow.diarycourse.features.feature_calendar.schedule.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.databinding.FragmentScheduleItemBottomSheetBinding
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.Priority
import com.easyflow.diarycourse.features.feature_calendar.task.TaskFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar

class ScheduleItemBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentScheduleItemBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var title: String = ""
    private var dayOfWeek: String = ""
    private var startTime: String = ""
    private var description: String = ""
    private var priority: String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).appComponent.inject(this)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScheduleItemBottomSheetBinding.inflate(inflater)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
    }

    private fun initialize() {
        val titleTV: TextView = binding.scheduleSheetTitle
        val descriptionTV: TextView = binding.scheduleSheetDescription
        val startTimeTV: TextView = binding.scheduleSheetTimeStart
        val dayOfWeekTV: TextView = binding.scheduleSheetDayOfWeek
        val priorityTV: TextView = binding.priorityText
        val priorityIcon: ImageView = binding.priorityIcon
        val separator: View = binding.scheduleSheetSeparator

        val deleteButton: Button = binding.scheduleSheetButtonDelete
        val completeButton: Button = binding.scheduleSheetButtonComplete
        val editButton: Button = binding.scheduleSheetButtonEdit

        val parcelItem = arguments?.getParcelable<ScheduleItem>("scheduleItem")

        parcelItem?.let {
            title = parcelItem.text
            description = parcelItem.description
            startTime = parcelItem.startTime
            dayOfWeek = parcelItem.date
            priority = getPriorityString(parcelItem.priority)

            if (parcelItem.priority == Priority.IMPORTANT) {
                val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
                priorityTV.setTextColor(primaryColor)
                priorityIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary))
            }

            if (parcelItem.isCompleteTask)
                completeButton.text = getString(R.string.button_uncomplete)

            if (description.isEmpty()) {
                separator.visibility = View.INVISIBLE
            }

            deleteButton.setOnClickListener {
                sendItemToDelete(parcelItem)
                dismiss()
            }
            completeButton.setOnClickListener {
                parcelItem.id?.let { itemId -> sendItemToUpdate(itemId) }
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
        if (dayOfWeek.isNotBlank()) {
            dayOfWeekTV.text = "${setDayOfWeek(dayOfWeek)},"
        } else {
            binding.dayOfWeekWrapper.visibility = View.GONE
        }
        priorityTV.text = priority
    }

    private fun sendItemToDelete(item: ScheduleItem) {
        val bundle = Bundle().apply {
            putParcelable(FRAGMENT_TASK_ITEM, item)
        }
        activity?.supportFragmentManager?.setFragmentResult(KEY_BOTTOM_SHEET_RESULT_DEL, bundle)
    }

    private fun sendItemToUpdate(itemId: Int) {
        val bundle = Bundle().apply {
            putInt(FRAGMENT_TASK_ITEM, itemId)
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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val KEY_BOTTOM_SHEET_RESULT_DEL = "KEY_BOTTOM_SHEET_RESULT_DEL"
        const val KEY_BOTTOM_SHEET_RESULT_UPD = "KEY_FRAGMENT_RESULT_UPD"

        const val FRAGMENT_TASK_ITEM = "taskItem"
    }

}
