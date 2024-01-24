package com.example.diarycourse.features.feature_home.schedule.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.example.diarycourse.App
import com.example.diarycourse.R
import com.example.diarycourse.domain.models.ScheduleItem
import com.example.diarycourse.domain.util.Resource
import com.example.diarycourse.features.feature_home.schedule.ScheduleViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import java.util.Calendar

class ScheduleItemBottomSheetFragment(private val viewModel: ScheduleViewModel, private val fragmentManager: FragmentManager) : BottomSheetDialogFragment() {
        private val TAG = "debugTag"
        private lateinit var title: String
        private lateinit var dayOfWeek: String
        private lateinit var startTime: String
        private lateinit var description: String
//        private lateinit var dialogListener: DialogListener

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
            val completeButton: Button = view.findViewById(R.id.schedule_sheet_buttonComplete)
            val editButton: Button = view.findViewById(R.id.schedule_sheet_buttonEdit)

            // Получите модель из аргументов
            val parcelItem = arguments?.getParcelable<ScheduleItem>("scheduleItem")

            // Теперь вы можете использовать все поля модели
            if (parcelItem != null) {
                title = parcelItem.text
                description = parcelItem.description
                startTime = parcelItem.startTime
                dayOfWeek = parcelItem.date

                if (parcelItem.isCompleteTask)
                    completeButton.text = getString(R.string.splash_uncomplete)

                deleteButton.setOnClickListener {
                    parcelItem.id?.let {
                        lifecycleScope.launch {
                            viewModel.deleteItem(parcelItem.id)

                            viewModel.result.collect { result: Resource ->
                                when (result) {
                                    is Resource.Success -> dismiss()
                                    is Resource.Empty.Failed -> onFailed()
                                }
                            }
                        }
                    }
                }
                completeButton.setOnClickListener {
                    lifecycleScope.launch {
                        val updatedItem = parcelItem.copy(isCompleteTask = !parcelItem.isCompleteTask)
                        viewModel.updateData(data = updatedItem)

                        viewModel.update.collect { result: Resource ->
                            when (result) {
                                is Resource.Success -> dismiss()
                                is Resource.Empty.Failed -> onFailed()
                            }
                        }
                    }
                }
                editButton.setOnClickListener {
                    val taskDialogFragment = TaskDialogFragment(R.layout.fragment_add, viewModel)
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

            return view
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
            Toast.makeText(requireContext(), "Возникла ошибка, попробуйте позже", Toast.LENGTH_SHORT).show()
        }

    }
