package com.composeproject.brewerysearch_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.composeproject.brewerysearch_app.ui.navigation.AppNavGraph
import com.composeproject.brewerysearch_app.ui.theme.JetpackNavigationAppTheme
import com.composeproject.brewerysearch_app.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        setContent {
            JetpackNavigationAppTheme {
                val viewModel = MainViewModel.default()
                CreateMainScreen(viewModel)
            }
        }
    }
}

@Composable
fun CreateMainScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()
    Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
        AppNavGraph(navController = navController, viewModel = viewModel)
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    JetpackNavigationAppTheme {
        // Preview with a ViewModel; not fetching network in preview
        val vm = MainViewModel.default()
        CreateMainScreen(vm)
    }
}