package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
import com.example.util.PreferencesHelper
import java.util.concurrent.Executor

class MainActivity : FragmentActivity() {
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var prefs: PreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        prefs = PreferencesHelper(applicationContext)

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = KnzRepository(database.knzDao())
        val viewModel: KnzViewModel by viewModels { KnzViewModelFactory(repository, applicationContext, prefs) }

        executor = ContextCompat.getMainExecutor(this)
        
        setContent {
            val themeIndex by viewModel.themeIndex.collectAsState()
            var isUnlocked by remember { mutableStateOf(!prefs.isAppLockEnabled) }

            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock App")
                .setSubtitle("Use your device credential or biometric")
                .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build()

            biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        isUnlocked = true
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                })

            LaunchedEffect(Unit) {
                if (prefs.isAppLockEnabled) {
                    biometricPrompt.authenticate(promptInfo)
                }
            }

            MyApplicationTheme(themeIndex = themeIndex) {
                var showWelcomeScreen by remember { mutableStateOf(true) }
                if (isUnlocked) {
                    if (showWelcomeScreen) {
                        WelcomeScreen(onStartClicked = { showWelcomeScreen = false })
                    } else {
                        MainApp(viewModel)
                    }
                } else {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Lock, contentDescription = "Locked", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("App is Locked", style = MaterialTheme.typography.headlineMedium)
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(onClick = { biometricPrompt.authenticate(promptInfo) }) {
                                    Text("Unlock")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Leads : Screen("leads", "Leads", Icons.Filled.Group)
    object Challenge : Screen("challenge", "Challenge", Icons.Filled.Timer)
    object Menu : Screen("menu", "Menu", Icons.Filled.Menu)
    object LeadDetail : Screen("lead_detail", "Lead Detail", Icons.Filled.Info)
}

val items = listOf(
    Screen.Home,
    Screen.Leads,
    Screen.Challenge,
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
            composable(Screen.Challenge.route) { RoutineScreen(viewModel = viewModel) }
            composable(Screen.Menu.route) { MenuScreen(viewModel = viewModel) }
            composable(Screen.LeadDetail.route) {
                LeadDetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}

