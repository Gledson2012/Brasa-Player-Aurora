package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Song
import com.example.data.radio.RadioBrowserClient
import com.example.data.radio.RadioBrowserStation
import com.example.ui.components.hapticTick
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private const val RADIOS_HOME_URL = "https://www.radios.com.br/"
private const val RADIOS_TOP_URL = "https://www.radios.com.br/#topradios-tabpanel"

enum class RadioSourceTab(val label: String, val icon: ImageVector) {
    BRAZIL("Brasil", Icons.Default.LocationOn),
    GLOBAL("Mundo", Icons.Default.Public),
    GENRES("Gêneros", Icons.Default.MusicNote),
    FAVORITES("Favoritas", Icons.Default.Favorite),
    CURATED("Curadas", Icons.Default.AutoAwesome)
}

val POPULAR_RADIO_GENRES = listOf(
    "Todos",
    "Sertanejo",
    "MPB",
    "Bossa Nova",
    "Rock",
    "Pop",
    "Notícias",
    "Gospel",
    "Flashback",
    "Jazz",
    "Eletrônica",
    "Esportes",
    "Forró",
    "Reggae",
    "Hip-Hop",
    "Clássica"
)

// Curated high quality baseline Brazilian stations (active offline fallback)
val CURATED_BASELINE_STATIONS = listOf(
    RadioBrowserStation(
        stationuuid = "curated_13492",
        name = "Rádio Jornal 91.3 FM",
        url = "https://www.radios.com.br/aovivo/radio-jornal-913-fm/13492",
        urlResolved = "https://www.radios.com.br/play/playlist/13492/listen-radio.m3u",
        homepage = "https://www.radios.com.br/aovivo/radio-jornal-913-fm/13492",
        favicon = "https://img.radios.com.br/radio/md/radio13492_1693568264.png",
        tags = "notícias, brasil, sergipe",
        country = "Brasil",
        countryCode = "BR",
        state = "Aracaju / SE",
        votes = 1250,
        codec = "MP3",
        bitrate = 128
    ),
    RadioBrowserStation(
        stationuuid = "curated_13734",
        name = "Rádio Regional 91.5 FM",
        url = "https://www.radios.com.br/aovivo/radio-regional-915-fm/13734",
        urlResolved = "https://www.radios.com.br/play/playlist/13734/listen-radio.m3u",
        homepage = "https://www.radios.com.br/aovivo/radio-regional-915-fm/13734",
        favicon = "https://img.radios.com.br/radio/md/radio13734_1739184764.jpg",
        tags = "música, hits, brasil",
        country = "Brasil",
        countryCode = "BR",
        state = "Brasil",
        votes = 980,
        codec = "AAC",
        bitrate = 64
    ),
    RadioBrowserStation(
        stationuuid = "curated_13416",
        name = "Rádio Rio FM 102.3",
        url = "https://www.radios.com.br/aovivo/radio-rio-fm-1023/13416",
        urlResolved = "https://www.radios.com.br/play/playlist/13416/listen-radio.m3u",
        homepage = "https://www.radios.com.br/aovivo/radio-rio-fm-1023/13416",
        favicon = "https://img.radios.com.br/radio/md/radio13416_1745327959.png",
        tags = "música, pop, rio",
        country = "Brasil",
        countryCode = "BR",
        state = "Rio de Janeiro",
        votes = 1840,
        codec = "AAC",
        bitrate = 96
    ),
    RadioBrowserStation(
        stationuuid = "curated_13335",
        name = "Rádio Vox 97.1 FM",
        url = "https://www.radios.com.br/aovivo/radio-vox-971-fm/13335",
        urlResolved = "https://www.radios.com.br/play/playlist/13335/listen-radio.m3u",
        homepage = "https://www.radios.com.br/aovivo/radio-vox-971-fm/13335",
        favicon = "https://img.radios.com.br/radio/md/radio13335_1759945649.jpg",
        tags = "música, pop, flashback",
        country = "Brasil",
        countryCode = "BR",
        state = "Brasil",
        votes = 740,
        codec = "MP3",
        bitrate = 128
    ),
    RadioBrowserStation(
        stationuuid = "curated_13900",
        name = "Rádio Metropolitana 98.5 FM",
        url = "https://www.radios.com.br/aovivo/radio-metropolitana-985-fm/13900",
        urlResolved = "https://www.radios.com.br/play/playlist/13900/listen-radio.m3u",
        homepage = "https://www.radios.com.br/aovivo/radio-metropolitana-985-fm/13900",
        favicon = "https://img.radios.com.br/radio/md/radio13900_1665067845.png",
        tags = "pop, funk, hits, jovem",
        country = "Brasil",
        countryCode = "BR",
        state = "São Paulo",
        votes = 5420,
        codec = "AAC",
        bitrate = 128
    ),
    RadioBrowserStation(
        stationuuid = "curated_12229",
        name = "Rádio Costa do Sol 101.7 FM",
        url = "https://www.radios.com.br/aovivo/radio-costa-do-sol-1017-fm/12229",
        urlResolved = "https://www.radios.com.br/play/playlist/12229/listen-radio.m3u",
        homepage = "https://www.radios.com.br/aovivo/radio-costa-do-sol-1017-fm/12229",
        favicon = "https://img.radios.com.br/radio/md/radio12229_1725363304.png",
        tags = "música, litoral, pop",
        country = "Brasil",
        countryCode = "BR",
        state = "Rio de Janeiro",
        votes = 630,
        codec = "MP3",
        bitrate = 128
    ),
    RadioBrowserStation(
        stationuuid = "curated_158074",
        name = "Rádio Morada Sertaneja",
        url = "https://www.radios.com.br/aovivo/radio-morada-sertaneja/158074",
        urlResolved = "https://www.radios.com.br/play/playlist/158074/listen-radio.m3u",
        homepage = "https://www.radios.com.br/aovivo/radio-morada-sertaneja/158074",
        favicon = "https://img.radios.com.br/radio/md/radio158074_1696528106.jpg",
        tags = "sertanejo, raiz, modão",
        country = "Brasil",
        countryCode = "BR",
        state = "Goiás",
        votes = 3210,
        codec = "MP3",
        bitrate = 128
    ),
    RadioBrowserStation(
        stationuuid = "curated_15245",
        name = "Rádio Nova Difusora 88.1 FM",
        url = "https://www.radios.com.br/aovivo/radio-nova-difusora-881-fm/15245",
        urlResolved = "https://www.radios.com.br/play/playlist/15245/listen-radio.m3u",
        homepage = "https://www.radios.com.br/aovivo/radio-nova-difusora-881-fm/15245",
        favicon = "https://img.radios.com.br/radio/md/radio15245_1686922098.jpeg",
        tags = "música, notícias, brasil",
        country = "Brasil",
        countryCode = "BR",
        state = "Brasil",
        votes = 890,
        codec = "MP3",
        bitrate = 128
    ),
    RadioBrowserStation(
        stationuuid = "curated_271701",
        name = "MPB FM",
        url = "https://www.radios.com.br/aovivo/mpb-fm/271701",
        urlResolved = "https://www.radios.com.br/play/playlist/271701/listen-radio.m3u",
        homepage = "https://www.radios.com.br/aovivo/mpb-fm/271701",
        favicon = "https://img.radios.com.br/radio/md/radio271701_1771452183.jpg",
        tags = "mpb, bossa nova, brasil",
        country = "Brasil",
        countryCode = "BR",
        state = "Brasil",
        votes = 4210,
        codec = "MP3",
        bitrate = 192
    ),
    RadioBrowserStation(
        stationuuid = "curated_120034",
        name = "Rádio CBN Salvador 107.9 FM",
        url = "https://www.radios.com.br/aovivo/radio-cbn-salvador-1079-fm/120034",
        urlResolved = "https://www.radios.com.br/play/playlist/120034/listen-radio.m3u",
        homepage = "https://www.radios.com.br/aovivo/radio-cbn-salvador-1079-fm/120034",
        favicon = "https://img.radios.com.br/radio/md/radio120034_1764155626.jpg",
        tags = "notícias, jornalismo, bahia",
        country = "Brasil",
        countryCode = "BR",
        state = "Salvador / BA",
        votes = 2390,
        codec = "AAC",
        bitrate = 96
    ),
    RadioBrowserStation(
        stationuuid = "curated_10410",
        name = "Rádio Bandeirantes 107.3 FM",
        url = "https://www.radios.com.br/aovivo/radio-bandeirantes-1073-fm/10410",
        urlResolved = "https://www.radios.com.br/play/playlist/10410/listen-radio.m3u",
        homepage = "https://www.radios.com.br/aovivo/radio-bandeirantes-1073-fm/10410",
        favicon = "https://img.radios.com.br/radio/md/radio10410_1779370265.png",
        tags = "notícias, esportes, debates",
        country = "Brasil",
        countryCode = "BR",
        state = "São Paulo",
        votes = 3890,
        codec = "MP3",
        bitrate = 128
    ),
    RadioBrowserStation(
        stationuuid = "curated_93525",
        name = "Rádio Elite Rock",
        url = "https://www.radios.com.br/aovivo/radio-elite-rock/93525",
        urlResolved = "https://www.radios.com.br/play/playlist/93525/listen-radio.m3u",
        homepage = "https://www.radios.com.br/aovivo/radio-elite-rock/93525",
        favicon = "https://img.radios.com.br/radio/md/radio93525_1777219585.png",
        tags = "rock, classic rock, metal",
        country = "Brasil",
        countryCode = "BR",
        state = "Brasil",
        votes = 2780,
        codec = "MP3",
        bitrate = 192
    ),
    RadioBrowserStation(
        stationuuid = "curated_8",
        name = "Radio Saudade 99.7 FM",
        url = "https://www.radios.com.br/aovivo/radio-saudade-997-fm/8",
        urlResolved = "https://www.radios.com.br/play/playlist/8/listen-radio.m3u",
        homepage = "https://www.radios.com.br/aovivo/radio-saudade-997-fm/8",
        favicon = "https://img.radios.com.br/radio/md/radio8_1781896993.png",
        tags = "flashback, anos 80, anos 70",
        country = "Brasil",
        countryCode = "BR",
        state = "Santos / SP",
        votes = 4690,
        codec = "AAC",
        bitrate = 128
    ),
    RadioBrowserStation(
        stationuuid = "curated_229211",
        name = "Rádio FM O Dia 99.7 FM",
        url = "https://www.radios.com.br/aovivo/radio-fm-o-dia-997-fm/229211",
        urlResolved = "https://www.radios.com.br/play/playlist/229211/listen-radio.m3u",
        homepage = "https://www.radios.com.br/aovivo/radio-fm-o-dia-997-fm/229211",
        favicon = "https://img.radios.com.br/radio/md/radio229211_1706128532.jpeg",
        tags = "pagode, funk, samba, brasil",
        country = "Brasil",
        countryCode = "BR",
        state = "Rio de Janeiro",
        votes = 6210,
        codec = "AAC",
        bitrate = 128
    )
)

