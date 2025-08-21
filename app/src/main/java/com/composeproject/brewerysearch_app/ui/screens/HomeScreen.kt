package com.composeproject.brewerysearch_app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.composeproject.brewerysearch_app.data.models.BreweryDto
import com.composeproject.brewerysearch_app.ui.viewmodel.PostsUiState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: PostsUiState,
    onRetry: () -> Unit,
    onOpenDetails: (String) -> Unit,
    currentPage: Int,
    onNextPage: () -> Unit,
    onPrevPage: () -> Unit,
    isSearchActive: Boolean,
    searchQuery: String,
    suggestions: List<BreweryDto>,
    onToggleSearch: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSuggestionClick: (BreweryDto) -> Unit,
    onDismissSearch: () -> Unit
) {

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Breweries") },
            actions = {
                IconButton(
                    onClick = {
                        if (isSearchActive) {
                            if (searchQuery.isNotEmpty()) {
                                onQueryChange("")
                            } else {
                                onDismissSearch()
                            }
                        } else {
                            onToggleSearch()
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isSearchActive) Icons.Filled.Close else Icons.Filled.Search,
                        contentDescription = if (isSearchActive) "Clear search" else "Search"
                    )
                }
            }
        )

        HorizontalDivider(modifier = Modifier
            .fillMaxWidth(),
            DividerDefaults.Thickness,
            color = Color.Gray)

        if (isSearchActive) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Search breweries...") }
                )
                // Simple dropdown of suggestions
                if (suggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp),
                        colors = CardDefaults.cardColors( containerColor = Color.Green )
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                        ) {
                            items(suggestions) { s ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSuggestionClick(s)
                                        }
                                        .padding(12.dp)
                                ) {
                                    Text(text = s.name ?: "")
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        when (state) {
            is PostsUiState.Idle, is PostsUiState.Loading -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is PostsUiState.Error -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: ${state.message}")
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onRetry) { Text("Retry") }
                }
            }
            is PostsUiState.Success -> {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(state.data) { item ->
                        // Zoom-in animation state per item
                        val scale = remember { Animatable(1f) }
                        var isAnimating by remember { mutableStateOf(false) }
                        val scope = rememberCoroutineScope()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    scaleX = scale.value
                                    scaleY = scale.value
                                }
                                .clickable(enabled = !isAnimating) {
                                    if (!isAnimating) {
                                        isAnimating = true
                                        scope.launch {
                                            // Slight delay and smooth zoom to give a feeling of "zooming into" the item
                                            scale.animateTo(
                                                targetValue = 1.04f,
                                                animationSpec = tween(
                                                    durationMillis = 300,
                                                    delayMillis = 60,
                                                    easing = LinearOutSlowInEasing
                                                )
                                            )
                                            onOpenDetails(item.id.orEmpty())
                                            // No need to animate back; navigation will change screen.
                                            // Reset in case user returns to the list quickly.
                                            scale.snapTo(1f)
                                            isAnimating = false
                                        }
                                    }
                                }
                                .padding(16.dp)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(item.name ?: "")
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${item.city ?: ""}, ${item.state ?: ""}")
                                }
                            }
                        }
                    }
                }
                // Pagination controls
                HorizontalDivider(modifier = Modifier
                    .fillMaxWidth(),
                    DividerDefaults.Thickness,
                    color = Color.Gray)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = onPrevPage, enabled = currentPage > 1) {
                        Text("Previous")
                    }
                    Text(text = "Page $currentPage")
                    Button(onClick = onNextPage) {
                        Text("Next")
                    }
                }
            }
        }
    }
}
