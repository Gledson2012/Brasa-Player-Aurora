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

private const val radiosHomeUrl = "https://www.radios.com.br/"
private const val radiosTopUrl = "https://www.radios.com.br/#topradios-tabpanel"

private data class RadioStation(
    val name: String,
    val category: String,
    val location: String,
    val imageUrl: String,
    val url: String,
    val radioId: String? = null,
    val streamUrl: String? = null
)

private val radioStations = listOf(
    RadioStation(
        name = "Rádio Jornal 91.3 FM",
        category = "Notícias",
        location = "Aracaju / SE",
        imageUrl = "https://img.radios.com.br/radio/md/radio13492_1693568264.png",
        url = "https://www.radios.com.br/aovivo/radio-jornal-913-fm/13492",
        radioId = "13492",
        streamUrl = "https://www.radios.com.br/play/playlist/13492/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Regional 91.5 FM",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio13734_1739184764.jpg",
        url = "https://www.radios.com.br/aovivo/radio-regional-915-fm/13734",
        radioId = "13734",
        streamUrl = "https://www.radios.com.br/play/playlist/13734/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Rio FM 102.3",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio13416_1745327959.png",
        url = "https://www.radios.com.br/aovivo/radio-rio-fm-1023/13416",
        radioId = "13416",
        streamUrl = "https://www.radios.com.br/play/playlist/13416/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Vox 97.1 FM",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio13335_1759945649.jpg",
        url = "https://www.radios.com.br/aovivo/radio-vox-971-fm/13335",
        radioId = "13335",
        streamUrl = "https://www.radios.com.br/play/playlist/13335/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Metropolitana 98.5 FM",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio13900_1665067845.png",
        url = "https://www.radios.com.br/aovivo/radio-metropolitana-985-fm/13900",
        radioId = "13900",
        streamUrl = "https://www.radios.com.br/play/playlist/13900/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Costa do Sol 101.7 FM",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio12229_1725363304.png",
        url = "https://www.radios.com.br/aovivo/radio-costa-do-sol-1017-fm/12229",
        radioId = "12229",
        streamUrl = "https://www.radios.com.br/play/playlist/12229/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Morada Sertaneja",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio158074_1696528106.jpg",
        url = "https://www.radios.com.br/aovivo/radio-morada-sertaneja/158074",
        radioId = "158074",
        streamUrl = "https://www.radios.com.br/play/playlist/158074/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Nova Difusora 88.1 FM",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio15245_1686922098.jpeg",
        url = "https://www.radios.com.br/aovivo/radio-nova-difusora-881-fm/15245",
        radioId = "15245",
        streamUrl = "https://www.radios.com.br/play/playlist/15245/listen-radio.m3u"
    ),
    RadioStation(
        name = "MPB FM",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio271701_1771452183.jpg",
        url = "https://www.radios.com.br/aovivo/mpb-fm/271701",
        radioId = "271701",
        streamUrl = "https://www.radios.com.br/play/playlist/271701/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio CBN Salvador 107.9 FM",
        category = "Notícias",
        location = "Salvador / BA",
        imageUrl = "https://img.radios.com.br/radio/md/radio120034_1764155626.jpg",
        url = "https://www.radios.com.br/aovivo/radio-cbn-salvador-1079-fm/120034",
        radioId = "120034",
        streamUrl = "https://www.radios.com.br/play/playlist/120034/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Bandeirantes 107.3 FM",
        category = "Notícias",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio10410_1779370265.png",
        url = "https://www.radios.com.br/aovivo/radio-bandeirantes-1073-fm/10410",
        radioId = "10410",
        streamUrl = "https://www.radios.com.br/play/playlist/10410/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio VIP FM",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio189281_1721377229.jpeg",
        url = "https://www.radios.com.br/aovivo/radio-vip-fm/189281",
        radioId = "189281",
        streamUrl = "https://www.radios.com.br/play/playlist/189281/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Fan 99.7 FM",
        category = "Esportes",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio22355_1757079261.jpeg",
        url = "https://www.radios.com.br/aovivo/radio-fan-997-fm/22355",
        radioId = "22355",
        streamUrl = "https://www.radios.com.br/play/playlist/22355/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio CV Mais 97.5 FM",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio157979_1628521296.jpeg",
        url = "https://www.radios.com.br/aovivo/radio-cv-mais-975-fm/157979",
        radioId = "157979",
        streamUrl = "https://www.radios.com.br/play/playlist/157979/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Feliz 98.3 FM",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio231497_1784746427.jpg",
        url = "https://www.radios.com.br/aovivo/radio-feliz-983-fm/231497",
        radioId = "231497",
        streamUrl = "https://www.radios.com.br/play/playlist/231497/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Ankh",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio271389_1757380275.jpg",
        url = "https://www.radios.com.br/aovivo/radio-ankh/271389",
        radioId = "271389",
        streamUrl = "https://www.radios.com.br/play/playlist/271389/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Trans Mundial - RTM",
        category = "Gospel",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio18759_1456511123.jpg",
        url = "https://www.radios.com.br/aovivo/radio-trans-mundial-rtm/18759",
        radioId = "18759",
        streamUrl = "https://www.radios.com.br/play/playlist/18759/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Super Amazônia Brasil",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio150873_1593609322.jpg",
        url = "https://www.radios.com.br/aovivo/radio-super-amazonia-brasil/150873",
        radioId = "150873",
        streamUrl = "https://www.radios.com.br/play/playlist/150873/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Elite Rock",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio93525_1777219585.png",
        url = "https://www.radios.com.br/aovivo/radio-elite-rock/93525",
        radioId = "93525",
        streamUrl = "https://www.radios.com.br/play/playlist/93525/listen-radio.m3u"
    ),
    RadioStation(
        name = "Radio Saudade 99.7 FM",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio8_1781896993.png",
        url = "https://www.radios.com.br/aovivo/radio-saudade-997-fm/8",
        radioId = "8",
        streamUrl = "https://www.radios.com.br/play/playlist/8/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Tropical 94.1 FM",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio18854_1551092167.png",
        url = "https://www.radios.com.br/aovivo/radio-tropical-941-fm/18854",
        radioId = "18854",
        streamUrl = "https://www.radios.com.br/play/playlist/18854/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Cidade Verde 93.5 FM",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio42987_1573498279.jpg",
        url = "https://www.radios.com.br/aovivo/radio-cidade-verde-935-fm/42987",
        radioId = "42987",
        streamUrl = "https://www.radios.com.br/play/playlist/42987/listen-radio.m3u"
    ),
    RadioStation(
        name = "Radio Gospel Life",
        category = "Gospel",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio49251_1552934266.jpg",
        url = "https://www.radios.com.br/aovivo/radio-gospel-life/49251",
        radioId = "49251",
        streamUrl = "https://www.radios.com.br/play/playlist/49251/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Difusora Pantanal 101.9 FM",
        category = "Notícias",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio12797_1710378155.jpg",
        url = "https://www.radios.com.br/aovivo/radio-difusora-pantanal-1019-fm/12797",
        radioId = "12797",
        streamUrl = "https://www.radios.com.br/play/playlist/12797/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio 105 FM",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio142153_1587118918.jpg",
        url = "https://www.radios.com.br/aovivo/radio-105-fm/142153",
        radioId = "142153",
        streamUrl = "https://www.radios.com.br/play/playlist/142153/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Sinai Web Gospel",
        category = "Gospel",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio22540_1474915941.jpg",
        url = "https://www.radios.com.br/aovivo/radio-sinai-web-gospel/22540",
        radioId = "22540",
        streamUrl = "https://www.radios.com.br/play/playlist/22540/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Kboing 100.3 FM",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio29604_1785320946.png",
        url = "https://www.radios.com.br/aovivo/radio-kboing-1003-fm/29604",
        radioId = "29604",
        streamUrl = "https://www.radios.com.br/play/playlist/29604/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Cidade das Águas 101.3 FM",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio11672_1737636758.jpeg",
        url = "https://www.radios.com.br/aovivo/radio-cidade-das-aguas-1013-fm/11672",
        radioId = "11672",
        streamUrl = "https://www.radios.com.br/play/playlist/11672/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Cidade 100.7 FM",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio116_1597749797.png",
        url = "https://www.radios.com.br/aovivo/radio-cidade-1007-fm/116",
        radioId = "116",
        streamUrl = "https://www.radios.com.br/play/playlist/116/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio 93 FM",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio98_1551111060.jpg",
        url = "https://www.radios.com.br/aovivo/radio-93-fm/98",
        radioId = "98",
        streamUrl = "https://www.radios.com.br/play/playlist/98/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio Vibra",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio284028_1775829778.png",
        url = "https://www.radios.com.br/aovivo/radio-vibra/284028",
        radioId = "284028",
        streamUrl = "https://www.radios.com.br/play/playlist/284028/listen-radio.m3u"
    ),
    RadioStation(
        name = "Rádio FM O Dia 99.7 FM",
        category = "Música",
        location = "Brasil",
        imageUrl = "https://img.radios.com.br/radio/md/radio229211_1706128532.jpeg",
        url = "https://www.radios.com.br/aovivo/radio-fm-o-dia-997-fm/229211",
        radioId = "229211",
        streamUrl = "https://www.radios.com.br/play/playlist/229211/listen-radio.m3u"
    )
)

