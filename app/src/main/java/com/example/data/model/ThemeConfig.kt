package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class ThemeMode(
    val title: String,
    val description: String
) {
    SYSTEM(
        title = "Padrão do Sistema",
        description = "Alterna automaticamente conforme o tema do Android"
    ),
    LIGHT(
        title = "Modo Claro",
        description = "Interface luminosa com alta legibilidade diurna"
    ),
    DARK(
        title = "Modo Escuro",
        description = "Preto imersivo para telas OLED e conforto noturno"
    ),
    CUSTOM(
        title = "Personalizado",
        description = "Crie sua própria paleta de cores e contraste"
    )
}

enum class AppThemeType(
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val surfaceDark: Color,
    val tertiaryColor: Color,
    val isDarkPreset: Boolean = true
) {
    MIDNIGHT_OLED(
        title = "Midnight OLED",
        subtitle = "Preto profundo com roxo neon",
        primaryColor = Color(0xFF9D4EDD),
        secondaryColor = Color(0xFF00F0FF),
        surfaceDark = Color(0xFF07050B),
        tertiaryColor = Color(0xFFFF007F),
        isDarkPreset = true
    ),
    CYBERPUNK_NEON(
        title = "Cyberpunk",
        subtitle = "Rosa neon e ciano futurista",
        primaryColor = Color(0xFFFF007F),
        secondaryColor = Color(0xFF00E5FF),
        surfaceDark = Color(0xFF0F0B1E),
        tertiaryColor = Color(0xFFFFD600),
        isDarkPreset = true
    ),
    SUNSET_GLOW(
        title = "Sunset Glow",
        subtitle = "Tons quentes de âmbar e coral",
        primaryColor = Color(0xFFFF6B35),
        secondaryColor = Color(0xFFFFD166),
        surfaceDark = Color(0xFF140F12),
        tertiaryColor = Color(0xFFFF5964),
        isDarkPreset = true
    ),
    EMERALD_FOREST(
        title = "Emerald Forest",
        subtitle = "Verde esmeralda e menta viva",
        primaryColor = Color(0xFF00E676),
        secondaryColor = Color(0xFF1DE9B6),
        surfaceDark = Color(0xFF091410),
        tertiaryColor = Color(0xFF76FF03),
        isDarkPreset = true
    ),
    SAPPHIRE_BLUE(
        title = "Sapphire Wave",
        subtitle = "Azul elétrico e cobalto puro",
        primaryColor = Color(0xFF2979FF),
        secondaryColor = Color(0xFF00B0FF),
        surfaceDark = Color(0xFF080F1E),
        tertiaryColor = Color(0xFF7C4DFF),
        isDarkPreset = true
    ),
    RETRO_SYNTHWAVE(
        title = "Retro Synthwave",
        subtitle = "Estética anos 80 magenta & laranja",
        primaryColor = Color(0xFFE040FB),
        secondaryColor = Color(0xFFFF5252),
        surfaceDark = Color(0xFF160924),
        tertiaryColor = Color(0xFF00E5FF),
        isDarkPreset = true
    ),
    AMOLED_PITCH_BLACK(
        title = "AMOLED Pitch Black",
        subtitle = "Preto 100% absoluto com verde menta",
        primaryColor = Color(0xFF00F5D4),
        secondaryColor = Color(0xFF7B2CBF),
        surfaceDark = Color(0xFF000000),
        tertiaryColor = Color(0xFFFFD166),
        isDarkPreset = true
    ),
    ROSE_GOLD(
        title = "Rosé & Velvet",
        subtitle = "Ouro rosa refinado com bordô luxuoso",
        primaryColor = Color(0xFFF72585),
        secondaryColor = Color(0xFFB5179E),
        surfaceDark = Color(0xFF150811),
        tertiaryColor = Color(0xFFFFD166),
        isDarkPreset = true
    ),
    NORDIC_FROST(
        title = "Nordic Frost",
        subtitle = "Azul glacial ártico e cinza titânio",
        primaryColor = Color(0xFF38BDF8),
        secondaryColor = Color(0xFF818CF8),
        surfaceDark = Color(0xFF0C1322),
        tertiaryColor = Color(0xFFBAE6FD),
        isDarkPreset = true
    ),
    AURORA_MINT(
        title = "Aurora Mint",
        subtitle = "Verde aurora com limão elétrico",
        primaryColor = Color(0xFF2DD4BF),
        secondaryColor = Color(0xFFA3E635),
        surfaceDark = Color(0xFF081413),
        tertiaryColor = Color(0xFF7CFFCB),
        isDarkPreset = true
    ),
    VOLCANIC_RED(
        title = "Volcanic Red",
        subtitle = "Vermelho intenso com dourado quente",
        primaryColor = Color(0xFFFF4D6D),
        secondaryColor = Color(0xFFFFB703),
        surfaceDark = Color(0xFF1A0B0E),
        tertiaryColor = Color(0xFFFF8A65),
        isDarkPreset = true
    ),
    MINIMALIST_LIGHT(
        title = "Studio Pure Light",
        subtitle = "Claro com contraste nítido e cobalto",
        primaryColor = Color(0xFF4338CA),
        secondaryColor = Color(0xFF06B6D4),
        surfaceDark = Color(0xFFF8FAFC),
        tertiaryColor = Color(0xFF0284C7),
        isDarkPreset = false
    ),
    PAPER_CREAM(
        title = "Paper & Teal",
        subtitle = "Creme confortável com verde petróleo",
        primaryColor = Color(0xFFA16207),
        secondaryColor = Color(0xFF0F766E),
        surfaceDark = Color(0xFFFAF8F0),
        tertiaryColor = Color(0xFFB45309),
        isDarkPreset = false
    ),
    OBSIDIAN_GOLD(
        title = "Obsidian & Gold",
        subtitle = "Grafite profundo com âmbar premium",
        primaryColor = Color(0xFFFFC857),
        secondaryColor = Color(0xFFD97706),
        surfaceDark = Color(0xFF0F1014),
        tertiaryColor = Color(0xFFA78BFA),
        isDarkPreset = true
    ),
    OCEANIC_TEAL(
        title = "Oceanic Teal",
        subtitle = "Azul oceano com aqua sofisticado",
        primaryColor = Color(0xFF22D3EE),
        secondaryColor = Color(0xFF14B8A6),
        surfaceDark = Color(0xFF061316),
        tertiaryColor = Color(0xFF60A5FA),
        isDarkPreset = true
    ),
    LAVENDER_STUDIO(
        title = "Lavender Studio",
        subtitle = "Lavanda suave com azul pervinca",
        primaryColor = Color(0xFF6D5EF5),
        secondaryColor = Color(0xFF4F8CFF),
        surfaceDark = Color(0xFFF8F7FF),
        tertiaryColor = Color(0xFFD946EF),
        isDarkPreset = false
    )
}

