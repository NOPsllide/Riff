package io.github.nopsllide.riff.feature.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import io.github.nopsllide.riff.core.designsystem.component.LucideSearchIcon
import io.github.nopsllide.riff.core.designsystem.component.LucideTelescopeIcon
import io.github.nopsllide.riff.core.model.Track

data class ArtistDisplay(
    val name: String,
    val imageUrl: String
)

private val defaultGlobalTracks = listOf(
    Track(id = "1", title = "HER GÜN AĞLADIM", artist = "Bege", album = "Single", duration = 210, thumbnail = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80", url = "", platform = "youtube"),
    Track(id = "2", title = "NKBİ", artist = "Güneş", album = "Atlantis", duration = 185, thumbnail = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80", url = "", platform = "youtube"),
    Track(id = "3", title = "Süzüler", artist = "Gazapizm", album = "Single", duration = 240, thumbnail = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80", url = "", platform = "youtube"),
    Track(id = "4", title = "Starboy", artist = "The Weeknd", album = "Starboy", duration = 230, thumbnail = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&auto=format&fit=crop&q=80", url = "", platform = "youtube"),
    Track(id = "5", title = "FE!N", artist = "Travis Scott", album = "UTOPIA", duration = 191, thumbnail = "https://images.unsplash.com/photo-1487180144351-b8472da7d491?w=600&auto=format&fit=crop&q=80", url = "", platform = "youtube"),
    Track(id = "6", title = "Levitating", artist = "Dua Lipa", album = "Future Nostalgia", duration = 203, thumbnail = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=600&auto=format&fit=crop&q=80", url = "", platform = "youtube")
)

private val defaultGlobalArtists = listOf(
    ArtistDisplay("Bege", "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80"),
    ArtistDisplay("Uzi", "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80"),
    ArtistDisplay("Ben Fero", "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&auto=format&fit=crop&q=80"),
    ArtistDisplay("The Weeknd", "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80"),
    ArtistDisplay("Travis Scott", "https://images.unsplash.com/photo-1487180144351-b8472da7d491?w=600&auto=format&fit=crop&q=80")
)

@Composable
fun DiscoverScreen(
    hotTracks: List<Track> = emptyList(),
    historyTracks: List<Track> = emptyList(),
    isLoading: Boolean = false,
    currentTrack: Track? = null,
    searchQuery: String = "",
    onQueryChange: (String) -> Unit = {},
    selectedPlatform: String = "youtube",
    onPlatformSelect: (String) -> Unit = {},
    searchResults: List<Track> = emptyList(),
    isSearchLoading: Boolean = false,
    onTrackClick: (Track, List<Track>) -> Unit = { _, _ -> },
    onFavoriteToggle: (Track) -> Unit = {},
    onRefreshWave: () -> Unit = {},
    onPlayWave: (List<Track>, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val displayTracks = if (hotTracks.isNotEmpty()) hotTracks else defaultGlobalTracks

    val artists = remember(displayTracks) {
        val list = displayTracks.map { track ->
            ArtistDisplay(
                name = track.artist,
                imageUrl = track.thumbnail
            )
        }.distinctBy { it.name }.filter { it.name.isNotBlank() }
        if (list.isNotEmpty()) list else defaultGlobalArtists
    }

    val primaryArtist = artists.firstOrNull()?.name ?: "Ezhel"
    val activeTrack = currentTrack ?: displayTracks.firstOrNull()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF181818))
    ) {
        val scale = (maxWidth.value / 606f).coerceIn(0.55f, 1.15f)

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-30 * scale).dp, y = (-60 * scale).dp)
                .size((220 * scale).dp, (200 * scale).dp)
                .blur(160.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Spacer(modifier = Modifier.statusBarsPadding())
                Spacer(modifier = Modifier.height((12 * scale).dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = (24 * scale).dp),
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

                Spacer(modifier = Modifier.height((16 * scale).dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = (24 * scale).dp),
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
                            BasicText(
                                text = "Search tracks, artists.",
                                style = TextStyle(
                                    color = Color(0xFF8E8E93),
                                    fontSize = (22 * scale).sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif
                                )
                            )
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
                                    .size((42 * scale).dp),
                                contentAlignment = Alignment.Center
                            ) {
                                FigmaYouTubeLogoIcon(
                                    size = (22 * scale).dp,
                                    tint = Color(0xFF8E8E93)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size((52 * scale).dp, (42 * scale).dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color.White.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                FigmaYouTubeMusicLogoIcon(
                                    size = (20 * scale).dp,
                                    tint = Color(0xFF8E8E93)
                                )
                            }
                            Box(
                                modifier = Modifier.size((42 * scale).dp),
                                contentAlignment = Alignment.Center
                            ) {
                                FigmaSoundCloudLogoIcon(
                                    size = (20 * scale).dp,
                                    tint = Color(0xFF8E8E93)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height((20 * scale).dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = (24 * scale).dp),
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

                Spacer(modifier = Modifier.height((28 * scale).dp))

                Column(modifier = Modifier.padding(horizontal = (24 * scale).dp)) {
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
                    Spacer(modifier = Modifier.height((8 * scale).dp))
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

                Spacer(modifier = Modifier.height((32 * scale).dp))

                Column(modifier = Modifier.padding(horizontal = (24 * scale).dp)) {
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
                                .background(Color.White),
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

                    Spacer(modifier = Modifier.height((18 * scale).dp))

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

                Spacer(modifier = Modifier.height((32 * scale).dp))

                BasicText(
                    text = "Because you listen to $primaryArtist",
                    modifier = Modifier.padding(horizontal = (24 * scale).dp),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = (26 * scale).sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif
                    )
                )

                Spacer(modifier = Modifier.height((16 * scale).dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = (24 * scale).dp),
                    horizontalArrangement = Arrangement.spacedBy((18 * scale).dp)
                ) {
                    items(artists) { artist ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width((162 * scale).dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size((162 * scale).dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF373737))
                            ) {
                                AsyncImage(
                                    model = artist.imageUrl,
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

                Spacer(modifier = Modifier.height((32 * scale).dp))

                BasicText(
                    text = "New for you",
                    modifier = Modifier.padding(horizontal = (24 * scale).dp),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = (26 * scale).sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.SansSerif
                    )
                )

                Spacer(modifier = Modifier.height((16 * scale).dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = (24 * scale).dp),
                    horizontalArrangement = Arrangement.spacedBy((18 * scale).dp)
                ) {
                    items(displayTracks) { track ->
                        Column(modifier = Modifier.width((217 * scale).dp)) {
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

                Spacer(modifier = Modifier.height((240 * scale).dp))
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
                    .height((80 * scale).dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF181818)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = (16 * scale).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .height((76 * scale).dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color(0xFF212121))
                        .padding(horizontal = (14 * scale).dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size((70 * scale).dp, (68 * scale).dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color(0xFF2C2C2C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size((53 * scale).dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF373737))
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
                        }

                        Spacer(modifier = Modifier.width((14 * scale).dp))

                        Column(modifier = Modifier.weight(1f)) {
                            BasicText(
                                text = activeTrack?.title ?: "NKBİ",
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = (20 * scale).sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height((2 * scale).dp))
                            BasicText(
                                text = activeTrack?.artist ?: "Güneş",
                                style = TextStyle(
                                    color = Color(0xFF8E8E93),
                                    fontSize = (18 * scale).sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width((10 * scale).dp))

                        Box(
                            modifier = Modifier
                                .size((40 * scale).dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            FigmaPlayActionIcon(
                                size = (18 * scale).dp,
                                tint = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.width((16 * scale).dp))

                        FigmaSkipForwardActionIcon(
                            size = (24 * scale).dp,
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height((10 * scale).dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .height((76 * scale).dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color(0xFF212121))
                        .padding(horizontal = (10 * scale).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size((93 * scale).dp, (70 * scale).dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color.White.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            LucideTelescopeIcon(
                                size = (32 * scale).dp,
                                tint = Color.White
                            )
                        }

                        LucideMessageCircleHeartIcon(
                            size = (32 * scale).dp,
                            tint = Color(0xFF8E8E93)
                        )

                        LucideFolderOpenIcon(
                            size = (32 * scale).dp,
                            tint = Color(0xFF8E8E93)
                        )

                        LucideClockIcon(
                            size = (32 * scale).dp,
                            tint = Color(0xFF8E8E93)
                        )

                        LucideCogIcon(
                            size = (32 * scale).dp,
                            tint = Color(0xFF8E8E93)
                        )
                    }
                }
            }
        }
    }
}