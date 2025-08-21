package com.composeproject.brewerysearch_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.composeproject.brewerysearch_app.data.models.BreweryDto
import com.composeproject.brewerysearch_app.ui.screens.DetailsScreen
import com.composeproject.brewerysearch_app.ui.screens.HomeScreen
import com.composeproject.brewerysearch_app.ui.viewmodel.MainViewModel
import com.composeproject.brewerysearch_app.ui.viewmodel.PostsUiState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import kotlinx.coroutines.launch

// Defines all navigation routes in a type-safe way
sealed class Routes(val route: String) {
    data object Home : Routes("home")
    data object Details : Routes("details/{id}")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    navController: NavHostController, // controller that manages app navigation stack
    viewModel: MainViewModel          // ViewModel that provides UI state and actions
) {
    // Collects state from ViewModel as a Compose state variable
    val uiState by viewModel.uiState.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()

    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchSuggestions by viewModel.searchSuggestions.collectAsState()

    // NavHost defines the navigation graph with a start destination
    NavHost(navController = navController, startDestination = Routes.Home.route) {

        // Home screen destination
        composable(Routes.Home.route) {
            // Runs side-effect block when this composable enters composition
            LaunchedEffect(Unit) {
                // If nothing has been loaded yet, trigger the first load of breweries
                if (uiState is PostsUiState.Idle) viewModel.loadBreweries()
            }

            // Bottom sheet state for showing details
            var showSheet by remember { mutableStateOf(false) }
            var selectedId by remember { mutableStateOf<String?>(null) }
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val scope = rememberCoroutineScope()

            // Render HomeScreen and pass necessary callbacks
            HomeScreen(
                state = uiState,                           // current UI state from ViewModel
                onRetry = { viewModel.loadBreweries() },   // retry action to reload data
                onOpenDetails = { id ->                    // show details in a bottom sheet instead of navigating
                    selectedId = id
                    showSheet = true
                    // try to ensure details are loaded if not in the list
                    val inList = (uiState as? PostsUiState.Success)?.data?.any { it.id == id } == true
                    if (!inList) viewModel.loadBreweryById(id)
                },
                currentPage = currentPage,
                onNextPage = { viewModel.nextPage() },
                onPrevPage = { viewModel.prevPage() },
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                suggestions = searchSuggestions,
                onToggleSearch = { viewModel.setSearchActive(!isSearchActive) },
                onQueryChange = { q -> viewModel.onSearchQueryChange(q) },
                onSuggestionClick = { brewery ->
                    viewModel.setSearchActive(false)
                    selectedId = brewery.id
                    showSheet = true
                    // ensure details are loaded for suggestion selection as well
                    viewModel.loadBreweryById(brewery.id ?: "")
                },
                onDismissSearch = { viewModel.setSearchActive(false) }
            )

            // Compute selected brewery from list or ViewModel-loaded details
            val breweryFromList: BreweryDto? = when (val s = uiState) {
                is PostsUiState.Success -> s.data.firstOrNull { it.id == selectedId }
                else -> null
            }
            val loadedBrewery by viewModel.detailsBrewery.collectAsState()

            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        scope.launch {
                            sheetState.hide() // animate down
                            showSheet = false
                            selectedId = null
                        }
                    },
                    sheetState = sheetState
                ) {
                    DetailsScreen(
                        brewery = breweryFromList ?: loadedBrewery,
                        onBack = {
                            scope.launch {
                                sheetState.hide()
                                showSheet = false
                                selectedId = null
                            }
                        },
                        isInBottomSheet = true
                    )
                }
            }
        }

        // Details screen destination
        composable("details/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id").orEmpty()
            val breweryFromList: BreweryDto? = when (val s = uiState) {
                is PostsUiState.Success -> s.data.firstOrNull { it.id == id }
                else -> null
            }
            val loadedBrewery by viewModel.detailsBrewery.collectAsState()

            LaunchedEffect(id) {
                if (breweryFromList == null) {
                    viewModel.loadBreweryById(id)
                }
            }
            // Render DetailsScreen and provide back navigation
            DetailsScreen(
                brewery = breweryFromList ?: loadedBrewery,
                onBack = { navController.popBackStack() }  // goes back to previous screen
            )
        }
    }
}