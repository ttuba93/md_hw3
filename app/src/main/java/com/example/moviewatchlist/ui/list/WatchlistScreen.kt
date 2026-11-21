@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.moviewatchlist.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moviewatchlist.di.AppViewModelProvider
import com.example.moviewatchlist.ui.ConfirmDeleteDialog
import com.example.moviewatchlist.ui.EmptyState
import com.example.moviewatchlist.ui.GenreFilterRow
import com.example.moviewatchlist.ui.LoadingBox
import com.example.moviewatchlist.ui.MovieCard
import com.example.moviewatchlist.ui.SearchField
import com.example.moviewatchlist.ui.StateDemoCard
import com.example.moviewatchlist.ui.StatsRow

@Composable
fun WatchlistScreen(
    onOpenMovie: (Long) -> Unit,
    onAdd: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenStats: () -> Unit,
    flash: String?,
    onFlashShown: () -> Unit,
    viewModel: WatchlistViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDemo by rememberSaveable { mutableStateOf(false) }
    var deleteId by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }
    LaunchedEffect(flash) {
        flash?.let {
            snackbarHostState.showSnackbar(it)
            onFlashShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Watchlist") },
                actions = {
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = onOpenStats) {
                        Icon(Icons.Default.BarChart, contentDescription = "Statistics")
                    }
                    TextButton(onClick = { showDemo = !showDemo }) {
                        Text(if (showDemo) "Hide demo" else "State demo")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add movie")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingBox(modifier = Modifier.padding(padding))
        } else {
            val visibleMovies = state.visibleMovies

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = "search") {
                    SearchField(query = state.query, onQueryChange = viewModel::onQueryChange)
                }
                item(key = "genres") {
                    GenreFilterRow(selected = state.genreFilter, onSelected = viewModel::onGenreFilter)
                }
                item(key = "hide-watched") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hide watched movies")
                        Switch(checked = state.hideWatched, onCheckedChange = viewModel::onHideWatchedChange)
                    }
                }
                item(key = "stats") {
                    StatsRow(
                        total = state.movies.size,
                        watched = state.watchedCount,
                        minutesLeft = state.minutesLeft
                    )
                }
                if (showDemo) {
                    item(key = "demo") {
                        StateDemoCard(vmCounter = state.vmCounter, onVmIncrement = viewModel::incrementVmCounter)
                    }
                }
                if (state.movies.isEmpty()) {
                    item(key = "empty") {
                        EmptyState("Your watchlist is empty", "Tap + to add your first movie")
                    }
                } else if (visibleMovies.isEmpty()) {
                    item(key = "no-match") {
                        EmptyState("No movies found", "Try another search or change the filters")
                    }
                }
                items(visibleMovies, key = { it.id }) { movie ->
                    MovieCard(
                        movie = movie,
                        onClick = { onOpenMovie(movie.id) },
                        onWatchedChange = { viewModel.setWatched(movie, it) },
                        onRate = { viewModel.rate(movie, it) },
                        onDelete = { deleteId = movie.id }
                    )
                }
            }
        }
    }

    val movieToDelete = state.movies.firstOrNull { it.id == deleteId }
    if (movieToDelete != null) {
        ConfirmDeleteDialog(
            title = movieToDelete.title,
            onConfirm = {
                viewModel.delete(movieToDelete)
                deleteId = null
            },
            onDismiss = { deleteId = null }
        )
    }
}
