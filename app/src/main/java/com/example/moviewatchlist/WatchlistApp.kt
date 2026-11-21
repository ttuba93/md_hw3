package com.example.moviewatchlist

import android.app.Application
import com.example.moviewatchlist.data.local.AppDatabase
import com.example.moviewatchlist.data.repository.MovieRepository

class WatchlistApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.create(this) }
    val repository: MovieRepository by lazy { MovieRepository(database) }
}
