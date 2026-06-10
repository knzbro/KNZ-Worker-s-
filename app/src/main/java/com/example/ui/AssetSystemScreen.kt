package com.example.ui

import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun AssetSystemScreen(viewModel: KnzViewModel) {
    val context = LocalContext.current
    val themeIndex by viewModel.themeIndex.collectAsState()
    val leads by viewModel.leads.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Workspace Assets & Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().height(160.dp).padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            onClick = {
                try {
                    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "KNZ_WORKER.apk")
                    file.writeText("MOCK_APK_PAYLOAD_GENERATED_BY_ARCHITECTURE")
                    Toast.makeText(context, "KNZ WORKER.apk generated at ${file.absolutePath}", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error generating file", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Folder",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp).padding(bottom = 8.dp)
                )
                Text(
                    text = "Extract KNZ WORKER.apk",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth().height(160.dp).padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            onClick = {
                try {
                    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "KNZ_Leads_Export.json")
                    val jsonArray = JSONArray()
                    leads.forEach { lead ->
                        val obj = JSONObject()
                        obj.put("businessName", lead.businessName)
                        obj.put("location", lead.location)
                        obj.put("whatsappNumber", lead.whatsappNumber)
                        obj.put("description", lead.description)
                        obj.put("status", lead.status)
                        jsonArray.put(obj)
                    }
                    file.writeText(jsonArray.toString(4))
                    Toast.makeText(context, "Leads exported to ${file.absolutePath}", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Export Error", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Export Leads",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(48.dp).padding(bottom = 8.dp)
                )
                Text(
                    text = "Export Leads Backup (JSON)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
             colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                    Icon(Icons.Default.ColorLens, contentDescription = "Theme", tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pro UI Theme Selection", style = MaterialTheme.typography.titleMedium)
                }
                androidx.compose.foundation.lazy.LazyRow {
                    val themes = listOf("Cyberpunk (Default)", "Midnight Blue", "Emerald Forest")
                    items(themes.size) { index ->
                        InputChip(
                            selected = themeIndex == index,
                            onClick = { viewModel.setThemeIndex(index) },
                            label = { Text(themes[index]) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
