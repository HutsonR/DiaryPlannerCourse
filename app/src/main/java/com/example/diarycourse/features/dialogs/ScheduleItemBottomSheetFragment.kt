package com.example.diarycourse.features.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.example.diarycourse.R
import com.example.diarycourse.domain.models.ScheduleItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ScheduleItemBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var title: String
    private lateinit var startTime: String
    private lateinit var description: String

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_schedule_item_bottom_sheet, container, false)

        // Access the views in the Bottom Sheet layout and set the data
        val titleTextView: TextView = view.findViewById(R.id.schedule_sheet_title)
        val descriptionTextView: TextView = view.findViewById(R.id.schedule_sheet_description)
        val startTimeTextView: TextView = view.findViewById(R.id.schedule_sheet_timeStart)

        // Получите модель из аргументов
        val scheduleItem = arguments?.getParcelable<ScheduleItem>("scheduleItem")

        // Теперь вы можете использовать все поля модели
        if (scheduleItem != null) {
            title = scheduleItem.text
            description = scheduleItem.description
            startTime = scheduleItem.startTime
        }

        titleTextView.text = title
        descriptionTextView.text = description
        startTimeTextView.text = startTime

        return view
    }
}
