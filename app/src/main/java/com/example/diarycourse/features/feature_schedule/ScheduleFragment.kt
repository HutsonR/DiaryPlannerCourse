package com.example.diarycourse.features.feature_schedule

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import com.example.diarycourse.databinding.FragmentScheduleBinding
import com.example.diarycourse.domain.util.Resource
import com.example.diarycourse.features.feature_schedule.adapter.ScheduleAdapter
import com.example.diarycourse.features.feature_schedule.dialogs.TaskDialogFragment
import com.example.diarycourse.features.feature_schedule.dialogs.DialogListener
import com.example.diarycourse.features.feature_schedule.utils.Color
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar
import dagger.Lazy
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class ScheduleFragment : Fragment(), DialogListener {
    private val TAG = "debugTag"
    private lateinit var binding: FragmentScheduleBinding
    @Inject lateinit var notesViewModelFactory: Lazy<ScheduleViewModel.NoteViewModelFactory>
    private val viewModel: ScheduleViewModel by viewModels {
        notesViewModelFactory.get()
    }
    private lateinit var collapsibleCalendar: CollapsibleCalendar
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleAdapter
    private var dataList: MutableList<ScheduleItem> = mutableListOf()
    private var adapterList: MutableList<ScheduleItem> = mutableListOf()
    private var dateSelected: String = ""

    companion object {
        fun newInstance() = ScheduleFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScheduleBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        collapsibleCalendar = binding.calendarView

//        viewModel.action.onEach(::handleAction).collectOnStart(viewLifecycleOwner)

        subscribeToFlow()
        viewModel.fetchData()
        setCalendarListener()
        setDayOfWeek()
        setSelectedDayButtons()
        setAddButton()
        setRecycler()
    }

    private fun setAddButton() {
        binding.fabAdd.setOnClickListener {
            TaskDialogFragment(R.layout.fragment_add, viewModel).show(childFragmentManager, "add fragment")
        }
    }

    private fun subscribeToFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataList.collect { scheduleItems: List<ScheduleItem> ->
                    dataList.apply {
                        clear()
                        addAll(scheduleItems)
                    }
                    sortItems(dataList)
                    updateEventsTag(dataList)
                    adapter.notifyDataSetChanged()
                    countSchedules(adapterList)
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
                }
            }
        }
    }

    private fun onSuccess() {
        viewModel.fetchData()
        updateEventsTag(dataList)
        countSchedules(adapterList)
    }

    private fun onFailed() {
        showCustomToast("Ошибка получения данных", Toast.LENGTH_SHORT)
    }

    private fun sortItemsByDate(dataList: List<ScheduleItem>): List<ScheduleItem> {
        val sortedData: MutableList<ScheduleItem> = mutableListOf()
        Log.d(TAG, "dateSelected $dateSelected")
        if (dateSelected.isNotEmpty()) {
            dataList.forEach {
                Log.d(TAG, "item date ${it.date}")
                if (it.date == dateSelected)
                    sortedData.add(it)
            }
            return sortedData
        } else {
            val today = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            dateSelected = dateFormat.format(today)
            return sortItemsByDate(dataList)
        }
    }

    private fun sortItemsByTime(dataList: List<ScheduleItem>): List<ScheduleItem> {
        return dataList.sortedBy { it.startTime }
    }

    private fun sortItems(dataList: List<ScheduleItem>) {
        val sortedDataByDate = sortItemsByDate(dataList)
        val sortedItemsByTime = sortItemsByTime(sortedDataByDate)
        adapterList.apply {
            clear()
            addAll(sortedItemsByTime)
        }
        adapter.notifyDataSetChanged()
        countSchedules(adapterList)
    }

    private fun updateEventsTag(dataList: List<ScheduleItem>) {
        val processedDates = mutableSetOf<String>()
        for (item in dataList) {
            val date = item.date
            if (processedDates.contains(date))
                continue
            else {
                processedDates.add(date)
                val dayOfMonth = date.substring(0, 2).toInt()
                val month = date.substring(3, 5).toInt() - 1
                val year = date.substring(6).toInt()
                collapsibleCalendar.addEventTag("20$year".toInt(), month, dayOfMonth, ContextCompat.getColor(requireContext(), R.color.blue))
            }
        }
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

    // Обработка нажатий календаря
    private fun setCalendarListener() {
        collapsibleCalendar.setCalendarListener(object : CollapsibleCalendar.CalendarListener {
            override fun onDaySelect() {
                val day = collapsibleCalendar.selectedDay
                val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
                dateSelected = if (day != null) {
                    val calendar = Calendar.getInstance()
                    calendar.set(day.year, day.month, day.day)
                    dateFormat.format(calendar.time)
                } else {
                    val currentDate = Calendar.getInstance().time
                    dateFormat.format(currentDate)
                }
                sortItems(dataList)
                setSelectedDayOfWeek()
            }

            override fun onItemClick(v: View) {}
            override fun onClickListener() {}
            override fun onDataUpdate() {}
            override fun onDayChanged() {}
            override fun onMonthChange() {}
            override fun onWeekChange(position: Int) {}
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

    private fun calculateDuration(startTime: String, endTime: String): String {
        if (endTime.isEmpty()) {
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

    private fun showCustomToast(message: String, duration: Int) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.custom_toast, binding.root.findViewById(R.id.custom_toast_layout))

        val text = layout.findViewById<TextView>(R.id.customToastText)
        text.text = message

        val toast = Toast(requireContext())
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 80)
        toast.duration = duration
        toast.view = layout
        toast.show()
    }

    private fun setRecycler() {
        recyclerView = binding.recycleSchedule
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        adapter = ScheduleAdapter(adapterList, viewModel, childFragmentManager)
        recyclerView.adapter = adapter
    }

    // Получение данных из диалога добавления расписания
    override fun onConfirmAddDialogResult(
        title: String,
        text: String,
        date: String,
        timeStart: String,
        timeEnd: String,
        color: Color
    ) {
        val data = ScheduleItem(
            text = title,
            description = text,
            date = date,
            startTime = timeStart,
            endTime = timeEnd,
            duration = calculateDuration(timeStart, timeEnd),
            color = color,
            isCompleteTask = false
        )
        viewModel.addData(data)
    }

//    override fun onScheduleItemDeleted(isDelete: Boolean) {
//        if (isDelete)
//            viewModel.fetchData()
//    }

}