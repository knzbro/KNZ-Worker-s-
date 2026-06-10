package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AuditChecklistScreen() {
    val items = listOf(
        "Responsive Design",
        "WhatsApp Button Configured",
        "Performance Optimization",
        "Lead Forms Active",
        "Dark Mode Enabled"
    )

    var states by remember { mutableStateOf(List(5) { false }) }
    val maxScore = 10
    val score = states.count { it } * 2
    
    val animatedProgress by animateFloatAsState(
        targetValue = score / 10f,
        animationSpec = tween(durationMillis = 800)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Manual Audit & QA",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Quality Score", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "$score/$maxScore",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = if (score >= 8) MaterialTheme.colorScheme.primary else if (score >= 6) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth().height(16.dp),
                    color = if (score >= 8) MaterialTheme.colorScheme.primary else if (score >= 6) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(8.dp)) {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = states[index],
                            onCheckedChange = { isChecked ->
                                states = states.toMutableList().apply { set(index, isChecked) }
                            }
                        )
                        Text(text = item, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}
