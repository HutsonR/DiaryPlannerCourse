package com.example.diarycourse.features.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.diarycourse.App
import com.example.diarycourse.R
import com.example.diarycourse.data.database.ScheduleItemDao
import com.example.diarycourse.domain.domain_api.UseCase
import com.example.diarycourse.domain.models.ScheduleItem
import com.example.diarycourse.domain.util.Resource
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

    class ScheduleItemBottomSheetFragment : BottomSheetDialogFragment() {

        private lateinit var title: String
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

            // Access the views in the Bottom Sheet layout and set the data
            val titleTextView: TextView = view.findViewById(R.id.schedule_sheet_title)
            val descriptionTextView: TextView = view.findViewById(R.id.schedule_sheet_description)
            val startTimeTextView: TextView = view.findViewById(R.id.schedule_sheet_timeStart)

            val deleteButton: Button = view.findViewById(R.id.schedule_sheet_buttonDelete)

            // Получите модель из аргументов
            val scheduleItem = arguments?.getParcelable<ScheduleItem>("scheduleItem")

            // Теперь вы можете использовать все поля модели
            if (scheduleItem != null) {
                title = scheduleItem.text
                description = scheduleItem.description
                startTime = scheduleItem.startTime

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

            titleTextView.text = title
            descriptionTextView.text = description
            startTimeTextView.text = startTime

            return view
        }
    }
