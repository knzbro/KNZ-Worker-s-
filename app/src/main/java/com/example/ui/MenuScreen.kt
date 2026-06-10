package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MenuScreen(viewModel: KnzViewModel) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Work Timers", "Workspace Settings")

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Menu & Tools",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )

        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            when (selectedTabIndex) {
                0 -> RoutineScreen(viewModel = viewModel)
                1 -> AssetSystemScreen(viewModel = viewModel)
            }
        }
    }
}
