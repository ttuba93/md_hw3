package com.example.moviewatchlist.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.moviewatchlist.model.Movie
import com.example.moviewatchlist.model.sampleMovies

@Database(entities = [Movie::class, WatchEvent::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun watchEventDao(): WatchEventDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "watchlist.db")
                .addCallback(SeedCallback)
                .build()
    }
}

private object SeedCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        sampleMovies().forEach { m ->
            val values = ContentValues().apply {
                put("title", m.title)
                put("year", m.year)
                put("genre", m.genre.name)
                put("minutes", m.minutes)
                put("type", m.type.name)
                put("status", m.status.name)
                put("rating", m.rating)
            }
            db.insert("movies", SQLiteDatabase.CONFLICT_NONE, values)
        }
    }
}
