package com.easyflow.diarycourse.features.feature_home.note

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.domain_api.NoteUseCase
import com.easyflow.diarycourse.domain.models.NoteItem
import com.easyflow.diarycourse.domain.util.Resource
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class NoteViewModel @Inject constructor(
    private val noteUseCase: NoteUseCase
) : BaseViewModel<NoteViewModel.State, NoteViewModel.Actions>(NoteViewModel.State()) {

    private val _result = MutableSharedFlow<Resource>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val result: SharedFlow<Resource> = _result.asSharedFlow()

    fun fetchData(date: String) {
        viewModelScope.launch {
            val fetchData = noteUseCase.getNote(date)
            modifyState {
                copy(
                    note = fetchData
                )
            }
        }
    }

    fun addData(data: NoteItem) {
        viewModelScope.launch {
            val addData = noteUseCase.insert(data)
            _result.emit(addData)
        }
    }

    fun updateData(data: NoteItem) {
        viewModelScope.launch {
            val updateData = noteUseCase.update(data)
            _result.emit(updateData)
        }
    }

    fun deleteItem(itemId: Int) {
        viewModelScope.launch {
            val deleteItem = noteUseCase.deleteById(itemId)
            _result.emit(deleteItem)
        }
    }

    data class State(
        var note: NoteItem? = null
    )

    sealed interface Actions {
        data class ShowAlert(val alertData: String) : Actions
    }

    class NoteViewModelFactory @Inject constructor(
        private val useCase: NoteUseCase
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteViewModel(useCase) as T
        }
    }
}