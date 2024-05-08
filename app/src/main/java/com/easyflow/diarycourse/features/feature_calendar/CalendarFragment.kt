package com.easyflow.diarycourse.features.feature_calendar

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.collapsiblecalendar.widget.CollapsibleCalendar
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.core.BaseFragment
import com.easyflow.diarycourse.core.utils.formatDate
import com.easyflow.diarycourse.databinding.FragmentCalendarBinding
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.domain.util.Resource
import com.easyflow.diarycourse.features.feature_calendar.schedule.adapter.ScheduleAdapter
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.TimeChangedReceiver
import com.easyflow.diarycourse.features.feature_calendar.task.TaskFragment
import dagger.Lazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class CalendarFragment : BaseFragment(), ScheduleAdapter.ScheduleTimeChangedListener {
    private val TAG = "debugTag"
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var calendarViewModelFactory: Lazy<CalendarViewModel.CalendarViewModelFactory>
    private val viewModel: CalendarViewModel by viewModels {
        calendarViewModelFactory.get()
    }

    private lateinit var collapsibleCalendar: CollapsibleCalendar
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleAdapter
    private var adapterList: MutableList<ScheduleItem> = mutableListOf()
    private var dateSelected: String = ""
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onResume() {
        super.onResume()
        val timeChangedReceiver = TimeChangedReceiver(this)
        timeChangedReceiver.register(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        setObservers()
    }

    private fun initialize() {
        collapsibleCalendar = binding.calendarView

        setFragmentListener()

        setCalendarListener()
        setCurrentDay()

        // Content
        setAddButton()
        setRecycler()

        viewModel.fetchTasksByDate(dateSelected)
    }

    private fun setObservers() {
        subscribeToFlow()
        observeState()
        observeActions()
    }

    private fun subscribeToFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.result.collect { result ->
                    resultCollect(result)
                }
            }
        }
    }

    private fun resultCollect(result: Resource?) {
        result?.let {
            when (it) {
                is Resource.Success<*> -> onSuccess()
                is Resource.Failed -> onFailed()
            }
        }
    }

    private fun onSuccess() {
        viewModel.fetchTasksByDate(dateSelected)
    }

    private fun onFailed() {
        Toast.makeText(requireContext(), getString(R.string.fetch_error), Toast.LENGTH_SHORT).show()
    }

    private fun observeState() {
        viewModel
            .state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                dataCollect(state.list)
                sortedDataCollect(state.selectedTasks)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun dataCollect(items: List<ScheduleItem>) {
        // TODO исправить повторное обновление тэгов при выборе дня
        updateEventsTag(items)
    }

    private fun sortedDataCollect(items: List<ScheduleItem>) {
        adapterList.apply {
            clear()
            addAll(items)
        }
        adapter.notifyDataSetChanged()
        countSchedules(adapterList)
    }

    private fun observeActions() {
        viewModel
            .action
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { action ->
                when (action) {
                    is CalendarViewModel.Actions.ShowAlert -> showAlert(action.alertData)
                    is CalendarViewModel.Actions.GoToTask -> {
                        TaskFragment().show(childFragmentManager, "taskFragment")
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setFragmentListener() {
        // Из TaskFragment
        activity?.supportFragmentManager?.setFragmentResultListener(
            KEY_TASK_FRAGMENT_RESULT_ADD,
            this
        ) { _, bundle ->
            val requestValue: ScheduleItem? = bundle.getParcelable(FRAGMENT_TASK_ITEM)
            requestValue?.let {
                viewModel.addData(it)
            }
        }
        activity?.supportFragmentManager?.setFragmentResultListener(
            KEY_FRAGMENT_RESULT_UPD,
            this
        ) { _, bundle ->
            val requestValue: ScheduleItem? = bundle.getParcelable(FRAGMENT_TASK_ITEM)
            requestValue?.let {
                viewModel.updateData(it)
            }
        }
        // Из ScheduleItemBottomSheetFragment
        activity?.supportFragmentManager?.setFragmentResultListener(
            KEY_BOTTOM_SHEET_RESULT_DEL,
            this
        ) { _, bundle ->
            val requestValue: ScheduleItem? = bundle.getParcelable(FRAGMENT_TASK_ITEM)
            requestValue?.let {
                it.id?.let { id -> viewModel.deleteItem(id) }
            }
        }
    }

    private fun updateEventsTag(dataList: List<ScheduleItem>) {
        Log.d("debugTag", "HOME updateEventsTag")
        val color = ContextCompat.getColor(requireContext(), R.color.primary)
        val processedDates = mutableSetOf<String>()
        val datesToProcess = mutableListOf<String>()

        for (item in dataList) {
            val date = item.date

            if (!processedDates.contains(date)) {
                processedDates.add(date)
                datesToProcess.add(date)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            for (date in datesToProcess) {
                val dayOfMonth = date.substring(0, 2).toInt()
                val month = date.substring(3, 5).toInt() - 1
                val year = date.substring(6).toInt()
                collapsibleCalendar.addEventTag("20$year".toInt(), month, dayOfMonth, color)
            }
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
                setSelectedDayOfWeek()
                viewModel.fetchTasksByDate(dateSelected)
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
    private fun setCurrentDay() {
        val today = Calendar.getInstance()
        dateSelected = formatDate(today)
        binding.textDayOfWeek.text = getString(R.string.week_today)
    }

    // Установка дня недели при выборе
    private fun setSelectedDayOfWeek() {
        Log.d("debugTag", "HOME setSelectedDayOfWeek")
        collapsibleCalendar.selectedDay?.let { selectedDate ->
            var calendar = Calendar.getInstance()
            val formattedToday = formatDate(calendar)

            calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedDate.year)
                set(Calendar.MONTH, selectedDate.month)
                set(Calendar.DAY_OF_MONTH, selectedDate.day)
            }
            val formattedSelectedDay = formatDate(calendar)

            val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> getString(R.string.week_sunday)
                Calendar.MONDAY -> getString(R.string.week_monday)
                Calendar.TUESDAY -> getString(R.string.week_tuesday)
                Calendar.WEDNESDAY -> getString(R.string.week_wednesday)
                Calendar.THURSDAY -> getString(R.string.week_thursday)
                Calendar.FRIDAY -> getString(R.string.week_friday)
                Calendar.SATURDAY -> getString(R.string.week_saturday)
                else -> getString(R.string.week_unknown)
            }
            binding.textDayOfWeek.text = if (formattedToday == formattedSelectedDay) getString(R.string.week_today) else dayOfWeek
        }
    }

    // Работа с задачами и группами
    private fun setAddButton() {
        binding.fabAdd.setOnClickListener {
            viewModel.goToTask()
        }
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.wave_animation)

        coroutineScope.launch {
            while (true) {
                delay(3000)
                binding.fabAddRipple.startAnimation(animation)
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

    private fun setRecycler() {
        recyclerView = binding.recycleSchedule
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        adapter = ScheduleAdapter(adapterList, viewModel, activity)
        recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        coroutineScope.cancel()
        _binding = null
        super.onDestroyView()
    }

    override fun onTimeChanged() {
        adapter.notifyDataSetChanged()
    }

    companion object {
        const val KEY_FRAGMENT_RESULT_UPD = "KEY_FRAGMENT_RESULT_UPD"
        const val KEY_TASK_FRAGMENT_RESULT_ADD = "KEY_TASK_FRAGMENT_RESULT_ADD"
        const val KEY_BOTTOM_SHEET_RESULT_DEL = "KEY_BOTTOM_SHEET_RESULT_DEL"

        const val FRAGMENT_TASK_ITEM = "taskItem"
    }

}