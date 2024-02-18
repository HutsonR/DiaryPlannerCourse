package com.easyflow.diarycourse.features.feature_home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.domain_api.NoteUseCase
import com.easyflow.diarycourse.domain.domain_api.ScheduleUseCase
import com.easyflow.diarycourse.domain.models.NoteItem
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.features.feature_home.models.CombineModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val scheduleUseCase: ScheduleUseCase,
    private val noteUseCase: NoteUseCase
) : BaseViewModel<HomeViewModel.State, HomeViewModel.Actions>(HomeViewModel.State()) {

    init {
        fetchData()
    }

    private fun fetchData() {
        Log.d("debugTag", "home fetch Data")
        viewModelScope.launch {
            val combineModels: MutableList<CombineModel> = mutableListOf()

            coroutineScope {
                val scheduleItemsDeferred = async { scheduleUseCase.getAll() }
                val noteItemsDeferred = async { noteUseCase.getAll() }

                // Ждем выполнения обеих корутин
                val scheduleItems = scheduleItemsDeferred.await()
                val noteItems = noteItemsDeferred.await()

                // Создаем список CombineModel и добавляем в него элементы из обеих списков
                combineModels.addAll(
                    scheduleItems.map { scheduleItem ->
                        CombineModel(
                            id = scheduleItem.id,
                            text = scheduleItem.text,
                            description = scheduleItem.description,
                            date = scheduleItem.date,
                            startTime = scheduleItem.startTime,
                            endTime = scheduleItem.endTime,
                            duration = scheduleItem.duration,
                            color = scheduleItem.color,
                            isCompleteTask = scheduleItem.isCompleteTask,
                            priority = scheduleItem.priority
                        )
                    }
                )
                combineModels.addAll(
                    noteItems.map { noteItem ->
                        CombineModel(
                            id = noteItem.id,
                            text = noteItem.text,
                            date = noteItem.date
                        )
                    }
                )
            }

            if (combineModels.isNotEmpty()) {
                modifyState { copy(list = combineModels) }
            }
        }
    }

    data class State(
        var list: List<CombineModel> = emptyList()
    )

    sealed interface Actions {
        data class ShowAlert(val alertData: String) : Actions
    }

    class HomeViewModelFactory @Inject constructor(
        private val scheduleUseCase: ScheduleUseCase,
        private val noteUseCase: NoteUseCase
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(scheduleUseCase, noteUseCase) as T
        }
    }
}