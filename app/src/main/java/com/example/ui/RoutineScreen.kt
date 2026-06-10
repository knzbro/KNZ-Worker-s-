package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Calendar

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
            
            ChallengeSection()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Daily Tasks & Timers",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (routines.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No Timers Setup.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(routines) { routine ->
                        val remaining = activeTimers[routine.id] ?: routine.durationSeconds

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
                                    val hours = remaining / 3600
                                    val minutes = (remaining % 3600) / 60
                                    val seconds = remaining % 60
                                    Text(
                                        String.format("%02d:%02d:%02d", hours, minutes, seconds),
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
            var hoursInput by remember { mutableStateOf("0") }
            var minutesInput by remember { mutableStateOf("25") }
            var secondsInput by remember { mutableStateOf("0") }
            var selectedDate by remember { mutableStateOf("Today") }
            var audioUri by remember { mutableStateOf<Uri?>(null) }
            
            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                audioUri = uri
            }

            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("New Custom Timer") },
                text = {
                    Column {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Task Name") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (e.g. Health, Work)") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Custom Duration", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = hoursInput, onValueChange = { hoursInput = it }, label = { Text("HH") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                            OutlinedTextField(value = minutesInput, onValueChange = { minutesInput = it }, label = { Text("MM") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                            OutlinedTextField(value = secondsInput, onValueChange = { secondsInput = it }, label = { Text("SS") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = selectedDate, onValueChange = { selectedDate = it }, label = { Text("Target Date (Optional)") }, modifier = Modifier.fillMaxWidth())

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { launcher.launch("audio/*") }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Audiotrack, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (audioUri == null) "Select Custom Ringtone" else "Ringtone Selected")
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val h = hoursInput.toLongOrNull() ?: 0L
                        val m = minutesInput.toLongOrNull() ?: 25L
                        val s = secondsInput.toLongOrNull() ?: 0L
                        val durationSeconds = (h * 3600L) + (m * 60L) + s
                        
                        viewModel.addRoutine(name, category, durationSeconds, audioUri?.toString())
                        showDialog = false
                    }) { Text("Add Timer") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun ChallengeSection() {
    var isRunning by remember { mutableStateOf(false) }
    var progressDays by remember { mutableStateOf(1) } // Assume out of 30 days
    val maxDays = 30
    var currentMotivationQuote by remember { mutableStateOf("Start your journey today.") }
    val quotes = listOf(
        "Believe in yourself.",
        "Every day is a new beginning.",
        "Push your limits.",
        "Consistency is key.",
        "Discipline equals freedom.",
        "Focus on the goal.",
        "Don't count the days, make the days count."
    )
    
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while(true) {
                delay(30000)
                currentMotivationQuote = quotes.random()
            }
        }
    }

    val animatedProgress by animateFloatAsState(targetValue = progressDays.toFloat() / maxDays.toFloat(), label = "progress")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("30-Day Challenge", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.height(16.dp))

            val primaryColor = MaterialTheme.colorScheme.primary
            val trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                Canvas(modifier = Modifier.size(160.dp)) {
                    val strokeWidth = 16.dp.toPx()
                    drawArc(
                        color = trackColor,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = primaryColor,
                        startAngle = 180f,
                        sweepAngle = 180f * animatedProgress,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.offset(y = (-16).dp)) {
                    Text("$progressDays", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Days", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { isRunning = !isRunning; if (!isRunning) currentMotivationQuote = "Challenge Paused." }, colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)) {
                    Text(if (isRunning) "Stop Challenge" else "Start Challenge")
                }
                if (isRunning) {
                    OutlinedButton(onClick = { if (progressDays < maxDays) progressDays++ }) {
                        Text("+1 Day")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = '"' + currentMotivationQuote + '"',
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()
                )
            }
        }
    }
}
