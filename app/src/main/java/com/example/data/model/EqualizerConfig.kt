package com.example.data.model

data class EqualizerPreset(
    val id: String,
    val name: String,
    val bandLevels: List<Int>, // 5 bands in dB (-10 to +10)
    val bassBoost: Int = 0, // 0 to 100
    val virtualizer: Int = 0, // 0 to 100
    val isCustom: Boolean = false,
    val description: String = ""
)

data class EqualizerState(
    val isEnabled: Boolean = true,
    val currentPresetId: String = "flat",
    val bandLevels: List<Int> = listOf(0, 0, 0, 0, 0), // 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz
    val bassBoost: Int = 0, // 0 to 100%
    val virtualizer: Int = 0, // 0 to 100%
    val balance: Float = 0.0f, // -1.0f (Left) to +1.0f (Right)
    val customPresets: List<EqualizerPreset> = emptyList()
) {
    companion object {
        val DEFAULT_PRESETS = listOf(
            EqualizerPreset("flat", "Referência", listOf(0, 0, 0, 0, 0), description = "Som neutro, sem coloração"),
            EqualizerPreset("headphones", "Fones Balanceados", listOf(2, 1, 0, 1, 2), 12, 8, description = "Compensação suave para fones e earbuds"),
            EqualizerPreset("clarity", "Clareza Vocal", listOf(-2, -1, 4, 5, 2), 0, 5, description = "Vozes e instrumentos centrais mais presentes"),
            EqualizerPreset("podcast", "Podcast", listOf(-6, -3, 5, 3, -2), 0, 0, description = "Fala clara com menos graves e ruído"),
            EqualizerPreset("bass_heavy", "Super Graves", listOf(6, 4, 0, -1, 1), 55, 15, description = "Impacto controlado para batidas e hip-hop"),
            EqualizerPreset("rock", "Rock & Metal", listOf(4, 2, -1, 3, 5), 35, 25, description = "Ataque de guitarras e bateria com energia"),
            EqualizerPreset("pop", "Pop Vibrante", listOf(-1, 1, 4, 2, -1), 25, 35, description = "Vocais à frente e refrões abertos"),
            EqualizerPreset("electronic", "Eletrônica / EDM", listOf(5, 4, 0, 3, 4), 50, 45, description = "Graves firmes e brilho para sintetizadores"),
            EqualizerPreset("jazz", "Jazz & Blues", listOf(2, 0, 2, 2, 3), 18, 30, description = "Timbre natural para metais, piano e contrabaixo"),
            EqualizerPreset("vocal", "Realce Vocal", listOf(-3, 0, 5, 4, 1), 5, 20, description = "Inteligibilidade para cantores e locução"),
            EqualizerPreset("acoustic", "Acústico", listOf(3, 1, 1, 3, 4), 18, 22, description = "Madeira, cordas e detalhes com naturalidade"),
            EqualizerPreset("classical", "Clássico", listOf(3, 2, 0, 2, 3), 10, 35, description = "Palco amplo sem perder equilíbrio"),
            EqualizerPreset("hi_fi", "Hi-Fi Natural", listOf(1, 0, 1, 1, 2), 8, 15, description = "Mais detalhe sem alterar o timbre"),
            EqualizerPreset("lofi", "Lo-fi Quente", listOf(3, 2, -2, -1, 1), 25, 5, description = "Textura macia para escuta confortável"),
            EqualizerPreset("cinema", "Cinema", listOf(5, 2, -1, 3, 5), 45, 65, description = "Palco amplo e impacto para filmes e séries"),
            EqualizerPreset("gaming", "Gaming", listOf(3, 1, 0, 4, 3), 20, 70, description = "Passos, efeitos e posicionamento espacial"),
            EqualizerPreset("night", "Escuta Noturna", listOf(2, 1, 1, 2, -2), 15, 0, description = "Som suave para ouvir em volume baixo")
        )
    }
}
