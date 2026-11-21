package com.example.moviewatchlist.di

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.moviewatchlist.WatchlistApp
import com.example.moviewatchlist.ui.detail.DetailViewModel
import com.example.moviewatchlist.ui.edit.EditViewModel
import com.example.moviewatchlist.ui.history.HistoryViewModel
import com.example.moviewatchlist.ui.list.WatchlistViewModel
import com.example.moviewatchlist.ui.stats.StatsViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { WatchlistViewModel(app().repository) }
        initializer { DetailViewModel(app().repository, createSavedStateHandle()) }
        initializer { EditViewModel(app().repository, createSavedStateHandle()) }
        initializer { HistoryViewModel(app().repository) }
        initializer { StatsViewModel(app().repository) }
    }
}

private fun CreationExtras.app(): WatchlistApp = this[APPLICATION_KEY] as WatchlistApp
