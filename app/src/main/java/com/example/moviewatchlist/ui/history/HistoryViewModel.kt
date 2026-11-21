package com.example.moviewatchlist.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviewatchlist.data.local.WatchEventWithTitle
import com.example.moviewatchlist.data.repository.MovieRepository
import com.example.moviewatchlist.model.messageOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HistoryUiState(
    val events: List<WatchEventWithTitle> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null
)

class HistoryViewModel(private val repository: MovieRepository) : ViewModel() {

    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(repository.history, message) { events, msg ->
        HistoryUiState(events = events, isLoading = false, message = msg)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun undoLast() {
        viewModelScope.launch { message.value = repository.undoLast().messageOrNull() }
    }

    fun messageShown() {
        message.value = null
    }
}
