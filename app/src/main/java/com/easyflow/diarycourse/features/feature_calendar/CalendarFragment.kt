package com.easyflow.diarycourse.features.feature_calendar

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.core.BaseFragment
import com.easyflow.diarycourse.core.utils.OnSwipeTouchListener
import com.easyflow.diarycourse.databinding.FragmentCalendarBinding
import com.easyflow.diarycourse.domain.models.NoteItem
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.features.feature_calendar.models.CombineModel
import com.easyflow.diarycourse.features.feature_calendar.note.NoteFragment
import com.easyflow.diarycourse.features.feature_calendar.schedule.ScheduleFragment
import com.google.android.material.tabs.TabLayout
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar
import dagger.Lazy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class CalendarFragment : BaseFragment() {
    private val TAG = "debugTag"
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var calendarViewModelFactory: Lazy<CalendarViewModel.CalendarViewModelFactory>
    private val viewModel: CalendarViewModel by viewModels {
        calendarViewModelFactory.get()
    }

    private lateinit var collapsibleCalendar: CollapsibleCalendar
    private var dataList: MutableList<ScheduleItem> = mutableListOf()
    private var noteList: MutableList<NoteItem> = mutableListOf()
    var dateSelected: String = ""
    private var gestureDetector: GestureDetector? = null
    private var defaultTabIndex = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        Log.d("debugTag", "onCreateView")
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setObservers()
        initializeSwipeDetector()
    }

    private fun initialize() {
        collapsibleCalendar = binding.calendarView

        initViewPager()

        setFragmentListener()
        setCalendarListener()
        setDayOfWeek()
        setSelectedDayButtons()
    }

    private fun setObservers() {
        observeState()
        observeActions()
    }

    private fun observeState() {
        viewModel
            .state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle) // добавляем flowWithLifecycle
            .onEach { state ->
                dataCollect(state.list)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope) // запускаем сборку потока
    }

    private fun dataCollect(items: List<CombineModel>) {
        Log.d("debugTag", "dataCollect")
        if (dataList.isEmpty() || noteList.isEmpty()) {
            dataList.clear()
            noteList.clear()

            items.map { combineModel ->
                when {
                    combineModel.startTime.isNotBlank() || combineModel.duration.isNotBlank() -> {
                        dataList.add(
                            ScheduleItem(
                                id = combineModel.id,
                                text = combineModel.text,
                                description = combineModel.description,
                                date = combineModel.date,
                                startTime = combineModel.startTime,
                                endTime = combineModel.endTime,
                                duration = combineModel.duration,
                                color = combineModel.color,
                                isCompleteTask = combineModel.isCompleteTask,
                                priority = combineModel.priority
                            )
                        )
                    }
                    else -> {
                        noteList.add(
                            NoteItem(
                                id = combineModel.id,
                                text = combineModel.text,
                                date = combineModel.date
                            )
                        )
                    }
                }
            }.let {
                // updateEventsTag(dataList, noteList)
            }
        }
    }

    private fun observeActions() {
        viewModel
            .action
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { action ->
                when (action) {
                    is CalendarViewModel.Actions.ShowAlert -> showAlert(action.alertData)
                }
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initializeSwipeDetector() {
        binding.mainSwiperContainer.setOnTouchListener(object : OnSwipeTouchListener(requireContext()) {
            override fun onSwipeLeft() {
                collapsibleCalendar.nextDay()
            }

            override fun onSwipeRight() {
                collapsibleCalendar.prevDay()
            }
        })
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setFragmentListener() {
        activity?.supportFragmentManager?.setFragmentResultListener("dataListKey", viewLifecycleOwner) { _, result ->
            val requestValue = result.getParcelableArrayList<ScheduleItem>("dataList")
            Log.d("debugTag", "HOME Listener dataListKey $requestValue")
            if (requestValue != null) {
                countSchedules(requestValue)
            }
        }

        activity?.supportFragmentManager?.setFragmentResultListener("itemAddedDateKey", viewLifecycleOwner) { _, bundle ->
            val requestValue = bundle.getString("date")
            Log.d("debugTag", "HOME Listener itemAddedDateKey $requestValue")
            if (requestValue != null) {
                val dayOfMonth = requestValue.substring(0, 2).toInt()
                val month = requestValue.substring(3, 5).toInt() - 1
                val year = requestValue.substring(6).toInt()
                collapsibleCalendar.addEventTag(
                    "20$year".toInt(),
                    month,
                    dayOfMonth,
                    ContextCompat.getColor(requireContext(), R.color.blue)
                )
            }
        }
    }

    private fun sendDateSelected(dateSelected: String) {
        val bundle = Bundle().apply {
            putString(FRAGMENT_DATE, dateSelected)
        }

        Log.d("debugTag", "HOME sendDateSelected $dateSelected")
        requireActivity().supportFragmentManager.setFragmentResult(KEY_FRAGMENT_SCHEDULE_RESULT_DATE, bundle)
        requireActivity().supportFragmentManager.setFragmentResult(KEY_FRAGMENT_NOTE_RESULT_DATE, bundle)
    }

    // Подсчет кол-ва записей на день
    private fun countSchedules(dataList: List<ScheduleItem>) {
        binding.countSchedules.text = dataList.size.toString()
    }

    private fun updateEventsTag(dataList: List<ScheduleItem>, noteList: List<NoteItem>) {
        Log.d("debugTag", "HOME updateEventsTag")
        val color = ContextCompat.getColor(requireContext(), R.color.blue)
        val processedDates = mutableSetOf<String>()
        val datesToProcess = mutableListOf<String>()

        for (item in dataList + noteList) {
            val date = when (item) {
                is ScheduleItem -> item.date
                is NoteItem -> item.date
                else -> continue // Пропустить элементы, не являющиеся ни ScheduleItem, ни NoteItem
            }

            if (!processedDates.contains(date)) {
                processedDates.add(date)
                datesToProcess.add(date)
            }
        }

        for (date in datesToProcess) {
            val dayOfMonth = date.substring(0, 2).toInt()
            val month = date.substring(3, 5).toInt() - 1
            val year = date.substring(6).toInt()
            collapsibleCalendar.addEventTag("20$year".toInt(), month, dayOfMonth, color)
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
                sendDateSelected(dateSelected)
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
            Calendar.SUNDAY -> getString(R.string.week_sunday)
            Calendar.MONDAY -> getString(R.string.week_monday)
            Calendar.TUESDAY -> getString(R.string.week_tuesday)
            Calendar.WEDNESDAY -> getString(R.string.week_wednesday)
            Calendar.THURSDAY -> getString(R.string.week_thursday)
            Calendar.FRIDAY -> getString(R.string.week_friday)
            Calendar.SATURDAY -> getString(R.string.week_saturday)
            else -> getString(R.string.week_unknown)
        }
        binding.textDayOfWeek.text = dayOfWeek
    }

    // Установка дня недели при выборе
    private fun setSelectedDayOfWeek() {
        Log.d("debugTag", "HOME setSelectedDayOfWeek")
        collapsibleCalendar.selectedDay?.let { selectedDate ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedDate.year)
                set(Calendar.MONTH, selectedDate.month)
                set(Calendar.DAY_OF_MONTH, selectedDate.day)
            }
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

    // Инициализация TabsLayout и отображение фрагментов
    private fun initViewPager() {
        // Находим TabLayout
        val tabLayout = binding.mainTabLayout

        // Создаем табы с нужными заголовками
        val tabTitles = arrayOf("Распорядок", "Заметка")
        for (title in tabTitles) {
            tabLayout.addTab(tabLayout.newTab().setText(title))
        }

        // Устанавливаем слушатель для переключения между табами
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // При выборе таба меняем фрагмент
                tab?.let {
                    replaceFragment(it.position)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Установка начально выбранного таба
        tabLayout.getTabAt(defaultTabIndex)?.select()
        replaceFragment(0)
    }


    private fun replaceFragment(position: Int) {
        val fragment = when (position) {
            0 -> ScheduleFragment()
            1 -> NoteFragment()
            else -> throw IllegalStateException("Invalid position: $position")
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.main_fragment_container, fragment)
            .commit()
    }

    companion object {
        const val KEY_FRAGMENT_SCHEDULE_RESULT_DATE = "dateKeySchedule"
        const val KEY_FRAGMENT_NOTE_RESULT_DATE = "dateKeyNote"

        const val FRAGMENT_DATE = "dateSelected"
    }

}