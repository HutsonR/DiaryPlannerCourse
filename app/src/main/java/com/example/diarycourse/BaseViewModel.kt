package com.example.diarycourse

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.Flow

//abstract class BaseViewModel<Actions>(initialState: State) : ViewModel() {
//    private val _action: MutableSharedFlow<Actions> = MutableSharedFlow(replay = 1)
//    val action: Flow<Actions> = _action.onEach { _action.resetReplayCache() }
//
//    protected fun onAction(action: Actions) {
//        _action.tryEmit(action)
//    }
//}