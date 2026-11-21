@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.moviewatchlist.ui.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moviewatchlist.di.AppViewModelProvider
import com.example.moviewatchlist.ui.EmptyState
import com.example.moviewatchlist.ui.HistoryRow
import com.example.moviewatchlist.ui.LoadingBox

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            state.isLoading -> LoadingBox(modifier = Modifier.padding(padding))
            state.events.isEmpty() -> EmptyState(
                "No activity yet",
                "Change a movie's status or rating and it will show up here",
                Modifier.padding(padding)
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item(key = "undo") {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(onClick = viewModel::undoLast, modifier = Modifier.fillMaxWidth()) {
                            Text("Undo last action")
                        }
                        Text(
                            "Undo reverts only the most recent entry.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                items(state.events, key = { it.event.id }) { item ->
                    HistoryRow(item = item, showTitle = true)
                }
            }
        }
    }
}
