package com.easyflow.diarycourse.features.feature_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.domain_api.ScheduleUseCase
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.features.feature_home.adapter.TaskListItem
import com.easyflow.diarycourse.features.feature_home.adapter.TaskListUiConverter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val scheduleUseCase: ScheduleUseCase
) : BaseViewModel<HomeViewModel.State, HomeViewModel.Actions>(State()) {
    // TODO сделать UseCase для Inbox

    private var jobChangeQuerySearch: Job? = null
    private var list: List<TaskListItem> = emptyList()

    init {
        viewModelScope.launch {
            val taskItems = scheduleUseCase.getAll()
            list = TaskListUiConverter().convertToTaskListItem(taskItems)
            modifyState {
                copy(
                    dataList = list
                )
            }
        }
    }

    fun fastAddTask() {
        onAction(Actions.GoToFastAddTask)
    }

    fun goToInbox() {
        onAction(Actions.GoToInbox)
    }

    fun getInboxItemsCount() =
        getState().inboxList.size.toString()

    fun onUpdateButtonClick(id: String) {
        onAction(Actions.ShowAlert("onUpdateButtonClick $id"))
    }

    fun onTaskContentClick(id: String) {
        val item = getState().dataList.find { it is TaskListItem.Task && it.id == id } as? TaskListItem.Task
            ?: return

//        onAction(Actions.ShowTaskBottomSheet(item))
    }

    fun onChangeQuerySearch(querySearch: String) {
        jobChangeQuerySearch?.cancel()
        jobChangeQuerySearch = viewModelScope.launch {
            delay(DELAY_QUERY_SEARCH)
            val filteredList = filterItemsByName(list, querySearch)
            modifyState {
                copy(
                    dataList = filteredList,
                    querySearch = querySearch
                )
            }
        }
    }

    private fun filterItemsByName(
        list: List<TaskListItem>,
        querySearch: String
    ): List<TaskListItem> {
        return if (querySearch.isEmpty()) {
            list
        } else {
            list.filter {
                it is TaskListItem.Task && it.text.contains(querySearch, ignoreCase = true)
            }
        }
    }

    data class State(
        var inboxList: List<Any> = emptyList(),
        var dataList: List<TaskListItem> = emptyList(),
        var querySearch: String = ""
    )

    sealed interface Actions {
        data object GoToInbox : Actions
        data object GoToFastAddTask : Actions
        data class ShowTaskBottomSheet(val item: ScheduleItem) : Actions
        data class ShowAlert(val alertData: String) : Actions
    }

    class HomeViewModelFactory @Inject constructor(
        private val scheduleUseCase: ScheduleUseCase
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(scheduleUseCase) as T
        }
    }

    companion object {
        private const val DELAY_QUERY_SEARCH = 200L
    }

}