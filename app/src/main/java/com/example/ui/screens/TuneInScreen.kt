package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private const val tuneInHomeUrl = "https://tunein.com/"
private const val tuneInPremiumUrl = "https://tunein.com/subscribe/?vt=su&source=sidebar.upsell&browse=home"

private data class TuneInStation(
    val name: String,
    val category: String,
    val imageUrl: String,
    val url: String,
    val tuneInId: String? = null,
    val fallbackStreamUrl: String? = null
)

private val tuneInStations = listOf(
    TuneInStation(
        name = "Today's Hits",
        category = "Música",
        imageUrl = "https://cdn-profiles.tunein.com/z8297/images/brickg.jpg?t=639223308700000000",
        url = "https://tunein.com/todays-hits/",
        tuneInId = "s242677"
    ),
    TuneInStation(
        name = "Classic Rock Hits",
        category = "Música",
        imageUrl = "https://cdn-profiles.tunein.com/z7806/images/brickg.jpg?t=639099845190000000",
        url = "https://tunein.com/radio/Classic-Rock-Hits-s249994/",
        tuneInId = "s249994"
    ),
    TuneInStation(
        name = "Smooth Jazz",
        category = "Música",
        imageUrl = "https://cdn-profiles.tunein.com/z7330/images/brickg.jpg?t=639011727140000000",
        url = "https://tunein.com/radio/Smooth-Jazz-s249973/",
        tuneInId = "s249973"
    ),
    TuneInStation(
        name = "Country Roads",
        category = "Música",
        imageUrl = "https://cdn-profiles.tunein.com/z8317/images/brickg.jpg?t=639232817620000000",
        url = "https://tunein.com/countryroads/",
        tuneInId = "s224628"
    ),
    TuneInStation(
        name = "Coffeehouse",
        category = "Música",
        imageUrl = "https://cdn-profiles.tunein.com/z7359/images/brickg.jpg?t=639103309540000000",
        url = "https://tunein.com/radio/Coffeehouse-s304385/",
        tuneInId = "s304385"
    ),
    TuneInStation(
        name = "Éxitos Mexicanos",
        category = "Música",
        imageUrl = "https://cdn-profiles.tunein.com/z8180/images/brickg.jpg?t=639231861250000000",
        url = "https://tunein.com/radio/%c3%89xitos-Mexicanos-s259790/",
        tuneInId = "s259790"
    ),
    TuneInStation(
        name = "CNN",
        category = "Notícias",
        imageUrl = "https://cdn-profiles.tunein.com/z5899/images/brickg.jpg?t=638660794510000000",
        url = "https://tunein.com/cnn/",
        tuneInId = "s20407"
    ),
    TuneInStation(
        name = "ABC News",
        category = "Notícias",
        imageUrl = "https://cdn-profiles.tunein.com/z8186/images/brickg.jpg?t=639108446150000000",
        url = "https://tunein.com/radio/ABC-News-s150918/",
        tuneInId = "s150918"
    ),
    TuneInStation(
        name = "CNBC",
        category = "Notícias",
        imageUrl = "https://cdn-profiles.tunein.com/z8086/images/brickg.jpg?t=639009213190000000",
        url = "https://tunein.com/radio/CNBC-s110052/",
        tuneInId = "s110052"
    ),
    TuneInStation(
        name = "Bloomberg Radio",
        category = "Notícias",
        imageUrl = "https://cdn-profiles.tunein.com/z7348/images/brickg.jpg?t=638666005490000000",
        url = "https://tunein.com/radio/Bloomberg-Radio-s165740/",
        tuneInId = "s165740"
    ),
    TuneInStation(
        name = "ESPN Radio",
        category = "Esportes",
        imageUrl = "https://cdn-profiles.tunein.com/z7531/images/brickg.jpg?t=638745277300000000",
        url = "https://tunein.com/espnradio/",
        tuneInId = "s25876"
    ),
    TuneInStation(
        name = "talkSPORT",
        category = "Esportes",
        imageUrl = "https://cdn-profiles.tunein.com/z7335/images/brickg.jpg?t=638660797820000000",
        url = "https://tunein.com/radio/talkSPORT-1089-s17077/",
        tuneInId = "s17077",
        fallbackStreamUrl = "https://radio.talksport.com/stream"
    ),
    TuneInStation(
        name = "Stuff You Should Know",
        category = "Podcasts",
        imageUrl = "https://cdn-profiles.tunein.com/p295446/images/logod.png?t=638853589160000000",
        url = "https://tunein.com/podcasts/Science-Podcasts/Stuff-You-Should-Know-p295446/",
        tuneInId = "p295446"
    ),
    TuneInStation(
        name = "The Daily",
        category = "Podcasts",
        imageUrl = "https://cdn-profiles.tunein.com/p952868/images/logod.png?t=639003119010000000",
        url = "https://tunein.com/podcasts/News/The-Daily-p952868/",
        tuneInId = "p952868"
    ),
    TuneInStation(
        name = "Crime Junkie",
        category = "Podcasts",
        imageUrl = "https://cdn-profiles.tunein.com/p1086263/images/logod.png?t=638242295120000000",
        url = "https://tunein.com/podcasts/True-Crime/Crime-Junkie-p1086263/",
        tuneInId = "p1086263"
    )
)

