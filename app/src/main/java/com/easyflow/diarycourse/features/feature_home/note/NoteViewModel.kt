package com.easyflow.diarycourse.features.feature_home.note

import android.provider.ContactsContract.CommonDataKinds.Note
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.domain_api.NoteUseCase
import com.easyflow.diarycourse.domain.models.NoteItem
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.domain.util.Resource
import com.easyflow.diarycourse.features.feature_home.schedule.ScheduleViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class NoteViewModel @Inject constructor(
    private val noteUseCase: NoteUseCase
) : BaseViewModel<NoteViewModel.State, NoteViewModel.Actions>(NoteViewModel.State()) {

    fun fetchData(date: String) {
        viewModelScope.launch {
            val fetchData = noteUseCase.getNote(date)
            fetchData?.let {
                modifyState { copy(list = it) }
            }
        }
    }

    fun addData(data: NoteItem) {
        viewModelScope.launch {
            val addData = noteUseCase.insert(data)
            modifyState { copy(result = addData) }
        }
    }

    fun updateData(data: NoteItem) {
        viewModelScope.launch {
            val updateData = noteUseCase.update(data)
            Log.d("debugTag", "NOTE viewModel updateData")
            modifyState { copy(result = updateData) }
        }
    }

    fun deleteItem(itemId: Int) {
        viewModelScope.launch {
            val deleteItem = noteUseCase.deleteById(itemId)
            modifyState { copy(result = deleteItem) }
        }
    }

//    TODO Сделать setDate

    data class State(
        var list: NoteItem? = null,
        var result: Resource? = null,
        var selectedDate: String? = null
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