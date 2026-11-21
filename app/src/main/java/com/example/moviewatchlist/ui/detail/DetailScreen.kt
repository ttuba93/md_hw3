@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.moviewatchlist.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moviewatchlist.di.AppViewModelProvider
import com.example.moviewatchlist.model.Movie
import com.example.moviewatchlist.model.WatchStatus
import com.example.moviewatchlist.ui.ConfirmDeleteDialog
import com.example.moviewatchlist.ui.EmptyState
import com.example.moviewatchlist.ui.HistoryRow
import com.example.moviewatchlist.ui.LoadingBox
import com.example.moviewatchlist.ui.PosterBadge
import com.example.moviewatchlist.ui.RatingStars

@Composable
fun DetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    flash: String?,
    onFlashShown: () -> Unit,
    viewModel: DetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDelete by rememberSaveable { mutableStateOf(false) }
    val movie = state.movie

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
                title = { Text(movie?.title ?: "Movie") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (movie != null) {
                        IconButton(onClick = { onEdit(movie.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { showDelete = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            state.isLoading -> LoadingBox(modifier = Modifier.padding(padding))
            movie == null -> EmptyState(
                "Movie not found",
                "It may have been deleted.",
                Modifier.padding(padding)
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "info") { MovieInfoCard(movie) }
                item(key = "workflow") {
                    StatusWorkflowCard(
                        movie = movie,
                        onChangeStatus = viewModel::changeStatus,
                        onRate = viewModel::rate
                    )
                }
                item(key = "history-title") {
                    Text(
                        "Activity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (state.history.isEmpty()) {
                    item(key = "history-empty") {
                        EmptyState("No activity yet", "Status and rating changes will appear here")
                    }
                } else {
                    items(state.history, key = { it.event.id }) { item ->
                        HistoryRow(item = item, showTitle = false)
                    }
                }
            }
        }
    }

    if (showDelete && movie != null) {
        ConfirmDeleteDialog(
            title = movie.title,
            onConfirm = {
                showDelete = false
                viewModel.delete(onDone = onBack)
            },
            onDismiss = { showDelete = false }
        )
    }
}

@Composable
private fun MovieInfoCard(movie: Movie) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            PosterBadge(title = movie.title, size = 64)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(movie.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${movie.year} • ${movie.genre.label}", style = MaterialTheme.typography.bodyMedium)
                Text("${movie.minutes} min • ${movie.type.label}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun StatusWorkflowCard(
    movie: Movie,
    onChangeStatus: (WatchStatus) -> Unit,
    onRate: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                WatchStatus.entries.forEachIndexed { index, status ->
                    val current = status == movie.status
                    Text(
                        text = status.label,
                        fontWeight = if (current) FontWeight.Bold else FontWeight.Normal,
                        color = if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                    if (index < WatchStatus.entries.size - 1) Text("→", color = MaterialTheme.colorScheme.outline)
                }
            }

            movie.status.allowedNext.forEach { next ->
                val label = actionLabel(movie.status, next)
                if (next == WatchStatus.WATCHED || next == WatchStatus.WATCHING) {
                    Button(onClick = { onChangeStatus(next) }, modifier = Modifier.fillMaxWidth()) { Text(label) }
                } else {
                    OutlinedButton(onClick = { onChangeStatus(next) }, modifier = Modifier.fillMaxWidth()) { Text(label) }
                }
            }

            if (movie.status == WatchStatus.WATCHED) {
                Text("Your rating", style = MaterialTheme.typography.labelLarge)
                RatingStars(rating = movie.rating, onRate = onRate)
            } else {
                Text(
                    "Rating unlocks after you mark the movie as watched.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun actionLabel(from: WatchStatus, to: WatchStatus): String = when (to) {
    WatchStatus.WATCHING -> "Start watching"
    WatchStatus.WATCHED -> "Mark as watched"
    WatchStatus.PLANNED ->
        if (from == WatchStatus.WATCHED) "Watch again (resets rating)" else "Move back to planned"
}
