package com.example.moviewatchlist.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchEventDao {
    @Query(
        "SELECT e.*, m.title AS movieTitle FROM watch_events e " +
            "INNER JOIN movies m ON m.id = e.movieId ORDER BY e.id DESC"
    )
    fun observeAll(): Flow<List<WatchEventWithTitle>>

    @Query(
        "SELECT e.*, m.title AS movieTitle FROM watch_events e " +
            "INNER JOIN movies m ON m.id = e.movieId WHERE e.movieId = :movieId ORDER BY e.id DESC"
    )
    fun observeForMovie(movieId: Long): Flow<List<WatchEventWithTitle>>

    @Query("SELECT * FROM watch_events ORDER BY id DESC LIMIT 1")
    suspend fun getLast(): WatchEvent?

    @Insert
    suspend fun insert(event: WatchEvent)

    @Delete
    suspend fun delete(event: WatchEvent)
}
