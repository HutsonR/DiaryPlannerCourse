package com.easyflow.diarycourse.features.feature_home

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.BaseFragment
import com.easyflow.diarycourse.databinding.FragmentHomeBinding
import com.easyflow.diarycourse.domain.models.NoteItem
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.features.feature_home.models.CombineModel
import com.easyflow.diarycourse.features.feature_home.note.NoteFragment
import com.easyflow.diarycourse.features.feature_home.schedule.ScheduleFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class HomeFragment : BaseFragment() {
    private val TAG = "debugTag"
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var homeViewModelFactory: Lazy<HomeViewModel.HomeViewModelFactory>
    private val viewModel: HomeViewModel by viewModels {
        homeViewModelFactory.get()
    }

    private lateinit var collapsibleCalendar: CollapsibleCalendar
    private var dataList: MutableList<ScheduleItem> = mutableListOf()
    private var noteList: MutableList<NoteItem> = mutableListOf()
    var dateSelected: String = ""
    private var defaultTabIndex = 0

    companion object {
        fun newInstance() = HomeFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        setObservers()
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
        Log.d("debugTag", "HOME observeState")
        viewModel
            .state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle) // добавляем flowWithLifecycle
            .onEach { state ->
                dataCollect(state.list)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope) // запускаем сборку потока
    }

    private fun dataCollect(items: List<CombineModel>) {
        Log.d("debugTag", "HOME dataCollect ${items.size}")
        dataList.apply {
            clear()
        }
        noteList.apply {
            clear()
        }

        for (combineModel in items) {
            when {
                // Здесь проверяем условие, по которому различаем объекты
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
        }

        updateEventsTag(dataList, noteList)
    }

    private fun observeActions() {
        viewModel
            .action
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { action ->
                when (action) {
                    is HomeViewModel.Actions.ShowAlert -> showAlert(action.alertData)
                }
            }
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
            putString("dateSelected", dateSelected)
        }

        Log.d("debugTag", "HOME sendDateSelected $dateSelected")
        requireActivity().supportFragmentManager.setFragmentResult("dateKey", bundle)
        requireActivity().supportFragmentManager.setFragmentResult("dateKeyNote", bundle)
    }

    // Подсчет кол-ва записей на день
    private fun countSchedules(dataList: List<ScheduleItem>) {
        binding.countSchedules.text = dataList.size.toString()
    }

    private fun updateEventsTag(dataList: List<ScheduleItem>, noteList: List<NoteItem>) {
        Log.d("debugTag", "HOME updateEventsTag")
        val color = ContextCompat.getColor(requireContext(), R.color.blue)
        val processedDates = mutableSetOf<String>()

        fun addEventTag(date: String) {
            val dayOfMonth = date.substring(0, 2).toInt()
            val month = date.substring(3, 5).toInt() - 1
            val year = date.substring(6).toInt()
            collapsibleCalendar.addEventTag("20$year".toInt(), month, dayOfMonth, color)
        }

        for (item in dataList + noteList) {
            val date = when (item) {
                is ScheduleItem -> item.date
                is NoteItem -> item.date
                else -> continue // Пропустить элементы, не являющиеся ни ScheduleItem, ни NoteItem
            }

            if (processedDates.contains(date)) {
                continue
            } else {
                processedDates.add(date)
                addEventTag(date)
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
        binding.mainViewPager.adapter = FragmentPagerAdapter(requireActivity())
        binding.mainViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
        })

        // инициализация всех добавленных табов (не изменять)
        // Находим, где будем отображать тексты табов
        val tabLayout = binding.mainTabLayout
        // Находим, где будем менять фрагменты на выбранный в табе
        val viewPager = binding.mainViewPager
        // Перечисляем все нужные табы
        val tabTitles = arrayOf("Распорядок", "Заметка")

        // viewPager меняет отображаемый фрагмент при выборе нужного таба с помощью SimpleFragmentPagerAdapter
        viewPager.adapter = FragmentPagerAdapter(requireActivity())
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
        // Установка начально выбранного таба
        tabLayout.getTabAt(defaultTabIndex)?.select()
    }

    // Переключение между фрагментами из табов
    private inner class FragmentPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        // Перечисление всех фрагментов (столько же, сколько и табов)
        private val fragment = arrayOf(ScheduleFragment(), NoteFragment())
        override fun getItemCount(): Int {
            return fragment.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragment[position]
        }
    }

}