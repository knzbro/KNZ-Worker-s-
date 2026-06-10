package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Lead

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadPipelineScreen(viewModel: KnzViewModel, onLeadClick: (Lead) -> Unit) {
    val leads by viewModel.filteredLeads.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Add Lead")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize()) {
            Text(
                text = "Pipeline Management",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                label = { Text("Search Leads") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            val filterStatuses = listOf("All", "Scouted", "Waiting", "Hired", "Rejected")
            LazyRow(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                items(filterStatuses) { status ->
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = { viewModel.statusFilter.value = status },
                        label = { Text(status) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            if (leads.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Leads Match.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(leads) { lead ->
                        LeadCard(lead = lead, onClick = { onLeadClick(lead) })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        if (showDialog) {
            AddLeadDialog(onDismiss = { showDialog = false }, onAdd = { name, loc, phone, desc ->
                viewModel.addLead(name, loc, phone, desc)
                showDialog = false
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadCard(lead: Lead, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(lead.businessName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Badge(containerColor = when(lead.status) {
                    "Scouted" -> MaterialTheme.colorScheme.secondary
                    "Waiting" -> MaterialTheme.colorScheme.error
                    "Hired" -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                }) {
                    Text(lead.status, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("📍 ${lead.location}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AddLeadDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String) -> Unit) {
    var businessName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Lead") },
        text = {
            Column {
                OutlinedTextField(value = businessName, onValueChange = { businessName = it }, label = { Text("Business Name") })
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
                OutlinedTextField(value = whatsapp, onValueChange = { whatsapp = it }, label = { Text("WhatsApp Number") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(businessName, location, whatsapp, description) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
