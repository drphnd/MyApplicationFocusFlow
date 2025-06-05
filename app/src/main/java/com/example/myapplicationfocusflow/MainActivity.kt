package com.example.myapplicationfocusflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplicationfocusflow.View.FocusSessionView
import com.example.myapplicationfocusflow.View.FocusSetUpView
import com.example.myapplicationfocusflow.View.HistoryListView
import com.example.myapplicationfocusflow.View.HomeView
import com.example.myapplicationfocusflow.ViewModel.FocusCategoryViewModel
import com.example.myapplicationfocusflow.ViewModel.FocusModeViewModel
import com.example.myapplicationfocusflow.ViewModel.FocusSessionViewModel
import com.example.myapplicationfocusflow.ViewModel.ViewModelFactory
import com.example.myapplicationfocusflow.ui.theme.MyApplicationFocusFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as? FocusFlowApplication ?: return
        if (application is FocusFlowApplication) {
            val app = application as FocusFlowApplication
            // lakukan sesuatu dengan app
        }
        val viewModelFactory = ViewModelFactory(app)

        setContent {
            MyApplicationFocusFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FocusFlowApp(viewModelFactory)
                }
            }
        }
    }
}

@Composable
fun FocusFlowApp(viewModelFactory: ViewModelFactory) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            val focusModeViewModel: FocusModeViewModel = viewModel(factory = viewModelFactory)
            HomeView(
                viewModel = focusModeViewModel,
                onStartFocusClick = { navController.navigate("focus_setup") },
                onHistoryClick = { navController.navigate("history") }
            )
        }

        composable("focus_setup") {
            val focusModeViewModel: FocusModeViewModel = viewModel(factory = viewModelFactory)
            val categoryViewModel: FocusCategoryViewModel = viewModel(factory = viewModelFactory)

            FocusSetUpView(
                focusViewModel = focusModeViewModel,
                categoryViewModel = categoryViewModel,
                onSaveClick = { focusModel ->
                    navController.navigate("focus_session/${focusModel.focus_id}")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("focus_session/{focusId}") { backStackEntry ->
            val focusId = backStackEntry.arguments?.getString("focusId")?.toIntOrNull() ?: 0
            val sessionViewModel: FocusSessionViewModel = viewModel(factory = viewModelFactory)
            val focusModeViewModel: FocusModeViewModel = viewModel(factory = viewModelFactory)

            FocusSessionView(
                focusId = focusId,
                sessionViewModel = sessionViewModel,
                focusViewModel = focusModeViewModel,
                onSessionComplete = { navController.navigate("history") },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("history") {
            val focusModeViewModel: FocusModeViewModel = viewModel(factory = viewModelFactory)
            val sessionViewModel: FocusSessionViewModel = viewModel(factory = viewModelFactory)

            HistoryListView(
                focusViewModel = focusModeViewModel,
                sessionViewModel = sessionViewModel,
                onBackClick = { navController.navigate("home") }
            )
        }
    }
}