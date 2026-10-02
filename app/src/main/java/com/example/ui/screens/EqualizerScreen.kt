package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EqualizerPreset
import com.example.data.model.EqualizerState
import com.example.ui.components.EqualizerBandSkeleton
import com.example.ui.components.EqualizerCurveGraph
import com.example.ui.components.SavePresetDialog
import com.example.ui.components.VisualizerView
import com.example.ui.components.hapticTick
import kotlin.math.abs

val BAND_LABELS = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
val BAND_NAMES = listOf("Sub-Grave", "Grave", "Médios", "Médio-Agudo", "Agudo")

val BAND_COLORS = listOf(
    Color(0xFFAB47BC), // 60 Hz Sub - Purple
    Color(0xFFFF7043), // 230 Hz Bass - Coral
    Color(0xFF26A69A), // 910 Hz Mid - Teal
    Color(0xFF29B6F6), // 3.6 kHz Mid-Treble - Cyan
    Color(0xFFEC407A)  // 14 kHz Treble - Pink
)

val BAND_DETAILS = listOf(
    "Batidas, sub-graves e peso físico",
    "Baixo, corpo e calor harmônico",
    "Vocais principais e instrumentos",
    "Presença vocal, ataque e articulação",
    "Brilho, pratos e ar espacial"
)

