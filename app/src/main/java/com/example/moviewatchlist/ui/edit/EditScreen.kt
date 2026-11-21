@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.moviewatchlist.ui.edit

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moviewatchlist.di.AppViewModelProvider
import com.example.moviewatchlist.model.Genre
import com.example.moviewatchlist.model.MovieType
import com.example.moviewatchlist.ui.EmptyState
import com.example.moviewatchlist.ui.LoadingBox

@Composable
fun EditScreen(
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: EditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedMessage) {
        state.savedMessage?.let(onSaved)
    }

    MovieFormContent(
        state = state,
        onTitleChange = viewModel::onTitleChange,
        onYearChange = viewModel::onYearChange,
        onMinutesChange = viewModel::onMinutesChange,
        onGenreChange = viewModel::onGenreChange,
        onTypeChange = viewModel::onTypeChange,
        onSave = viewModel::save,
        onBack = onBack
    )
}

@Composable
private fun MovieFormContent(
    state: MovieFormState,
    onTitleChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    onMinutesChange: (String) -> Unit,
    onGenreChange: (Genre) -> Unit,
    onTypeChange: (MovieType) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit movie" else "Add movie") },
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
            state.notFound -> EmptyState("Movie not found", "It may have been deleted.", Modifier.padding(padding))
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = onTitleChange,
                    label = { Text("Title") },
                    singleLine = true,
                    isError = state.titleError != null,
                    supportingText = { state.titleError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.year,
                        onValueChange = onYearChange,
                        label = { Text("Year (2000-2015)") },
                        singleLine = true,
                        isError = state.yearError != null,
                        supportingText = { state.yearError?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state.minutes,
                        onValueChange = onMinutesChange,
                        label = { Text("Minutes") },
                        singleLine = true,
                        isError = state.minutesError != null,
                        supportingText = { state.minutesError?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Genre", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Genre.entries.forEach {
                        FilterChip(
                            selected = state.genre == it,
                            onClick = { onGenreChange(it) },
                            label = { Text(it.label) }
                        )
                    }
                }

                Text("Type", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MovieType.entries.forEach {
                        FilterChip(
                            selected = state.type == it,
                            onClick = { onTypeChange(it) },
                            label = { Text(it.label) }
                        )
                    }
                }

                Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.isEditing) "Save changes" else "Add movie")
                }
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
            }
        }
    }
}
