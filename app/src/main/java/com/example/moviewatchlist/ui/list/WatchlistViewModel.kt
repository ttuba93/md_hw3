package com.example.moviewatchlist.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviewatchlist.data.repository.MovieRepository
import com.example.moviewatchlist.model.ActionResult
import com.example.moviewatchlist.model.Genre
import com.example.moviewatchlist.model.Movie
import com.example.moviewatchlist.model.WatchStatus
import com.example.moviewatchlist.model.messageOrNull
import com.example.moviewatchlist.model.watched
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WatchlistUiState(
    val movies: List<Movie> = emptyList(),
    val query: String = "",
    val genreFilter: Genre? = null,
    val hideWatched: Boolean = false,
    val isLoading: Boolean = true,
    val message: String? = null,
    val vmCounter: Int = 0
) {

    val visibleMovies: List<Movie>
        get() = movies
            .filter { it.title.contains(query.trim(), ignoreCase = true) }
            .filter { genreFilter == null || it.genre == genreFilter }
            .filter { !hideWatched || !it.watched }
            .sortedBy { it.year }

    val watchedCount: Int get() = movies.count { it.watched }
    val minutesLeft: Int get() = movies.filter { !it.watched }.sumOf { it.minutes }
}

class WatchlistViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchlistUiState())
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allMovies.collect { movies ->
                _uiState.update { it.copy(movies = movies, isLoading = false) }
            }
        }
    }

    fun onQueryChange(query: String) = _uiState.update { it.copy(query = query) }
    fun onGenreFilter(genre: Genre?) = _uiState.update { it.copy(genreFilter = genre) }
    fun onHideWatchedChange(hide: Boolean) = _uiState.update { it.copy(hideWatched = hide) }

    fun setWatched(movie: Movie, watched: Boolean) = launchAction {
        repository.changeStatus(movie.id, if (watched) WatchStatus.WATCHED else WatchStatus.PLANNED)
    }

    fun rate(movie: Movie, rating: Int) = launchAction { repository.rate(movie.id, rating) }

    fun delete(movie: Movie) {
        viewModelScope.launch {
            repository.delete(movie)
            _uiState.update { it.copy(message = "Deleted: ${movie.title}") }
        }
    }

    fun messageShown() = _uiState.update { it.copy(message = null) }

    fun incrementVmCounter() = _uiState.update { it.copy(vmCounter = it.vmCounter + 1) }

    private fun launchAction(block: suspend () -> ActionResult) {
        viewModelScope.launch {
            block().messageOrNull()?.let { text -> _uiState.update { it.copy(message = text) } }
        }
    }
}
