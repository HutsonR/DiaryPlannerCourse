package com.example.diarycourse.features.feature_home.note

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
import com.example.diarycourse.App
import com.example.diarycourse.R
import com.example.diarycourse.databinding.FragmentNoteBinding
import com.example.diarycourse.domain.models.NoteItem
import com.example.diarycourse.domain.util.Resource
import com.example.diarycourse.features.feature_home.note.dialogs.NoteDialogFragment
import com.example.diarycourse.features.feature_home.note.dialogs.NoteDialogListener
import com.example.diarycourse.features.feature_home.schedule.ScheduleFragment
import dagger.Lazy
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class NoteFragment : Fragment(), NoteDialogListener {
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
        _binding = FragmentNoteBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentListener()
        setNoteText()
        subscribeToFlow()
        viewModel.fetchData(dateSelected)
        setDate()
        openNote()
    }

    private fun setFragmentListener() {
        setFragmentResultListener("dateKeyNote") { _, bundle ->
            val requestValue = bundle.getString("dateSelected")
            if (requestValue != null) {
                dateSelected = requestValue
                resetValue()
                viewModel.fetchData(dateSelected)
            }
        }
    }

    private fun subscribeToFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collect {
                    note = it
                    setNoteText()
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
        resetValue()
        viewModel.fetchData(dateSelected)
    }

    private fun onFailed() {
        showCustomToast(getString(R.string.fetch_error), Toast.LENGTH_SHORT)
    }

    private fun setNoteText() {
        note?.let {
            val noteText = it.text
            if (noteText.isEmpty()) {
                it.id?.let { id -> viewModel.deleteItem(id) }
            } else binding.noteText.text = it.text
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
    }
}