package com.easyflow.diarycourse.features.feature_home.inbox

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
import com.easyflow.diarycourse.databinding.FragmentHomeInboxBinding
import dagger.Lazy
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class InboxFragment : BaseFragment() {
    private var _binding: FragmentHomeInboxBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var inboxViewModelFactory: Lazy<InboxViewModel.InboxViewModelFactory>
    private val viewModel: InboxViewModel by viewModels {
        inboxViewModelFactory.get()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeInboxBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        setObservers()
    }

    private fun initialize() {
        initializeNavBar()
    }

    private fun initializeNavBar() {
        binding.inboxToolbar.toolbar.title = getString(R.string.home_folder_inbox)
        binding.inboxToolbar.toolbar.setNavigationOnClickListener {
            viewModel.goBack()
        }
    }

    private fun setObservers() {
        viewModel.state.onEach(::handleState).collectOnStart(viewLifecycleOwner)
        viewModel.action.onEach(::handleActions).collectOnStart(viewLifecycleOwner)
    }

    private fun handleState(state: InboxViewModel.State) {

    }

    private fun handleActions(action: InboxViewModel.Actions) {
        when (action) {
            is InboxViewModel.Actions.ShowAlert -> showAlert(action.alertData)
            is InboxViewModel.Actions.GoBack -> popBackStack()
        }
    }
}