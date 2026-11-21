package com.example.moviewatchlist.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moviewatchlist.data.repository.MovieRepository
import com.example.moviewatchlist.model.AddResult
import com.example.moviewatchlist.model.Genre
import com.example.moviewatchlist.model.Movie
import com.example.moviewatchlist.model.MovieType
import com.example.moviewatchlist.model.resultMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MovieFormState(
    val title: String = "",
    val year: String = "",
    val minutes: String = "100",
    val genre: Genre = Genre.FAMILY,
    val type: MovieType = MovieType.ANIMATED,
    val titleError: String? = null,
    val yearError: String? = null,
    val minutesError: String? = null,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val notFound: Boolean = false,
    val savedMessage: String? = null
)

// One ViewModel for both "add" (movieId = null) and "edit" (movieId = id).
class EditViewModel(
    private val repository: MovieRepository,
    handle: SavedStateHandle
) : ViewModel() {

    private val movieId: Long? = (handle.get<Long>("movieId") ?: -1L).takeIf { it > 0 }

    private val _state = MutableStateFlow(
        MovieFormState(isEditing = movieId != null, isLoading = movieId != null)
    )
    val state: StateFlow<MovieFormState> = _state.asStateFlow()

    init {
        if (movieId != null) {
            viewModelScope.launch {
                val movie = repository.getMovie(movieId)
                _state.update {
                    if (movie == null) {
                        it.copy(isLoading = false, notFound = true)
                    } else {
                        it.copy(
                            title = movie.title,
                            year = movie.year.toString(),
                            minutes = movie.minutes.toString(),
                            genre = movie.genre,
                            type = movie.type,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun onTitleChange(value: String) = _state.update { it.copy(title = value, titleError = null) }
    fun onYearChange(value: String) =
        _state.update { it.copy(year = value.filter(Char::isDigit).take(4), yearError = null) }
    fun onMinutesChange(value: String) =
        _state.update { it.copy(minutes = value.filter(Char::isDigit).take(3), minutesError = null) }
    fun onGenreChange(value: Genre) = _state.update { it.copy(genre = value) }
    fun onTypeChange(value: MovieType) = _state.update { it.copy(type = value) }

    fun save() {
        val s = _state.value
        val title = s.title.trim()
        val year = s.year.toIntOrNull()
        val minutes = s.minutes.toIntOrNull()

        val titleError = when {
            title.isEmpty() -> "Title is required"
            title.length > 80 -> "Title is too long (max 80)"
            else -> null
        }
        val yearError = when {
            year == null -> "Year must be a number"
            year !in 2000..2015 -> "Year must be between 2000 and 2015"
            else -> null
        }
        val minutesError = when {
            minutes == null -> "Minutes must be a number"
            minutes !in 1..600 -> "Minutes must be between 1 and 600"
            else -> null
        }

        if (titleError != null || yearError != null || minutesError != null || year == null || minutes == null) {
            _state.update {
                it.copy(titleError = titleError, yearError = yearError, minutesError = minutesError)
            }
            return
        }

        viewModelScope.launch {
            val draft = Movie(
                id = movieId ?: 0L,
                title = title,
                year = year,
                genre = s.genre,
                minutes = minutes,
                type = s.type
            )
            when (val result = repository.save(draft)) {
                is AddResult.Added, is AddResult.Updated ->
                    _state.update { it.copy(savedMessage = resultMessage(result)) }
                else ->
                    _state.update { it.copy(titleError = resultMessage(result)) }
            }
        }
    }
}
