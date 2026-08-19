package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.model.LastFmSettings

@Composable
fun LastFmSettingsDialog(
    settings: LastFmSettings,
    message: String?,
    authUrl: String?,
    pendingScrobbleCount: Int = 0,
    onDismiss: () -> Unit,
    onSaveCredentials: (String, String) -> Unit,
    onRequestAuthorization: () -> Unit,
    onOpenAuthorization: (String) -> Unit,
    onCompleteAuthorization: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onDisconnect: () -> Unit,
    onClearPendingScrobbles: () -> Unit = {},
    onProcessPendingScrobbles: () -> Unit = {}
) {
    var apiKey by remember(settings.apiKey) { mutableStateOf(settings.apiKey) }
    var apiSecret by remember(settings.apiSecret) { mutableStateOf(settings.apiSecret) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Last.fm Scrobbling") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Crie uma aplicação em last.fm/api/account/create e informe as credenciais dela.")
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API key") },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = apiSecret,
                    onValueChange = { apiSecret = it },
                    label = { Text("API secret") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                Button(
                    onClick = { onSaveCredentials(apiKey, apiSecret) },
                    modifier = Modifier.padding(top = 10.dp)
                ) { Text("Salvar credenciais") }

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

                Text(
                    text = if (settings.isAuthenticated) "Conectado como ${settings.username}" else "Não conectado"
                )
                Button(
                    onClick = onRequestAuthorization,
                    modifier = Modifier.padding(top = 8.dp)
                ) { Text("Solicitar autorização") }
                if (authUrl != null) {
                    OutlinedButton(
                        onClick = { onOpenAuthorization(authUrl) },
                        modifier = Modifier.padding(top = 6.dp)
                    ) { Text("Abrir autorização no navegador") }
                    Button(
                        onClick = onCompleteAuthorization,
                        modifier = Modifier.padding(top = 6.dp)
                    ) { Text("Concluir login") }
                }
                if (settings.isAuthenticated) {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                    ) {
                        Text("Enviar scrobbles")
                        Switch(
                            checked = settings.enabled,
                            onCheckedChange = onToggleEnabled,
                            modifier = Modifier.testTag("lastfm_enabled_switch")
                        )
                    }
                    TextButton(onClick = onDisconnect) { Text("Desconectar") }
                }
                // Pending scrobbles section
                if (settings.isAuthenticated && settings.enabled) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))
                    Text(
                        text = if (pendingScrobbleCount > 0) 
                            "Scrobbles pendentes: $pendingScrobbleCount"
                        else 
                            "Fila de scrobbles vazia",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (pendingScrobbleCount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onProcessPendingScrobbles,
                                modifier = Modifier.weight(1f)
                            ) { Text("Enviar agora") }
                            OutlinedButton(
                                onClick = onClearPendingScrobbles,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) { Text("Limpar fila") }
                        }
                    }
                }

                if (message != null) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}