@Composable
fun RadiosScreen(
    onOpenLink: (String) -> Unit,
    onPlayStation: (String, String, String, String?, String?) -> Unit,
    modifier: Modifier = Modifier,
    currentPlayingSong: Song? = null,
    isPlaying: Boolean = false,
    onTogglePlayPause: () -> Unit = {},
    radioBrowserClient: RadioBrowserClient = remember { RadioBrowserClient() }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Persistent storage for favorites and custom stations
    val prefs = remember { context.getSharedPreferences("brasa_radio_prefs", Context.MODE_PRIVATE) }
    var favoriteUuids by remember {
        mutableStateOf(prefs.getStringSet("favorite_station_uuids", emptySet()) ?: emptySet())
    }

    // Local custom stations
    var customStations by remember {
        val initialJson = prefs.getString("custom_stations_json", "[]") ?: "[]"
        mutableStateOf(radioBrowserClient.parseStationsJson(initialJson))
    }

    // UI States
    var selectedTab by rememberSaveable { mutableStateOf(RadioSourceTab.BRAZIL) }
    var selectedGenre by rememberSaveable { mutableStateOf("Todos") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var debouncedQuery by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var networkError by remember { mutableStateOf(false) }

    // Remote stations loaded via Radio Browser API
    var onlineStations by remember { mutableStateOf<List<RadioBrowserStation>>(emptyList()) }

    // Add station dialog state
    var showAddStationDialog by rememberSaveable { mutableStateOf(false) }
    var customStationName by rememberSaveable { mutableStateOf("") }
    var customStationUrl by rememberSaveable { mutableStateOf("") }
    var customStationGenre by rememberSaveable { mutableStateOf("Pop") }
    var customStationCountry by rememberSaveable { mutableStateOf("Brasil") }
    var customStationError by rememberSaveable { mutableStateOf<String?>(null) }

    // Debounce search input
    LaunchedEffect(searchQuery) {
        delay(350)
        debouncedQuery = searchQuery.trim()
    }

    // Load stations from Radio Browser API based on tab / genre / search
    fun reloadStations() {
        coroutineScope.launch {
            isLoading = true
            networkError = false
            try {
                val fetched = when {
                    debouncedQuery.isNotBlank() -> {
                        radioBrowserClient.searchStations(query = debouncedQuery, limit = 50)
                    }
                    selectedTab == RadioSourceTab.BRAZIL -> {
                        radioBrowserClient.getStationsByCountry(countryCode = "BR", limit = 40)
                    }
                    selectedTab == RadioSourceTab.GLOBAL -> {
                        radioBrowserClient.getTopVoted(limit = 40)
                    }
                    selectedTab == RadioSourceTab.GENRES -> {
                        if (selectedGenre == "Todos") {
                            radioBrowserClient.getTopClicked(limit = 40)
                        } else {
                            radioBrowserClient.getStationsByTag(tag = selectedGenre.lowercase(), limit = 40)
                        }
                    }
                    else -> emptyList()
                }
                onlineStations = fetched
                if (fetched.isEmpty() && (selectedTab == RadioSourceTab.BRAZIL || selectedTab == RadioSourceTab.GLOBAL)) {
                    networkError = true
                }
            } catch (e: Exception) {
                networkError = true
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedTab, selectedGenre, debouncedQuery) {
        if (selectedTab != RadioSourceTab.CURATED && selectedTab != RadioSourceTab.FAVORITES) {
            reloadStations()
        }
    }

    // Save favorites helper
    val toggleFavorite: (RadioBrowserStation) -> Unit = { station ->
        context.hapticTick()
        val newFavorites = if (favoriteUuids.contains(station.stationuuid)) {
            favoriteUuids - station.stationuuid
        } else {
            favoriteUuids + station.stationuuid
        }
        favoriteUuids = newFavorites
        prefs.edit().putStringSet("favorite_station_uuids", newFavorites).apply()
    }

    // Combined active station list
    val displayedStations: List<RadioBrowserStation> = remember(
        selectedTab,
        selectedGenre,
        debouncedQuery,
        onlineStations,
        customStations,
        favoriteUuids
    ) {
        val baseList: List<RadioBrowserStation> = when (selectedTab) {
            RadioSourceTab.BRAZIL -> {
                if (onlineStations.isNotEmpty()) onlineStations else CURATED_BASELINE_STATIONS
            }
            RadioSourceTab.GLOBAL -> {
                if (onlineStations.isNotEmpty()) onlineStations else CURATED_BASELINE_STATIONS
            }
            RadioSourceTab.GENRES -> {
                if (onlineStations.isNotEmpty()) onlineStations
                else {
                    if (selectedGenre == "Todos") CURATED_BASELINE_STATIONS
                    else CURATED_BASELINE_STATIONS.filter {
                        it.tags.contains(selectedGenre, ignoreCase = true)
                    }
                }
            }
            RadioSourceTab.FAVORITES -> {
                val allKnown = (CURATED_BASELINE_STATIONS + customStations + onlineStations).distinctBy { it.stationuuid }
                allKnown.filter { favoriteUuids.contains(it.stationuuid) }
            }
            RadioSourceTab.CURATED -> {
                CURATED_BASELINE_STATIONS + customStations
            }
        }

        // Apply search filter if query is active
        if (debouncedQuery.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.name.contains(debouncedQuery, ignoreCase = true) ||
                    it.tags.contains(debouncedQuery, ignoreCase = true) ||
                    it.state.contains(debouncedQuery, ignoreCase = true) ||
                    it.country.contains(debouncedQuery, ignoreCase = true)
            }
        }
    }

    // Active currently playing radio detection
    val isRadioSongPlaying = isPlaying && currentPlayingSong?.sourceKey?.startsWith("radio:") == true
    val activePlayingStationName = currentPlayingSong?.takeIf { isRadioSongPlaying }?.title

    // Hero station: currently playing station, or first featured item
    val heroStation = remember(displayedStations, activePlayingStationName) {
        displayedStations.find { it.name == activePlayingStationName }
            ?: displayedStations.firstOrNull()
            ?: CURATED_BASELINE_STATIONS.first()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .testTag("radios_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ───────────────────────────────────────────────
        // 1. Sleek Header Bar & Badges
        // ───────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
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
                            imageVector = Icons.Default.Radio,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Rádios ao Vivo",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            LivePulsingChip()
                        }
                        Text(
                            text = "+30.000 rádios • Radio Browser API & Brasil",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = {
                            context.hapticTick()
                            reloadStations()
                        },
                        modifier = Modifier.testTag("refresh_radios_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Atualizar rádios",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    FilledIconButton(
                        onClick = {
                            context.hapticTick()
                            customStationError = null
                            showAddStationDialog = true
                        },
                        modifier = Modifier.testTag("add_radio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Adicionar rádio por URL"
                        )
                    }
                }
            }
        }

        // ───────────────────────────────────────────────
        // 2. Active Now Playing / Hero Featured Station
        // ───────────────────────────────────────────────
        item {
            RadioHeroCard(
                station = heroStation,
                isCurrentlyPlaying = isRadioSongPlaying && currentPlayingSong?.title == heroStation.name,
                isPlaying = isPlaying,
                onPlayClick = {
                    if (isRadioSongPlaying && currentPlayingSong?.title == heroStation.name) {
                        onTogglePlayPause()
                    } else {
                        onPlayStation(
                            heroStation.name,
                            heroStation.displayGenre,
                            heroStation.favicon,
                            heroStation.stationuuid,
                            heroStation.playableUrl
                        )
                        coroutineScope.launch { radioBrowserClient.registerClick(heroStation.stationuuid) }
                    }
                }
            )
        }

        // ───────────────────────────────────────────────
        // 3. Search Bar
        // ───────────────────────────────────────────────
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("radio_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                placeholder = { Text("Buscar estação por nome, cidade ou gênero...") },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Limpar busca")
                        }
                    }
                }
            )
        }

        // ───────────────────────────────────────────────
        // 4. Source Navigation Tabs (Brasil, Mundo, Gêneros, Favoritas, Curadas)
        // ───────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RadioSourceTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            context.hapticTick()
                            selectedTab = tab
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("filter_tab_${tab.name}")
                    )
                }
            }
        }

        // ───────────────────────────────────────────────
        // 5. Genre Chips (Shown when Gêneros is active)
        // ───────────────────────────────────────────────
        if (selectedTab == RadioSourceTab.GENRES) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    POPULAR_RADIO_GENRES.forEach { genre ->
                        val isSelected = selectedGenre == genre
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                context.hapticTick()
                                selectedGenre = genre
                            },
                            label = {
                                Text(
                                    text = genre,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("genre_chip_$genre")
                        )
                    }
                }
            }
        }

        // ───────────────────────────────────────────────
        // 6. Section Info & Quick External Link
        // ───────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = when {
                            debouncedQuery.isNotBlank() -> "Resultados da Busca"
                            selectedTab == RadioSourceTab.BRAZIL -> "Top Rádios Brasileiras"
                            selectedTab == RadioSourceTab.GLOBAL -> "Mais Populares do Mundo"
                            selectedTab == RadioSourceTab.GENRES -> "Gênero: $selectedGenre"
                            selectedTab == RadioSourceTab.FAVORITES -> "Minhas Rádios Favoritas"
                            selectedTab == RadioSourceTab.CURATED -> "Seleção Curada Radios.com.br"
                            else -> "Estações"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${displayedStations.size} estações disponíveis • Toque para sintonizar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (selectedTab == RadioSourceTab.CURATED || selectedTab == RadioSourceTab.BRAZIL) {
                    TextButton(onClick = { onOpenLink(RADIOS_TOP_URL) }) {
                        Text("Radios.com.br", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // ───────────────────────────────────────────────
        // 7. Stations List
        // ───────────────────────────────────────────────
        if (displayedStations.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (selectedTab == RadioSourceTab.FAVORITES) Icons.Default.FavoriteBorder else Icons.Default.Radio,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = if (selectedTab == RadioSourceTab.FAVORITES) {
                                "Nenhuma rádio favoritada ainda."
                            } else {
                                "Nenhuma estação encontrada para a busca."
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (selectedTab == RadioSourceTab.FAVORITES) {
                                "Toque no ícone de coração nas estações para salvá-las aqui."
                            } else {
                                "Tente buscar por outro termo ou explore os gêneros."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(displayedStations, key = { it.stationuuid + it.url }) { station ->
                val isThisStationPlaying = isRadioSongPlaying && currentPlayingSong?.title == station.name
                val isFavorite = favoriteUuids.contains(station.stationuuid)

                RadioStationCard(
                    station = station,
                    isCurrentlyPlaying = isThisStationPlaying,
                    isFavorite = isFavorite,
                    onPlayClick = {
                        context.hapticTick()
                        if (isThisStationPlaying) {
                            onTogglePlayPause()
                        } else {
                            onPlayStation(
                                station.name,
                                station.displayGenre,
                                station.favicon,
                                station.stationuuid,
                                station.playableUrl
                            )
                            coroutineScope.launch {
                                radioBrowserClient.registerClick(station.stationuuid)
                            }
                        }
                    },
                    onFavoriteToggle = { toggleFavorite(station) },
                    onOpenWebsite = {
                        if (station.homepage.isNotBlank()) onOpenLink(station.homepage)
                        else if (station.url.isNotBlank()) onOpenLink(station.url)
                    }
                )
            }
        }
    }

    // ───────────────────────────────────────────────
    // 8. Custom Radio Stream URL Dialog
    // ───────────────────────────────────────────────
    if (showAddStationDialog) {
        AlertDialog(
            onDismissRequest = { showAddStationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Radio, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Adicionar Rádio Personalizada")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Insira o stream de áudio direto (MP3, AAC ou HLS).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = customStationName,
                        onValueChange = { customStationName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_station_name_input"),
                        singleLine = true,
                        label = { Text("Nome da Rádio") },
                        placeholder = { Text("Ex: Minha Rádio Rock") }
                    )

                    OutlinedTextField(
                        value = customStationUrl,
                        onValueChange = {
                            customStationUrl = it
                            customStationError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_station_url_input"),
                        singleLine = true,
                        label = { Text("URL do Stream") },
                        placeholder = { Text("https://servidor.com/live.mp3") }
                    )

                    OutlinedTextField(
                        value = customStationGenre,
                        onValueChange = { customStationGenre = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Gênero Musical") }
                    )

                    if (customStationError != null) {
                        Text(
                            text = customStationError!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = customStationName.trim()
                        val url = customStationUrl.trim()
                        if (name.isBlank()) {
                            customStationError = "Informe o nome da rádio."
                            return@Button
                        }
                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            customStationError = "A URL deve começar com http:// ou https://"
                            return@Button
                        }

                        val newStation = RadioBrowserStation(
                            stationuuid = "custom_" + System.currentTimeMillis(),
                            name = name,
                            url = url,
                            urlResolved = url,
                            tags = customStationGenre.trim().ifBlank { "Personalizada" },
                            country = customStationCountry.trim().ifBlank { "Brasil" },
                            countryCode = "BR",
                            codec = "STREAM",
                            bitrate = 128
                        )

                        val updated = customStations + newStation
                        customStations = updated

                        // Persist to SharedPreferences JSON
                        val array = JSONArray()
                        updated.forEach { s ->
                            val obj = JSONObject()
                            obj.put("stationuuid", s.stationuuid)
                            obj.put("name", s.name)
                            obj.put("url", s.url)
                            obj.put("url_resolved", s.urlResolved)
                            obj.put("tags", s.tags)
                            obj.put("country", s.country)
                            obj.put("countrycode", s.countryCode)
                            obj.put("codec", s.codec)
                            obj.put("bitrate", s.bitrate)
                            array.put(obj)
                        }
                        prefs.edit().putString("custom_stations_json", array.toString()).apply()

                        showAddStationDialog = false
                        customStationName = ""
                        customStationUrl = ""

                        // Play immediately
                        onPlayStation(newStation.name, newStation.displayGenre, "", newStation.stationuuid, newStation.playableUrl)
                    },
                    modifier = Modifier.testTag("confirm_add_station_button")
                ) {
                    Text("Adicionar & Sintonizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStationDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Subcomponents
// ─────────────────────────────────────────────────────────────

@Composable
private fun LivePulsingChip() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Color.Red.copy(alpha = alpha))
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = "LIVE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun RadioHeroCard(
    station: RadioBrowserStation,
    isCurrentlyPlaying: Boolean,
    isPlaying: Boolean,
    onPlayClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("radio_hero_card"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.75f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isCurrentlyPlaying) "SINTONIZADO AGORA" else "DESTAQUE AO VIVO",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.1.sp
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = station.countryFlagEmoji,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${station.displayGenre} • ${station.displayLocation}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(4.dp))

                    Button(
                        onClick = onPlayClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("hero_play_button")
                    ) {
                        Icon(
                            imageVector = if (isCurrentlyPlaying && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isCurrentlyPlaying && isPlaying) "Pausar" else "Ouvir Agora",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                StationArtwork(
                    station = station,
                    modifier = Modifier.size(96.dp)
                )
            }
        }
    }
}

@Composable
private fun RadioStationCard(
    station: RadioBrowserStation,
    isCurrentlyPlaying: Boolean,
    isFavorite: Boolean,
    onPlayClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onOpenWebsite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlayClick)
            .testTag("station_card_${station.stationuuid}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = if (isCurrentlyPlaying) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StationArtwork(
                station = station,
                modifier = Modifier.size(56.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = station.countryFlagEmoji,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "${station.displayGenre} • ${station.displayLocation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (station.codec.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = station.codec,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (station.formattedBitrate.isNotBlank()) {
                        Text(
                            text = station.formattedBitrate,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (station.votes > 0) {
                        Text(
                            text = "▲ ${station.votes}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.testTag("favorite_station_${station.stationuuid}")
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favoritar rádio",
                    tint = if (isFavorite) Color(0xFFFF007F) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FilledIconButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .size(42.dp)
                    .testTag("play_station_${station.stationuuid}")
            ) {
                Icon(
                    imageVector = if (isCurrentlyPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                    contentDescription = "Sintonizar ${station.name}"
                )
            }
        }
    }
}

@Composable
private fun StationArtwork(
    station: RadioBrowserStation,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Radio,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier.size(28.dp)
        )
        if (station.favicon.isNotBlank()) {
            AsyncImage(
                model = station.favicon,
                contentDescription = "Logo de ${station.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
