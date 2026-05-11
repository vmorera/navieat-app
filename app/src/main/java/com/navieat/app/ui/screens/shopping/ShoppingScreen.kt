package com.navieat.app.ui.screens.shopping

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.navieat.app.R
import com.navieat.app.domain.model.ShoppingItem

@Composable
fun ShoppingScreen(viewModel: ShoppingViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val locale = LocalConfiguration.current.locales[0].language
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.error, state.info) {
        (state.error ?: state.info)?.let {
            snackbar.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.shopping_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.generateFromPlan(locale) }) {
                    Text(stringResource(R.string.shopping_generate))
                }
                OutlinedButton(onClick = { viewModel.sendToMicrosoftTodo() }) {
                    Text(stringResource(R.string.shopping_send_todo))
                }
            }
            OutlinedButton(
                onClick = {
                    val text = state.items
                        .filter { !it.checked }
                        .joinToString("\n") { item ->
                            buildString {
                                append("• ")
                                append(item.name)
                                item.quantity?.let { append(" — $it") }
                            }
                        }
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(Intent.createChooser(send, null))
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.shopping_share)) }

            if (state.items.isEmpty()) {
                Text(stringResource(R.string.shopping_empty))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(state.items, key = { it.id }) { item ->
                        ShoppingRow(item) { viewModel.toggle(item.id, it) }
                    }
                }
            }
        }

        if (state.loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter)) { data ->
            Snackbar { Text(data.visuals.message) }
        }
    }
}

@Composable
private fun ShoppingRow(item: ShoppingItem, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = item.checked, onCheckedChange = onToggle)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(item.name)
            item.quantity?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