@Composable
fun TuneInScreen(
    onOpenLink: (String) -> Unit,
    onPlayStation: (String, String, String, String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Todos") }
    var showAddStationDialog by rememberSaveable { mutableStateOf(false) }
    var customStationName by rememberSaveable { mutableStateOf("") }
    var customStationUrl by rememberSaveable { mutableStateOf("") }
    var customStationCategory by rememberSaveable { mutableStateOf("Música") }
    var customStationError by rememberSaveable { mutableStateOf<String?>(null) }
    val customStations = androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateListOf<TuneInStation>()
    }
    val categories = listOf("Todos", "Música", "Notícias", "Esportes", "Podcasts")
    val allStations = tuneInStations + customStations
    val normalizedQuery = searchQuery.trim()
    val filteredStations = allStations.filter { station ->
        val matchesCategory = selectedCategory == "Todos" || station.category == selectedCategory
        val matchesQuery = normalizedQuery.isBlank() ||
            station.name.contains(normalizedQuery, ignoreCase = true) ||
            station.category.contains(normalizedQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }
    val featuredStations = allStations
        .filter { selectedCategory == "Todos" || it.category == selectedCategory }
        .take(6)
    val heroStation = featuredStations.firstOrNull() ?: allStations.first()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.13f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
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
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RÁDIO ONLINE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp
                    )
                    Text(
                        text = "Descubra seu som",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Estações, notícias, esportes e podcasts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "AO VIVO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            TuneInHeroCard(station = heroStation, onClick = {
                onPlayStation(
                    heroStation.name,
                    heroStation.category,
                    heroStation.imageUrl,
                    heroStation.tuneInId,
                    heroStation.fallbackStreamUrl
                )
            })
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TuneInStatCard(
                    value = allStations.size.toString(),
                    label = "estações selecionadas",
                    modifier = Modifier.weight(1f)
                )
                TuneInStatCard(
                    value = (categories.size - 1).toString(),
                    label = "categorias para explorar",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radio,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "TuneIn Premium",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mais conteúdo, menos interrupções.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { onOpenLink(tuneInPremiumUrl) },
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Text("Ver")
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                placeholder = { Text("Buscar estação ou categoria") },
                label = { Text("Descobrir") },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Limpar busca")
                        }
                    }
                }
            )
        }

        item {
            OutlinedButton(
                onClick = {
                    customStationError = null
                    showAddStationDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Radio, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Adicionar rádio por URL")
            }
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 4.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }
        }

        item {
            SectionTitle(
                title = "Em destaque",
                subtitle = "Comece por uma seleção popular",
                onClick = { onOpenLink(tuneInHomeUrl) }
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 4.dp)
            ) {
                items(featuredStations, key = { it.url }) { station ->
                    TuneInFeaturedCard(
                        station = station,
                        onClick = {
                            onPlayStation(
                                station.name,
                                station.category,
                                station.imageUrl,
                                station.tuneInId,
                                station.fallbackStreamUrl
                            )
                        }
                    )
                }
            }
        }

        item {
            SectionTitle(
                title = if (normalizedQuery.isBlank()) "Todas as estações" else "Resultados",
                subtitle = "Toque para ouvir no player interno • link para abrir a página"
            )
        }

        if (filteredStations.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
                    )
                ) {
                    Text(
                        text = "Nenhuma estação encontrada.",
                        modifier = Modifier.padding(20.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredStations, key = { it.url }) { station ->
                TuneInStationRow(
                    station = station,
                    onClick = {
                        onPlayStation(
                            station.name,
                            station.category,
                            station.imageUrl,
                            station.tuneInId,
                            station.fallbackStreamUrl
                        )
                    },
                    onOpenPage = { onOpenLink(station.url) }
                )
            }
        }
    }

    if (showAddStationDialog) {
        AlertDialog(
            onDismissRequest = { showAddStationDialog = false },
            title = { Text("Adicionar rádio") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Cole uma URL pública de áudio MP3, AAC ou HLS.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = customStationName,
                        onValueChange = { customStationName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Nome da estação") }
                    )
                    OutlinedTextField(
                        value = customStationUrl,
                        onValueChange = {
                            customStationUrl = it
                            customStationError = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("URL do stream") },
                        placeholder = { Text("https://servidor.com/radio.mp3") }
                    )
                    Text(
                        text = "Categoria",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories.drop(1)) { category ->
                            FilterChip(
                                selected = customStationCategory == category,
                                onClick = { customStationCategory = category },
                                label = { Text(category) }
                            )
                        }
                    }
                    customStationError?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val stationName = customStationName.trim()
                    val streamUrl = customStationUrl.trim()
                    if (stationName.isBlank()) {
                        customStationError = "Informe o nome da estação."
                    } else if (!streamUrl.startsWith("https://") && !streamUrl.startsWith("http://")) {
                        customStationError = "Informe uma URL http:// ou https:// válida."
                    } else {
                        val station = TuneInStation(
                            name = stationName,
                            category = customStationCategory,
                            imageUrl = "",
                            url = streamUrl,
                            fallbackStreamUrl = streamUrl
                        )
                        customStations.add(station)
                        showAddStationDialog = false
                        customStationName = ""
                        customStationUrl = ""
                        onPlayStation(station.name, station.category, station.imageUrl, null, station.fallbackStreamUrl)
                    }
                }) {
                    Text("Ouvir agora")
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

@Composable
private fun TuneInHeroCard(
    station: TuneInStation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
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
                    Text(
                        text = "RECOMENDADO PARA VOCÊ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Ouça ${station.category.lowercase()} ao vivo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.86f)
                    )
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 15.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Ouvir agora")
                    }
                }
                Spacer(Modifier.width(12.dp))
                StationArtwork(
                    station = station,
                    modifier = Modifier.size(116.dp)
                )
            }
        }
    }
}

@Composable
private fun TuneInStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (onClick != null) {
            Text(
                text = "Ver tudo",
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onClick)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TuneInFeaturedCard(
    station: TuneInStation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(184.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(122.dp)
            ) {
                StationArtwork(
                    station = station,
                    modifier = Modifier.fillMaxSize()
                )
                FilledIconButton(
                    onClick = onClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Ouvir ${station.name}"
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = station.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TuneInStationRow(
    station: TuneInStation,
    onClick: () -> Unit,
    onOpenPage: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StationArtwork(
                station = station,
                modifier = Modifier.size(64.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = station.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = station.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onOpenPage != null) {
                IconButton(onClick = onOpenPage) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Abrir página de ${station.name}"
                    )
                }
            }
            FilledIconButton(
                onClick = onClick,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Ouvir ${station.name}")
            }
        }
    }
}

@Composable
private fun StationArtwork(
    station: TuneInStation,
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
            imageVector = if (station.category == "Música") Icons.Default.MusicNote else Icons.Default.Radio,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
            modifier = Modifier.size(28.dp)
        )
        AsyncImage(
            model = station.imageUrl,
            contentDescription = "Capa de ${station.name}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
