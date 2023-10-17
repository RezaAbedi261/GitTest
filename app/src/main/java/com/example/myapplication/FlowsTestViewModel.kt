package com.example.myapplication


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FlowsTestViewModel : ViewModel() {

    private val _liveData = MutableLiveData<String>()
    val liveData: LiveData<String> = _liveData

    private val _stateFlow = MutableStateFlow<String>("empty")
    val stateFlow: StateFlow<String> = _stateFlow.asStateFlow()

    private val _sharedFlow = MutableSharedFlow<String>()
    val sharedFlow: SharedFlow<String> = _sharedFlow.asSharedFlow()

    fun triggerLiveData() {
        _liveData.value = ""
    }

    fun triggerFlow() {

    }

    fun triggerStateFlow() {
        viewModelScope.launch {
            _stateFlow.emit("")
        }
    }

    fun triggerSharedFlow() {
        viewModelScope.launch {
            _sharedFlow.emit("")
        }
    }


}