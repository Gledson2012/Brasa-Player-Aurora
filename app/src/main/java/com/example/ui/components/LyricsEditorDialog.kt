package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.Song

@Composable
fun LyricsEditorDialog(
    song: Song,
    initialLrc: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var content by remember(initialLrc) { mutableStateOf(initialLrc) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar letras") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "${song.title} • ${song.artist}",
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp)
                        .testTag("lyrics_editor_input"),
                    label = { Text("LRC sincronizado") },
                    placeholder = { Text("[00:12.00] Primeira linha") },
                    minLines = 9
                )
                Text(
                    text = "Use uma linha por verso no formato [mm:ss.xx] Texto.",
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(content) }, modifier = Modifier.testTag("save_lyrics_button")) {
                Text("Salvar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
