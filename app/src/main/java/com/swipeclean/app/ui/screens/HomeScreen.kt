package com.swipeclean.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swipeclean.app.data.Access
import com.swipeclean.app.ui.AppViewModel
import com.swipeclean.app.ui.Screen
import com.swipeclean.app.ui.components.AppScaffold
import com.swipeclean.app.ui.components.PhotoImage
import com.swipeclean.app.ui.components.StreakBadge
import com.swipeclean.app.ui.components.formatBytes
import com.swipeclean.app.ui.theme.AppTheme
import java.time.LocalTime

/** A single, non-scrolling screen: the hero flexes to fill whatever height is free. */
@Composable
fun HomeScreen(vm: AppViewModel, onManageAccess: () -> Unit) {
    val scheme = MaterialTheme.colorScheme

    AppScaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Header(vm)

            // At most one notice, so the layout never grows past the screen.
            when {
                vm.loadError != null -> Notice(
                    icon = Icons.Rounded.ErrorOutline,
                    text = vm.loadError!!,
                    action = "Retry",
                    onClick = vm::reload,
                    container = scheme.errorContainer,
                    content = scheme.onErrorContainer,
                )
                vm.queuedPhotos.isNotEmpty() -> Notice(
                    icon = Icons.Rounded.DeleteSweep,
                    text = "${vm.queuedPhotos.size} photos waiting · ${formatBytes(vm.queuedBytes)}",
                    action = "Review",
                    onClick = { vm.navigate(Screen.Confirm) },
                    container = scheme.tertiaryContainer,
                    content = scheme.onTertiaryContainer,
                )
                vm.access == Access.Partial -> Notice(
                    icon = Icons.Rounded.Lock,
                    text = "Only some photos are shared",
                    action = "Choose more",
                    onClick = onManageAccess,
                    container = scheme.secondaryContainer,
                    content = scheme.onSecondaryContainer,
                )
            }

            HeroCard(vm, Modifier.weight(1f).fillMaxWidth())

            if (vm.settings.dailyGoalEnabled) GoalCard(vm)
            StatsRow(vm)

            StartButton(vm)
        }
    }
}

private fun greeting(): String = when (LocalTime.now().hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    in 17..21 -> "Good evening"
    else -> "Hello, night owl"
}

@Composable
private fun Header(vm: AppViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(greeting(), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Text("SwipeClean", style = MaterialTheme.typography.headlineLarge)
        }
        FilledTonalIconButton(
            onClick = { vm.navigate(Screen.Gallery) },
            shapes = IconButtonDefaults.shapes(),
            modifier = Modifier.size(IconButtonDefaults.mediumContainerSize()),
        ) { Icon(Icons.Rounded.Collections, contentDescription = "Gallery") }
        Spacer(Modifier.width(8.dp))
        FilledTonalIconButton(
            onClick = { vm.navigate(Screen.Settings) },
            shapes = IconButtonDefaults.shapes(),
            modifier = Modifier.size(IconButtonDefaults.mediumContainerSize()),
        ) { Icon(Icons.Rounded.Settings, contentDescription = "Settings") }
    }
}

