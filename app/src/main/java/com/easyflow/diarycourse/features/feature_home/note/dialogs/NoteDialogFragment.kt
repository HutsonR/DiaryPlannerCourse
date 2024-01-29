package com.easyflow.diarycourse.features.feature_home.note.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.easyflow.diarycourse.features.feature_home.note.NoteViewModel
import kotlinx.coroutines.launch


class NoteDialogFragment(private val layoutResourceId: Int, private val viewModel: NoteViewModel) : DialogFragment() {
    private val TAG = "debugTag"
    private lateinit var binding: FragmentNoteDialogBinding
    private var parcelItem: NoteItem? = null
    private var previousText: String = ""
    private var text: String = ""
    private lateinit var textEditTV: TextView
    private lateinit var dialogListener: NoteDialogListener
    private lateinit var saveButton: ImageButton
    private lateinit var cancelButton: ImageButton
    private lateinit var deleteButton: Button

    //  проверка, что активити, вызывающая DialogFragment, реализует интерфейс DialogListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            dialogListener = requireParentFragment() as NoteDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("Parent fragment must implement DialogListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNoteDialogBinding.inflate(inflater)
        return binding.root
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
                    parcelItem!!.id?.let { id -> viewModel.deleteItem(id) }
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
                val updatedItem = parcelItem!!.copy(
                    text = text,
                )
                viewModel.updateData(data = updatedItem)
                dismiss()
            }
        } else {
            // По умолчанию обычное добавление элемента
            dialogListener.onConfirmAddDialogResult(text)
            dismiss()
        }
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
}