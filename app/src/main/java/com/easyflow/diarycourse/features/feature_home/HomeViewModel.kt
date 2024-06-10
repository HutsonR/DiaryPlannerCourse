package com.easyflow.diarycourse.features.feature_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.easyflow.diarycourse.R
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.core.models.AlertData
import com.easyflow.diarycourse.domain.domain_api.ScheduleUseCase
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.domain.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val scheduleUseCase: ScheduleUseCase
) : BaseViewModel<HomeViewModel.State, HomeViewModel.Actions>(State()) {
    // TODO сделать UseCase для Inbox

    private var jobChangeQuerySearch: Job? = null
    private var list: List<ScheduleItem> = emptyList()

    init {
        viewModelScope.launch {
            modifyState { copy(isLoading = true) }
            list = scheduleUseCase.getAll()
            modifyState {
                copy(
                    dataList = list,
                    isLoading = false
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

    fun onTaskContentClick(id: String) {
        val item = getState().dataList.firstOrNull { it.id == id.toInt() }

        if (item != null) {
            onAction(Actions.ShowTaskBottomSheet(item))
        } else {
            onAction(Actions.ShowAlert(AlertData(message = R.string.error)))
        }
    }

    fun updateData(itemId: Int) {
        viewModelScope.launch {
            val stateList = getState().dataList
            val item = stateList.firstOrNull { it.id == itemId }
            item?.let {
                val updatedItem = item.copy(isCompleteTask = !item.isCompleteTask)
                when (scheduleUseCase.update(updatedItem)) {
                    is Resource.Success<*> -> {
                        modifyState {
                            copy(
                                dataList = stateList.map { stateItem ->
                                    if (stateItem.id == updatedItem.id) {
                                        updatedItem
                                    } else {
                                        stateItem
                                    }
                                }
                            )
                        }
                    }
                    is Resource.Failed -> {
                        onAction(Actions.ShowAlert(AlertData(message = R.string.error)))
                    }
                }
            }
        }
    }

    fun deleteItem(itemId: Int) {
        viewModelScope.launch {
            val deleteItem = scheduleUseCase.deleteById(itemId)
            when (deleteItem) {
                is Resource.Success<*> -> {
                    modifyState {
                        copy(
                            dataList = getState().dataList.filter { it.id != itemId }
                        )
                    }
                }
                is Resource.Failed -> {
                    onAction(Actions.ShowAlert(AlertData(message = R.string.error)))
                }
            }
        }
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
        list: List<ScheduleItem>,
        querySearch: String
    ): List<ScheduleItem> {
        return if (querySearch.isEmpty()) {
            list
        } else {
            list.filter {
                it.text.contains(querySearch, ignoreCase = true)
            }
        }
    }

    data class State(
        var isLoading: Boolean = false,
        var inboxList: List<Any> = emptyList(),
        var dataList: List<ScheduleItem> = emptyList(),
        var querySearch: String = ""
    )

    sealed interface Actions {
        data object GoToInbox : Actions
        data object GoToFastAddTask : Actions
        data class ShowTaskBottomSheet(val item: ScheduleItem) : Actions
        data class ShowAlert(val alertData: AlertData) : Actions
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