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
    private val useCase: NoteUseCase
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
            useCase.getNote(date)?.let { _data.emit(it) }
        }
    }

    fun addData(data: NoteItem) {
        viewModelScope.launch {
            _result.emit(useCase.insert(data))
        }
    }

    fun updateData(data: NoteItem) {
        viewModelScope.launch {
            _result.emit(useCase.update(data))
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