@Composable
fun RadiosScreen(
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
        androidx.compose.runtime.mutableStateListOf<RadioStation>()
    }
    val categories = listOf("Todos", "Música", "Notícias", "Esportes", "Gospel")
    val allStations = radioStations + customStations
    val normalizedQuery = searchQuery.trim()
    val filteredStations = allStations.filter { station ->
        val matchesCategory = selectedCategory == "Todos" || station.category == selectedCategory
        val matchesQuery = normalizedQuery.isBlank() ||
            station.name.contains(normalizedQuery, ignoreCase = true) ||
            station.category.contains(normalizedQuery, ignoreCase = true) ||
            station.location.contains(normalizedQuery, ignoreCase = true)
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
                    text = "RÁDIOS.COM.BR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp
                    )
                    Text(
                        text = "Milhares de rádios ao vivo",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Busque por nome, cidade, estado ou país",
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
            RadioHeroCard(station = heroStation, onClick = {
                onPlayStation(
                    heroStation.name,
                    heroStation.category,
                    heroStation.imageUrl,
                    heroStation.radioId,
                    heroStation.streamUrl
                )
            })
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RadioStatCard(
                    value = allStations.size.toString(),
                    label = "rádios em destaque",
                    modifier = Modifier.weight(1f)
                )
                RadioStatCard(
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
                        text = "Catálogo completo",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Explore milhares de rádios no Radios.com.br.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { onOpenLink(radiosHomeUrl) },
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Text("Explorar")
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
                placeholder = { Text("Buscar rádio, cidade ou país") },
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
                subtitle = "As mais acessadas no catálogo",
                onClick = { onOpenLink(radiosTopUrl) }
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 4.dp)
            ) {
                items(featuredStations, key = { it.url }) { station ->
                    RadioFeaturedCard(
                        station = station,
                        onClick = {
                            onPlayStation(
                                station.name,
                                station.category,
                                station.imageUrl,
                                station.radioId,
                                station.streamUrl
                            )
                        }
                    )
                }
            }
        }

        item {
            SectionTitle(
                title = if (normalizedQuery.isBlank()) "Todas as estações" else "Resultados",
                subtitle = "Toque para ouvir no player interno • link para abrir no Radios.com.br"
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
                RadioStationRow(
                    station = station,
                    onClick = {
                        onPlayStation(
                            station.name,
                            station.category,
                            station.imageUrl,
                            station.radioId,
                            station.streamUrl
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
                        val station = RadioStation(
                            name = stationName,
                            category = customStationCategory,
                            location = "URL personalizada",
                            imageUrl = "",
                            url = streamUrl,
                            streamUrl = streamUrl
                        )
                        customStations.add(station)
                        showAddStationDialog = false
                        customStationName = ""
                        customStationUrl = ""
                        onPlayStation(station.name, station.category, station.imageUrl, null, station.streamUrl)
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
private fun RadioHeroCard(
    station: RadioStation,
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
                        text = "${station.category} • ${station.location}",
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
private fun RadioStatCard(
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
private fun RadioFeaturedCard(
    station: RadioStation,
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
                    text = "${station.category} • ${station.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RadioStationRow(
    station: RadioStation,
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
                    text = "${station.category} • ${station.location}",
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
    station: RadioStation,
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
