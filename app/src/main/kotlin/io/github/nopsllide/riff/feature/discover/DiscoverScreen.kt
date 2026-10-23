package io.github.nopsllide.riff.feature.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import io.github.nopsllide.riff.core.designsystem.component.LucideCogIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideFolderOpenIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideMessageCircleHeartIcon
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
    onRefreshWave: () -> Unit = {},
    onPlayWave: (List<Track>, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val displayTracks = hotTracks
    val artists = popularArtists
    val activeTrack = currentTrack ?: displayTracks.firstOrNull()
    var selectedGenre by remember { mutableStateOf("Tümü") }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        val scale = (maxWidth.value / 412f).coerceIn(0.75f, 1.25f)

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
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
                                    text = "Şarkı, sanatçı veya tür ara...",
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
                item {
                    Spacer(modifier = Modifier.height((180 * scale).dp))
                }
            } else {
                item {
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
                                    .clickable { onArtistClick(artist) }
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
                if (activeTrack != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color(0xFF202020))
                            .clickable {
                                if (currentTrack == null) {
                                    onTrackClick(activeTrack, displayTracks)
                                }
                                onOpenFullPlayer()
                            }
                            .padding(start = 6.dp, end = 16.dp),
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
                                if (activeTrack.thumbnail.isNotBlank()) {
                                    AsyncImage(
                                        model = activeTrack.thumbnail,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = activeTrack.title,
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
                                    text = activeTrack.artist,
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
                                        val track = currentTrack ?: activeTrack
                                        if (isPlaying) {
                                            onPlayPauseToggle()
                                        } else {
                                            onTrackClick(track, displayTracks)
                                        }
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height(58.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color(0xFF202020))
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                AudioLinesLogoIcon(
                                    size = 22.dp,
                                    tint = Color.White
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            LucideSearchIcon(
                                size = 22.dp,
                                tint = Color(0xFF8E8E93)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            LucideFolderOpenIcon(
                                size = 22.dp,
                                tint = Color(0xFF8E8E93)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            LucideClockIcon(
                                size = 22.dp,
                                tint = Color(0xFF8E8E93)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            LucideCogIcon(
                                size = 22.dp,
                                tint = Color(0xFF8E8E93)
                            )
                        }
                    }
                }
            }
        }
    }
}
