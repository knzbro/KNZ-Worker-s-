package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(viewModel: KnzViewModel) {
    val activeTimers by viewModel.activeTimers.collectAsState()
    val leads by viewModel.leads.collectAsState()

    val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
    ) {
        // Top Greeting
        Text(
            text = "Welcome to workspace,\nbuilt for productivity",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        var showAddLeadDialog by remember { mutableStateOf(false) }

        // Main Dashboard Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Major Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Active Projects", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${leads.size}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(8.dp).size(24.dp))
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }

            // Right Stacked Cards
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Right Card
                Card(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                    onClick = { showAddLeadDialog = true }
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.align(Alignment.TopEnd))
                        Text(
                            text = "Add\nLead",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.align(Alignment.BottomStart)
                        )
                    }
                }
                
                // Bottom Right Card
                Card(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary, modifier = Modifier.align(Alignment.TopEnd))
                        
                        var progressDays by remember { mutableStateOf(0) }
                        LaunchedEffect(Unit) {
                            if (viewModel.prefs.isChallengeRunning && viewModel.prefs.challengeStartTimeMs > 0) {
                                val diffMs = System.currentTimeMillis() - viewModel.prefs.challengeStartTimeMs
                                val daysPassed = (diffMs / (1000 * 60 * 60 * 24)).toInt()
                                progressDays = (daysPassed + 1).coerceAtMost(30)
                            }
                        }
                        
                        Text(
                            text = if (viewModel.prefs.isChallengeRunning) "Day $progressDays/30\nChallenge" else "Challenge\nPaused",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.align(Alignment.BottomStart)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Activity List
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Recent Leads", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text("${leads.size} Total", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(leads.take(5)) { lead ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        ) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(12.dp).size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(lead.businessName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(lead.status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(lead.location, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        if (showAddLeadDialog) {
            var businessName by remember { mutableStateOf("") }
            var location by remember { mutableStateOf("") }
            var whatsapp by remember { mutableStateOf("") }
            var description by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddLeadDialog = false },
                title = { Text("New Account", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(value = businessName, onValueChange = { businessName = it }, label = { Text("Business Name") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("WhatsApp Number") }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp), maxLines = 4)
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.addLead(businessName, location, whatsapp, description)
                        showAddLeadDialog = false
                    }, shape = RoundedCornerShape(12.dp)) {
                        Text("Add Business")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddLeadDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
