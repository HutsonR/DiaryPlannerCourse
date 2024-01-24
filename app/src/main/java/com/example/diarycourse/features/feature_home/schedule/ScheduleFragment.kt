package com.example.diarycourse.features.feature_home.schedule

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
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
import com.example.diarycourse.features.common.SharedViewModel
import com.example.diarycourse.features.feature_home.schedule.adapter.ScheduleAdapter
import com.example.diarycourse.features.feature_home.schedule.dialogs.TaskDialogFragment
import com.example.diarycourse.features.feature_home.schedule.dialogs.DialogListener
import com.example.diarycourse.features.feature_home.schedule.utils.Color
import dagger.Lazy
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class ScheduleFragment : Fragment(), DialogListener {
    private val TAG = "debugTag"
    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var scheduleViewModelFactory: Lazy<ScheduleViewModel.ScheduleViewModelFactory>
    private val viewModel: ScheduleViewModel by viewModels {
        scheduleViewModelFactory.get()
    }
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
    ): View? {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentListener()
        subscribeToFlow()
        viewModel.fetchData()
        setAddButton()
        setRecycler()
    }

    private fun setAddButton() {
        binding.fabAdd.setOnClickListener {
            TaskDialogFragment(R.layout.fragment_add, viewModel).show(childFragmentManager, "add fragment")
        }
    }

    private fun setFragmentListener() {
        setFragmentResultListener("dateKey") { key, bundle ->
            val requestValue = bundle.getString("dateSelected")
            Log.d("debugTag", "requestValue Schedule $requestValue")
            if (requestValue != null) {
                dateSelected = requestValue
                sortItems(dataList)
            }
        }
    }

    private fun sendDataList(dataList: List<ScheduleItem>) {
        val bundle = Bundle().apply {
            putParcelableArrayList("dataList", ArrayList(dataList))
        }

        parentFragmentManager.setFragmentResult("dataListKey", bundle)
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
                    Log.d(TAG, "dataList $dataList")
                    Log.d(TAG, "adapterList $adapterList")
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

        sendDataList(adapterList)
    }

    // Подсчет кол-ва записей на день
    private fun countSchedules(dataList: List<ScheduleItem>) {
        if (dataList.isEmpty()) {
            binding.scheduleBlank.visibility = View.VISIBLE
        } else {
            binding.scheduleBlank.visibility = View.GONE
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