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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import io.github.nopsllide.riff.core.designsystem.component.FigmaSoundCloudLogoIcon
import io.github.nopsllide.riff.core.designsystem.component.FigmaYouTubeLogoIcon
import io.github.nopsllide.riff.core.designsystem.component.FigmaYouTubeMusicLogoIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideClockIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideCogIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideFolderOpenIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideMessageCircleHeartIcon
import io.github.nopsllide.riff.core.designsystem.component.LucidePauseIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideSearchIcon
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
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val displayTracks = hotTracks
    val artists = popularArtists
    val activeTrack = currentTrack ?: displayTracks.firstOrNull()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF181818))
    ) {
        val scale = (maxWidth.value / 606f).coerceIn(0.55f, 1.15f)
        val navWidth = (maxWidth * 0.90f).coerceIn(340.dp, 380.dp)
        val musicCardWidth = navWidth * 0.95f

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.statusBarsPadding())
                Spacer(modifier = Modifier.height((12 * scale).dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = (26 * scale).dp),
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
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                }

                Spacer(modifier = Modifier.height((38 * scale).dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = (26 * scale).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(363f)
                            .height((62 * scale).dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(horizontal = (18 * scale).dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LucideSearchIcon(
                                size = (24 * scale).dp,
                                tint = Color(0xFF8E8E93)
                            )
                            Spacer(modifier = Modifier.width((12 * scale).dp))
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) {
                                    BasicText(
                                        text = "Search tracks, artists.",
                                        style = TextStyle(
                                            color = Color(0xFF8E8E93),
                                            fontSize = (20 * scale).sp,
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
                                        fontSize = (20 * scale).sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = FontFamily.SansSerif
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width((14 * scale).dp))

                    Box(
                        modifier = Modifier
                            .weight(180f)
                            .height((62 * scale).dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(horizontal = (6 * scale).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size((42 * scale).dp)
                                    .clickable { onPlatformSelect("youtube") },
                                contentAlignment = Alignment.Center
                            ) {
                                FigmaYouTubeLogoIcon(
                                    size = (22 * scale).dp,
                                    tint = if (selectedPlatform == "youtube") Color.White else Color(0xFF8E8E93)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size((52 * scale).dp, (42 * scale).dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (selectedPlatform == "youtubemusic") Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f))
                                    .clickable { onPlatformSelect("youtubemusic") },
                                contentAlignment = Alignment.Center
                            ) {
                                FigmaYouTubeMusicLogoIcon(
                                    size = (20 * scale).dp,
                                    tint = if (selectedPlatform == "youtubemusic") Color.White else Color(0xFF8E8E93)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size((42 * scale).dp)
                                    .clickable { onPlatformSelect("soundcloud") },
                                contentAlignment = Alignment.Center
                            ) {
                                FigmaSoundCloudLogoIcon(
                                    size = (20 * scale).dp,
                                    tint = if (selectedPlatform == "soundcloud") Color.White else Color(0xFF8E8E93)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height((30 * scale).dp))
            }

            if (searchQuery.isNotBlank()) {
                if (isSearchLoading) {
                    item {
                        BasicText(
                            text = "Searching...",
                            modifier = Modifier.padding(horizontal = (26 * scale).dp, vertical = 20.dp),
                            style = TextStyle(color = Color(0xFF8E8E93), fontSize = (18 * scale).sp)
                        )
                    }
                } else if (searchResults.isEmpty()) {
                    item {
                        BasicText(
                            text = "No results found",
                            modifier = Modifier.padding(horizontal = (26 * scale).dp, vertical = 20.dp),
                            style = TextStyle(color = Color(0xFF8E8E93), fontSize = (18 * scale).sp)
                        )
                    }
                } else {
                    items(searchResults) { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTrackClick(track, searchResults) }
                                .padding(horizontal = (26 * scale).dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF373737))
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
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                BasicText(
                                    text = track.title,
                                    style = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                BasicText(
                                    text = track.artist,
                                    style = TextStyle(color = Color(0xFF8E8E93), fontSize = 13.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(200.dp))
                }
            } else {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = (26 * scale).dp),
                        horizontalArrangement = Arrangement.spacedBy((10 * scale).dp)
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .height((53 * scale).dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .padding(horizontal = (24 * scale).dp),
                                contentAlignment = Alignment.Center
                            ) {
                                BasicText(
                                    text = "All",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = (22 * scale).sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                )
                            }
                        }
                        val tabsList = listOf("Relax", "Workout", "Electronic", "Rock")
                        items(tabsList) { tabName ->
                            Box(
                                modifier = Modifier
                                    .height((53 * scale).dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .padding(horizontal = (24 * scale).dp),
                                contentAlignment = Alignment.Center
                            ) {
                                BasicText(
                                    text = tabName,
                                    style = TextStyle(
                                        color = Color(0xFF8E8E93),
                                        fontSize = (22 * scale).sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = FontFamily.SansSerif
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height((53 * scale).dp))

                    Column(modifier = Modifier.padding(horizontal = (26 * scale).dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicText(
                                text = "Jump back in",
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = (26 * scale).sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif
                                )
                            )
                            BasicText(
                                text = "Recently played",
                                style = TextStyle(
                                    color = Color(0xFF8E8E93),
                                    fontSize = (20 * scale).sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height((31 * scale).dp))
                        if (historyTracks.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy((14 * scale).dp)
                            ) {
                                items(historyTracks.take(8)) { trk ->
                                    Column(
                                        modifier = Modifier
                                            .width((140 * scale).dp)
                                            .clickable { onTrackClick(trk, historyTracks) }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size((140 * scale).dp)
                                                .clip(RoundedCornerShape((18 * scale).dp))
                                                .background(Color(0xFF373737))
                                        ) {
                                            AsyncImage(
                                                model = trk.thumbnail,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        Spacer(modifier = Modifier.height((6 * scale).dp))
                                        BasicText(
                                            text = trk.title,
                                            style = TextStyle(
                                                color = Color.White,
                                                fontSize = (17 * scale).sp,
                                                fontWeight = FontWeight.Medium,
                                                fontFamily = FontFamily.SansSerif
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        } else {
                            BasicText(
                                text = "Play some tracks to see them here",
                                style = TextStyle(
                                    color = Color(0xFF8E8E93),
                                    fontSize = (20 * scale).sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height((82 * scale).dp))

                    Column(modifier = Modifier.padding(horizontal = (28 * scale).dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicText(
                                text = "My Wave",
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = (32 * scale).sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif
                                )
                            )

                            Box(
                                modifier = Modifier
                                    .size((185 * scale).dp, (54 * scale).dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color.White)
                                    .clickable {
                                        if (displayTracks.isNotEmpty()) {
                                            onPlayWave(displayTracks, 0)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    FigmaPlayActionIcon(
                                        size = (20 * scale).dp,
                                        tint = Color.Black
                                    )
                                    Spacer(modifier = Modifier.width((8 * scale).dp))
                                    BasicText(
                                        text = "Play wave",
                                        style = TextStyle(
                                            color = Color.Black,
                                            fontSize = (21 * scale).sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.SansSerif
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height((175 * scale).dp))

                        BasicText(
                            text = "Save at least 5 tracks to tune\nMy Wave to your taste.",
                            modifier = Modifier.fillMaxWidth(),
                            style = TextStyle(
                                color = Color(0xFF8E8E93),
                                fontSize = (21 * scale).sp,
                                lineHeight = (28 * scale).sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.SansSerif,
                                textAlign = TextAlign.Center
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height((205 * scale).dp))

                    BasicText(
                        text = "Popular artists",
                        modifier = Modifier.padding(horizontal = (34 * scale).dp),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = (26 * scale).sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        )
                    )

                    Spacer(modifier = Modifier.height((27 * scale).dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = (26 * scale).dp),
                        horizontalArrangement = Arrangement.spacedBy((18 * scale).dp)
                    ) {
                        items(artists) { artist ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width((162 * scale).dp)
                                    .clickable { onArtistClick(artist) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size((162 * scale).dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF373737))
                                ) {
                                    AsyncImage(
                                        model = artist.pictureUrl,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.height((10 * scale).dp))
                                BasicText(
                                    text = artist.name,
                                    style = TextStyle(
                                        color = Color(0xFF8E8E93),
                                        fontSize = (20 * scale).sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = FontFamily.SansSerif,
                                        textAlign = TextAlign.Center
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height((48 * scale).dp))

                    BasicText(
                        text = "New for you",
                        modifier = Modifier.padding(horizontal = (26 * scale).dp),
                        style = TextStyle(
                            color = Color.White,
                            fontSize = (26 * scale).sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        )
                    )

                    Spacer(modifier = Modifier.height((20 * scale).dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = (26 * scale).dp),
                        horizontalArrangement = Arrangement.spacedBy((18 * scale).dp)
                    ) {
                        items(displayTracks) { track ->
                            Column(
                                modifier = Modifier
                                    .width((217 * scale).dp)
                                    .clickable { onTrackClick(track, displayTracks) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size((217 * scale).dp)
                                        .clip(RoundedCornerShape((24 * scale).dp))
                                        .background(Color(0xFF373737))
                                ) {
                                    AsyncImage(
                                        model = track.thumbnail,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.height((10 * scale).dp))
                                BasicText(
                                    text = track.title,
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = (20 * scale).sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height((4 * scale).dp))
                                BasicText(
                                    text = track.artist,
                                    style = TextStyle(
                                        color = Color(0xFF8E8E93),
                                        fontSize = (20 * scale).sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = FontFamily.SansSerif
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(200.dp))
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
                    .height(180.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF181818).copy(alpha = 0.9f),
                                Color(0xFF181818)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(musicCardWidth, 64.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color(0xFF212121))
                        .clickable {
                            if (currentTrack == null && activeTrack != null) {
                                onTrackClick(activeTrack, displayTracks)
                            }
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
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF373737)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (activeTrack != null && activeTrack.thumbnail.isNotBlank()) {
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
                                text = activeTrack?.title ?: "",
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.SansSerif
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            BasicText(
                                text = activeTrack?.artist ?: "",
                                style = TextStyle(
                                    color = Color(0xFF8E8E93),
                                    fontSize = 13.sp,
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable {
                                    if (currentTrack == null && activeTrack != null) {
                                        onTrackClick(activeTrack, displayTracks)
                                    } else {
                                        onPlayPauseToggle()
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

                        Spacer(modifier = Modifier.width(14.dp))

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

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .size(navWidth, 66.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color(0xFF212121))
                        .padding(5.dp),
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
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                AudioLinesLogoIcon(
                                    size = 28.dp,
                                    tint = Color.White
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onNavigateToFavorites() },
                            contentAlignment = Alignment.Center
                        ) {
                            LucideMessageCircleHeartIcon(
                                size = 28.dp,
                                tint = Color(0xFF8E8E93)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onNavigateToLibrary() },
                            contentAlignment = Alignment.Center
                        ) {
                            LucideFolderOpenIcon(
                                size = 28.dp,
                                tint = Color(0xFF8E8E93)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onNavigateToHistory() },
                            contentAlignment = Alignment.Center
                        ) {
                            LucideClockIcon(
                                size = 28.dp,
                                tint = Color(0xFF8E8E93)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onNavigateToSettings() },
                            contentAlignment = Alignment.Center
                        ) {
                            LucideCogIcon(
                                size = 28.dp,
                                tint = Color(0xFF8E8E93)
                            )
                        }
                    }
                }
            }
        }
    }
}