private enum class PresetCategory(val label: String) {
    ALL("Todos"),
    GENRES("Gêneros"),
    PROFILES("Dispositivos"),
    CUSTOM("Personalizados")
}

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
    var selectedCategory by remember { mutableStateOf(PresetCategory.ALL) }
    val context = LocalContext.current

    val allPresets = remember(equalizerState.customPresets) {
        EqualizerState.DEFAULT_PRESETS + equalizerState.customPresets
    }

    val selectedPreset = allPresets.firstOrNull { it.id == equalizerState.currentPresetId }

    val genrePresetIds = remember {
        setOf("rock", "pop", "electronic", "jazz", "acoustic", "classical", "lofi")
    }
    val profilePresetIds = remember {
        setOf("flat", "headphones", "clarity", "podcast", "bass_heavy", "vocal", "hi_fi", "cinema", "gaming", "night")
    }

    val filteredPresets = remember(allPresets, selectedCategory) {
        when (selectedCategory) {
            PresetCategory.ALL -> allPresets
            PresetCategory.GENRES -> allPresets.filter { it.id in genrePresetIds }
            PresetCategory.PROFILES -> allPresets.filter { it.id in profilePresetIds }
            PresetCategory.CUSTOM -> allPresets.filter { it.isCustom || it.id.startsWith("custom_") }
        }
    }

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
        // ───────────────────────────────────────────────
        // 1. Sleek Modern Header Bar
        // ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                if (equalizerState.isEnabled) listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                ) else listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = if (equalizerState.isEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "Equalizador",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (equalizerState.isEnabled) "DSP Ativo • 5 Bandas & Efeitos" else "Desativado • Som Original (Bypass)",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (equalizerState.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick A/B Bypass Button
            FilledTonalButton(
                onClick = {
                    context.hapticTick()
                    onToggleEnabled(!equalizerState.isEnabled)
                },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (equalizerState.isEnabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (equalizerState.isEnabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = if (equalizerState.isEnabled) Icons.Default.AutoAwesome else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (equalizerState.isEnabled) "A/B Ativo" else "Bypass",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ───────────────────────────────────────────────
        // 2. Master DSP Processing & Headroom Card
        // ───────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (equalizerState.isEnabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            border = if (equalizerState.isEnabled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
            else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (equalizerState.isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Equalizer,
                                contentDescription = null,
                                tint = if (equalizerState.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Processamento de áudio DSP",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (equalizerState.isEnabled) "Equalização e efeitos ativos em tempo real" else "Bypass total (áudio puro original)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = equalizerState.isEnabled,
                        onCheckedChange = {
                            context.hapticTick()
                            onToggleEnabled(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("equalizer_switch")
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    thickness = 0.8.dp
                )

                // Headroom and Limiter Protection Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clipping_protection_card")
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

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
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ───────────────────────────────────────────────
        // 3. Audio Response Curve & Spectrum Visualizer
        // ───────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "CURVA DE RESPOSTA DSP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        )
                        Text(
                            text = if (isPlaying) "Reproduzindo" else "Pausado",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Smooth Graphic Equalizer Curve with Bezier Smoothing
                EqualizerCurveGraph(
                    bandLevels = equalizerState.bandLevels,
                    isEnabled = equalizerState.isEnabled,
                    height = 95.dp,
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryColor = MaterialTheme.colorScheme.secondary
                )

                // Frequency labels at the bottom of the curve
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BAND_LABELS.forEachIndexed { idx, freq ->
                        Text(
                            text = freq,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (equalizerState.isEnabled) BAND_COLORS.getOrElse(idx) { MaterialTheme.colorScheme.onSurfaceVariant }
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    }
                }

                // Audio Spectrum Real-time Visualizer
                VisualizerView(
                    amplitudes = visualizerAmplitudes,
                    isPlaying = isPlaying,
                    height = 40.dp,
                    barCount = 30,
                    primaryColor = MaterialTheme.colorScheme.primary,
                    secondaryColor = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ───────────────────────────────────────────────
        // 4. Presets Carousel with Category Filter
        // ───────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Presets de Equalização",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${allPresets.size} perfis disponíveis",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(
                onClick = {
                    context.hapticTick()
                    showSaveDialog = true
                },
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

        Spacer(modifier = Modifier.height(8.dp))

        // Preset Category Filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetCategory.entries.forEach { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        context.hapticTick()
                        selectedCategory = category
                    },
                    label = {
                        Text(
                            text = category.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Presets Chips Carousel (Horizontal Scroll Row)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("presets_row")
        ) {
            filteredPresets.forEach { preset ->
                val isSelected = equalizerState.currentPresetId == preset.id
                val isCustom = preset.isCustom || preset.id.startsWith("custom_")

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        context.hapticTick()
                        onSelectPreset(preset)
                    },
                    modifier = Modifier.testTag("preset_chip_${preset.id}"),
                    enabled = equalizerState.isEnabled,
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCustom) {
                                Icon(
                                    imageVector = Icons.Outlined.BookmarkBorder,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .padding(end = 2.dp),
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
                                onClick = {
                                    context.hapticTick()
                                    onDeleteCustomPreset(preset)
                                },
                                modifier = Modifier
                                    .size(20.dp)
                                    .testTag("delete_preset_${preset.id}")
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
                    shape = RoundedCornerShape(14.dp)
                )
            }
        }

        // Active Preset Card Summary
        selectedPreset?.let { preset ->
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.24f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
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

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (preset.isCustom) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Personalizado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = preset.description.ifBlank { "Ajuste personalizado salvo por você" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
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

        // ───────────────────────────────────────────────
        // 5. 5-Band Frequency Sliders
        // ───────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Bandas de Frequência",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Toque em +/- para ajuste fino ou no valor para zerar",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Quick Zero All Bands
            if (equalizerState.bandLevels.any { it != 0 } && equalizerState.isEnabled) {
                TextButton(
                    onClick = {
                        context.hapticTick()
                        for (i in 0 until 5) {
                            onBandGainChange(i, 0)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Zerar Bandas",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
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
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    equalizerState.bandLevels.forEachIndexed { index, gainDb ->
                        val label = BAND_LABELS.getOrElse(index) { "Banda $index" }
                        val desc = BAND_NAMES.getOrElse(index) { "" }
                        val detail = BAND_DETAILS.getOrElse(index) { "" }
                        val bandColor = BAND_COLORS.getOrElse(index) { MaterialTheme.colorScheme.primary }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .testTag("frequency_band_$index")
                        ) {
                            // Band Title, Details & Value Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (equalizerState.isEnabled) bandColor.copy(alpha = 0.16f)
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .padding(horizontal = 7.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (equalizerState.isEnabled) bandColor else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = detail,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (gainDb > 0) "+$gainDb dB" else "$gainDb dB",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            !equalizerState.isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant
                                            gainDb > 0 -> bandColor
                                            gainDb < 0 -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )

                                    if (gainDb != 0 && equalizerState.isEnabled) {
                                        Text(
                                            text = "0 dB",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                                .clickable {
                                                    context.hapticTick()
                                                    onBandGainChange(index, 0)
                                                }
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Micro-step Buttons and Slider Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // -1 dB Button
                                IconButton(
                                    onClick = {
                                        context.hapticTick()
                                        onBandGainChange(index, (gainDb - 1).coerceAtLeast(-10))
                                    },
                                    enabled = equalizerState.isEnabled && gainDb > -10,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Diminuir 1 dB",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (equalizerState.isEnabled && gainDb > -10) bandColor
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                }

                                // Interactive Slider
                                Slider(
                                    value = gainDb.toFloat(),
                                    onValueChange = { onBandGainChange(index, it.toInt()) },
                                    valueRange = -10f..10f,
                                    steps = 19,
                                    enabled = equalizerState.isEnabled,
                                    colors = SliderDefaults.colors(
                                        thumbColor = bandColor,
                                        activeTrackColor = bandColor,
                                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("slider_band_$index")
                                )

                                // +1 dB Button
                                IconButton(
                                    onClick = {
                                        context.hapticTick()
                                        onBandGainChange(index, (gainDb + 1).coerceAtMost(10))
                                    },
                                    enabled = equalizerState.isEnabled && gainDb < 10,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Aumentar 1 dB",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (equalizerState.isEnabled && gainDb < 10) bandColor
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }

                            if (index < 4) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(top = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ───────────────────────────────────────────────
        // 6. Sound Effects & Acoustic Immersion
        // ───────────────────────────────────────────────
        Text(
            text = "Efeitos e Imersão Acústica",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Ajustes avançados para dar mais impacto, palco e equilíbrio ao áudio",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                // Bass Boost Effect
                EffectSliderRow(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Reforço de graves (Bass Boost)",
                    subtitle = "Acentua o impacto de subwoofers e fones",
                    valueLabel = "${equalizerState.bassBoost}%",
                    value = equalizerState.bassBoost.toFloat(),
                    valueRange = 0f..100f,
                    quickLevels = listOf(0, 25, 50, 75, 100),
                    onValueChange = { onBassBoostChange(it.toInt()) },
                    enabled = equalizerState.isEnabled,
                    testTag = "bass_boost_slider"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 36.dp, top = 8.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    thickness = 0.5.dp
                )

                // Virtualizer (Surround 3D) Effect
                EffectSliderRow(
                    icon = Icons.Default.SurroundSound,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "Surround 3D (Virtualizer)",
                    subtitle = "Expansão de palco estéreo e espacialidade",
                    valueLabel = "${equalizerState.virtualizer}%",
                    value = equalizerState.virtualizer.toFloat(),
                    valueRange = 0f..100f,
                    quickLevels = listOf(0, 25, 50, 75, 100),
                    onValueChange = { onVirtualizerChange(it.toInt()) },
                    enabled = equalizerState.isEnabled,
                    testTag = "virtualizer_slider"
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 36.dp, top = 8.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                    thickness = 0.5.dp
                )

                // Stereo Balance Slider with Centering Action
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Equalizer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Balanço de canal estéreo",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Equilíbrio entre canal esquerdo e direito",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = when {
                                    equalizerState.balance < -0.05f -> "E (${(abs(equalizerState.balance) * 100).toInt()}%)"
                                    equalizerState.balance > 0.05f -> "D (${(equalizerState.balance * 100).toInt()}%)"
                                    else -> "Centro"
                                },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Quick Center Button
                            if (abs(equalizerState.balance) > 0.02f && equalizerState.isEnabled) {
                                Text(
                                    text = "Centralizar",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                        .clickable {
                                            context.hapticTick()
                                            onBalanceChange(0f)
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Slider(
                        value = equalizerState.balance,
                        onValueChange = onBalanceChange,
                        valueRange = -1f..1f,
                        enabled = equalizerState.isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.testTag("balance_slider")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Esquerda", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Centro (0.0)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                        Text("Direita", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ───────────────────────────────────────────────
        // 7. Reset Equalizer CTA
        // ───────────────────────────────────────────────
        OutlinedButton(
            onClick = {
                context.hapticTick()
                onReset()
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reset_equalizer_button"),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Restaurar Equalizador Padrão (Flat)")
        }

        Spacer(modifier = Modifier.height(90.dp))
    }

    // Save Preset Dialog Modal
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
    subtitle: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    quickLevels: List<Int>,
    onValueChange: (Float) -> Unit,
    enabled: Boolean,
    testTag: String
) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

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
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.testTag(testTag)
        )

        // Quick Preset Level Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            quickLevels.forEach { lvl ->
                val isSelected = value.toInt() == lvl
                Text(
                    text = if (lvl == 0) "0%" else "$lvl%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected && enabled) iconTint else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isSelected && enabled) iconTint.copy(alpha = 0.14f)
                            else Color.Transparent
                        )
                        .clickable(enabled = enabled) {
                            context.hapticTick()
                            onValueChange(lvl.toFloat())
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
