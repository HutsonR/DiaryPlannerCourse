package com.example.diarycourse.features.feature_home.note.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.diarycourse.R
import com.example.diarycourse.databinding.FragmentNoteDialogBinding
import com.example.diarycourse.domain.models.NoteItem
import com.example.diarycourse.domain.util.Resource
import com.example.diarycourse.features.feature_home.note.NoteViewModel
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
        setStyle(STYLE_NORMAL, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNoteDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        textEditTV = binding.noteBody

        saveButton = binding.addSave
        cancelButton = binding.addClose
        saveButton.setOnClickListener {
            handleSaveButtonClicked()
        }
        cancelButton.setOnClickListener {
            dismiss()
        }

        // Если имеется модель (фрагмент открыт для редактирования)
        parcelItem = arguments?.getParcelable("scheduleItem")
        if (parcelItem != null) {
            text = parcelItem!!.text
            previousText = text

            textEditTV.text = parcelItem!!.text
        }

        // Изначально деактивируем кнопку "Сохранить"
        updateSaveButtonState()
    }


    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
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

                viewModel.result.collect { result: Resource ->
                    when (result) {
                        is Resource.Success -> dismiss()
                        is Resource.Empty.Failed -> onFailed()
                    }
                }
            }
        } else {
            // По умолчанию обычное добавление элемента
            dialogListener.onConfirmAddDialogResult(text)
            dismiss()
        }
    }

    private fun onFailed() {
        showCustomToast("Возникла ошибка, попробуйте позже", Toast.LENGTH_SHORT)
        dismiss()
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

//    Listeners

}