/** Purple hero: photo count plus a fan of real photos cut into M3 Expressive shapes. */
@Composable
private fun HeroCard(vm: AppViewModel, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    val extended = AppTheme.extended
    val preview = remember(vm.photos) { vm.photos.shuffled().take(3) }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = scheme.primaryContainer,
        contentColor = scheme.onPrimaryContainer,
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                if (vm.loading && vm.photos.isEmpty()) "…" else "%,d".format(vm.photos.size),
                style = MaterialTheme.typography.displayMedium,
            )
            Text(
                "photos · ${formatBytes(vm.totalBytes)}",
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onPrimaryContainer.copy(alpha = 0.75f),
            )

            BoxWithConstraints(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    vm.loading && preview.isEmpty() -> LoadingIndicator(Modifier.size(64.dp))
                    preview.isEmpty() -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.PhotoLibrary, null, Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No photos found yet", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                    }
                    else -> {
                        val unit = minOf(maxHeight * 0.92f, maxWidth * 0.5f, 260.dp)
                        // Back photos use playful expressive shapes; the front one is a rounded card.
                        val shapes: List<Shape> = listOf(
                            MaterialShapes.Cookie12Sided.toShape(),
                            MaterialShapes.Clover4Leaf.toShape(),
                            RoundedCornerShape(32.dp),
                        )
                        val sizes = listOf(unit * 0.72f, unit * 0.72f, unit)
                        val shifts = listOf(-0.62f, 0.62f, 0f)
                        val angles = listOf(-10f, 12f, -3f)
                        preview.forEachIndexed { i, photo ->
                            PhotoImage(
                                photo,
                                Modifier
                                    .size(width = if (i == 2) sizes[i] * 0.78f else sizes[i], height = sizes[i])
                                    .graphicsLayer {
                                        translationX = shifts[i] * unit.toPx()
                                        rotationZ = angles[i]
                                        shadowElevation = (if (i == 2) 14 else 4).dp.toPx()
                                        shape = shapes[i]
                                        clip = true
                                    }
                                    .border(3.dp, scheme.surface, shapes[i]),
                            )
                        }
                        HintBadge(
                            Icons.Rounded.Close, scheme.error, scheme.onError, MaterialShapes.Cookie4Sided.toShape(),
                            Modifier.align(Alignment.BottomStart).offset(x = 8.dp, y = (-4).dp),
                        )
                        HintBadge(
                            Icons.Rounded.Favorite, extended.keep, extended.onKeep, MaterialShapes.Sunny.toShape(),
                            Modifier.align(Alignment.BottomEnd).offset(x = (-8).dp, y = (-4).dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HintBadge(icon: ImageVector, container: Color, content: Color, shape: Shape, modifier: Modifier) {
    Box(
        modifier
            .size(56.dp)
            .graphicsLayer { shadowElevation = 6.dp.toPx(); this.shape = shape; clip = true }
            .background(container, shape),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, Modifier.size(26.dp), tint = content) }
}

@Composable
private fun GoalCard(vm: AppViewModel) {
    val extended = AppTheme.extended
    val goal = vm.settings.dailyGoal
    val today = vm.stats.reviewedToday
    val progress = (today.toFloat() / goal).coerceIn(0f, 1f)
    val streak = vm.stats.liveStreak
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularWavyProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(52.dp),
                    color = extended.keep,
                    trackColor = extended.keepContainer,
                )
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(if (today >= goal) "Daily goal complete" else "Today's goal", style = MaterialTheme.typography.titleMedium)
                Text(
                    "$today of $goal photos reviewed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (streak > 0) StreakBadge(streak)
        }
    }
}

@Composable
private fun StatsRow(vm: AppViewModel) {
    val scheme = MaterialTheme.colorScheme
    val extended = AppTheme.extended
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatTile("Sessions", vm.stats.sessions.toString(), scheme.secondaryContainer, scheme.onSecondaryContainer, Modifier.weight(1f))
        StatTile("Reviewed", vm.stats.reviewed.toString(), scheme.tertiaryContainer, scheme.onTertiaryContainer, Modifier.weight(1f))
        StatTile("Freed", formatBytes(vm.stats.bytesFreed), extended.keepContainer, extended.onKeepContainer, Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(label: String, value: String, container: Color, content: Color, modifier: Modifier) {
    Surface(modifier = modifier, shape = MaterialTheme.shapes.large, color = container, contentColor = content) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

@Composable
private fun StartButton(vm: AppViewModel) {
    val height = 72.dp
    Button(
        onClick = vm::startSession,
        shapes = ButtonDefaults.shapesFor(height),
        enabled = !vm.loading && vm.photos.isNotEmpty(),
        modifier = Modifier.fillMaxWidth().height(height),
        contentPadding = ButtonDefaults.contentPaddingFor(height),
    ) {
        Icon(Icons.Rounded.PlayArrow, null, Modifier.size(ButtonDefaults.iconSizeFor(height)))
        Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(height)))
        Text("Start cleaning", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun Notice(
    icon: ImageVector,
    text: String,
    action: String,
    onClick: () -> Unit,
    container: Color,
    content: Color,
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
    ) {
        Row(
            Modifier.padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, maxLines = 2, modifier = Modifier.weight(1f))
            TextButton(onClick = onClick, shapes = ButtonDefaults.shapes()) {
                Text(action, color = content)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(16.dp), tint = content)
            }
        }
    }
}
