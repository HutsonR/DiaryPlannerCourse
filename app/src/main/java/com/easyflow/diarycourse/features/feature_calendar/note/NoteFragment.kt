package com.easyflow.diarycourse.features.feature_calendar.note

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.App
import com.easyflow.diarycourse.core.utils.collectOnStart
import com.easyflow.diarycourse.databinding.FragmentNoteBinding
import com.easyflow.diarycourse.domain.models.NoteItem
import com.easyflow.diarycourse.features.feature_calendar.note.util.NotePurpose
import dagger.Lazy
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


class NoteFragment : DialogFragment() {
    private val TAG = "debugTag"
    private var _binding: FragmentNoteBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var noteViewModelFactory: Lazy<NoteViewModel.NoteViewModelFactory>
    private val viewModel: NoteViewModel by viewModels {
        noteViewModelFactory.get()
    }

    private var currentNote = NoteItem(
        text = "",
        date = "",
    )

    private var purpose = NotePurpose.ADD

    private lateinit var textEditTV: TextView
    private lateinit var saveButton: ImageButton
    private lateinit var cancelButton: ImageButton
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentNoteBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setObservers()
        initialize()
        initializeParcel()
        initializeListeners()

        Log.d("debugTag", "updateSaveButtonState FROM FRAGMENT onViewCreated")
        Log.d("debugTag", "currentNote $currentNote, purpose $purpose")
        viewModel.updateSaveButtonState()
    }

    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog!!.window?.setLayout(width, height)
    }

    private fun initialize() {
        textEditTV = binding.noteBody

        saveButton = binding.btnSave
        cancelButton = binding.btnClose
        deleteButton = binding.btnDelete
        deleteButton.visibility = View.GONE

        viewModel.updateNote(currentNote)
    }

    private fun initializeParcel() {
        val parcelItem: NoteItem? = arguments?.getParcelable("noteItem")
        parcelItem?.let { note ->
            viewModel.setParcelItem(note)

            if (note.text.isNotEmpty()) {
                purpose = NotePurpose.CHANGE
                deleteButton.visibility = View.VISIBLE
                binding.viewOffsetHelper.visibility = View.GONE

                deleteButton.setOnClickListener {
                    currentNote.id?.let { viewModel.onDeleteItem(it) }
                }
            }

            currentNote = note
            textEditTV.text = note.text
        }
    }

    private fun initializeListeners() {
        saveButton.setOnClickListener {
            viewModel.onSaveButtonClicked()
        }
        cancelButton.setOnClickListener {
            viewModel.goBack()
        }
        textListener()
    }

    private fun setObservers() {
        viewModel.state.onEach(::handleState).collectOnStart(viewLifecycleOwner)
        viewModel.action.onEach(::handleActions).collectOnStart(viewLifecycleOwner)
    }

    private fun handleState(state: NoteViewModel.State) {
        updateItem(state.item)
    }

    private fun updateItem(item: NoteItem?) {
        item?.let { currentNote = it }
    }

    private fun handleActions(action: NoteViewModel.Actions) {
        when (action) {
            is NoteViewModel.Actions.GoBack -> goBack()
            is NoteViewModel.Actions.ChangeSaveButtonState -> updateSaveButtonState(action.state)
        }
    }

    private fun goBack() {
        Log.d("debugTag", "FRAGMENT goback")
        dismiss()
    }

    private fun updateSaveButtonState(state: Boolean) {
        saveButton.isEnabled = state
        saveButton.alpha = if (state) 1.0f else 0.5f
    }

//    Listeners
    private fun textListener() {
        textEditTV.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateNote(currentNote.copy(text = s.toString()))
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}