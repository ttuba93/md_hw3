@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.moviewatchlist.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moviewatchlist.di.AppViewModelProvider
import com.example.moviewatchlist.ui.EmptyState
import com.example.moviewatchlist.ui.LoadingBox
import com.example.moviewatchlist.ui.StatCard
import java.util.Locale

@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LoadingBox(modifier = Modifier.padding(padding))
            state.total == 0 -> EmptyState(
                "No data yet",
                "Add some movies to see your statistics",
                Modifier.padding(padding)
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProgressCard(state)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("Planned", state.planned.toString(), Modifier.weight(1f))
                    StatCard("Watching", state.watching.toString(), Modifier.weight(1f))
                    StatCard("Watched", state.watched.toString(), Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        "Time watched",
                        "${state.minutesWatched / 60}h ${state.minutesWatched % 60}m",
                        Modifier.weight(1f)
                    )
                    StatCard(
                        "Average rating",
                        state.averageRating?.let { String.format(Locale.US, "%.1f ★", it) } ?: "—",
                        Modifier.weight(1f)
                    )
                }
                RatingsCard(state)
                GenresCard(state)
            }
        }
    }
}

@Composable
private fun ProgressCard(state: StatsUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${state.watched} of ${state.total} movies watched (${(state.progress * 100).toInt()}%)")
            LinearProgressIndicator(progress = { state.progress }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun RatingsCard(state: StatsUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Ratings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            val maxCount = (state.ratingCounts.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
            state.ratingCounts.forEach { (stars, count) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$stars ★", modifier = Modifier.width(40.dp))
                    LinearProgressIndicator(
                        progress = { count.toFloat() / maxCount },
                        modifier = Modifier.weight(1f)
                    )
                    Text(count.toString(), modifier = Modifier.width(32.dp).padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun GenresCard(state: StatsUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Watched by genre", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (state.genreCounts.isEmpty()) {
                Text("Nothing watched yet", style = MaterialTheme.typography.bodyMedium)
            } else {
                state.genreCounts.forEach { (genre, count) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(genre.label)
                        Text(count.toString(), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
