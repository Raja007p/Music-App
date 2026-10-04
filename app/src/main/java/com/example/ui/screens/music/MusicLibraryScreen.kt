package com.example.ui.screens.music

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song
import com.example.ui.components.ArtworkImage
import com.example.ui.components.formatDuration
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

import com.example.ui.theme.LibraryLayout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

enum class LibraryTab(val title: String) {
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    GENRES("Genres"),
    FOLDERS("Folders")
}

enum class SortMode(val label: String) {
    TITLE("Title (A-Z)"),
    ARTIST("Artist"),
    DURATION("Duration"),
    RECENT("Recently Added")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicLibraryScreen(
    songs: List<Song>,
    layout: LibraryLayout = LibraryLayout.TABBED_LIST,
    onSongClick: (Song, List<Song>) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    onRescan: () -> Unit = {},
    initialTab: LibraryTab = LibraryTab.SONGS
) {
    var selectedTab by remember { mutableStateOf(initialTab) }
    var searchQuery by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(SortMode.TITLE) }
    var showSortMenu by remember { mutableStateOf(false) }

    val filteredSongs = remember(songs, searchQuery, sortMode) {
        val filtered = if (searchQuery.isBlank()) {
            songs
        } else {
            songs.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true) ||
                        it.album.contains(searchQuery, ignoreCase = true)
            }
        }
        when (sortMode) {
            SortMode.TITLE -> filtered.sortedBy { it.title.lowercase() }
            SortMode.ARTIST -> filtered.sortedBy { it.artist.lowercase() }
            SortMode.DURATION -> filtered.sortedByDescending { it.durationMs }
            SortMode.RECENT -> filtered.sortedByDescending { it.dateAdded }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 90.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Music Library",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = TextWhite
                )
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = TextWhite
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(DarkSurfaceElevated)
                    ) {
                        SortMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = mode.label,
                                        color = if (mode == sortMode) MaterialTheme.colorScheme.primary else TextWhite
                                    )
                                },
                                onClick = {
                                    sortMode = mode
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 20.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    height = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = {}
        ) {
            LibraryTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else TextMuted
                            )
                        )
                    }
                )
            }
        }

        // Search Input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search songs, artists, albums...",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextMuted
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = TextMuted
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            )
        }

        // Content based on tab or empty state
        if (songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceElevated)
                            .border(1.dp, DarkBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Album,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Audio Files Found",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "No local music detected on your device. Place audio files in your Music folder and tap scan.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onRescan,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan Device Media")
                    }
                }
            }
        } else {
            when (selectedTab) {
                LibraryTab.SONGS -> {
                    if (layout == LibraryLayout.GRID_CARDS) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(filteredSongs, key = { it.id }) { song ->
                                AlbumCard(
                                    albumName = song.title,
                                    artist = song.artist,
                                    coverArt = song.albumArtUri,
                                    trackCount = 1,
                                    onClick = { onSongClick(song, filteredSongs) }
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(if (layout == LibraryLayout.COMPACT_LIST) 4.dp else 8.dp)
                        ) {
                            items(filteredSongs, key = { it.id }) { song ->
                                SongListItem(
                                    song = song,
                                    isCompact = layout == LibraryLayout.COMPACT_LIST,
                                    onClick = { onSongClick(song, filteredSongs) },
                                    onPlayNext = { onPlayNext(song) },
                                    onAddToQueue = { onAddToQueue(song) },
                                    onToggleFavorite = { onToggleFavorite(song) }
                                )
                            }
                        }
                    }
                }

            LibraryTab.ALBUMS -> {
                val albums = remember(filteredSongs) {
                    filteredSongs.groupBy { it.album.ifBlank { "Unknown Album" } }
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(albums.entries.toList()) { (albumName, albumSongs) ->
                        AlbumCard(
                            albumName = albumName,
                            artist = albumSongs.firstOrNull()?.artist ?: "Various Artists",
                            coverArt = albumSongs.firstOrNull()?.albumArtUri,
                            trackCount = albumSongs.size,
                            onClick = {
                                if (albumSongs.isNotEmpty()) {
                                    onSongClick(albumSongs.first(), albumSongs)
                                }
                            }
                        )
                    }
                }
            }

            LibraryTab.ARTISTS -> {
                val artists = remember(filteredSongs) {
                    filteredSongs.groupBy { it.artist.ifBlank { "Unknown Artist" } }
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(artists.entries.toList()) { (artistName, artistSongs) ->
                        ArtistCard(
                            artistName = artistName,
                            trackCount = artistSongs.size,
                            artwork = artistSongs.firstOrNull()?.albumArtUri,
                            onClick = {
                                if (artistSongs.isNotEmpty()) {
                                    onSongClick(artistSongs.first(), artistSongs)
                                }
                            }
                        )
                    }
                }
            }

            LibraryTab.GENRES -> {
                val genres = remember(filteredSongs) {
                    filteredSongs.groupBy { it.genre.ifBlank { "Pop" } }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(genres.entries.toList()) { (genre, genreSongs) ->
                        FolderOrGenreItem(
                            title = genre,
                            subtitle = "${genreSongs.size} tracks",
                            icon = Icons.Default.Album,
                            onClick = {
                                if (genreSongs.isNotEmpty()) {
                                    onSongClick(genreSongs.first(), genreSongs)
                                }
                            }
                        )
                    }
                }
            }

            LibraryTab.FOLDERS -> {
                val folders = remember(filteredSongs) {
                    filteredSongs.groupBy { it.folderName.ifBlank { "Music" } }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(folders.entries.toList()) { (folder, folderSongs) ->
                        FolderOrGenreItem(
                            title = folder,
                            subtitle = "${folderSongs.size} audio files",
                            icon = Icons.Default.Folder,
                            onClick = {
                                if (folderSongs.isNotEmpty()) {
                                    onSongClick(folderSongs.first(), folderSongs)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
fun SongListItem(
    song: Song,
    isCompact: Boolean = false,
    onClick: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (isCompact) 8.dp else 12.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(if (isCompact) 8.dp else 12.dp))
            .clickable { onClick() }
            .padding(if (isCompact) 6.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ArtworkImage(
            uri = song.albumArtUri,
            modifier = Modifier
                .size(if (isCompact) 36.dp else 48.dp)
                .clip(RoundedCornerShape(if (isCompact) 6.dp else 10.dp))
        )

        Spacer(modifier = Modifier.width(if (isCompact) 8.dp else 12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite,
                    fontSize = if (isCompact) 13.sp else 14.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = if (isCompact) 11.sp else 12.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = formatDuration(song.durationMs),
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextMuted,
                fontSize = 12.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(DarkSurfaceElevated)
            ) {
                DropdownMenuItem(
                    text = { Text("Play Next", color = TextWhite) },
                    onClick = {
                        onPlayNext()
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add to Queue", color = TextWhite) },
                    onClick = {
                        onAddToQueue()
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            if (song.isFavorite) "Remove from Favorites" else "Add to Favorites",
                            color = TextWhite
                        )
                    },
                    onClick = {
                        onToggleFavorite()
                        menuExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AlbumCard(
    albumName: String,
    artist: String,
    coverArt: String?,
    trackCount: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        ArtworkImage(
            uri = coverArt,
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = albumName,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextWhite
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "$artist • $trackCount tracks",
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextMuted,
                fontSize = 11.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ArtistCard(
    artistName: String,
    trackCount: Int,
    artwork: String?,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        ArtworkImage(
            uri = artwork,
            fallbackIcon = Icons.Default.Person,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = artistName,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextWhite
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "$trackCount tracks",
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextMuted,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
fun FolderOrGenreItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted
                )
            )
        }
    }
}
