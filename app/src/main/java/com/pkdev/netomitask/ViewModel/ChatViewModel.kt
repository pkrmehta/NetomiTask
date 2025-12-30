package com.pkdev.netomitask.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pkdev.netomitask.Model.ListConnection
import com.pkdev.netomitask.Repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch

@HiltViewModel
class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()

    private val _connections = MutableLiveData<List<ListConnection>>()
    val connection: LiveData<List<ListConnection>> = _connections

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun loadConnections() {
        _loading.value = true
        viewModelScope.launch {
            try {
                val data = repository.fetchUsers()
                _connections.value = data
            } catch (e: Exception) {
                _loading.value = false
                Log.d("EXCEPTION", "exception message: ${e.message}")
            }
        }
    }
}