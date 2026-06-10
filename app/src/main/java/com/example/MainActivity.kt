package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.AppDatabase
import com.example.data.KnzRepository
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = KnzRepository(database.knzDao())
        val viewModel: KnzViewModel by viewModels { KnzViewModelFactory(repository, applicationContext) }

        setContent {
            val themeIndex by viewModel.themeIndex.collectAsState()
            MyApplicationTheme(themeIndex = themeIndex) {
                MainApp(viewModel)
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Leads : Screen("leads", "Leads", Icons.Filled.Group)
    object Browser : Screen("browser", "Browser", Icons.Filled.Language)
    object Calculator : Screen("calculator", "Profit", Icons.Filled.AttachMoney)
    object Menu : Screen("menu", "Menu", Icons.Filled.Menu)
    object LeadDetail : Screen("lead_detail", "Lead Detail", Icons.Filled.Info)
}

val items = listOf(
    Screen.Home,
    Screen.Leads,
    Screen.Browser,
    Screen.Calculator,
    Screen.Menu
)

@Composable
fun MainApp(viewModel: KnzViewModel) {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen(viewModel = viewModel) }
            composable(Screen.Leads.route) { 
                LeadPipelineScreen(viewModel = viewModel, onLeadClick = { lead ->
                    viewModel.selectedLead.value = lead
                    navController.navigate(Screen.LeadDetail.route)
                }) 
            }
            composable(Screen.Browser.route) { MiniBrowserScreen() }
            composable(Screen.Calculator.route) { ProfitGuardScreen() }
            composable(Screen.Menu.route) { MenuScreen(viewModel = viewModel) }
            composable(Screen.LeadDetail.route) {
                LeadDetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}

