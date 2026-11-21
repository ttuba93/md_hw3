package com.example.moviewatchlist.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.moviewatchlist.model.Movie
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies ORDER BY year, title")
    fun observeAll(): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE id = :id")
    fun observe(id: Long): Flow<Movie?>

    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getById(id: Long): Movie?

    @Query("SELECT COUNT(*) FROM movies WHERE status = 'WATCHING'")
    suspend fun countWatching(): Int

    @Query("SELECT COUNT(*) FROM movies WHERE LOWER(title) = LOWER(:title) AND id != :excludeId")
    suspend fun countByTitle(title: String, excludeId: Long): Int

    @Insert
    suspend fun insert(movie: Movie): Long

    @Update
    suspend fun update(movie: Movie)

    @Delete
    suspend fun delete(movie: Movie)
}
