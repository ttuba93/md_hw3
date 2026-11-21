package com.example.moviewatchlist.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviewatchlist.data.repository.MovieRepository
import com.example.moviewatchlist.model.Genre
import com.example.moviewatchlist.model.Movie
import com.example.moviewatchlist.model.WatchStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val isLoading: Boolean = true,
    val total: Int = 0,
    val planned: Int = 0,
    val watching: Int = 0,
    val watched: Int = 0,
    val minutesWatched: Int = 0,
    val averageRating: Double? = null,
    val ratingCounts: List<Pair<Int, Int>> = emptyList(),
    val genreCounts: List<Pair<Genre, Int>> = emptyList()
) {
    val progress: Float get() = if (total == 0) 0f else watched.toFloat() / total

    companion object {
        fun from(movies: List<Movie>): StatsUiState {
            val watchedMovies = movies.filter { it.status == WatchStatus.WATCHED }
            val rated = watchedMovies.filter { it.rating > 0 }
            return StatsUiState(
                isLoading = false,
                total = movies.size,
                planned = movies.count { it.status == WatchStatus.PLANNED },
                watching = movies.count { it.status == WatchStatus.WATCHING },
                watched = watchedMovies.size,
                minutesWatched = watchedMovies.sumOf { it.minutes },
                averageRating = if (rated.isEmpty()) null else rated.map { it.rating }.average(),
                ratingCounts = (5 downTo 1).map { stars -> stars to rated.count { it.rating == stars } },
                genreCounts = watchedMovies.groupingBy { it.genre }.eachCount()
                    .toList().sortedByDescending { it.second }
            )
        }
    }
}

class StatsViewModel(repository: MovieRepository) : ViewModel() {
    val uiState: StateFlow<StatsUiState> = repository.allMovies
        .map { StatsUiState.from(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())
}
