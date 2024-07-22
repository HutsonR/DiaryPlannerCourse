package com.easyflow.diarycourse.features.feature_home.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.TaskColor
import javax.inject.Inject

class FastTaskViewModel @Inject constructor() :
    BaseViewModel<FastTaskViewModel.State, FastTaskViewModel.Actions>(State()) {

    fun goBack() = onAction(Actions.GoBack)

    fun openDurationDialog() = onAction(Actions.GoToDurationDialog(getState().task))

    fun openPriorityDialog() = onAction(Actions.GoToPriorityDialog)

    fun openColorDialog() = onAction(Actions.GoToColorDialog)

    fun saveTask() {
        onAction(Actions.GoBackWithItem(getState().task ?: return))
    }

    fun updateTask(item: ScheduleItem) {
        modifyState {
            copy(
                task = item,
                taskColor = item.taskColor
            )
        }
        updateSaveButtonState()
    }

    fun getCurrentTask(): ScheduleItem? {
        return getState().task
    }

    private fun updateSaveButtonState() {
        val isEnable = getState().task?.text?.isNotEmpty()
        modifyState { copy(isSaveButtonEnable = isEnable ?: false) }
    }

    data class State(
        val task: ScheduleItem? = null,
        val taskColor: TaskColor = TaskColor.BLUE,
        val isSaveButtonEnable: Boolean = false
    )

    sealed interface Actions {
        data object GoBack : Actions
        data class GoBackWithItem(val item: ScheduleItem) : Actions
        data class GoToDurationDialog(val task: ScheduleItem?) : Actions
        data object GoToPriorityDialog : Actions
        data object GoToColorDialog : Actions
    }

    class FastTaskViewModelFactory @Inject constructor() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FastTaskViewModel() as T
        }
    }
}