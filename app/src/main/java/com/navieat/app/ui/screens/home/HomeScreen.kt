package com.navieat.app.ui.screens.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.navieat.app.R
import com.navieat.app.domain.model.Dish
import com.navieat.app.domain.model.Meal

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val locale = LocalConfiguration.current.locales[0].language
    val snackbar = remember { SnackbarHostState() }

    val pickPdf = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.onPdfPicked(uri, locale)
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.home_now_eating),
                style = MaterialTheme.typography.headlineSmall,
            )

            if (state.plan == null) {
                EmptyPlanCard(onUpload = { pickPdf.launch(arrayOf("application/pdf")) })
            } else {
                CurrentMealCard(state.currentMeal) { dish ->
                    viewModel.onSwapDish(dish, locale)
                }
                NextMealCard(state.nextMeal)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { pickPdf.launch(arrayOf("application/pdf")) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.home_upload_pdf)) }
            }
        }

        if (state.loading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) { data -> Snackbar { Text(data.visuals.message) } }
    }
}

@Composable
private fun EmptyPlanCard(onUpload: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.home_no_plan))
            Button(onClick = onUpload) { Text(stringResource(R.string.home_upload_pdf)) }
        }
    }
}

@Composable
private fun CurrentMealCard(meal: Meal?, onSwap: (Dish) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = meal?.slot?.name ?: stringResource(R.string.loading),
                style = MaterialTheme.typography.titleMedium,
            )
            meal?.dishes?.forEach { dish ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(dish.name, style = MaterialTheme.typography.bodyLarge)
                    dish.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    OutlinedButton(onClick = { onSwap(dish) }) {
                        Text(stringResource(R.string.home_swap_dish))
                    }
                }
            }
        }
    }
}

@Composable
private fun NextMealCard(meal: Meal?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.home_next_meal),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(meal?.slot?.name ?: "—")
            meal?.dishes?.firstOrNull()?.let { Text(it.name) }
        }
    }
}
