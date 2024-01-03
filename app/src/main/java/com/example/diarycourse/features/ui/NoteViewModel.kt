package com.example.diarycourse.features.ui

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.View
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diarycourse.R
import com.example.diarycourse.data.database.AppDatabase
import com.example.diarycourse.data.database.ScheduleItemDao
import com.example.diarycourse.data.models.ScheduleItem
import com.example.diarycourse.databinding.FragmentNoteBinding
import com.example.diarycourse.domain.domain_api.UseCase
import com.example.diarycourse.features.adapter.ScheduleAdapter
import com.example.diarycourse.features.dialogs.AddDialogFragment
import com.example.diarycourse.features.dialogs.DialogListener
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

class NoteViewModel @Inject constructor (
    private val useCase: UseCase,
    private val scheduleItemDao: ScheduleItemDao,
    application: Application
) : ViewModel() {

    private val TAG = "debugTag"
//    private val appContext: Context = application.applicationContext
    private var dataList: MutableList<ScheduleItem> = mutableListOf()
    private var adapter: ScheduleAdapter = ScheduleAdapter(dataList)

    fun init() {
        val ioScope = CoroutineScope(Dispatchers.IO)
        ioScope.launch {
            val historyItems = scheduleItemDao.getAll()
            val startPosition = dataList.size // Store the current size of dataList

            dataList.addAll(historyItems)

            adapter.notifyItemRangeInserted(startPosition, historyItems.size)
        }

    }

    private fun calculateDuration(startTime: String, endTime: String?): String {
        if (endTime == null) {
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

    fun addData(title: String, text: String, date: String, timeStart: String, timeEnd: String) {
        val data = ScheduleItem(
            startTime = timeStart,
            endTime = timeEnd,
            text = title,
            description = text,
            duration = calculateDuration(timeStart, timeEnd),
            isCompleteTask = false
        )
        dataList.add(data)
        viewModelScope.launch {
            useCase.insert(data)
        }
        adapter.notifyDataSetChanged()
    }

    class NoteViewModelFactory @Inject constructor(
        private val useCase: UseCase,
        private val scheduleItemDao: ScheduleItemDao,
        private val application: Application
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteViewModel(useCase, scheduleItemDao, application) as T
        }
    }

}