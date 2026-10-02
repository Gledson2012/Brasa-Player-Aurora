package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlbumArtStyle
import com.example.data.model.AppThemeType
import com.example.data.model.CustomThemeConfig
import com.example.data.model.ThemeConfig
import com.example.data.model.ThemeMode
import com.example.data.model.VisualizerStyle
import com.example.ui.components.hapticTick
import com.example.ui.theme.DynamicColorPreview
import com.example.ui.theme.extractDynamicColorPreview

// Curated Vibrant Colors for Custom Palette Builder
val CUSTOM_PRIMARY_PALETTE = listOf(
    Color(0xFF9D4EDD), // Neon Purple
    Color(0xFF00F0FF), // Cyber Cyan
    Color(0xFFFF007F), // Neon Pink
    Color(0xFFFF6B35), // Sunset Orange
    Color(0xFFFFD166), // Sunset Gold
    Color(0xFF00E676), // Emerald Mint
    Color(0xFF2979FF), // Sapphire Blue
    Color(0xFFE040FB), // Retro Magenta
    Color(0xFF00F5D4), // Mint Neon
    Color(0xFFF72585), // Rose Velvet
    Color(0xFFEF4444), // Crimson Red
    Color(0xFFA3E635)  // Electric Lime
)

val CUSTOM_SECONDARY_PALETTE = listOf(
    Color(0xFF00F0FF), // Cyber Cyan
    Color(0xFFFF007F), // Neon Pink
    Color(0xFFFFD166), // Sunset Gold
    Color(0xFF1DE9B6), // Mint Teal
    Color(0xFF00B0FF), // Sky Blue
    Color(0xFFFF5252), // Coral Red
    Color(0xFF7B2CBF), // Deep Violet
    Color(0xFF818CF8), // Indigo Soft
    Color(0xFF14B8A6), // Teal Modern
    Color(0xFFF59E0B)  // Amber Warm
)

val CUSTOM_TERTIARY_PALETTE = listOf(
    Color(0xFFFFD166), // Gold
    Color(0xFF7CFFCB), // Aurora
    Color(0xFFFF8A65), // Coral
    Color(0xFF818CF8), // Indigo
    Color(0xFFF9A8D4), // Rose
    Color(0xFFBEF264), // Lime
    Color(0xFFA78BFA), // Violet
    Color(0xFFFDE68A)  // Warm cream
)

val CUSTOM_SURFACE_DARK_PALETTE = listOf(
    Pair("OLED Puro", Color(0xFF000000) to Color(0xFF0B0B0E)),
    Pair("Noite Cósmica", Color(0xFF07050B) to Color(0xFF130E1F)),
    Pair("Cyber Dark", Color(0xFF0A0518) to Color(0xFF150D2E)),
    Pair("Bordô Luxo", Color(0xFF150811) to Color(0xFF230D1D)),
    Pair("Abismo Azul", Color(0xFF050C1B) to Color(0xFF0D1B36)),
    Pair("Floresta", Color(0xFF06120E) to Color(0xFF0E231C))
)

val CUSTOM_SURFACE_LIGHT_PALETTE = listOf(
    Pair("Studio Puro", Color(0xFFF8FAFC) to Color(0xFFFFFFFF)),
    Pair("Cinza Suave", Color(0xFFF1F5F9) to Color(0xFFFFFFFF)),
    Pair("Quente Neutro", Color(0xFFFAF8F5) to Color(0xFFFFFFFF)),
    Pair("Menta Claro", Color(0xFFF0FDF4) to Color(0xFFFFFFFF))
)

