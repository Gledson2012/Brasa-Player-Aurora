package com.example.data.model

data class EqualizerPreset(
    val id: String,
    val name: String,
    val bandLevels: List<Int>, // 5 bands in dB (-10 to +10)
    val bassBoost: Int = 0, // 0 to 100
    val virtualizer: Int = 0, // 0 to 100
    val isCustom: Boolean = false
)

data class EqualizerState(
    val isEnabled: Boolean = true,
    val currentPresetId: String = "flat",
    val bandLevels: List<Int> = listOf(0, 0, 0, 0, 0), // 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz
    val bassBoost: Int = 30, // 0 to 100%
    val virtualizer: Int = 20, // 0 to 100%
    val balance: Float = 0.0f, // -1.0f (Left) to +1.0f (Right)
    val customPresets: List<EqualizerPreset> = emptyList()
) {
    companion object {
        val DEFAULT_PRESETS = listOf(
            EqualizerPreset("flat", "Normal", listOf(0, 0, 0, 0, 0), 0, 0),
            EqualizerPreset("headphones", "Fones Balanceados", listOf(2, 1, 0, 2, 3), 15, 10),
            EqualizerPreset("clarity", "Clareza Vocal", listOf(-2, 0, 3, 4, 2), 0, 5),
            EqualizerPreset("podcast", "Podcast", listOf(-5, -1, 5, 3, -2), 0, 0),
            EqualizerPreset("bass_heavy", "Super Graves", listOf(8, 6, 1, -1, 0), 80, 20),
            EqualizerPreset("rock", "Rock & Metal", listOf(5, 3, -1, 3, 6), 40, 30),
            EqualizerPreset("pop", "Pop Vibrante", listOf(-1, 2, 5, 2, -2), 30, 40),
            EqualizerPreset("electronic", "Eletrônica / EDM", listOf(6, 4, 0, 3, 5), 60, 50),
            EqualizerPreset("jazz", "Jazz & Blues", listOf(3, 1, 2, 2, 4), 20, 40),
            EqualizerPreset("vocal", "Realce Vocal", listOf(-3, 1, 6, 4, 1), 10, 30),
            EqualizerPreset("acoustic", "Acústico", listOf(4, 2, 1, 3, 4), 20, 25),
            EqualizerPreset("classical", "Clássico", listOf(4, 3, 0, 2, 3), 10, 50)
        )
    }
}
