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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
    val activeModeLabel = when (themeConfig.themeMode) {
        ThemeMode.SYSTEM -> "Seguindo o sistema"
        ThemeMode.LIGHT -> "Modo claro"
        ThemeMode.DARK -> "Modo escuro"
        ThemeMode.CUSTOM -> "Personalizado"
    }
    val activeThemeLabel = if (themeConfig.themeMode == ThemeMode.CUSTOM) {
        "Paleta criada por você"
    } else {
        themeConfig.presetTheme.title
    }
    val activeThemeColor = if (themeConfig.themeMode == ThemeMode.CUSTOM) {
        themeConfig.customTheme.primaryColor
    } else {
        themeConfig.presetTheme.primaryColor
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
        Spacer(modifier = Modifier.height(8.dp))

        // Header Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Temas & Cores",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Persistido via Jetpack DataStore",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            OutlinedButton(
                onClick = { context.hapticTick(); onResetDefaults() },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("reset_theme_button")
            ) {
                Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Padrão", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("active_theme_summary"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(activeThemeColor, MaterialTheme.colorScheme.secondary))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aparência ativa",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$activeThemeLabel • $activeModeLabel",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. THEME MODE SELECTOR (System, Light, Dark, Custom)
        Text(
            text = "Modo de Aparência",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeModeOptionCard(
                title = "Sistema",
                icon = Icons.Default.SettingsBrightness,
                isSelected = themeConfig.themeMode == ThemeMode.SYSTEM,
                modifier = Modifier.fillMaxWidth(0.48f),
                testTag = "theme_mode_system",
                onClick = { onSelectThemeMode(ThemeMode.SYSTEM) }
            )
            ThemeModeOptionCard(
                title = "Claro",
                icon = Icons.Default.LightMode,
                isSelected = themeConfig.themeMode == ThemeMode.LIGHT,
                modifier = Modifier.fillMaxWidth(0.48f),
                testTag = "theme_mode_light",
                onClick = { onSelectThemeMode(ThemeMode.LIGHT) }
            )
            ThemeModeOptionCard(
                title = "Escuro",
                icon = Icons.Default.DarkMode,
                isSelected = themeConfig.themeMode == ThemeMode.DARK,
                modifier = Modifier.fillMaxWidth(0.48f),
                testTag = "theme_mode_dark",
                onClick = { onSelectThemeMode(ThemeMode.DARK) }
            )
            ThemeModeOptionCard(
                title = "Custom",
                icon = Icons.Default.Brush,
                isSelected = themeConfig.themeMode == ThemeMode.CUSTOM,
                modifier = Modifier.fillMaxWidth(0.48f),
                testTag = "theme_mode_custom",
                onClick = { onSelectThemeMode(ThemeMode.CUSTOM) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. DYNAMIC COLORS (Material You - Android 12+)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dynamic_colors_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
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
                            text = if (supportsDynamicColor) "Extrai as cores do papel de parede do seu Android"
                            else "Disponível a partir do Android 12 (API 31+)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = themeConfig.dynamicColors,
                    onCheckedChange = { context.hapticTick(); onToggleDynamicColors(it) },
                    enabled = supportsDynamicColor,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("dynamic_colors_switch")
                )
            }
        }

        // Dynamic Color Preview (shows extracted wallpaper colors)
        if (supportsDynamicColor && !themeConfig.dynamicColors) {
            val colorPreview = remember { extractDynamicColorPreview(context) }
            if (colorPreview != null) {
                Spacer(modifier = Modifier.height(12.dp))
                DynamicColorPreviewCard(colorPreview)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. CUSTOM THEME BUILDER (If Custom Mode selected, or expandable)
        AnimatedVisibility(
            visible = themeConfig.themeMode == ThemeMode.CUSTOM,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_theme_studio_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Estúdio de Tema Personalizado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Base Surface Mode (Dark vs Light)
                        Text(
                            text = "1. Base de Contraste:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = customIsDark,
                                onClick = {
                                    customIsDark = true
                                    customBackground = CUSTOM_SURFACE_DARK_PALETTE[0].second.first
                                    customSurface = CUSTOM_SURFACE_DARK_PALETTE[0].second.second
                                },
                                label = { Text("Base Escura / OLED") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary),
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = !customIsDark,
                                onClick = {
                                    customIsDark = false
                                    customBackground = CUSTOM_SURFACE_LIGHT_PALETTE[0].second.first
                                    customSurface = CUSTOM_SURFACE_LIGHT_PALETTE[0].second.second
                                },
                                label = { Text("Base Clara / Studio") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Primary Color Picker
                        Text(
                            text = "2. Cor Primária de Destaque:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CUSTOM_PRIMARY_PALETTE.forEach { color ->
                                val isSelected = customPrimary == color
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { context.hapticTick(); customPrimary = color }
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
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Secondary Color Picker
                        Text(
                            text = "3. Cor Secundária (Glow & Acentos):",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CUSTOM_SECONDARY_PALETTE.forEach { color ->
                                val isSelected = customSecondary == color
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { context.hapticTick(); customSecondary = color }
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
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tertiary color for badges, status and secondary emphasis
                        Text(
                            text = "4. Cor Terciária (Status & Selos):",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CUSTOM_TERTIARY_PALETTE.forEach { color ->
                                val isSelected = customTertiary == color
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { context.hapticTick(); customTertiary = color }
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
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Background and surface pair selector
                        Text(
                            text = "5. Fundo e Superfície:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Escolha uma combinação pronta para manter o contraste equilibrado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val surfacePalettes = if (customIsDark) {
                            CUSTOM_SURFACE_DARK_PALETTE
                        } else {
                            CUSTOM_SURFACE_LIGHT_PALETTE
                        }
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

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live Preview Box
                        Text(
                            text = "Pré-visualização em Tempo Real:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = customSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, customPrimary.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Brush.linearGradient(listOf(customPrimary, customSecondary, customTertiary))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Faixa de Demonstração",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (customIsDark) Color.White else Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "Artista Visual",
                                            fontSize = 11.sp,
                                            color = customPrimary
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(customTertiary.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Ativo", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = customTertiary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

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
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = customPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salvar & Aplicar no DataStore")
                        }
                    }
                }
            }
        }

        // 6. PRESET COLOR SCHEMES
        Text(
            text = "Paletas Prontas & Gradientes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Escolha um tema com contraste aperfeiçoado para áudio",
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
            AppThemeType.values().forEach { themeType ->
                val isSelected = themeConfig.themeMode != ThemeMode.CUSTOM && themeConfig.presetTheme == themeType

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.48f)
                        .clickable {
                            context.hapticTick()
                            onSelectPresetTheme(themeType)
                            if (themeConfig.themeMode == ThemeMode.CUSTOM) {
                                onSelectThemeMode(ThemeMode.DARK)
                            }
                        }
                        .testTag("theme_option_${themeType.name}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    else androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
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
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selecionado",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
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
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "LIGHT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
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

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Transição entre músicas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (crossfadeSeconds == 0) "Sem transição gradual" else "Reduz o volume no fim e suaviza a entrada da próxima faixa (${crossfadeSeconds}s)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(0, 3, 5, 8).forEach { seconds ->
                FilterChip(
                    selected = crossfadeSeconds == seconds,
                    onClick = { context.hapticTick(); onSetCrossfadeSeconds(seconds) },
                    label = { Text(if (seconds == 0) "Off" else "${seconds}s", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. VISUALIZER STYLE PICKER
        Text(
            text = "Estilo do Visualizador de Áudio",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VisualizerStyle.values().forEach { style ->
                val isSelected = themeConfig.visualizerStyle == style
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectVisualizerStyle(style) },
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
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(0.48f)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 6. ALBUM ART STYLE PICKER
        Text(
            text = "Estilo da Capa do Player",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AlbumArtStyle.values().forEach { style ->
                val isSelected = themeConfig.albumArtStyle == style
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectAlbumArtStyle(style) },
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
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(0.48f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 7. STORAGE & LIBRARY IMPORTER
        Text(
            text = "Biblioteca de Áudio Offline",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Importar Músicas do Armazenamento",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Escaneie o dispositivo ou importe uma pasta completa, incluindo subpastas, para ouvir offline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { context.hapticTick(); onScanLocalStorage(context) },
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
                        onClick = { context.hapticTick(); filePickerLauncher.launch(arrayOf("audio/*")) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Importar")
                    }

                    OutlinedButton(
                        onClick = { context.hapticTick(); onImportAudioFolder(context) },
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

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Sincronização e dados",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Last.fm Scrobbling", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = "Envie automaticamente o que você ouviu para o seu perfil Last.fm.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                OutlinedButton(
                    onClick = { context.hapticTick(); onOpenLastFm() },
                    modifier = Modifier.padding(top = 10.dp)
                ) { Text("Configurar Last.fm") }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Backup local", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = "Salve ou restaure músicas, playlists, letras editadas, temas e preferências em JSON.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "Credenciais do Last.fm não são incluídas no arquivo de backup.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = { context.hapticTick(); onBackup() }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Exportar")
                    }
                    Button(onClick = { context.hapticTick(); onRestore() }, modifier = Modifier.weight(1f)) {
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
private fun PresetThemePreview(theme: AppThemeType) {
    Column(
        modifier = Modifier.width(84.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
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
                .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.24f))
                        )
                    )
            )
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(theme.tertiaryColor)
                    .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (theme.isDarkPreset) "DARK • AUDIO" else "LIGHT • AUDIO",
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
            .clickable { context.hapticTick(); onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            }
        ),
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
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
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ThemeModeOptionCard(
    title: String,
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
            .clickable { context.hapticTick(); onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 4.dp),
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
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
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
        border = androidx.compose.foundation.BorderStroke(1.dp, preview.primary.copy(alpha = 0.4f))
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
                text = "Pré-visualização do Material You (tema escuro):",
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
                    preview.tertiary to "Terciária",
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
                text = "Ative o Material You para aplicar automaticamente.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