private enum class ThemePresetCategory(val label: String) {
    ALL("Todos (16)"),
    DARK("Escuro & OLED (13)"),
    LIGHT("Claro & Studio (3)")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemesScreen(
    themeConfig: ThemeConfig,
    crossfadeSeconds: Int,
    scanStatusMessage: String?,
    onSelectThemeMode: (ThemeMode) -> Unit,
    onSelectPresetTheme: (AppThemeType) -> Unit,
    onSaveCustomTheme: (primary: Color, secondary: Color, tertiary: Color, surface: Color, background: Color, isDark: Boolean) -> Unit,
    onToggleDynamicColors: (Boolean) -> Unit,
    onSelectVisualizerStyle: (VisualizerStyle) -> Unit,
    onSelectAlbumArtStyle: (AlbumArtStyle) -> Unit,
    onSetCrossfadeSeconds: (Int) -> Unit,
    onResetDefaults: () -> Unit,
    onScanLocalStorage: (Context) -> Unit,
    onImportAudioFile: (Context, android.net.Uri, String) -> Unit,
    onImportAudioFolder: (Context) -> Unit,
    onOpenLastFm: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    val context = LocalContext.current
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    var selectedPresetCategory by remember { mutableStateOf(ThemePresetCategory.ALL) }

    val activeModeLabel = when (themeConfig.themeMode) {
        ThemeMode.SYSTEM -> "Sistema"
        ThemeMode.LIGHT -> "Modo Claro"
        ThemeMode.DARK -> "Modo Escuro"
        ThemeMode.CUSTOM -> "Personalizado"
    }

    val activeThemeLabel = if (themeConfig.themeMode == ThemeMode.CUSTOM) {
        "Estúdio Customizado"
    } else {
        themeConfig.presetTheme.title
    }

    val activeThemePrimary = if (themeConfig.themeMode == ThemeMode.CUSTOM) {
        themeConfig.customTheme.primaryColor
    } else {
        themeConfig.presetTheme.primaryColor
    }

    val activeThemeSecondary = if (themeConfig.themeMode == ThemeMode.CUSTOM) {
        themeConfig.customTheme.secondaryColor
    } else {
        themeConfig.presetTheme.secondaryColor
    }

    val activeThemeTertiary = if (themeConfig.themeMode == ThemeMode.CUSTOM) {
        themeConfig.customTheme.tertiaryColor
    } else {
        themeConfig.presetTheme.tertiaryColor
    }

    // Custom Theme Builder Local State
    var customPrimary by remember(themeConfig.customTheme) { mutableStateOf(themeConfig.customTheme.primaryColor) }
    var customSecondary by remember(themeConfig.customTheme) { mutableStateOf(themeConfig.customTheme.secondaryColor) }
    var customTertiary by remember(themeConfig.customTheme) { mutableStateOf(themeConfig.customTheme.tertiaryColor) }
    var customIsDark by remember(themeConfig.customTheme) { mutableStateOf(themeConfig.customTheme.isDark) }
    var customSurface by remember(themeConfig.customTheme) { mutableStateOf(themeConfig.customTheme.surfaceColor) }
    var customBackground by remember(themeConfig.customTheme) { mutableStateOf(themeConfig.customTheme.backgroundColor) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val fileName = context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            } ?: uri.lastPathSegment ?: "Faixa Importada"
            onImportAudioFile(context, it, fileName)
        }
    }

    val filteredPresetThemes = remember(selectedPresetCategory) {
        when (selectedPresetCategory) {
            ThemePresetCategory.ALL -> AppThemeType.entries
            ThemePresetCategory.DARK -> AppThemeType.entries.filter { it.isDarkPreset }
            ThemePresetCategory.LIGHT -> AppThemeType.entries.filter { !it.isDarkPreset }
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
            .testTag("themes_screen")
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
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = "Temas & Estilo",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "16 temas exclusivos • Material You & Cores",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    context.hapticTick()
                    onResetDefaults()
                },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.testTag("reset_theme_button")
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Padrão",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ───────────────────────────────────────────────
        // 2. Active Theme Showcase Card
        // ───────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("active_theme_summary"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Visual Color Orb Swatches
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            activeThemePrimary,
                                            activeThemeSecondary,
                                            activeThemeTertiary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Aparência Ativa",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = activeThemeLabel,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = activeModeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Miniature Color Swatch Preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiniColorChip(color = activeThemePrimary, label = "Primária")
                    MiniColorChip(color = activeThemeSecondary, label = "Secundária")
                    MiniColorChip(color = activeThemeTertiary, label = "Acento")
                    if (themeConfig.dynamicColors) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Material You Ativo",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ───────────────────────────────────────────────
        // 3. Theme Mode Selector (System, Light, Dark, Custom)
        // ───────────────────────────────────────────────
        Text(
            text = "Modo de Aparência",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Escolha o comportamento de iluminação da interface",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeModeOptionCard(
                title = "Sistema",
                subtitle = "Automático",
                icon = Icons.Default.SettingsBrightness,
                isSelected = themeConfig.themeMode == ThemeMode.SYSTEM,
                modifier = Modifier.fillMaxWidth(0.48f),
                testTag = "theme_mode_system",
                onClick = { onSelectThemeMode(ThemeMode.SYSTEM) }
            )
            ThemeModeOptionCard(
                title = "Claro",
                subtitle = "Dia & Alta Luz",
                icon = Icons.Default.LightMode,
                isSelected = themeConfig.themeMode == ThemeMode.LIGHT,
                modifier = Modifier.fillMaxWidth(0.48f),
                testTag = "theme_mode_light",
                onClick = { onSelectThemeMode(ThemeMode.LIGHT) }
            )
            ThemeModeOptionCard(
                title = "Escuro",
                subtitle = "Preto OLED",
                icon = Icons.Default.DarkMode,
                isSelected = themeConfig.themeMode == ThemeMode.DARK,
                modifier = Modifier.fillMaxWidth(0.48f),
                testTag = "theme_mode_dark",
                onClick = { onSelectThemeMode(ThemeMode.DARK) }
            )
            ThemeModeOptionCard(
                title = "Custom",
                subtitle = "Estúdio de Cores",
                icon = Icons.Default.Brush,
                isSelected = themeConfig.themeMode == ThemeMode.CUSTOM,
                modifier = Modifier.fillMaxWidth(0.48f),
                testTag = "theme_mode_custom",
                onClick = { onSelectThemeMode(ThemeMode.CUSTOM) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ───────────────────────────────────────────────
        // 4. Material You (Dynamic Colors)
        // ───────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dynamic_colors_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Material You (Cores Dinâmicas)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (supportsDynamicColor) "Extrai cores suaves do papel de parede do Android"
                                else "Disponível a partir do Android 12 (API 31+)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = themeConfig.dynamicColors,
                        onCheckedChange = {
                            context.hapticTick()
                            onToggleDynamicColors(it)
                        },
                        enabled = supportsDynamicColor,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("dynamic_colors_switch")
                    )
                }

                // Dynamic Color Wallpaper Preview
                if (supportsDynamicColor && !themeConfig.dynamicColors) {
                    val colorPreview = remember { extractDynamicColorPreview(context) }
                    if (colorPreview != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        DynamicColorPreviewCard(colorPreview)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ───────────────────────────────────────────────
        // 5. Custom Theme Studio (Expandable when Custom is active)
        // ───────────────────────────────────────────────
        AnimatedVisibility(
            visible = themeConfig.themeMode == ThemeMode.CUSTOM,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_theme_studio_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ColorLens,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Estúdio de Tema Personalizado",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Misture cores neon e contrastes para seu player",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Contrast Base Selector
                        Text(
                            text = "1. Base de Contraste:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = customIsDark,
                                onClick = {
                                    context.hapticTick()
                                    customIsDark = true
                                    customBackground = CUSTOM_SURFACE_DARK_PALETTE[0].second.first
                                    customSurface = CUSTOM_SURFACE_DARK_PALETTE[0].second.second
                                },
                                label = { Text("Base Escura / OLED", fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = !customIsDark,
                                onClick = {
                                    context.hapticTick()
                                    customIsDark = false
                                    customBackground = CUSTOM_SURFACE_LIGHT_PALETTE[0].second.first
                                    customSurface = CUSTOM_SURFACE_LIGHT_PALETTE[0].second.second
                                },
                                label = { Text("Base Clara / Studio", fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Primary Color Picker
                        Text(
                            text = "2. Cor Primária de Destaque:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CUSTOM_PRIMARY_PALETTE.forEach { color ->
                                val isSelected = customPrimary == color
                                ColorOrb(
                                    color = color,
                                    isSelected = isSelected,
                                    onClick = {
                                        context.hapticTick()
                                        customPrimary = color
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Secondary Color Picker
                        Text(
                            text = "3. Cor Secundária (Glow & Acentos):",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CUSTOM_SECONDARY_PALETTE.forEach { color ->
                                val isSelected = customSecondary == color
                                ColorOrb(
                                    color = color,
                                    isSelected = isSelected,
                                    onClick = {
                                        context.hapticTick()
                                        customSecondary = color
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Tertiary Color Picker
                        Text(
                            text = "4. Cor Terciária (Status & Selos):",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CUSTOM_TERTIARY_PALETTE.forEach { color ->
                                val isSelected = customTertiary == color
                                ColorOrb(
                                    color = color,
                                    isSelected = isSelected,
                                    onClick = {
                                        context.hapticTick()
                                        customTertiary = color
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Surface & Background Palette Combo
                        Text(
                            text = "5. Fundo e Superfície de Cartões:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Combinações com contraste otimizado para legibilidade",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val surfacePalettes = if (customIsDark) CUSTOM_SURFACE_DARK_PALETTE else CUSTOM_SURFACE_LIGHT_PALETTE
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            surfacePalettes.forEach { palette ->
                                val paletteBackground = palette.second.first
                                val paletteSurface = palette.second.second
                                SurfacePaletteOption(
                                    name = palette.first,
                                    backgroundColor = paletteBackground,
                                    surfaceColor = paletteSurface,
                                    selected = customBackground == paletteBackground && customSurface == paletteSurface,
                                    onClick = {
                                        customBackground = paletteBackground
                                        customSurface = paletteSurface
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Live Mockup Preview
                        Text(
                            text = "Pré-visualização do Seu Tema:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = customSurface),
                            border = BorderStroke(1.dp, customPrimary.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Brush.linearGradient(listOf(customPrimary, customSecondary, customTertiary))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Brasa Player Aurora",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (customIsDark) Color.White else Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "Seu som, suas cores",
                                            fontSize = 12.sp,
                                            color = customPrimary
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(customTertiary.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Preview",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = customTertiary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                context.hapticTick()
                                onSaveCustomTheme(
                                    customPrimary,
                                    customSecondary,
                                    customTertiary,
                                    customSurface,
                                    customBackground,
                                    customIsDark
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_custom_theme_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = customPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salvar & Aplicar Tema Personalizado")
                        }
                    }
                }
            }
        }

        // ───────────────────────────────────────────────
        // 6. Preset Color Schemes (16 Curated Themes)
        // ───────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Paletas Prontas & Gradientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Design acústico com harmonia de cores refinada",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Preset Category Filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemePresetCategory.entries.forEach { category ->
                val isSelected = selectedPresetCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        context.hapticTick()
                        selectedPresetCategory = category
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

        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filteredPresetThemes.forEach { themeType ->
                val isSelected = themeConfig.themeMode != ThemeMode.CUSTOM && themeConfig.presetTheme == themeType

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.48f)
                        .clickable {
                            context.hapticTick()
                            onSelectPresetTheme(themeType)
                            if (themeConfig.themeMode == ThemeMode.CUSTOM) {
                                onSelectThemeMode(if (themeType.isDarkPreset) ThemeMode.DARK else ThemeMode.LIGHT)
                            }
                        }
                        .testTag("theme_option_${themeType.name}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            PresetThemePreview(themeType)
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selecionado",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = themeType.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!themeType.isDarkPreset) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "LIGHT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        Text(
                            text = themeType.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ───────────────────────────────────────────────
        // 7. Audio Crossfade (Transição entre músicas)
        // ───────────────────────────────────────────────
        Text(
            text = "Transição entre Músicas (Crossfade)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (crossfadeSeconds == 0) "Sem transição gradual (troca instantânea)"
            else "Suaviza a troca reduzindo o fim da faixa e introduzindo a próxima (${crossfadeSeconds}s)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(0, 3, 5, 8, 12).forEach { seconds ->
                FilterChip(
                    selected = crossfadeSeconds == seconds,
                    onClick = {
                        context.hapticTick()
                        onSetCrossfadeSeconds(seconds)
                    },
                    label = {
                        Text(
                            text = if (seconds == 0) "Off" else "${seconds}s",
                            fontSize = 12.sp,
                            fontWeight = if (crossfadeSeconds == seconds) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ───────────────────────────────────────────────
        // 8. Visualizer Style Picker
        // ───────────────────────────────────────────────
        Text(
            text = "Estilo do Visualizador de Áudio",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Animação gráfica na tela do player em reprodução",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VisualizerStyle.entries.forEach { style ->
                val isSelected = themeConfig.visualizerStyle == style
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        context.hapticTick()
                        onSelectVisualizerStyle(style)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when (style) {
                                VisualizerStyle.BARS -> Icons.Default.BarChart
                                VisualizerStyle.WAVEFORM -> Icons.Default.GraphicEq
                                VisualizerStyle.CIRCULAR_PULSE -> Icons.Default.Speed
                                VisualizerStyle.SPECTRUM -> Icons.Default.Tune
                                VisualizerStyle.MIRRORED_BARS -> Icons.Default.BarChart
                                VisualizerStyle.DOT_MATRIX -> Icons.Default.AutoAwesome
                                VisualizerStyle.ORBITAL -> Icons.Default.Radio
                                VisualizerStyle.RADIAL_BURST -> Icons.Default.ColorLens
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        Text(
                            text = style.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(0.48f)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // ───────────────────────────────────────────────
        // 9. Album Art Style Picker
        // ───────────────────────────────────────────────
        Text(
            text = "Estilo da Capa do Player",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Formato e efeitos da arte do álbum no tocador principal",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AlbumArtStyle.entries.forEach { style ->
                val isSelected = themeConfig.albumArtStyle == style
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        context.hapticTick()
                        onSelectAlbumArtStyle(style)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when (style) {
                                AlbumArtStyle.VINYL_ROTATION -> Icons.Default.Album
                                AlbumArtStyle.CARD_ROUNDED -> Icons.Default.MusicNote
                                AlbumArtStyle.FULLSCREEN_GLOW -> Icons.Default.AutoAwesome
                                AlbumArtStyle.POLAROID_FRAME -> Icons.Default.Palette
                                AlbumArtStyle.GLASSMORPHIC -> Icons.Default.ColorLens
                                AlbumArtStyle.NEON_RING -> Icons.Default.Radio
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        Text(
                            text = style.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(0.48f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ───────────────────────────────────────────────
        // 10. Local Storage & Importer
        // ───────────────────────────────────────────────
        Text(
            text = "Biblioteca de Áudio Offline",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Importar Músicas do Armazenamento",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Escaneie o dispositivo ou importe pastas completas com subpastas para ouvir offline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            context.hapticTick()
                            onScanLocalStorage(context)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("scan_storage_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Escanear")
                    }

                    OutlinedButton(
                        onClick = {
                            context.hapticTick()
                            filePickerLauncher.launch(arrayOf("audio/*"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Arquivo")
                    }

                    OutlinedButton(
                        onClick = {
                            context.hapticTick()
                            onImportAudioFolder(context)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pasta")
                    }
                }

                if (scanStatusMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = scanStatusMessage,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ───────────────────────────────────────────────
        // 11. Last.fm & Backup / Restore
        // ───────────────────────────────────────────────
        Text(
            text = "Sincronização & Backup",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Last.fm Scrobbling", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = "Envie automaticamente o histórico do que você ouve para o seu perfil Last.fm.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
                OutlinedButton(
                    onClick = {
                        context.hapticTick()
                        onOpenLastFm()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Text("Configurar Conta Last.fm")
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                Text("Backup Local em JSON", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = "Exporte ou restaure suas playlists, músicas favoritas, letras editadas e temas salvos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            context.hapticTick()
                            onBackup()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Exportar")
                    }
                    Button(
                        onClick = {
                            context.hapticTick()
                            onRestore()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restaurar")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(90.dp))
    }
}

@Composable
private fun MiniColorChip(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(color)
                .border(0.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ColorOrb(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(2.5.dp, Color.White, CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun PresetThemePreview(theme: AppThemeType) {
    Column(
        modifier = Modifier.width(84.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            theme.surfaceDark,
                            theme.primaryColor,
                            theme.secondaryColor
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.25f))
                        )
                    )
            )
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(5.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(theme.tertiaryColor)
                    .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (theme.isDarkPreset) "DARK • OLED" else "LIGHT • STUDIO",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun SurfacePaletteOption(
    name: String,
    backgroundColor: Color,
    surfaceColor: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .width(104.dp)
            .clickable {
                context.hapticTick()
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            }
        ),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(25.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(surfaceColor)
                        .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ThemeModeOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    testTag: String = "",
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                context.hapticTick()
                onClick()
            }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun DynamicColorPreviewCard(preview: DynamicColorPreview) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dynamic_color_preview"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = preview.surface.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, preview.primary.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = preview.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cores extraídas do seu wallpaper",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Paleta de Material You disponível no sistema:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    preview.primary to "Primária",
                    preview.secondary to "Secundária",
                    preview.tertiary to "Acento",
                    preview.primaryContainer to "Container"
                ).forEach { (color, label) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ative a chave acima para sincronizar com seu papel de parede.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
