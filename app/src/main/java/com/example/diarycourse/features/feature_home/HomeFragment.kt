package com.example.diarycourse.features.feature_home

import android.content.Context
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Note
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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.diarycourse.App
import com.example.diarycourse.R
import com.example.diarycourse.databinding.FragmentHomeBinding
import com.example.diarycourse.domain.models.NoteItem
import com.example.diarycourse.domain.models.ScheduleItem
import com.example.diarycourse.features.feature_home.note.NoteFragment
import com.example.diarycourse.features.feature_home.schedule.ScheduleFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar
import dagger.Lazy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class HomeFragment : Fragment() {
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
        collapsibleCalendar = binding.calendarView

        initViewPager()

        setFragmentListener()
        subscribeToFlow()
        viewModel.fetchData()
        setCalendarListener()
        setDayOfWeek()
        setSelectedDayButtons()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun subscribeToFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataList.combine(viewModel.noteList) { scheduleItems, noteItems ->
                    Pair(scheduleItems, noteItems)
                }.collect { (scheduleItems, noteItems) ->
                    Log.d(TAG, "dataList and noteList collect")
                    dataList.apply {
                        clear()
                        addAll(scheduleItems)
                    }
                    noteList.apply {
                        clear()
                        addAll(noteItems)
                    }
                    launch(Dispatchers.Main) {
                        updateEventsTag(dataList, noteList)
                    }
                }
            }
        }
    }

    private fun setFragmentListener() {
        setFragmentResultListener("dataListKey") { _, bundle ->
            val requestValue = bundle.getParcelableArrayList<ScheduleItem>("dataList")
            if (requestValue != null) {
                countSchedules(requestValue)
            }
        }
        setFragmentResultListener("itemAddedDateKey") { _, bundle ->
            val requestValue = bundle.getString("date")
            if (requestValue != null) {
                val dayOfMonth = requestValue.substring(0, 2).toInt()
                val month = requestValue.substring(3, 5).toInt() - 1
                val year = requestValue.substring(6).toInt()
                collapsibleCalendar.addEventTag("20$year".toInt(), month, dayOfMonth, ContextCompat.getColor(requireContext(), R.color.blue))
            }
        }
    }

    private fun sendDateSelected(dateSelected: String) {
        val bundle = Bundle().apply {
            putString("dateSelected", dateSelected)
        }

        parentFragmentManager.setFragmentResult("dateKey", bundle)
        parentFragmentManager.setFragmentResult("dateKeyNote", bundle)
    }

    // Подсчет кол-ва записей на день
    private fun countSchedules(dataList: List<ScheduleItem>) {
        binding.countSchedules.text = dataList.size.toString()
    }

    private fun updateEventsTag(dataList: List<ScheduleItem>, noteList: List<NoteItem>) {
        Log.d(TAG, "updateEventsTag")
        Log.d(TAG, "noteList count: ${noteList.size} elements: $noteList")

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

    // Инициализация TabsLayout и отображение фрагментов
    private fun initViewPager() {
        binding.mainViewPager.adapter = FragmentPagerAdapter(requireActivity())
        binding.mainViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
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