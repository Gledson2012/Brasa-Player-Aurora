package com.example.ui.viewmodel.delegate

import com.example.audio.AudioPlayerEngine
import com.example.data.model.CustomPresetEntity
import com.example.data.model.EqualizerPreset
import com.example.data.model.EqualizerState
import com.example.data.repository.MusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Encapsulates all equalizer-related operations: state management,
 * preset selection, band level adjustments, bass boost, virtualizer,
 * balance, and custom preset persistence.
 */
class EqualizerController(
    private val repository: MusicRepository,
    private val playerEngine: AudioPlayerEngine,
    private val scope: CoroutineScope
) {
    private val _equalizerState = MutableStateFlow(EqualizerState())
    val equalizerState: StateFlow<EqualizerState> = _equalizerState.asStateFlow()

    /** Callback to trigger user settings persistence. */
    var onSettingsChanged: (() -> Unit)? = null

    fun updateFromSettings(settings: com.example.data.model.UserSettingsEntity) {
        val eq = EqualizerState(
            isEnabled = settings.equalizerEnabled,
            currentPresetId = settings.currentPresetId,
            bandLevels = listOf(
                settings.band0, settings.band1, settings.band2,
                settings.band3, settings.band4
            ).map { it.coerceIn(-10, 10) },
            bassBoost = settings.bassBoost.coerceIn(0, 100),
            virtualizer = settings.virtualizer.coerceIn(0, 100),
            balance = settings.balance.coerceIn(-1f, 1f),
            customPresets = _equalizerState.value.customPresets
        )
        _equalizerState.value = eq
        playerEngine.syncEqualizer(eq)
    }

    fun updateCustomPresets(customList: List<com.example.data.model.CustomPresetEntity>) {
        val converted = customList.map {
            EqualizerPreset(
                id = "custom_${it.id}",
                name = it.name,
                bandLevels = listOf(it.band0, it.band1, it.band2, it.band3, it.band4),
                bassBoost = it.bassBoost,
                virtualizer = it.virtualizer,
                isCustom = true
            )
        }
        _equalizerState.value = _equalizerState.value.copy(customPresets = converted)
    }

    // --- Toggle & Presets ---
    fun toggleEqualizer(enabled: Boolean) {
        val updated = _equalizerState.value.copy(isEnabled = enabled)
        _equalizerState.value = updated
        playerEngine.syncEqualizer(updated)
        onSettingsChanged?.invoke()
    }

    fun selectPreset(preset: EqualizerPreset) {
        val updated = _equalizerState.value.copy(
            currentPresetId = preset.id,
            bandLevels = preset.bandLevels.map { it.coerceIn(-10, 10) },
            bassBoost = preset.bassBoost.coerceIn(0, 100),
            virtualizer = preset.virtualizer.coerceIn(0, 100)
        )
        _equalizerState.value = updated
        playerEngine.syncEqualizer(updated)
        onSettingsChanged?.invoke()
    }

    // --- Band Levels ---
    fun setBandLevel(bandIndex: Int, level: Int) {
        val currentBands = _equalizerState.value.bandLevels.toMutableList()
        if (bandIndex in currentBands.indices) {
            currentBands[bandIndex] = level.coerceIn(-10, 10)
            val updated = _equalizerState.value.copy(
                bandLevels = currentBands,
                currentPresetId = "custom_user"
            )
            _equalizerState.value = updated
            playerEngine.syncEqualizer(updated)
            onSettingsChanged?.invoke()
        }
    }

    fun resetEqualizer() {
        val resetState = EqualizerState(
            customPresets = _equalizerState.value.customPresets
        )
        _equalizerState.value = resetState
        playerEngine.syncEqualizer(resetState)
        onSettingsChanged?.invoke()
    }

    // --- Effects ---
    fun setBassBoost(value: Int) {
        val updated = _equalizerState.value.copy(bassBoost = value.coerceIn(0, 100))
        _equalizerState.value = updated
        playerEngine.syncEqualizer(updated)
        onSettingsChanged?.invoke()
    }

    fun setVirtualizer(value: Int) {
        val updated = _equalizerState.value.copy(virtualizer = value.coerceIn(0, 100))
        _equalizerState.value = updated
        playerEngine.syncEqualizer(updated)
        onSettingsChanged?.invoke()
    }

    fun setBalance(value: Float) {
        val updated = _equalizerState.value.copy(balance = value.coerceIn(-1f, 1f))
        _equalizerState.value = updated
        playerEngine.syncEqualizer(updated)
        onSettingsChanged?.invoke()
    }

    // --- Custom Presets ---
    fun saveCustomPreset(name: String) {
        scope.launch {
            val cleanName = name.trim().take(100)
            if (cleanName.isBlank()) return@launch
            val currentEq = _equalizerState.value
            val bands = currentEq.bandLevels
            val entity = CustomPresetEntity(
                name = cleanName,
                band0 = bands.getOrElse(0) { 0 }.coerceIn(-10, 10),
                band1 = bands.getOrElse(1) { 0 }.coerceIn(-10, 10),
                band2 = bands.getOrElse(2) { 0 }.coerceIn(-10, 10),
                band3 = bands.getOrElse(3) { 0 }.coerceIn(-10, 10),
                band4 = bands.getOrElse(4) { 0 }.coerceIn(-10, 10),
                bassBoost = currentEq.bassBoost.coerceIn(0, 100),
                virtualizer = currentEq.virtualizer.coerceIn(0, 100)
            )
            repository.saveCustomPreset(entity)
        }
    }

    fun deleteCustomPreset(presetId: String) {
        scope.launch {
            val longId = presetId.removePrefix("custom_").toLongOrNull()
            if (longId != null) {
                repository.deleteCustomPreset(longId)
            }
        }
    }
}
