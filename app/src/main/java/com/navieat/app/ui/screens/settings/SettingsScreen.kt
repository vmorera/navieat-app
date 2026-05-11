package com.navieat.app.ui.screens.settings

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.navieat.app.R

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineSmall,
        )

        Text(stringResource(R.string.settings_ai_provider), style = MaterialTheme.typography.titleMedium)
        Text("Current: ${state.provider}")

        OutlinedTextField(
            value = state.geminiApiKey,
            onValueChange = viewModel::onGeminiKeyChanged,
            label = { Text("Gemini ${stringResource(R.string.settings_api_key)}") },
            supportingText = { Text(stringResource(R.string.settings_api_key_hint)) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        HorizontalDivider()

        Text("Microsoft To Do", style = MaterialTheme.typography.titleMedium)
        if (state.microsoftAccount == null) {
            Button(onClick = { viewModel.signInMicrosoft(context as Activity) }) {
                Text(stringResource(R.string.settings_microsoft_signin))
            }
        } else {
            Text(stringResource(R.string.settings_microsoft_signed_in, state.microsoftAccount!!))
            OutlinedButton(onClick = viewModel::signOutMicrosoft) {
                Text(stringResource(R.string.settings_microsoft_signout))
            }
        }

        HorizontalDivider()
        Text(
            "NaviEat 0.1.0 — open source",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
