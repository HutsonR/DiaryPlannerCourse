package com.easyflow.diarycourse.features.feature_home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.core.BaseFragment
import com.easyflow.diarycourse.core.utils.collectOnStart
import com.easyflow.diarycourse.databinding.FragmentHomeBinding
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.features.feature_home.task.FastTaskFragment
import dagger.Lazy
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class HomeFragment : BaseFragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var homeViewModelFactory: Lazy<HomeViewModel.HomeViewModelFactory>
    private val viewModel: HomeViewModel by viewModels {
        homeViewModelFactory.get()
    }

//    private lateinit var recyclerView: RecyclerView
//    private lateinit var adapter: ScheduleAdapter
    private var adapterList: MutableList<ScheduleItem> = mutableListOf()

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

    }

    private fun handleActions(action: HomeViewModel.Actions) {
        when (action) {
            is HomeViewModel.Actions.ShowAlert -> showAlert(action.alertData)
            is HomeViewModel.Actions.GoToInbox -> navigateTo(R.id.actionGoToInbox)
            is HomeViewModel.Actions.GoToFastAddTask -> showBottomSheet()
        }
    }

    private fun showBottomSheet() {
        val bottomSheetFragment = FastTaskFragment()

        bottomSheetFragment.show(
            childFragmentManager,
            bottomSheetFragment.tag
        )
    }

    private fun setRecycler() {
//        recyclerView = binding.recycler
//        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
//
//        adapter = ScheduleAdapter(adapterList, viewModel, activity)
//        recyclerView.adapter = adapter
    }

    private fun setListeners() {
        binding.inboxButton.setOnClickListener { viewModel.goToInbox() }
        binding.fastAddTask.setOnClickListener { viewModel.fastAddTask() }
    }


}