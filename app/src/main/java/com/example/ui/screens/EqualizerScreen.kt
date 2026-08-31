package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EqualizerPreset
import com.example.data.model.EqualizerState
import com.example.ui.components.EqualizerBandSkeleton
import com.example.ui.components.EqualizerCurveGraph
import com.example.ui.components.SavePresetDialog
import com.example.ui.components.SectionHeader
import com.example.ui.components.VisualizerView
import com.example.ui.components.hapticTick

val BAND_LABELS = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
val BAND_NAMES = listOf("Sub-Grave", "Grave", "Médios", "Médio-Agudo", "Agudo")

@Composable
fun EqualizerScreen(
    equalizerState: EqualizerState,
    isLoading: Boolean = false,
    isPlaying: Boolean,
    visualizerAmplitudes: FloatArray,
    onToggleEnabled: (Boolean) -> Unit,
    onSelectPreset: (EqualizerPreset) -> Unit,
    onBandGainChange: (bandIndex: Int, gainDb: Int) -> Unit,
    onBassBoostChange: (Int) -> Unit,
    onVirtualizerChange: (Int) -> Unit,
    onBalanceChange: (Float) -> Unit,
    onReset: () -> Unit,
    onSaveCustomPreset: (name: String) -> Unit = {},
    onDeleteCustomPreset: (EqualizerPreset) -> Unit = {}
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("equalizer_screen")
    ) {
        SectionHeader(
            title = "Equalizador",
            subtitle = "Modele o som e salve seus próprios perfis",
            icon = Icons.Default.GraphicEq
        )

        // Processing status and protection grouped into one control panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (equalizerState.isEnabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = if (equalizerState.isEnabled) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) else null
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = if (equalizerState.isEnabled) listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                        else listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = if (equalizerState.isEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Processamento de áudio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (equalizerState.isEnabled) "Efeitos DSP em tempo real" else "Desativado (som original)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = equalizerState.isEnabled,
                        onCheckedChange = { context.hapticTick(); onToggleEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("equalizer_switch")
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clipping_protection_card")
                        .padding(horizontal = 18.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Proteção contra distorção",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Headroom automático e limiter suave ativos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    }
                    Text(
                        text = "ATIVO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Curve Graph & Spectrum Visualizer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "RESPOSTA EM FREQUÊNCIA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Text(
                        text = if (isPlaying) "Reproduzindo" else "Pausado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Smooth Graphic Equalizer Curve
                EqualizerCurveGraph(
                    bandLevels = equalizerState.bandLevels,
                    isEnabled = equalizerState.isEnabled,
                    height = 95.dp,
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryColor = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Audio Spectrum
                VisualizerView(
                    amplitudes = visualizerAmplitudes,
                    isPlaying = isPlaying,
                    height = 42.dp,
                    barCount = 28,
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryColor = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Presets Header with Save Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Presets de Equalização",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = { context.hapticTick(); showSaveDialog = true },
                enabled = equalizerState.isEnabled,
                modifier = Modifier.testTag("save_preset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Salvar Atual", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Presets Chips Carousel (Built-in + Custom from Room Database)
        val allPresets = EqualizerState.DEFAULT_PRESETS + equalizerState.customPresets
        val selectedPreset = allPresets.firstOrNull { it.id == equalizerState.currentPresetId }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("presets_row")
        ) {
            items(allPresets, key = { it.id }) { preset ->
                val isSelected = equalizerState.currentPresetId == preset.id
                val isCustom = preset.isCustom || preset.id.startsWith("custom_")

                FilterChip(
                    selected = isSelected,
                    onClick = { context.hapticTick(); onSelectPreset(preset) },
                    enabled = equalizerState.isEnabled,
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCustom) {
                                Icon(
                                    imageVector = Icons.Outlined.BookmarkBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp).padding(end = 2.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = preset.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    trailingIcon = if (isCustom) {
                        {
                            IconButton(
                                onClick = { context.hapticTick(); onDeleteCustomPreset(preset) },
                                modifier = Modifier.size(20.dp).testTag("delete_preset_${preset.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Excluir preset",
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        selectedPreset?.let { preset ->
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = preset.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = preset.description.ifBlank { "Ajuste personalizado salvo por você" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${preset.bassBoost}% graves",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${preset.virtualizer}% surround",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 5-Band Equalizer Sliders
        Text(
            text = "Bandas de Frequência",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Arraste os controles para ajustar; toque no valor para voltar a 0 dB.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            // Show skeleton loading while data loads
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(5) { index ->
                    EqualizerBandSkeleton(animDelay = index * 100)
                }
            }
        } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                equalizerState.bandLevels.forEachIndexed { index, gainDb ->
                    val label = BAND_LABELS.getOrElse(index) { "Banda $index" }
                    val desc = BAND_NAMES.getOrElse(index) { "" }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("frequency_band_$index")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• $desc",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (gainDb > 0) "+$gainDb dB" else "$gainDb dB",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (gainDb != 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (gainDb != 0 && equalizerState.isEnabled) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "0 dB",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                            .clickable { context.hapticTick(); onBandGainChange(index, 0) }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Slider(
                            value = gainDb.toFloat(),
                            onValueChange = { onBandGainChange(index, it.toInt()) },
                            valueRange = -10f..10f,
                            steps = 19,
                            enabled = equalizerState.isEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.testTag("slider_band_$index")
                        )                    }
                }
            }
        }
        } // end else

        Spacer(modifier = Modifier.height(16.dp))

        // Sound effects grouped in one panel to keep the tuning flow compact.
        Text(
            text = "Efeitos e imersão",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Ajustes extras para dar mais corpo e espaço ao áudio",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                EffectSliderRow(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Reforço de graves",
                    valueLabel = "${equalizerState.bassBoost}%",
                    value = equalizerState.bassBoost.toFloat(),
                    valueRange = 0f..100f,
                    onValueChange = { onBassBoostChange(it.toInt()) },
                    enabled = equalizerState.isEnabled,
                    testTag = "bass_boost_slider"
                )

                EffectDivider()

                EffectSliderRow(
                    icon = Icons.Default.SurroundSound,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "Surround 3D",
                    valueLabel = "${equalizerState.virtualizer}%",
                    value = equalizerState.virtualizer.toFloat(),
                    valueRange = 0f..100f,
                    onValueChange = { onVirtualizerChange(it.toInt()) },
                    enabled = equalizerState.isEnabled,
                    testTag = "virtualizer_slider"
                )

                EffectDivider()

                EffectSliderRow(
                    icon = Icons.Default.Equalizer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Balanço de canal",
                    valueLabel = when {
                        equalizerState.balance < -0.1f -> "E (${(kotlin.math.abs(equalizerState.balance) * 100).toInt()}%)"
                        equalizerState.balance > 0.1f -> "D (${(equalizerState.balance * 100).toInt()}%)"
                        else -> "Centro"
                    },
                    value = equalizerState.balance,
                    valueRange = -1f..1f,
                    onValueChange = onBalanceChange,
                    enabled = equalizerState.isEnabled,
                    testTag = "balance_slider"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 30.dp, end = 2.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Esquerda", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Centro", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Direita", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reset Button
        OutlinedButton(
            onClick = { context.hapticTick(); onReset() },
            modifier = Modifier.fillMaxWidth().testTag("reset_equalizer_button"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Restaurar Equalizador Padrão")
        }

        Spacer(modifier = Modifier.height(90.dp))
    }

    // Save Preset Dialog
    if (showSaveDialog) {
        SavePresetDialog(
            bandLevels = equalizerState.bandLevels,
            bassBoost = equalizerState.bassBoost,
            virtualizer = equalizerState.virtualizer,
            onDismiss = { showSaveDialog = false },
            onSave = { name ->
                onSaveCustomPreset(name)
                showSaveDialog = false
            }
        )
    }
}

@Composable
private fun EffectSliderRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    enabled: Boolean,
    testTag: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(modifier = Modifier.width(9.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = iconTint
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            enabled = enabled,
            colors = SliderDefaults.colors(
                thumbColor = iconTint,
                activeTrackColor = iconTint,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun EffectDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 39.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    )
}
