package com.example.moviewatchlist.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviewatchlist.data.local.WatchEventWithTitle
import com.example.moviewatchlist.data.repository.MovieRepository
import com.example.moviewatchlist.model.ActionResult
import com.example.moviewatchlist.model.Movie
import com.example.moviewatchlist.model.WatchStatus
import com.example.moviewatchlist.model.messageOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailUiState(
    val movie: Movie? = null,
    val history: List<WatchEventWithTitle> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null
)

class DetailViewModel(
    private val repository: MovieRepository,
    handle: SavedStateHandle
) : ViewModel() {

    private val movieId: Long = checkNotNull(handle["movieId"])
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DetailUiState> = combine(
        repository.observeMovie(movieId),
        repository.observeHistory(movieId),
        message
    ) { movie, history, msg ->
        DetailUiState(movie = movie, history = history, isLoading = false, message = msg)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState())

    fun changeStatus(to: WatchStatus) = launchAction { repository.changeStatus(movieId, to) }

    fun rate(rating: Int) = launchAction { repository.rate(movieId, rating) }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.getMovie(movieId)?.let { repository.delete(it) }
            onDone()
        }
    }

    fun messageShown() {
        message.value = null
    }

    private fun launchAction(block: suspend () -> ActionResult) {
        viewModelScope.launch { message.value = block().messageOrNull() }
    }
}
