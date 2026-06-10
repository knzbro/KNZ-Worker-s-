package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineScreen(viewModel: KnzViewModel) {
    val routines by viewModel.routines.collectAsState()
    val activeTimers by viewModel.activeTimers.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Add Task Timer")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize()) {
            Text(
                text = "Daily Calendar & Timers",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (routines.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Timers Setup.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn {
                    items(routines) { routine ->
                        val remaining = activeTimers[routine.id] ?: routine.durationSeconds
                        val isRunning = remaining < routine.durationSeconds && remaining > 0 // rough estimate of running state if it's counting down. For a true UI state we'd track isRunning boolean. We can just use toggle.
                        // Wait, timerJobs doesn't expose isRunning directly. Let's just track it via the remaining time difference.
                        // Actually, I'll pass the toggle event to ViewModel.

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "${routine.name} (${routine.category})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val minutes = remaining / 60
                                    val seconds = remaining % 60
                                    Text(
                                        String.format("%02d:%02d", minutes, seconds),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = if (remaining <= 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    )
                                    if (routine.audioUri != null) {
                                        Text("Ringtone Attached", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                                Row {
                                    IconButton(onClick = { viewModel.toggleTimer(routine) }) {
                                         Icon(Icons.Default.PlayArrow, contentDescription = "Toggle Timer", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.resetTimer(routine) }) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Reset Timer", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { viewModel.deleteRoutine(routine) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            var name by remember { mutableStateOf("") }
            var category by remember { mutableStateOf("Work") }
            var durationInput by remember { mutableStateOf("25") }
            var audioUri by remember { mutableStateOf<Uri?>(null) }
            
            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                audioUri = uri
            }

            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("New Routine Timer") },
                text = {
                    Column {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Task Name") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (e.g. Work, School)") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = durationInput, onValueChange = { durationInput = it }, label = { Text("Duration (Minutes)") })
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { launcher.launch("audio/*") }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Audiotrack, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (audioUri == null) "Select .mp3 Ringtone" else "Ringtone Selected")
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val durationMinutes = durationInput.toLongOrNull() ?: 25L
                        val durationSeconds = durationMinutes * 60L
                        viewModel.addRoutine(name, category, durationSeconds, audioUri?.toString())
                        showDialog = false
                    }) { Text("Add") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
