package com.easyflow.diarycourse.features.feature_home.schedule

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.BaseFragment
import com.easyflow.diarycourse.core.utils.formatDate
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.databinding.FragmentScheduleBinding
import com.easyflow.diarycourse.domain.util.Resource
import com.easyflow.diarycourse.features.feature_home.schedule.adapter.ScheduleAdapter
import com.easyflow.diarycourse.features.feature_home.schedule.utils.TimeChangedReceiver
import com.easyflow.diarycourse.features.feature_home.task.TaskFragment
import dagger.Lazy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

class ScheduleFragment : BaseFragment(), ScheduleAdapter.ScheduleTimeChangedListener {
    private val TAG = "debugTag"
    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    @Inject
    lateinit var scheduleViewModelFactory: Lazy<ScheduleViewModel.ScheduleViewModelFactory>
    private val viewModel: ScheduleViewModel by viewModels {
        scheduleViewModelFactory.get()
    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleAdapter
    private var dataList: MutableList<ScheduleItem> = mutableListOf()
    private var adapterList: MutableList<ScheduleItem> = mutableListOf()
    private var dateSelected: String = ""

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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun initialize() {
        setFragmentListener()
        setAddButton()
        setRecycler()
    }

    private fun setObservers() {
        subscribeToFlow()
        observeState()
        observeActions()
    }

    private fun observeState() {
        viewModel
            .state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                dataCollect(state.list)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope) // запускаем сборку потока
    }

    private fun dataCollect(items: List<ScheduleItem>) {
        Log.d("debugTag", "SCHEDULE dataCollect")
        dataList.apply {
            clear()
            addAll(items)
        }
        sortItems(dataList)
        adapter.notifyDataSetChanged()
        countSchedules(adapterList)
    }

    private fun observeActions() {
        viewModel
            .action
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { action ->
                when (action) {
                    is ScheduleViewModel.Actions.ShowAlert -> showAlert(action.alertData)
                }
            }
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
                is Resource.Success -> onSuccess()
                is Resource.Empty.Failed -> onFailed()
            }
        }
    }

    private fun onSuccess() {
        viewModel.fetchData()
        countSchedules(adapterList)
    }

    private fun onFailed() {
        Toast.makeText(requireContext(), getString(R.string.fetch_error), Toast.LENGTH_SHORT).show()
    }

    private fun setFragmentListener() {
        // Из HomeFragment
        setFragmentResultListener(KEY_FRAGMENT_SCHEDULE_RESULT_DATE) { _, bundle ->
            val requestValue = bundle.getString(FRAGMENT_DATE)
            requestValue?.let {
                dateSelected = it
                sortItems(dataList)
            }
        }
        // Из TaskFragment
        activity?.supportFragmentManager?.setFragmentResultListener(
            KEY_TASK_FRAGMENT_RESULT_ADD,
            this
        ) { _, bundle ->
            val requestValue: ScheduleItem? = bundle.getParcelable(FRAGMENT_TASK_ITEM)
            requestValue?.let {
                viewModel.addData(it)
                sendItemDate(it.date)
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

    private fun sendDataList(dataList: List<ScheduleItem>) {
        val bundle = Bundle().apply {
            putParcelableArrayList("dataList", ArrayList(dataList))
        }
        activity?.supportFragmentManager?.setFragmentResult("dataListKey", bundle)
    }

    private fun sendItemDate(date: String) {
        val bundle = Bundle().apply {
            putString("date", date)
        }
        activity?.supportFragmentManager?.setFragmentResult("itemAddedDateKey", bundle)
    }

    private fun setAddButton() {
        binding.fabAdd.setOnClickListener {
            TaskFragment().show(childFragmentManager, "taskFragment")
        }
    }

    private fun sortItemsByDate(dataList: List<ScheduleItem>): List<ScheduleItem> {
        val sortedData: MutableList<ScheduleItem> = mutableListOf()
        if (dateSelected.isNotEmpty()) {
            dataList.forEach {
                if (it.date == dateSelected)
                    sortedData.add(it)
            }
            return sortedData
        } else {
            val today = Calendar.getInstance()
            dateSelected = formatDate(today)
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

    private fun setRecycler() {
        recyclerView = binding.recycleSchedule
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        adapter = ScheduleAdapter(adapterList, viewModel, activity)
        recyclerView.adapter = adapter
    }

    override fun onTimeChanged() {
        adapter.notifyDataSetChanged()
    }

    companion object {
        const val KEY_FRAGMENT_SCHEDULE_RESULT_DATE = "dateKeySchedule"
        const val KEY_FRAGMENT_RESULT_UPD = "KEY_FRAGMENT_RESULT_UPD"
        const val KEY_TASK_FRAGMENT_RESULT_ADD = "KEY_TASK_FRAGMENT_RESULT_ADD"
        const val KEY_BOTTOM_SHEET_RESULT_DEL = "KEY_BOTTOM_SHEET_RESULT_DEL"

        const val FRAGMENT_TASK_ITEM = "taskItem"
        const val FRAGMENT_DATE = "dateSelected"
    }

}