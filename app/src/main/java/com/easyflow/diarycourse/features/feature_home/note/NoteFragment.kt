package com.easyflow.diarycourse.features.feature_home.note

import android.content.Context
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Note
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
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.BaseFragment
import com.easyflow.diarycourse.databinding.FragmentNoteBinding
import com.easyflow.diarycourse.domain.models.NoteItem
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.domain.util.Resource
import com.easyflow.diarycourse.features.feature_home.note.dialogs.NoteDialogFragment
import com.easyflow.diarycourse.features.feature_home.note.dialogs.NoteDialogListener
import com.easyflow.diarycourse.features.feature_home.schedule.ScheduleFragment
import com.easyflow.diarycourse.features.feature_home.schedule.ScheduleViewModel
import dagger.Lazy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class NoteFragment : BaseFragment(), NoteDialogListener {
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
        setFragmentListener()
        setNoteText()
        viewModel.fetchData(dateSelected)
        setDate()
        openNote()
    }

    private fun setObservers() {
        observeState()
        observeActions()
    }

    private fun observeState() {
        viewModel
            .state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                dataCollect(state.list)
                resultCollect(state.result)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun dataCollect(item: NoteItem?) {
        Log.d("debugTag", "====================================")
        Log.d("debugTag", "NOTE dataCollect $item")
        item?.let {
            Log.d("debugTag", "NOTE dataCollect old note: $note")
            note = item
            Log.d("debugTag", "NOTE dataCollect new note: $note")
            setNoteText()
        }
    }

    private fun resultCollect(result: Resource?) {
        Log.d("debugTag", "NOTE resultCollect $result")
        result?.let {
            when (it) {
                is Resource.Success -> onSuccess()
                is Resource.Empty.Failed -> onFailed()
            }
        }
    }

    private fun onSuccess() {
        Log.d("debugTag", "NOTE onSuccess")
        resetValue()
        viewModel.fetchData(dateSelected)
    }

    private fun onFailed() {
        showCustomToast(getString(R.string.fetch_error), Toast.LENGTH_SHORT)
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

    private fun setFragmentListener() {
        // Установка новой заметки
        setFragmentResultListener(KEY_FRAGMENT_NOTE_RESULT_DATE) { _, bundle ->
            val requestValue = bundle.getString(FRAGMENT_DATE)
            if (requestValue != null) {
                dateSelected = requestValue
                resetValue()
                viewModel.fetchData(dateSelected)
            }
        }
    }

    private fun sendItemDate(date: String) {
        val bundle = Bundle().apply {
            putString("date", date)
        }
        parentFragmentManager.setFragmentResult("itemAddedDateKey", bundle)
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
            val noteDialogFragment = NoteDialogFragment(R.layout.fragment_note_dialog, viewModel)
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

    override fun onConfirmAddDialogResult(text: String) {
        val data = NoteItem(
            text = text,
            date = dateSelected
        )
        viewModel.addData(data)
        sendItemDate(dateSelected)
    }

    companion object {
        const val KEY_FRAGMENT_NOTE_RESULT_DATE = "dateKeyNote"

        const val FRAGMENT_DATE = "dateSelected"
    }
}