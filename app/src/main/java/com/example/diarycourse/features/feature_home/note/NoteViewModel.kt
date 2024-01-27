package com.example.diarycourse.features.feature_home.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diarycourse.domain.domain_api.NoteUseCase
import com.example.diarycourse.domain.models.NoteItem
import com.example.diarycourse.domain.models.ScheduleItem
import com.example.diarycourse.domain.util.Resource
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class NoteViewModel @Inject constructor (
    private val noteUseCase: NoteUseCase
) : ViewModel() {

    private val _data = MutableSharedFlow<NoteItem>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val data: SharedFlow<NoteItem> = _data.asSharedFlow()

    private val _result = MutableSharedFlow<Resource>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val result: SharedFlow<Resource> = _result.asSharedFlow()

    fun fetchData(date: String) {
        viewModelScope.launch {
            noteUseCase.getNote(date)?.let { _data.emit(it) }
        }
    }

    fun addData(data: NoteItem) {
        viewModelScope.launch {
            _result.emit(noteUseCase.insert(data))
        }
    }

    fun updateData(data: NoteItem) {
        viewModelScope.launch {
            _result.emit(noteUseCase.update(data))
        }
    }

    fun deleteItem(itemId: Int) {
        viewModelScope.launch {
            _result.emit(noteUseCase.deleteById(itemId))
        }
    }

    class NoteViewModelFactory @Inject constructor(
        private val useCase: NoteUseCase
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteViewModel(useCase) as T
        }
    }
}