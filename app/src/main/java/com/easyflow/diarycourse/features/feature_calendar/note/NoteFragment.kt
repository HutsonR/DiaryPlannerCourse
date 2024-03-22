package com.easyflow.diarycourse.features.feature_calendar.note

import android.content.Context
import android.os.Bundle
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
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.BaseFragment
import com.easyflow.diarycourse.core.utils.formatDate
import com.easyflow.diarycourse.databinding.FragmentNoteBinding
import com.easyflow.diarycourse.domain.models.NoteItem
import com.easyflow.diarycourse.domain.util.Resource
import com.easyflow.diarycourse.features.feature_calendar.note.dialogs.NoteDialogFragment
import dagger.Lazy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class NoteFragment : BaseFragment() {
    private val TAG = "debugTag"
    private var _binding: FragmentNoteBinding? = null
    private val binding get() = _binding!!
    @Inject
    lateinit var noteViewModelFactory: Lazy<NoteViewModel.NoteViewModelFactory>
    private val viewModel: NoteViewModel by viewModels {
        noteViewModelFactory.get()
    }
    private var dateSelected: String = ""
    private var note: NoteItem? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).appComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteBinding.inflate(inflater, container, false)
        return _binding?.root
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
        dateInitialize()
        setFragmentListener()
        setNoteText()
        viewModel.fetchData(dateSelected)
        setDate()
        openNote()
    }

    private fun setObservers() {
        subscribeToFlow()
        observeState()
        observeActions()
    }

    private fun observeState() {
        viewModel
            .state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                dataCollect(state.note)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun dataCollect(item: NoteItem?) {
        item?.let {
            note = item
            setNoteText()
        }
    }

    private fun observeActions() {
        viewModel
            .action
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { action ->
                when (action) {
                    is NoteViewModel.Actions.ShowAlert -> showAlert(action.alertData)
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
                is Resource.Success<*> -> onSuccess()
                is Resource.Failed -> onFailed()
            }
        }
    }

    private fun onSuccess() {
        resetValue()
        viewModel.fetchData(dateSelected)
    }

    private fun onFailed() {
        Toast.makeText(requireContext(), getString(R.string.fetch_error), Toast.LENGTH_SHORT).show()
    }

    private fun setFragmentListener() {
        // Из HomeFragment
        setFragmentResultListener(KEY_FRAGMENT_NOTE_RESULT_DATE) { _, bundle ->
            val requestValue = bundle.getString(FRAGMENT_DATE)
            requestValue?.let {
                dateSelected = it
                resetValue()
                viewModel.fetchData(dateSelected)
            }
        }
        // Из NoteDialogFragment
        activity?.supportFragmentManager?.setFragmentResultListener(
            KEY_NOTE_FRAGMENT_RESULT_ADD,
            this
        ) { _, bundle ->
            val requestValue: NoteItem? = bundle.getParcelable(FRAGMENT_NOTE_ITEM)
            requestValue?.let {
                val noteItem = NoteItem(
                    text = it.text,
                    date = dateSelected
                )
                viewModel.addData(noteItem)
                sendItemDate(dateSelected)
            }
        }
        activity?.supportFragmentManager?.setFragmentResultListener(
            KEY_NOTE_FRAGMENT_RESULT_UPD,
            this
        ) { _, bundle ->
            val requestValue: NoteItem? = bundle.getParcelable(FRAGMENT_NOTE_ITEM)
            requestValue?.let {
                viewModel.updateData(it)
            }
        }
        activity?.supportFragmentManager?.setFragmentResultListener(
            KEY_NOTE_FRAGMENT_RESULT_DEL,
            this
        ) { _, bundle ->
            val requestValue: Int? = bundle.getInt(FRAGMENT_NOTE_ITEM_ID)
            requestValue?.let {
                viewModel.deleteItem(it)
            }
        }
    }

    private fun sendItemDate(date: String) {
        val bundle = Bundle().apply {
            putString("date", date)
        }
        parentFragmentManager.setFragmentResult("itemAddedDateKey", bundle)
    }

    private fun dateInitialize() {
        val today = Calendar.getInstance()
        dateSelected = formatDate(today)
    }

    private fun setNoteText() {
        note?.let {
            val noteText = it.text
            if (noteText.isEmpty()) {
                it.id?.let { id -> viewModel.deleteItem(id) }
            } else {
                binding.noteText.text = noteText
            }
        }
    }

    private fun resetValue() {
        binding.noteText.text = getString(R.string.note_blank)
        note = null
    }

    private fun openNote() {
        binding.noteWrapper.setOnClickListener {
            val noteDialogFragment = NoteDialogFragment()
            if (note != null) {
                val args = Bundle()
                args.putParcelable("noteItem", note)
                noteDialogFragment.arguments = args
            }
            noteDialogFragment.show(childFragmentManager, "note view fragment")
        }
    }

    private fun setDate() {
        if (dateSelected.isEmpty()) {
            val today = Calendar.getInstance().time
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            dateSelected = dateFormat.format(today)
        }
    }

    companion object {
        const val KEY_FRAGMENT_NOTE_RESULT_DATE = "dateKeyNote"
        const val KEY_NOTE_FRAGMENT_RESULT_ADD = "KEY_NOTE_FRAGMENT_RESULT_ADD"
        const val KEY_NOTE_FRAGMENT_RESULT_UPD = "KEY_NOTE_FRAGMENT_RESULT_UPD"
        const val KEY_NOTE_FRAGMENT_RESULT_DEL = "KEY_NOTE_FRAGMENT_RESULT_DEL"

        const val FRAGMENT_DATE = "dateSelected"
        const val FRAGMENT_NOTE_ITEM = "NOTE_ITEM"
        const val FRAGMENT_NOTE_ITEM_ID = "NOTE_ITEM_ID"
    }
}