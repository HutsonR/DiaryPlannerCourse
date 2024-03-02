package com.easyflow.diarycourse.features.feature_home.note.dialogs

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
import androidx.lifecycle.lifecycleScope
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.databinding.FragmentNoteDialogBinding
import com.easyflow.diarycourse.domain.models.NoteItem
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.features.feature_home.note.NoteViewModel
import com.easyflow.diarycourse.features.feature_home.task.TaskFragment
import kotlinx.coroutines.launch


class NoteDialogFragment : DialogFragment() {
    private val TAG = "debugTag"
    private var _binding: FragmentNoteDialogBinding? = null
    private val binding get() = _binding!!
    private var parcelItem: NoteItem? = null
    private var previousText: String = ""
    private var text: String = ""
    private lateinit var textEditTV: TextView
    private lateinit var saveButton: ImageButton
    private lateinit var cancelButton: ImageButton
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentNoteDialogBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        textEditTV = binding.noteBody

        saveButton = binding.btnSave
        cancelButton = binding.btnClose
        deleteButton = binding.btnDelete
        saveButton.setOnClickListener {
            handleSaveButtonClicked()
        }
        cancelButton.setOnClickListener {
            dismiss()
        }
        deleteButton.visibility = View.GONE

        textListener()

        // Если имеется модель (фрагмент открыт для редактирования)
        parcelItem = arguments?.getParcelable("noteItem")
        if (parcelItem != null) {
            text = parcelItem!!.text
            previousText = text

            textEditTV.text = parcelItem!!.text

            deleteButton.visibility = View.VISIBLE
            binding.viewOffsetHelper.visibility = View.GONE
            deleteButton.setOnClickListener {
                lifecycleScope.launch {
                    parcelItem!!.id?.let { id -> sendDeleteItem(id) }
                    dismiss()
                }
            }
        }

        // Изначально деактивируем кнопку "Сохранить"
        updateSaveButtonState()
    }

    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog!!.window?.setLayout(width, height)
    }

    private fun updateSaveButtonState() {
        if (parcelItem != null) {
            // Для редактирования элемента
            val isEnabled = (text != previousText)

            saveButton.isEnabled = isEnabled
            saveButton.alpha = if (isEnabled) 1.0f else 0.5f
        } else {
            // По умолчанию обычное добавление элемента
            val isEnabled = text.isNotEmpty()

            saveButton.isEnabled = isEnabled
            saveButton.alpha = if (isEnabled) 1.0f else 0.5f
        }
    }

    private fun handleSaveButtonClicked() {
        if (parcelItem != null) {
            // Для редактирования элемента
            lifecycleScope.launch {
                val updatedNoteItem = parcelItem!!.copy(
                    text = text
                )
                sendNoteItem(updatedNoteItem)
                Log.d("debugTag", "NOTE handleSaveButtonClicked $updatedNoteItem")
                dismiss()
            }
        } else {
            // По умолчанию обычное добавление элемента
            val noteItem = NoteItem(
                text = text,
                date = ""
            )
            sendNoteItem(noteItem)
            dismiss()
        }
    }

    private fun sendNoteItem(item: NoteItem) {
        val bundle = Bundle().apply {
            putParcelable(FRAGMENT_NOTE_ITEM, item)
        }
        Log.d("debugTag", "==========================================================================")
        Log.d("debugTag", "sendNoteItem item: $item bundle: $bundle")
        if (parcelItem != null) {
            activity?.supportFragmentManager?.setFragmentResult(KEY_NOTE_FRAGMENT_RESULT_UPD, bundle)
        } else activity?.supportFragmentManager?.setFragmentResult(KEY_NOTE_FRAGMENT_RESULT_ADD, bundle)
    }

    private fun sendDeleteItem(id: Int) {
        val bundle = Bundle().apply {
            putInt(FRAGMENT_NOTE_ITEM_ID, id)
        }
        Log.d("debugTag", "==========================================================================")
        Log.d("debugTag", "sendNoteItem delete id: $id bundle: $bundle")
        activity?.supportFragmentManager?.setFragmentResult(KEY_NOTE_FRAGMENT_RESULT_DEL, bundle)
    }

//    Listeners
    private fun textListener() {
        textEditTV.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                text = s.toString()
                updateSaveButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    companion object {
        const val KEY_NOTE_FRAGMENT_RESULT_ADD = "KEY_NOTE_FRAGMENT_RESULT_ADD"
        const val KEY_NOTE_FRAGMENT_RESULT_UPD = "KEY_NOTE_FRAGMENT_RESULT_UPD"
        const val KEY_NOTE_FRAGMENT_RESULT_DEL = "KEY_NOTE_FRAGMENT_RESULT_DEL"

        const val FRAGMENT_NOTE_ITEM = "NOTE_ITEM"
        const val FRAGMENT_NOTE_ITEM_ID = "NOTE_ITEM_ID"
    }
}