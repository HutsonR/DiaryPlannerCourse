package com.example.diarycourse.features.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diarycourse.App
import com.example.diarycourse.R
import com.example.diarycourse.domain.models.ScheduleItem
import com.example.diarycourse.databinding.FragmentNoteBinding
import com.example.diarycourse.domain.util.Resource
import com.example.diarycourse.features.adapter.ScheduleAdapter
import com.example.diarycourse.features.dialogs.AddDialogFragment
import com.example.diarycourse.features.dialogs.DialogListener
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar
import dagger.Lazy
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

class NoteFragment : Fragment(), DialogListener {
    private val TAG = "debugTag"
    private lateinit var binding: FragmentNoteBinding
    @Inject lateinit var notesViewModelFactory: Lazy<NoteViewModel.NoteViewModelFactory>
    private val viewModel: NoteViewModel by viewModels {
        notesViewModelFactory.get()
    }
    private lateinit var collapsibleCalendar: CollapsibleCalendar
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleAdapter
    private var dataList: MutableList<ScheduleItem> = mutableListOf()
    private var dateSelected: String = ""

    companion object {
        fun newInstance() = NoteFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collapsibleCalendar = binding.calendarView

//        viewModel.action.onEach(::handleAction).collectOnStart(viewLifecycleOwner)

//        viewModel.init()
        subscribeToFlow()
        viewModel.fetchData()
        setCalendarListener()
        setDayOfWeek()
        setSelectedDayButtons()
        setAddButton()
        setRecycler()

    }

    private fun setAddButton() {
        binding.fabAdd.setOnLongClickListener {
            dateSelected = ""
            viewModel.fetchData()
            true
        }
        binding.fabAdd.setOnClickListener {
            AddDialogFragment(R.layout.fragment_add).show(childFragmentManager, "add fragment")
        }
    }

    private fun subscribeToFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataList.collect { scheduleItems: List<ScheduleItem> ->
                    dataList.clear()
                    val sortedDataByTime = sortItemsByTime(scheduleItems)
                    val sortedDataByDate = sortItemsByDate(sortedDataByTime)
                    dataList.addAll(sortedDataByDate)
                    adapter.notifyDataSetChanged()
                    countSchedules(dataList)
                    Log.d(TAG, "Выполнился dataList collect $dataList")
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
                viewModel.result.collect { result: Resource ->
                    when (result) {
                        is Resource.Success -> onSuccess()
                        is Resource.Empty.Failed -> onFailed()
                    }
                    Log.d(TAG, "Выполнился result collect")
                }
            }
        }
    }

    private fun onSuccess() {
        viewModel.fetchData()
        countSchedules(dataList)
        Toast.makeText(requireContext(), "Данные получены", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Выполнился success collect")
    }

    private fun onFailed() {
        Toast.makeText(requireContext(), "Ошибка получения данных", Toast.LENGTH_SHORT).show()
        countSchedules(dataList)
        Log.d(TAG, "Выполнился failed collect")
    }

    private fun sortItemsByTime(dataList: List<ScheduleItem>): List<ScheduleItem> {
        return dataList.sortedBy { it.startTime }
    }

    private fun sortItemsByDate(dataList: List<ScheduleItem>): List<ScheduleItem> {
        val sortedData: MutableList<ScheduleItem> = mutableListOf()
        Log.d(TAG, dateSelected)
        if (dateSelected.isNotEmpty()) {
            dataList.forEach {
                if (it.date == dateSelected)
                    sortedData.add(it)
            }
            Log.d(TAG, "dataList $dataList")
            Log.d(TAG, "sortedData $sortedData")
            return sortedData
        } else
            return dataList
    }

    // Подсчет кол-ва записей на день
    private fun countSchedules(dataList: List<ScheduleItem>) {
        binding.countSchedules.text = dataList.size.toString()
        if (dataList.isEmpty()) {
            binding.scheduleBlank.visibility = View.VISIBLE
        } else {
            binding.scheduleBlank.visibility = View.GONE
        }
    }

    private fun setRecycler() {
        recyclerView = binding.recycleSchedule
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        adapter = ScheduleAdapter(dataList)
        recyclerView.adapter = adapter
    }

    // Обработка нажатий календаря
    private fun setCalendarListener() {
        collapsibleCalendar.setCalendarListener(object : CollapsibleCalendar.CalendarListener {
            override fun onDaySelect() {
                val day = collapsibleCalendar.selectedDay
                dateSelected = "${day?.year}${(day?.month)?.plus(1)}${day?.day}"
                viewModel.fetchData()
                setSelectedDayOfWeek()
            }

            override fun onItemClick(view: View) {}
            override fun onClickListener() {}
            override fun onDataUpdate() {}
            override fun onDayChanged() {}
            override fun onMonthChange() {}
            override fun onWeekChange(i: Int) {}
        })
    }

    // Установка дня недели при запуске приложения
    private fun setDayOfWeek() {
        val calendar = Calendar.getInstance()
        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Воскресенье"
            Calendar.MONDAY -> "Понедельник"
            Calendar.TUESDAY -> "Вторник"
            Calendar.WEDNESDAY -> "Среда"
            Calendar.THURSDAY -> "Четверг"
            Calendar.FRIDAY -> "Пятница"
            Calendar.SATURDAY -> "Суббота"
            else -> "Неизвестно"
        }
        binding.textDayOfWeek.text = dayOfWeek
    }

    // Установка дня недели при выборе
    private fun setSelectedDayOfWeek() {
        collapsibleCalendar.selectedDay?.let { selectedDate ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedDate.year)
                set(Calendar.MONTH, selectedDate.month)
                set(Calendar.DAY_OF_MONTH, selectedDate.day)
            }
            val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "Воскресенье"
                Calendar.MONDAY -> "Понедельник"
                Calendar.TUESDAY -> "Вторник"
                Calendar.WEDNESDAY -> "Среда"
                Calendar.THURSDAY -> "Четверг"
                Calendar.FRIDAY -> "Пятница"
                Calendar.SATURDAY -> "Суббота"
                else -> "Неизвестно"
            }
            binding.textDayOfWeek.text = dayOfWeek
        }
    }

    // Обработка переключения дней в календаре с помощью кнопок
    private fun setSelectedDayButtons() {
        binding.dayBackButton.setOnClickListener {
            collapsibleCalendar.prevDay()
        }
        binding.dayNextButton.setOnClickListener {
            collapsibleCalendar.nextDay()
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

    // Получение данных из диалога добавления расписания
    override fun onConfirmAddDialogResult(
        title: String,
        text: String,
        date: String,
        timeStart: String,
        timeEnd: String
    ) {
        val data = ScheduleItem(
            text = title,
            description = text,
            date = date,
            startTime = timeStart,
            endTime = timeEnd,
            duration = calculateDuration(timeStart, timeEnd),
            isCompleteTask = false
        )
        viewModel.addData(data)
    }

}