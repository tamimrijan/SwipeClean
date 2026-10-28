@file:OptIn(ExperimentalMaterial3Api::class)

package com.swipeclean.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.swipeclean.app.data.Photo
import com.swipeclean.app.ui.AppViewModel
import com.swipeclean.app.ui.Screen
import com.swipeclean.app.ui.components.AppScaffold
import com.swipeclean.app.ui.components.PhotoImage
import com.swipeclean.app.ui.components.formatBytes
import com.swipeclean.app.ui.components.formatDate

@Composable
fun GalleryScreen(vm: AppViewModel) {
    var album by rememberSaveable { mutableStateOf<Long?>(null) }
    var viewerIndex by remember { mutableStateOf<Int?>(null) }
    val filtered = remember(vm.photos, album) {
        if (album == null) vm.photos else vm.photos.filter { it.bucketId == album }
    }
    val scroll = TopAppBarDefaults.enterAlwaysScrollBehavior()

    AppScaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text("Gallery")
                            Text(
                                "${filtered.size} photos · ${formatBytes(filtered.sumOf { it.size })}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = vm::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                    },
                    actions = {
                        if (vm.queuedPhotos.isNotEmpty()) {
                            IconButton(onClick = { vm.navigate(Screen.Confirm) }) {
                                BadgedBox(badge = { Badge { Text("${vm.queuedPhotos.size}") } }) {
                                    Icon(Icons.Rounded.DeleteOutline, "Deletion queue")
                                }
                            }
                        }
                    },
                    scrollBehavior = scroll,
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    item {
                        FilterChip(
                            selected = album == null,
                            onClick = { album = null },
                            label = { Text("All · ${vm.photos.size}") },
                        )
                    }
                    items(vm.albums, key = { it.id }) { a ->
                        FilterChip(
                            selected = album == a.id,
                            onClick = { album = a.id },
                            label = { Text("${a.name} · ${a.count}") },
                            colors = FilterChipDefaults.filterChipColors(),
                        )
                    }
                }
            }
        },
    ) { padding ->
        when {
            vm.loading && vm.photos.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            filtered.isEmpty() -> Box(Modifier.padding(padding)) {
                EmptyState("No photos here", vm.loadError ?: "Photos you take will show up here.", vm::goHome)
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(96.dp),
                contentPadding = PaddingValues(
                    start = 8.dp, end = 8.dp,
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(filtered.size, key = { filtered[it].id }) { i ->
                    val photo = filtered[i]
                    Thumb(photo, queued = photo.id in vm.queue) { viewerIndex = i }
                }
            }
        }
    }

    viewerIndex?.let { start ->
        Viewer(vm, filtered, start, onClose = { viewerIndex = null })
    }
}

@Composable
private fun Thumb(photo: Photo, queued: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Box(
        Modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable(onClick = onClick)
    ) {
        PhotoImage(photo, Modifier.fillMaxSize())
        if (queued) {
            Box(Modifier.fillMaxSize().background(scheme.error.copy(alpha = 0.4f)))
            Icon(
                Icons.Rounded.Delete, "Marked for deletion", tint = Color.White,
                modifier = Modifier.align(Alignment.Center).size(28.dp),
            )
        }
        if (photo.isFavorite) {
            Icon(
                Icons.Rounded.Star, "Favorite", tint = Color.White,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(16.dp),
            )
        }
    }
}

@Composable
private fun Viewer(vm: AppViewModel, photos: List<Photo>, start: Int, onClose: () -> Unit) {
    BackHandler(onBack = onClose)
    val pager = rememberPagerState(initialPage = start.coerceIn(0, (photos.size - 1).coerceAtLeast(0))) { photos.size }
    val scheme = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(state = pager, key = { photos[it].id }, modifier = Modifier.fillMaxSize()) { page ->
            PhotoImage(photos[page], Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        }
        val photo = photos.getOrNull(pager.currentPage) ?: return@Box
        val queued = photo.id in vm.queue

        Row(
            Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.45f))
                .statusBarsPadding()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, "Close", tint = Color.White) }
            Column(Modifier.weight(1f)) {
                Text(formatDate(photo.date), color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${photo.bucketName} · ${formatBytes(photo.size)} · ${photo.width}×${photo.height}",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        val buttonModifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(24.dp)
            .height(56.dp)
        if (queued) {
            FilledTonalButton(
                onClick = { vm.toggleQueued(photo) },
                modifier = buttonModifier,
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            ) {
                Icon(Icons.Rounded.Close, null, Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text("Remove from deletion queue")
            }
        } else {
            Button(
                onClick = { vm.toggleQueued(photo) },
                modifier = buttonModifier,
                colors = ButtonDefaults.buttonColors(containerColor = scheme.error, contentColor = scheme.onError),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            ) {
                Icon(Icons.Rounded.DeleteOutline, null, Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text("Add to deletion queue")
            }
        }
    }
}
