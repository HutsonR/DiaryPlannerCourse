package com.easyflow.diarycourse.features.feature_home

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.core.BaseFragment
import com.easyflow.diarycourse.core.composite.adapter.CompositeAdapter
import com.easyflow.diarycourse.core.utils.collectOnStart
import com.easyflow.diarycourse.databinding.FragmentHomeBinding
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.features.feature_calendar.schedule.dialogs.ScheduleItemBottomSheetFragment
import com.easyflow.diarycourse.features.feature_home.adapter.DateDelegate
import com.easyflow.diarycourse.features.feature_home.adapter.LoadingDelegate
import com.easyflow.diarycourse.features.feature_home.adapter.TaskDelegate
import com.easyflow.diarycourse.features.feature_home.adapter.TaskListUiConverter
import com.easyflow.diarycourse.features.feature_home.task.FastTaskFragment
import dagger.Lazy
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlin.properties.Delegates

class HomeFragment : BaseFragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var homeViewModelFactory: Lazy<HomeViewModel.HomeViewModelFactory>
    private val viewModel: HomeViewModel by viewModels {
        homeViewModelFactory.get()
    }

    private var adapter: CompositeAdapter by Delegates.notNull()

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
        binding.homeNavbar.title.text = getString(R.string.home_title)
        binding.homeSearch.searchET.hint = getString(R.string.home_search)
        binding.inboxItemCount.text = viewModel.getInboxItemsCount()
        setRecycler()
        setListeners()
    }

    private fun setObservers() {
        viewModel.state.onEach(::handleState).collectOnStart(viewLifecycleOwner)
        viewModel.action.onEach(::handleActions).collectOnStart(viewLifecycleOwner)
    }

    private fun handleState(state: HomeViewModel.State) {
        val list = TaskListUiConverter().convertToTaskListItem(state.dataList, state.isLoading)
        adapter.submitList(list)
    }

    private fun handleActions(action: HomeViewModel.Actions) {
        when (action) {
            is HomeViewModel.Actions.ShowAlert -> showAlert(action.alertData)
            is HomeViewModel.Actions.ShowTaskBottomSheet -> showTaskBottomSheet(action.item)
            is HomeViewModel.Actions.GoToInbox -> navigateTo(R.id.actionGoToInbox)
            is HomeViewModel.Actions.GoToFastAddTask -> showAddTaskBottomSheet()
        }
    }

    private fun showAddTaskBottomSheet() {
        val bottomSheetFragment = FastTaskFragment()

        bottomSheetFragment.show(
            childFragmentManager,
            bottomSheetFragment.tag
        )
    }

    private fun showTaskBottomSheet(item: ScheduleItem) {
        val bottomSheetFragment = ScheduleItemBottomSheetFragment()

        val args = Bundle()
        args.putParcelable("scheduleItem", item)
        bottomSheetFragment.arguments = args

        bottomSheetFragment.show(
            childFragmentManager,
            bottomSheetFragment.tag
        )
    }

    private fun setRecycler() {
        adapter = CompositeAdapter
            .Builder()
            .add(DateDelegate())
            .add(TaskDelegate(viewModel::updateData, viewModel::onTaskContentClick))
            .add(LoadingDelegate())
            .build()

        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter
    }

    private fun setListeners() {
        binding.inboxButton.setOnClickListener { viewModel.goToInbox() }
        binding.fastAddTask.setOnClickListener { viewModel.fastAddTask() }
        setFragmentListener()
        searchListener()
    }

    private fun setFragmentListener() {
        // Из ScheduleItemBottomSheetFragment
        activity?.supportFragmentManager?.setFragmentResultListener(
            ScheduleItemBottomSheetFragment.KEY_BOTTOM_SHEET_RESULT_UPD,
            this
        ) { _, bundle ->
            val requestValue: Int = bundle.getInt(ScheduleItemBottomSheetFragment.FRAGMENT_TASK_ITEM)
            viewModel.updateData(requestValue)
        }
        activity?.supportFragmentManager?.setFragmentResultListener(
            ScheduleItemBottomSheetFragment.KEY_BOTTOM_SHEET_RESULT_DEL,
            this
        ) { _, bundle ->
            val requestValue: ScheduleItem? = bundle.getParcelable(ScheduleItemBottomSheetFragment.FRAGMENT_TASK_ITEM)
            requestValue?.let {
                it.id?.let { id -> viewModel.deleteItem(id) }
            }
        }
        // Из FastTaskFragment
        activity?.supportFragmentManager?.setFragmentResultListener(
            FastTaskFragment.REQ_KEY_TASK_ITEM,
            this
        ) { _, bundle ->
            val requestValue: ScheduleItem? = bundle.getParcelable(FastTaskFragment.KEY_TASK_ITEM)
            requestValue?.let {
                viewModel.addData(requestValue)
            }
        }
    }

    private fun searchListener() {
        binding.homeSearch.searchET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onChangeQuerySearch(s.toString())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

}