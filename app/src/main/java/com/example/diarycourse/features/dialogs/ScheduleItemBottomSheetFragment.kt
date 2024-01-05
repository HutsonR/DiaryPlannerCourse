package com.example.diarycourse.features.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.diarycourse.App
import com.example.diarycourse.R
import com.example.diarycourse.domain.domain_api.UseCase
import com.example.diarycourse.domain.models.ScheduleItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar
import javax.inject.Inject

    class ScheduleItemBottomSheetFragment : BottomSheetDialogFragment() {

        private lateinit var title: String
        private lateinit var dayOfWeek: String
        private lateinit var startTime: String
        private lateinit var description: String
//        private lateinit var dialogListener: DialogListener
        @Inject
        lateinit var useCase: UseCase

        override fun onAttach(context: Context) {
            super.onAttach(context)
            (context.applicationContext as App).appComponent.inject(this)

//            try {
//                dialogListener = parentFragment as DialogListener
//            } catch (e: ClassCastException) {
//                throw ClassCastException("Parent fragment must implement DialogListener")
//            }
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

            val deleteButton: Button = view.findViewById(R.id.schedule_sheet_buttonDelete)

            // Получите модель из аргументов
            val scheduleItem = arguments?.getParcelable<ScheduleItem>("scheduleItem")

            // Теперь вы можете использовать все поля модели
            if (scheduleItem != null) {
                title = scheduleItem.text
                description = scheduleItem.description
                startTime = scheduleItem.startTime
                dayOfWeek = scheduleItem.date

//                deleteButton.setOnClickListener {
//                    scheduleItem.id?.let {
//                        lifecycleScope.launch {
//                            val result = useCase.deleteById(it)
//                            if (result is Resource.Success) {
//                                dialogListener.onScheduleItemDeleted(true)
//                                dismiss()
//                            } else if (result is Resource.Empty.Failed) {
//                                dialogListener.onScheduleItemDeleted(false)
//                                Toast.makeText(requireContext(), "Ошибка удаления", Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    }
//                }
            }

            titleTV.text = title
            descriptionTV.text = description
            startTimeTV.text = startTime
            dayOfWeekTV.text = "${setDayOfWeek(dayOfWeek)},"

            return view
        }

        private fun setDayOfWeek(day: String): String {
            val year = day.substring(0, 4).toInt()
            val month = day.substring(4, 5).toInt() - 1
            val dayOfMonth = day.substring(5).toInt()

            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
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

    }