data class CustomThemeConfig(
    val primaryColorVal: Long = 0xFF9D4EDDL,
    val secondaryColorVal: Long = 0xFF00F0FFL,
    val tertiaryColorVal: Long = 0xFFFF007FL,
    val surfaceColorVal: Long = 0xFF140F22L,
    val backgroundColorVal: Long = 0xFF080512L,
    val isDark: Boolean = true
) {
    val primaryColor: Color get() = Color(primaryColorVal)
    val secondaryColor: Color get() = Color(secondaryColorVal)
    val tertiaryColor: Color get() = Color(tertiaryColorVal)
    val surfaceColor: Color get() = Color(surfaceColorVal)
    val backgroundColor: Color get() = Color(backgroundColorVal)
}

enum class VisualizerStyle(val title: String) {
    BARS("Barras de Frequência"),
    WAVEFORM("Onda Sonora Suave"),
    CIRCULAR_PULSE("Pulso Circular"),
    SPECTRUM("Espectro Completo"),
    MIRRORED_BARS("Barras Espelhadas"),
    DOT_MATRIX("Matriz de Pontos"),
    ORBITAL("Órbita Neon"),
    RADIAL_BURST("Explosão Radial")
}

enum class AlbumArtStyle(val title: String) {
    VINYL_ROTATION("Vinil Giratório"),
    CARD_ROUNDED("Card Moderno"),
    FULLSCREEN_GLOW("Glow Dinâmico"),
    POLAROID_FRAME("Moldura Polaroid"),
    GLASSMORPHIC("Vidro Translúcido"),
    NEON_RING("Anel Neon")
}

data class ThemeConfig(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val presetTheme: AppThemeType = AppThemeType.MIDNIGHT_OLED,
    val customTheme: CustomThemeConfig = CustomThemeConfig(),
    val dynamicColors: Boolean = false,
    val visualizerStyle: VisualizerStyle = VisualizerStyle.BARS,
    val albumArtStyle: AlbumArtStyle = AlbumArtStyle.VINYL_ROTATION
)

// Legacy alias for compatibility
typealias ThemeSettings = ThemeConfig
