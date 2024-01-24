package com.example.diarycourse.features.feature_home

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
import com.example.diarycourse.domain.models.ScheduleItem
import com.example.diarycourse.features.common.SharedViewModel
import com.example.diarycourse.features.feature_home.note.NoteFragment
import com.example.diarycourse.features.feature_home.schedule.ScheduleFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar
import dagger.Lazy
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class HomeFragment @Inject constructor() : Fragment() {
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

        setFragmentListener()
        subscribeToFlow()
        viewModel.fetchData()
        setCalendarListener()
        setDayOfWeek()
        setSelectedDayButtons()

        initViewPager()
        initTabLayout()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun subscribeToFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataList.collect { scheduleItems: List<ScheduleItem> ->
                    dataList.apply {
                        clear()
                        addAll(scheduleItems)
                    }
                    updateEventsTag(dataList)
                }
            }
        }
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.fetchAdapterList().collect { scheduleItems: List<ScheduleItem> ->
//                    Log.d(TAG, "Сработал collect в HomeFragment")
//                    adapterList.apply {
//                        clear()
//                        addAll(scheduleItems)
//                    }
//                    countSchedules(adapterList)
//                }
//            }
//        }
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
//                viewModel.result.collect { result: Resource ->
//                    when (result) {
//                        is Resource.Success -> onSuccess()
//                        is Resource.Empty.Failed -> onFailed()
//                    }
//                }
//            }
//        }
    }

//    private fun onSuccess() {
//        viewModel.fetchData()
//        updateEventsTag(dataList)
//        countSchedules(adapterList)
//    }
//
//    private fun onFailed() {
//        showCustomToast("Ошибка получения данных", Toast.LENGTH_SHORT)
//    }

    // Подсчет кол-ва записей на день
    private fun countSchedules(dataList: List<ScheduleItem>) {
        binding.countSchedules.text = dataList.size.toString()
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

    private fun setFragmentListener() {
        setFragmentResultListener("dataListKey") { key, bundle ->
            val requestValue = bundle.getParcelableArrayList<ScheduleItem>("dataList")
            Log.d("debugTag", "requestValue Home $requestValue")
            if (requestValue != null) {
                countSchedules(requestValue)
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

    // Инициализация TabsLayout и отображение фрагментов
    private fun initViewPager() {
        binding.mainViewPager.adapter = SimpleFragmentPagerAdapter(requireActivity())
        binding.mainViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    // инициализация всех добавленных табов (не изменять)
    private fun initTabLayout() {
        // Находим, где будем отображать тексты табов
        val tabLayout = binding.mainTabLayout
        // Находим, где будем менять фрагменты на выбранный в табе
        val viewPager = binding.mainViewPager
        // Перечисляем все нужные табы
        val tabTitles = arrayOf("Распорядок", "Заметка")

        // viewPager меняет отображаемый фрагмент при выборе нужного таба с помощью SimpleFragmentPagerAdapter
        viewPager.adapter = SimpleFragmentPagerAdapter(requireActivity())
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
        // Установка начально выбранного таба
        tabLayout.getTabAt(defaultTabIndex)?.select()
    }

    // Переключение между фрагментами из табов
    private inner class SimpleFragmentPagerAdapter(fragmentActivity: FragmentActivity) :
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