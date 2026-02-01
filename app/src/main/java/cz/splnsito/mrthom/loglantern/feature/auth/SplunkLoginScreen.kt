package cz.splnsito.mrthom.loglantern.feature.auth

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.splnsito.mrthom.loglantern.core.ui.components.PrimaryButton

@Composable
fun SplunkLoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: SplunkAuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var baseUrl by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    // Dialog pro zobrazení vytvořeného tokenu
    if (uiState.createdToken != null) {
        AlertDialog(
            onDismissRequest = { /* Dialog nelze zavřít kliknutím mimo */ },
            title = { Text("Token Created Successfully!") },
            text = {
                Column {
                    Text("Your access token has been created. Please copy and save it securely.")
                    Spacer(Modifier.height(16.dp))
                    Text("Token:", style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
                    Text(
                        text = uiState.createdToken ?: "",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Expires:", style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
                    Text(
                        text = uiState.tokenExpiry ?: "Unknown",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Zkopírovat token do schránky
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Splunk Token", uiState.createdToken)
                        clipboard.setPrimaryClip(clip)

                        viewModel.acknowledgeToken()
                        onLoginSuccess()
                    }
                ) {
                    Text("Copy & Continue")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.acknowledgeToken()
                        onLoginSuccess()
                    }
                ) {
                    Text("Continue")
                }
            }
        )
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Login to Splunk", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("Base URL") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            if (uiState.error != null) {
                Text(
                    text = "Error: ${uiState.error}",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
            }

            PrimaryButton(
                text = "Create Token & Login",
                onClick = { viewModel.login(baseUrl, username, password) },
                enabled = !uiState.isLoading && baseUrl.isNotBlank() && username.isNotBlank() && password.isNotBlank()
            )
        }
    }
}
