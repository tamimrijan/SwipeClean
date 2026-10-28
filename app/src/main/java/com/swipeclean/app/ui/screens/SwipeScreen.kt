@file:OptIn(ExperimentalMaterial3Api::class)

package com.swipeclean.app.ui.screens

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swipeclean.app.data.Photo
import com.swipeclean.app.ui.AppViewModel
import com.swipeclean.app.ui.Messages
import com.swipeclean.app.ui.components.AppScaffold
import com.swipeclean.app.ui.components.PhotoImage
import com.swipeclean.app.ui.components.formatBytes
import com.swipeclean.app.ui.components.formatDate
import com.swipeclean.app.ui.theme.AppTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun SwipeScreen(vm: AppViewModel) {
    val photos = vm.sessionPhotos
    val index = vm.decisions.size
    val current = photos.getOrNull(index)
    val next = photos.getOrNull(index + 1)
    val sessionDeletes = vm.decisions.count { !it.keep }
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val scheme = MaterialTheme.colorScheme
    val extended = AppTheme.extended

    // Card width drives thresholds; start from the screen width until the card is measured.
    val screenWidthPx = with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    var cardWidth by remember { mutableFloatStateOf(screenWidthPx) }
    val threshold = cardWidth * 0.28f

    // Per-card drag state, reset whenever the top card changes.
    var dx by remember(index) { mutableFloatStateOf(vm.enterFrom * cardWidth * 1.4f) }
    var dy by remember(index) { mutableFloatStateOf(0f) }
    var busy by remember(index) { mutableStateOf(false) }

    // Undone cards fly back in from the side they left.
    LaunchedEffect(index) {
        if (dx != 0f) {
            animate(dx, 0f, animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow)) { v, _ -> dx = v }
        }
    }

    fun fling(keep: Boolean) {
        if (busy || current == null) return
        busy = true
        if (vm.settings.haptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch {
            coroutineScope {
                launch { animate(dy, dy + 140f, animationSpec = tween(230)) { v, _ -> dy = v } }
                animate(
                    dx, (if (keep) 1f else -1f) * cardWidth * 1.6f,
                    animationSpec = tween(230, easing = FastOutLinearInEasing),
                ) { v, _ -> dx = v }
            }
            vm.decide(keep)
        }
    }

    fun settleBack() {
        scope.launch {
            coroutineScope {
                launch { animate(dy, 0f, animationSpec = spring(dampingRatio = 0.6f)) { v, _ -> dy = v } }
                animate(dx, 0f, animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)) { v, _ -> dx = v }
            }
        }
    }

    BackHandler { vm.finishSession() }

    AppScaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Session #${vm.sessionNumber}", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                        Text("${(index + 1).coerceAtMost(photos.size)} of ${photos.size}", style = MaterialTheme.typography.titleMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = vm::finishSession) { Icon(Icons.Rounded.Close, "End session") }
                },
                actions = {
                    BadgedBox(
                        badge = { if (sessionDeletes > 0) Badge { Text("$sessionDeletes") } },
                        modifier = Modifier.padding(end = 16.dp),
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, "$sessionDeletes marked for deletion", tint = scheme.onSurfaceVariant)
                    }
                },
            )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = 4.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SwipeFab(Icons.Rounded.Close, "Delete", scheme.errorContainer, scheme.onErrorContainer) { fling(false) }
                FilledTonalIconButton(
                    onClick = {
                        if (vm.settings.haptics) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        vm.undo()
                    },
                    enabled = index > 0,
                    modifier = Modifier.size(56.dp),
                ) { Icon(Icons.AutoMirrored.Rounded.Undo, "Undo") }
                SwipeFab(Icons.Rounded.Favorite, "Keep", extended.keepContainer, extended.onKeepContainer) { fling(true) }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val progress by animateFloatAsState(
                if (photos.isEmpty()) 0f else index.toFloat() / photos.size,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "progress",
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )
            Text(
                Messages.swipeHint(index, photos.size),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )

            // Card stack
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .onSizeChanged { if (it.width > 0) cardWidth = it.width.toFloat() }
            ) {
                val drag = (abs(dx) / threshold).coerceIn(0f, 1f)

                next?.let { photo ->
                    key(photo.id) {
                        PhotoCard(
                            photo = photo,
                            keepAlpha = 0f,
                            deleteAlpha = 0f,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    val s = 0.92f + 0.08f * drag
                                    scaleX = s; scaleY = s
                                    translationY = (1f - drag) * 18.dp.toPx()
                                    alpha = 0.55f + 0.45f * drag
                                },
                        )
                    }
                }
                current?.let { photo ->
                    key(photo.id) {
                        PhotoCard(
                            photo = photo,
                            keepAlpha = (dx / threshold).coerceIn(0f, 1f),
                            deleteAlpha = (-dx / threshold).coerceIn(0f, 1f),
                            modifier = Modifier
                                .fillMaxSize()
                                // Gestures sit before graphicsLayer so coordinates don't move with the card.
                                .pointerInput(index) {
                                    val tracker = VelocityTracker()
                                    var crossed = false
                                    detectDragGestures(
                                        onDragStart = { tracker.resetTracking(); crossed = false },
                                        onDragCancel = { if (!busy) settleBack() },
                                        onDragEnd = {
                                            if (!busy) {
                                                val vx = tracker.calculateVelocity().x
                                                when {
                                                    dx > threshold || (vx > 1800f && dx > 0f) -> fling(true)
                                                    dx < -threshold || (vx < -1800f && dx < 0f) -> fling(false)
                                                    else -> settleBack()
                                                }
                                            }
                                        },
                                        onDrag = { change, amount ->
                                            if (!busy) {
                                                change.consume()
                                                tracker.addPosition(change.uptimeMillis, change.position)
                                                dx += amount.x
                                                dy += amount.y * 0.35f
                                                val over = abs(dx) > threshold
                                                if (over != crossed) {
                                                    crossed = over
                                                    if (over && vm.settings.haptics) {
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    }
                                                }
                                            }
                                        },
                                    )
                                }
                                .pointerInput(index) {
                                    detectTapGestures(onDoubleTap = { fling(true) })
                                }
                                .graphicsLayer {
                                    translationX = dx
                                    translationY = dy
                                    rotationZ = dx / cardWidth * 16f
                                },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeFab(icon: ImageVector, label: String, container: Color, content: Color, onClick: () -> Unit) {
    LargeFloatingActionButton(
        onClick = onClick,
        containerColor = container,
        contentColor = content,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(FloatingActionButtonDefaults.LargeIconSize))
    }
}

@Composable
private fun PhotoCard(photo: Photo, keepAlpha: Float, deleteAlpha: Float, modifier: Modifier) {
    val shape = MaterialTheme.shapes.extraLarge
    val keep = AppTheme.extended.keep
    val delete = MaterialTheme.colorScheme.error
    Box(
        modifier
            .graphicsLayer {
                shadowElevation = 8.dp.toPx()
                this.shape = shape
                clip = true
            }
            .background(Color.Black)
    ) {
        // Blurred backdrop so portrait and landscape photos both fill the card nicely.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PhotoImage(photo, Modifier.fillMaxSize().blur(40.dp).graphicsLayer { alpha = 0.55f })
        }
        PhotoImage(photo, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)

        // Info scrim
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(120.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
        )
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(formatDate(photo.date), color = Color.White, style = MaterialTheme.typography.titleLarge)
            Text(
                "${photo.bucketName} · ${formatBytes(photo.size)}" + if (photo.isFavorite) " · Favorite" else "",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        // Tint + stamps
        if (keepAlpha > 0f) {
            Box(Modifier.fillMaxSize().background(keep.copy(alpha = 0.25f * keepAlpha)))
            Stamp("KEEP", keep, -14f, keepAlpha, Modifier.align(Alignment.TopStart).padding(24.dp))
        }
        if (deleteAlpha > 0f) {
            Box(Modifier.fillMaxSize().background(delete.copy(alpha = 0.25f * deleteAlpha)))
            Stamp("DELETE", delete, 14f, deleteAlpha, Modifier.align(Alignment.TopEnd).padding(24.dp))
        }
    }
}

@Composable
private fun Stamp(text: String, color: Color, angle: Float, alpha: Float, modifier: Modifier) {
    Text(
        text,
        color = color,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier
            .graphicsLayer {
                rotationZ = angle
                this.alpha = alpha
                val s = 0.8f + 0.2f * alpha
                scaleX = s; scaleY = s
            }
            .border(BorderStroke(4.dp, color), RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(horizontal = 14.dp, vertical = 4.dp),
    )
}
