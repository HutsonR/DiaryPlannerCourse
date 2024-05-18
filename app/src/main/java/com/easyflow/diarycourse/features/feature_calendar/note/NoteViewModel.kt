package com.easyflow.diarycourse.features.feature_calendar.note

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.domain_api.NoteUseCase
import com.easyflow.diarycourse.domain.models.NoteItem
import com.easyflow.diarycourse.features.feature_calendar.note.util.NotePurpose
import kotlinx.coroutines.launch
import javax.inject.Inject

class NoteViewModel @Inject constructor(
    private val noteUseCase: NoteUseCase
) : BaseViewModel<NoteViewModel.State, NoteViewModel.Actions>(State()) {
    private var currentNote: NoteItem? = null
    private var parcelNote: NoteItem? = null
    private var purpose = NotePurpose.ADD

    fun setParcelItem(item: NoteItem) {
        parcelNote = item
        currentNote = item
        updateNote(item) // Для установки currentTask в Fragment
        if (item.text.isNotEmpty()) {
            purpose = NotePurpose.CHANGE
        }

        Log.d("debugTag", "updateSaveButtonState FROM VM setParcelItem")
        updateSaveButtonState()
    }

    fun updateNote(item: NoteItem) {
        if (purpose == NotePurpose.CHANGE) {
            parcelNote = item
            modifyState { copy(item = parcelNote) }
        } else {
            currentNote = item
            modifyState { copy(item = currentNote) }
        }

        Log.d("debugTag", "updateSaveButtonState FROM VM updateTask")
        updateSaveButtonState()
    }

    fun fetchData(date: String) {
        viewModelScope.launch {
            val fetchData = noteUseCase.getNote(date)
            modifyState {
                copy(
                    item = fetchData
                )
            }
        }
    }

    fun onDeleteItem(itemId: Int) {
        viewModelScope.launch {
            noteUseCase.deleteById(itemId)
            onAction(Actions.GoBack)
        }
    }

    fun onSaveButtonClicked() {
        viewModelScope.launch {
            if (purpose == NotePurpose.CHANGE) {
                parcelNote?.let { noteUseCase.update(it) }
            } else {
                currentNote?.let { noteUseCase.insert(it) }
            }
            onAction(Actions.GoBack)
        }
    }

    fun updateSaveButtonState() {
        if (purpose == NotePurpose.CHANGE) {
            if (currentNote != null && parcelNote != null) {
                val isEnabled = currentNote != parcelNote && parcelNote!!.text.isNotEmpty()
                onAction(Actions.ChangeSaveButtonState(isEnabled))
            }
        } else {
            var isEnabled = false
            currentNote?.let {
                isEnabled = it.text.isNotEmpty()
            }
            onAction(Actions.ChangeSaveButtonState(isEnabled))
        }
    }

    fun goBack() {
        onAction(Actions.GoBack)
    }

    data class State(
        var item: NoteItem? = null
    )

    sealed interface Actions {
        data object GoBack : Actions
        data class ChangeSaveButtonState(val state: Boolean): Actions
    }

    class NoteViewModelFactory @Inject constructor(
        private val noteUseCase: NoteUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteViewModel(noteUseCase) as T
        }
    }
}