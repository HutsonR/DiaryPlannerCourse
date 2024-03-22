package com.easyflow.diarycourse.features.feature_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.domain_api.NoteUseCase
import com.easyflow.diarycourse.domain.domain_api.ScheduleUseCase
import com.easyflow.diarycourse.features.feature_calendar.models.CombineModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val scheduleUseCase: ScheduleUseCase,
    private val noteUseCase: NoteUseCase
) : BaseViewModel<SettingsViewModel.State, SettingsViewModel.Actions>(SettingsViewModel.State()) {

    private fun fetchData() {
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

    class SettingsViewModelFactory @Inject constructor(
        private val scheduleUseCase: ScheduleUseCase,
        private val noteUseCase: NoteUseCase
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(scheduleUseCase, noteUseCase) as T
        }
    }
}