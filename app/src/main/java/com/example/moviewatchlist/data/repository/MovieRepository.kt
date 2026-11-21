package com.example.moviewatchlist.data.repository

import androidx.room.withTransaction
import com.example.moviewatchlist.data.local.AppDatabase
import com.example.moviewatchlist.data.local.WatchEvent
import com.example.moviewatchlist.data.local.WatchEventWithTitle
import com.example.moviewatchlist.model.ActionResult
import com.example.moviewatchlist.model.AddResult
import com.example.moviewatchlist.model.Movie
import com.example.moviewatchlist.model.WatchStatus
import kotlinx.coroutines.flow.Flow

// The only place where the UI layer gets data from. ViewModels never touch DAOs directly.
class MovieRepository(private val db: AppDatabase) {

    companion object {
        const val MAX_WATCHING = 3
    }

    private val movies = db.movieDao()
    private val events = db.watchEventDao()

    // ---- observable data (Room Flow: UI updates automatically) ----
    val allMovies: Flow<List<Movie>> = movies.observeAll()
    val history: Flow<List<WatchEventWithTitle>> = events.observeAll()
    fun observeMovie(id: Long): Flow<Movie?> = movies.observe(id)
    fun observeHistory(movieId: Long): Flow<List<WatchEventWithTitle>> = events.observeForMovie(movieId)
    suspend fun getMovie(id: Long): Movie? = movies.getById(id)

    // ---- CRUD ----
    // id == 0 -> create, otherwise update the details (status and rating are never touched here).
    suspend fun save(draft: Movie): AddResult = db.withTransaction {
        if (movies.countByTitle(draft.title, draft.id) > 0) {
            return@withTransaction AddResult.Duplicate(draft.title)
        }
        if (draft.id == 0L) {
            val id = movies.insert(draft)
            AddResult.Added(draft.copy(id = id))
        } else {
            val current = movies.getById(draft.id)
                ?: return@withTransaction AddResult.InvalidInput("This movie no longer exists")
            val updated = current.copy(
                title = draft.title,
                year = draft.year,
                genre = draft.genre,
                minutes = draft.minutes,
                type = draft.type
            )
            movies.update(updated)
            AddResult.Updated(updated)
        }
    }

    suspend fun delete(movie: Movie) = movies.delete(movie)

    // ---- Additional feature: workflow + history + undo ----

    // Status transition with business rules. Movie update and history entry are ONE transaction.
    suspend fun changeStatus(movieId: Long, to: WatchStatus): ActionResult = db.withTransaction {
        val movie = movies.getById(movieId)
            ?: return@withTransaction ActionResult.Rejected("Movie not found")

        if (to !in movie.status.allowedNext) {
            return@withTransaction ActionResult.Rejected(
                "Can't move from ${movie.status.label} to ${to.label}"
            )
        }
        if (to == WatchStatus.WATCHING && movies.countWatching() >= MAX_WATCHING) {
            return@withTransaction ActionResult.Rejected(
                "You can watch at most $MAX_WATCHING movies at once. Finish one first."
            )
        }

        // The rating exists only for watched movies.
        val newRating = if (to == WatchStatus.WATCHED) movie.rating else 0
        movies.update(movie.copy(status = to, rating = newRating))
        events.insert(
            WatchEvent(
                movieId = movie.id,
                fromStatus = movie.status,
                toStatus = to,
                fromRating = movie.rating,
                toRating = newRating
            )
        )
        ActionResult.Done("${movie.title}: ${movie.status.label} → ${to.label}")
    }

    suspend fun rate(movieId: Long, rating: Int): ActionResult = db.withTransaction {
        val movie = movies.getById(movieId)
            ?: return@withTransaction ActionResult.Rejected("Movie not found")

        if (movie.status != WatchStatus.WATCHED) {
            return@withTransaction ActionResult.Rejected("Only watched movies can be rated")
        }
        if (rating !in 1..5) {
            return@withTransaction ActionResult.Rejected("Rating must be from 1 to 5")
        }
        if (rating == movie.rating) {
            return@withTransaction ActionResult.Done()
        }

        movies.update(movie.copy(rating = rating))
        events.insert(
            WatchEvent(
                movieId = movie.id,
                fromStatus = movie.status,
                toStatus = movie.status,
                fromRating = movie.rating,
                toRating = rating
            )
        )
        ActionResult.Done("${movie.title}: rated $rating★")
    }

    // Reverts the most recent history entry and removes it from the history.
    suspend fun undoLast(): ActionResult = db.withTransaction {
        val last = events.getLast()
            ?: return@withTransaction ActionResult.Rejected("Nothing to undo")
        val movie = movies.getById(last.movieId)
        if (movie != null) {
            movies.update(movie.copy(status = last.fromStatus, rating = last.fromRating))
        }
        events.delete(last)
        ActionResult.Done("Undone: ${movie?.title ?: "last action"}")
    }
}
