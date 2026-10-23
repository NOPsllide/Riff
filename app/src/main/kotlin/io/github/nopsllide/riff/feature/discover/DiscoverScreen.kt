package io.github.nopsllide.riff.feature.discover

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.github.nopsllide.riff.core.designsystem.component.AudioLinesLogoIcon
import io.github.nopsllide.riff.core.designsystem.component.FigmaPlayActionIcon
import io.github.nopsllide.riff.core.designsystem.component.FigmaSkipForwardActionIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideClockIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideFolderOpenIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideHeartOutlinedIcon
import io.github.nopsllide.riff.core.designsystem.component.LucidePauseIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideSearchIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideXIcon
import io.github.nopsllide.riff.core.model.Artist
import io.github.nopsllide.riff.core.model.Track

@Composable
fun DiscoverScreen(
    hotTracks: List<Track> = emptyList(),
    popularArtists: List<Artist> = emptyList(),
    historyTracks: List<Track> = emptyList(),
    favoriteTracks: List<Track> = emptyList(),
    isLoading: Boolean = false,
    currentTrack: Track? = null,
    isPlaying: Boolean = false,
    onPlayPauseToggle: () -> Unit = {},
    onNextTrack: () -> Unit = {},
    onOpenFullPlayer: () -> Unit = {},
    searchQuery: String = "",
    onQueryChange: (String) -> Unit = {},
    selectedPlatform: String = "youtube",
    onPlatformSelect: (String) -> Unit = {},
    searchResults: List<Track> = emptyList(),
    isSearchLoading: Boolean = false,
    onTrackClick: (Track, List<Track>) -> Unit = { _, _ -> },
    onArtistClick: (Artist) -> Unit = {},
    onFavoriteToggle: (Track) -> Unit = {},
    onClearHistory: () -> Unit = {},
    onRefreshWave: () -> Unit = {},
    onPlayWave: (List<Track>, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val displayTracks = hotTracks
    val artists = popularArtists

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedGenre by remember { mutableStateOf("Tümü") }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        val scale = (maxWidth.value / 412f).coerceIn(0.75f, 1.25f)
        val navWidth = (maxWidth * 0.70f).coerceIn(250.dp, 280.dp)
        val musicCardWidth = (maxWidth * 0.92f).coerceIn(340.dp, 390.dp)

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.statusBarsPadding())
            Spacer(modifier = Modifier.height((12 * scale).dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (20 * scale).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AudioLinesLogoIcon(
                    size = (24 * scale).dp,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width((8 * scale).dp))
                BasicText(
                    text = "Riff",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = (22 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                )
            }

            Spacer(modifier = Modifier.height((16 * scale).dp))

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val forward = targetState > initialState
                    val enterSlide = slideInHorizontally(
                        initialOffsetX = { fullWidth -> if (forward) (fullWidth * 0.25f).toInt() else -(fullWidth * 0.25f).toInt() },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                    val exitSlide = slideOutHorizontally(
                        targetOffsetX = { fullWidth -> if (forward) -(fullWidth * 0.25f).toInt() else (fullWidth * 0.25f).toInt() },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) + fadeOut(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                    enterSlide.togetherWith(exitSlide)
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "tab_page_transition"
            ) { tab ->
                when (tab) {
                    0 -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = (20 * scale).dp)
                                .height((50 * scale).dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable { selectedTab = 1 }
                                .padding(horizontal = (16 * scale).dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LucideSearchIcon(
                                    size = (20 * scale).dp,
                                    tint = Color(0xFF8E8E93)
                                )
                                Spacer(modifier = Modifier.width((10 * scale).dp))
                                BasicText(
                                    text = "Şarkı, sanatçı veya tür ara...",
                                    style = TextStyle(
                                        color = Color(0xFF8E8E93),
                                        fontSize = (15 * scale).sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height((18 * scale).dp))

                        val genreList = listOf("Tümü", "Pop", "Rock", "Hip-Hop", "Elektronik", "Akustik", "Chill")
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = (20 * scale).dp),
                            horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)
                        ) {
                            items(genreList) { genre ->
                                val isSelected = selectedGenre == genre
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.08f))
                                    .clickable {
                                        selectedGenre = genre
                                        if (genre != "Tümü") {
                                            selectedTab = 1
                                            onQueryChange(genre)
                                        }
                                    }
                                    .padding(horizontal = (18 * scale).dp, vertical = (8 * scale).dp),
                                contentAlignment = Alignment.Center
                            ) {
                                BasicText(
                                    text = genre,
                                    style = TextStyle(
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = (14 * scale).sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height((24 * scale).dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = (20 * scale).dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicText(
                            text = "Trend Şarkılar",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = (20 * scale).sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )
                        )

                        if (displayTracks.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .clickable { onPlayWave(displayTracks, 0) }
                                    .padding(horizontal = (14 * scale).dp, vertical = (6 * scale).dp),
                                contentAlignment = Alignment.Center
                            ) {
                                BasicText(
                                    text = "Hepsini Çal",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = (13 * scale).sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height((14 * scale).dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = (20 * scale).dp),
                        horizontalArrangement = Arrangement.spacedBy((14 * scale).dp)
                    ) {
                        items(displayTracks) { track ->
                            Column(
                                modifier = Modifier
                                    .width((148 * scale).dp)
                                    .clickable { onTrackClick(track, displayTracks) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size((148 * scale).dp)
                                        .clip(RoundedCornerShape((18 * scale).dp))
                                        .background(Color(0xFF242424))
                                ) {
                                    if (track.thumbnail.isNotBlank()) {
                                        AsyncImage(
                                            model = track.thumbnail,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height((8 * scale).dp))
                                BasicText(
                                    text = track.title,
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = (15 * scale).sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.SansSerif
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height((2 * scale).dp))
                                BasicText(
                                    text = track.artist,
                                    style = TextStyle(
                                        color = Color(0xFF8E8E93),
                                        fontSize = (13 * scale).sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = FontFamily.SansSerif
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height((32 * scale).dp))

                    BasicText(
                        text = "Popüler Sanatçılar",
                        modifier = Modifier.padding(horizontal = (20 * scale).dp),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = (20 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    )

                    Spacer(modifier = Modifier.height((14 * scale).dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = (20 * scale).dp),
                        horizontalArrangement = Arrangement.spacedBy((16 * scale).dp)
                    ) {
                        items(artists) { artist ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width((108 * scale).dp)
                                    .clickable {
                                        selectedTab = 1
                                        onArtistClick(artist)
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size((108 * scale).dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF242424))
                                ) {
                                    if (artist.pictureUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = artist.pictureUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height((8 * scale).dp))
                                BasicText(
                                    text = artist.name,
                                    style = TextStyle(
                                        color = Color(0xFFCCCCCC),
                                        fontSize = (14 * scale).sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = FontFamily.SansSerif,
                                        textAlign = TextAlign.Center
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height((32 * scale).dp))

                    BasicText(
                        text = "Sana Özel Seçilenler",
                        modifier = Modifier.padding(horizontal = (20 * scale).dp),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = (20 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    )

                    Spacer(modifier = Modifier.height((12 * scale).dp))
                }

                itemsIndexed(displayTracks.take(8)) { idx, track ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTrackClick(track, displayTracks) }
                            .padding(horizontal = (20 * scale).dp, vertical = (7 * scale).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicText(
                            text = "${idx + 1}",
                            style = TextStyle(
                                color = Color(0xFF8E8E93),
                                fontSize = (14 * scale).sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            ),
                            modifier = Modifier.width((22 * scale).dp)
                        )

                        Box(
                            modifier = Modifier
                                .size((46 * scale).dp)
                                .clip(RoundedCornerShape((10 * scale).dp))
                                .background(Color(0xFF242424))
                        ) {
                            if (track.thumbnail.isNotBlank()) {
                                AsyncImage(
                                    model = track.thumbnail,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width((12 * scale).dp))

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = track.title,
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = (15 * scale).sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.SansSerif
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height((2 * scale).dp))
                            BasicText(
                                text = track.artist,
                                style = TextStyle(
                                    color = Color(0xFF8E8E93),
                                    fontSize = (13 * scale).sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height((180 * scale).dp))
                }
            }
        }

        1 -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = (20 * scale).dp)
                            .height((50 * scale).dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = (16 * scale).dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LucideSearchIcon(
                                size = (20 * scale).dp,
                                tint = Color(0xFF8E8E93)
                            )
                            Spacer(modifier = Modifier.width((10 * scale).dp))
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) {
                                    BasicText(
                                        text = "Şarkı veya sanatçı adı gir...",
                                        style = TextStyle(
                                            color = Color(0xFF8E8E93),
                                            fontSize = (16 * scale).sp,
                                            fontWeight = FontWeight.Normal,
                                            fontFamily = FontFamily.SansSerif
                                        )
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = onQueryChange,
                                    singleLine = true,
                                    cursorBrush = SolidColor(Color.White),
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = (16 * scale).sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = FontFamily.SansSerif
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size((28 * scale).dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .clickable { onQueryChange("") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    LucideXIcon(size = (14 * scale).dp, tint = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height((16 * scale).dp))
                }

                if (searchQuery.isNotBlank()) {
                    if (isSearchLoading) {
                        item {
                            BasicText(
                                text = "Aranıyor...",
                                modifier = Modifier.padding(horizontal = (20 * scale).dp, vertical = (20 * scale).dp),
                                style = TextStyle(color = Color(0xFF8E8E93), fontSize = (16 * scale).sp)
                            )
                        }
                    } else if (searchResults.isEmpty()) {
                        item {
                            BasicText(
                                text = "Sonuç bulunamadı",
                                modifier = Modifier.padding(horizontal = (20 * scale).dp, vertical = (20 * scale).dp),
                                style = TextStyle(color = Color(0xFF8E8E93), fontSize = (16 * scale).sp)
                            )
                        }
                    } else {
                        items(searchResults) { track ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onTrackClick(track, searchResults) }
                                    .padding(horizontal = (20 * scale).dp, vertical = (8 * scale).dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size((48 * scale).dp)
                                        .clip(RoundedCornerShape((10 * scale).dp))
                                        .background(Color(0xFF262626))
                                ) {
                                    if (track.thumbnail.isNotBlank()) {
                                        AsyncImage(
                                            model = track.thumbnail,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width((12 * scale).dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    BasicText(
                                        text = track.title,
                                        style = TextStyle(color = Color.White, fontSize = (15 * scale).sp, fontWeight = FontWeight.SemiBold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height((2 * scale).dp))
                                    BasicText(
                                        text = track.artist,
                                        style = TextStyle(color = Color(0xFF8E8E93), fontSize = (13 * scale).sp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                } else {
                    item {
                        BasicText(
                            text = "Popüler Türler",
                            modifier = Modifier.padding(horizontal = (20 * scale).dp, vertical = (10 * scale).dp),
                            style = TextStyle(color = Color.White, fontSize = (18 * scale).sp, fontWeight = FontWeight.Bold)
                        )

                        val searchGenres = listOf("Pop", "Rock", "Rap & Hip-Hop", "Elektronik", "R&B", "Klasik", "Caz", "Akustik", "Lo-Fi", "Chill")
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = (20 * scale).dp),
                            verticalArrangement = Arrangement.spacedBy((10 * scale).dp)
                        ) {
                            for (pair in searchGenres.chunked(2)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy((10 * scale).dp)
                                ) {
                                    for (g in pair) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height((56 * scale).dp)
                                                .clip(RoundedCornerShape((14 * scale).dp))
                                                .background(Color(0xFF1E1E1E))
                                                .clickable { onQueryChange(g) }
                                                .padding(horizontal = (14 * scale).dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            BasicText(
                                                text = g,
                                                style = TextStyle(color = Color.White, fontSize = (15 * scale).sp, fontWeight = FontWeight.SemiBold)
                                            )
                                        }
                                    }
                                    if (pair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height((180 * scale).dp))
                }
            }
        }

        2 -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = (20 * scale).dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            BasicText(
                                text = "Kitaplığım",
                                style = TextStyle(color = Color.White, fontSize = (22 * scale).sp, fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height((2 * scale).dp))
                            BasicText(
                                text = "Beğenilen Şarkılar (${favoriteTracks.size})",
                                style = TextStyle(color = Color(0xFF8E8E93), fontSize = (14 * scale).sp)
                            )
                        }

                        if (favoriteTracks.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color.White)
                                    .clickable { onPlayWave(favoriteTracks, 0) }
                                    .padding(horizontal = (16 * scale).dp, vertical = (7 * scale).dp),
                                contentAlignment = Alignment.Center
                            ) {
                                BasicText(
                                    text = "Çal",
                                    style = TextStyle(color = Color.Black, fontSize = (13 * scale).sp, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height((20 * scale).dp))
                }

                if (favoriteTracks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = (32 * scale).dp, vertical = (60 * scale).dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LucideHeartOutlinedIcon(size = (48 * scale).dp, tint = Color(0xFF555555))
                            Spacer(modifier = Modifier.height((16 * scale).dp))
                            BasicText(
                                text = "Henüz beğendiğin bir şarkı yok",
                                style = TextStyle(color = Color.White, fontSize = (16 * scale).sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                            )
                            Spacer(modifier = Modifier.height((6 * scale).dp))
                            BasicText(
                                text = "Şarkı çalarken kalp butonuna basarak favorilerine ekleyebilirsin.",
                                style = TextStyle(color = Color(0xFF8E8E93), fontSize = (13 * scale).sp, textAlign = TextAlign.Center)
                            )
                        }
                    }
                } else {
                    items(favoriteTracks) { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTrackClick(track, favoriteTracks) }
                                .padding(horizontal = (20 * scale).dp, vertical = (8 * scale).dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size((48 * scale).dp)
                                    .clip(RoundedCornerShape((10 * scale).dp))
                                    .background(Color(0xFF262626))
                            ) {
                                if (track.thumbnail.isNotBlank()) {
                                    AsyncImage(
                                        model = track.thumbnail,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width((12 * scale).dp))
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = track.title,
                                    style = TextStyle(color = Color.White, fontSize = (15 * scale).sp, fontWeight = FontWeight.SemiBold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height((2 * scale).dp))
                                BasicText(
                                    text = track.artist,
                                    style = TextStyle(color = Color(0xFF8E8E93), fontSize = (13 * scale).sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size((36 * scale).dp)
                                    .clickable { onFavoriteToggle(track) },
                                contentAlignment = Alignment.Center
                            ) {
                                LucideHeartOutlinedIcon(size = (20 * scale).dp, tint = Color.White)
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height((180 * scale).dp))
                }
            }
        }

        3 -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = (20 * scale).dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicText(
                            text = "Dinleme Geçmişi",
                            style = TextStyle(color = Color.White, fontSize = (22 * scale).sp, fontWeight = FontWeight.Bold)
                        )

                        if (historyTracks.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color.White.copy(alpha = 0.10f))
                                    .clickable { onClearHistory() }
                                    .padding(horizontal = (14 * scale).dp, vertical = (6 * scale).dp),
                                contentAlignment = Alignment.Center
                            ) {
                                BasicText(
                                    text = "Temizle",
                                    style = TextStyle(color = Color(0xFF8E8E93), fontSize = (13 * scale).sp, fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height((16 * scale).dp))
                }

                if (historyTracks.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = (32 * scale).dp, vertical = (60 * scale).dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LucideClockIcon(size = (48 * scale).dp, tint = Color(0xFF555555))
                            Spacer(modifier = Modifier.height((16 * scale).dp))
                            BasicText(
                                text = "Henüz dinleme geçmişi yok",
                                style = TextStyle(color = Color.White, fontSize = (16 * scale).sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                            )
                            Spacer(modifier = Modifier.height((6 * scale).dp))
                            BasicText(
                                text = "Çaldığın şarkılar burada listelenecektir.",
                                style = TextStyle(color = Color(0xFF8E8E93), fontSize = (13 * scale).sp, textAlign = TextAlign.Center)
                            )
                        }
                    }
                } else {
                    items(historyTracks) { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTrackClick(track, historyTracks) }
                                .padding(horizontal = (20 * scale).dp, vertical = (8 * scale).dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size((48 * scale).dp)
                                    .clip(RoundedCornerShape((10 * scale).dp))
                                    .background(Color(0xFF262626))
                            ) {
                                if (track.thumbnail.isNotBlank()) {
                                    AsyncImage(
                                        model = track.thumbnail,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width((12 * scale).dp))
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = track.title,
                                    style = TextStyle(color = Color.White, fontSize = (15 * scale).sp, fontWeight = FontWeight.SemiBold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height((2 * scale).dp))
                                BasicText(
                                    text = track.artist,
                                    style = TextStyle(color = Color(0xFF8E8E93), fontSize = (13 * scale).sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height((180 * scale).dp))
                }
            }
        }
    }
}
}

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF121212).copy(alpha = 0.85f),
                                Color(0xFF121212)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentTrack != null) {
                    Box(
                        modifier = Modifier
                            .size(musicCardWidth, 64.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color(0xFF212121))
                            .clickable {
                                onOpenFullPlayer()
                            }
                            .padding(start = 7.dp, end = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2C2C2C)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentTrack.thumbnail.isNotBlank()) {
                                    AsyncImage(
                                        model = currentTrack.thumbnail,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = currentTrack.title,
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.SansSerif
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                BasicText(
                                    text = currentTrack.artist,
                                    style = TextStyle(
                                        color = Color(0xFF8E8E93),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = FontFamily.SansSerif
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable {
                                        onPlayPauseToggle()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isPlaying) {
                                    LucidePauseIcon(
                                        size = 18.dp,
                                        tint = Color.Black
                                    )
                                } else {
                                    FigmaPlayActionIcon(
                                        size = 18.dp,
                                        tint = Color.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Box(
                                modifier = Modifier
                                    .clickable { onNextTrack() },
                                contentAlignment = Alignment.Center
                            ) {
                                FigmaSkipForwardActionIcon(
                                    size = 20.dp,
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                val dockTabWidth = (navWidth - 10.dp) / 4
                val dockIndicatorOffset by animateDpAsState(
                    targetValue = dockTabWidth * selectedTab,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "dockIndicatorOffset"
                )

                Box(
                    modifier = Modifier
                        .size(navWidth, 64.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color(0xFF212121))
                        .padding(5.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .offset(x = dockIndicatorOffset)
                            .width(dockTabWidth)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                    )

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val navTabs = listOf(0, 1, 2, 3)
                        for (tabIdx in navTabs) {
                            val isActive = selectedTab == tabIdx
                            val tabScale by animateFloatAsState(
                                targetValue = if (isActive) 1.10f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "tabScale"
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        selectedTab = tabIdx
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = tabScale
                                        scaleY = tabScale
                                    },
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (tabIdx) {
                                        0 -> AudioLinesLogoIcon(
                                            size = 26.dp,
                                            tint = if (isActive) Color.White else Color(0xFF8E8E93)
                                        )
                                        1 -> LucideSearchIcon(
                                            size = 26.dp,
                                            tint = if (isActive) Color.White else Color(0xFF8E8E93)
                                        )
                                        2 -> LucideFolderOpenIcon(
                                            size = 26.dp,
                                            tint = if (isActive) Color.White else Color(0xFF8E8E93)
                                        )
                                        3 -> LucideClockIcon(
                                            size = 26.dp,
                                            tint = if (isActive) Color.White else Color(0xFF8E8E93)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
