@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.swipeclean.app.ui.screens

import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.swipeclean.app.data.Photo
import com.swipeclean.app.ui.AppViewModel
import com.swipeclean.app.ui.Screen
import com.swipeclean.app.ui.components.AppScaffold
import com.swipeclean.app.ui.components.HeroIcon
import com.swipeclean.app.ui.components.PhotoImage
import com.swipeclean.app.ui.components.formatBytes
import com.swipeclean.app.ui.components.formatDate

/** Final check before anything is deleted: confirm, review, or cancel. */
@Composable
fun ConfirmDeleteScreen(vm: AppViewModel) {
    val photos = vm.queuedPhotos
    val scheme = MaterialTheme.colorScheme
    var askCancel by remember { mutableStateOf(false) }
    val plural = if (photos.size == 1) "" else "s"

    AppScaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ready to delete?") },
                navigationIcon = {
                    IconButton(onClick = vm::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                },
            )
        },
        bottomBar = {
            if (photos.isNotEmpty()) {
                Surface(color = scheme.surfaceContainer) {
                    Column(
                        Modifier
                            .navigationBarsPadding()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = vm::confirmDeletion,
                            enabled = !vm.deleting,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = scheme.error, contentColor = scheme.onError),
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                        ) {
                            if (vm.deleting) {
                                CircularProgressIndicator(Modifier.size(20.dp), color = scheme.onError, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Rounded.DeleteForever, null, Modifier.size(ButtonDefaults.IconSize))
                                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                                Text("Delete ${photos.size} photo$plural")
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { vm.navigate(Screen.Review) },
                            enabled = !vm.deleting,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                        ) { Text("Review selection") }
                        TextButton(
                            onClick = { askCancel = true },
                            enabled = !vm.deleting,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Cancel deletion") }
                    }
                }
            }
        },
    ) { padding ->
        if (photos.isEmpty()) {
            Box(Modifier.padding(padding)) {
                EmptyState("Your deletion queue is empty", "Swipe left on photos you want to remove.", vm::goHome)
            }
            return@AppScaffold
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(104.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.padding(bottom = 12.dp)) {
                    Text("Delete ${photos.size} photo$plural?", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "This frees about ${formatBytes(vm.queuedBytes)}.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = scheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = scheme.secondaryContainer, contentColor = scheme.onSecondaryContainer)) {
                        ListItem(
                            leadingContent = { Icon(Icons.Rounded.Info, null) },
                            headlineContent = {
                                Text(
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                                        "Android will ask you to approve once more. Deleted photos can't be restored by SwipeClean."
                                    else
                                        "Deleted photos are removed from your device and can't be restored by SwipeClean.",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                    }
                }
            }
            items(photos, key = { it.id }) { photo ->
                PhotoImage(
                    photo,
                    Modifier
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.small)
                )
            }
        }
    }

    if (askCancel) {
        AlertDialog(
            onDismissRequest = { askCancel = false },
            icon = { Icon(Icons.Rounded.Info, null) },
            title = { Text("Cancel deletion?") },
            text = { Text("All ${photos.size} photo$plural will be kept and removed from your deletion queue.") },
            confirmButton = {
                TextButton(onClick = { askCancel = false; vm.cancelDeletion() }) { Text("Keep all") }
            },
            dismissButton = {
                TextButton(onClick = { askCancel = false }) { Text("Go back") }
            },
        )
    }
}

/** Tap to toggle a photo between "delete" and "keep"; long-press to view it large. */
@Composable
fun ReviewScreen(vm: AppViewModel) {
    // Snapshot so un-marked photos stay visible and can be re-marked.
    val items = remember { vm.queuedPhotos }
    var preview by remember { mutableStateOf<Photo?>(null) }
    val selected = items.count { it.id in vm.queue }
    val scheme = MaterialTheme.colorScheme

    AppScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Review selection")
                        Text(
                            "$selected selected to delete",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = vm::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                },
            )
        },
        bottomBar = {
            Surface(color = scheme.surfaceContainer) {
                Button(
                    onClick = { if (selected == 0) vm.goHome() else vm.back() },
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Text(if (selected == 0) "Keep all & go home" else "Done")
                }
            }
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(104.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    "Tap a photo to keep it. Long-press to view it larger.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            items(items, key = { it.id }) { photo ->
                val marked = photo.id in vm.queue
                Box(
                    Modifier
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.small)
                        .combinedClickable(
                            onClick = { vm.toggleQueued(photo) },
                            onLongClick = { preview = photo },
                        )
                ) {
                    PhotoImage(photo, Modifier.fillMaxSize())
                    if (!marked) Box(Modifier.fillMaxSize().background(scheme.scrim.copy(alpha = 0.5f)))
                    Icon(
                        if (marked) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = if (marked) "Marked for deletion" else "Kept",
                        tint = if (marked) scheme.error else Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .background(if (marked) Color.White else Color.Transparent, CircleShape)
                            .size(24.dp),
                    )
                }
            }
        }
    }

    preview?.let { photo ->
        Dialog(onDismissRequest = { preview = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .combinedClickable(onClick = { preview = null })
            ) {
                PhotoImage(photo, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                Text(
                    "${formatDate(photo.date)} · ${formatBytes(photo.size)}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(24.dp),
                )
            }
        }
    }
}

@Composable
internal fun EmptyState(title: String, body: String, onHome: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        HeroIcon(Icons.Rounded.Inbox)
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onClick = onHome) { Text("Back home") }
    }
}
