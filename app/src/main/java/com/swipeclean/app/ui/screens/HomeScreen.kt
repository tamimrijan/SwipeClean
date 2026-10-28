@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

/** A single, non-scrolling screen: the photo stack flexes to fill whatever height is free. */
@Composable
fun HomeScreen(vm: AppViewModel, onManageAccess: () -> Unit) {
    val scheme = MaterialTheme.colorScheme

    AppScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(greeting(), style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
                        Text("SwipeClean", style = MaterialTheme.typography.headlineSmall)
                    }
                },
                actions = {
                    IconButton(onClick = { vm.navigate(Screen.Gallery) }) {
                        Icon(Icons.Rounded.Collections, contentDescription = "Gallery")
                    }
                    IconButton(onClick = { vm.navigate(Screen.Settings) }) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
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
                    container = scheme.errorContainer,
                    content = scheme.onErrorContainer,
                )
                vm.access == Access.Partial -> Notice(
                    icon = Icons.Rounded.Lock,
                    text = "Only some photos are shared",
                    action = "Choose more",
                    onClick = onManageAccess,
                    container = scheme.tertiaryContainer,
                    content = scheme.onTertiaryContainer,
                )
            }

            PhotoStackHero(vm, Modifier.weight(1f).fillMaxWidth())

            ProgressCard(vm)

            Button(
                onClick = vm::startSession,
                enabled = !vm.loading && vm.photos.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(20.dp),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            ) {
                Icon(Icons.Rounded.PlayArrow, null, Modifier.size(28.dp))
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text("Start cleaning", style = MaterialTheme.typography.titleMedium)
            }
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
private fun PhotoStackHero(vm: AppViewModel, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    val extended = AppTheme.extended
    val preview = remember(vm.photos) { vm.photos.shuffled().take(3) }

    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        // Soft glow behind the stack.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(scheme.primaryContainer.copy(alpha = 0.55f), Color.Transparent)
                    )
                )
        )

        when {
            vm.loading && preview.isEmpty() -> CircularProgressIndicator()
            preview.isEmpty() -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.PhotoLibrary, null, Modifier.size(56.dp), tint = scheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Text(
                    "No photos found yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            else -> {
                // Size the cards from the space available, leaving room for the caption pill.
                val cardHeight = minOf(maxHeight * 0.74f, 320.dp)
                val cardWidth = cardHeight * 0.74f
                val shape = RoundedCornerShape(24.dp)
                val angles = listOf(-11f, 9f, -1f)
                val shifts = listOf(-0.32f, 0.32f, 0f)

                Box(Modifier.offset(y = (-14).dp), contentAlignment = Alignment.Center) {
                    preview.forEachIndexed { i, photo ->
                        PhotoImage(
                            photo,
                            Modifier
                                .size(cardWidth, cardHeight)
                                .graphicsLayer {
                                    rotationZ = angles[i]
                                    translationX = shifts[i] * cardWidth.toPx()
                                    shadowElevation = (if (i == 2) 16 else 6).dp.toPx()
                                    this.shape = shape
                                    clip = true
                                }
                                .border(3.dp, scheme.surface, shape),
                        )
                    }
                    // Swipe hints on either side of the front card.
                    HintBadge(
                        Icons.Rounded.Close, scheme.errorContainer, scheme.onErrorContainer,
                        Modifier.align(Alignment.CenterStart).offset(x = (-22).dp),
                    )
                    HintBadge(
                        Icons.Rounded.Favorite, extended.keepContainer, extended.onKeepContainer,
                        Modifier.align(Alignment.CenterEnd).offset(x = 22.dp),
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = scheme.secondaryContainer,
                    contentColor = scheme.onSecondaryContainer,
                    shadowElevation = 2.dp,
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    Text(
                        "%,d photos · %s".format(vm.photos.size, formatBytes(vm.totalBytes)),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HintBadge(icon: ImageVector, container: Color, content: Color, modifier: Modifier) {
    Surface(shape = CircleShape, color = container, contentColor = content, shadowElevation = 4.dp, modifier = modifier.size(48.dp)) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(24.dp)) }
    }
}

/** Daily goal (optional) plus lifetime stats in one compact card. */
@Composable
private fun ProgressCard(vm: AppViewModel) {
    val extended = AppTheme.extended
    OutlinedCard(Modifier.fillMaxWidth()) {
        if (vm.settings.dailyGoalEnabled) {
            val goal = vm.settings.dailyGoal
            val today = vm.stats.reviewedToday
            val progress = (today.toFloat() / goal).coerceIn(0f, 1f)
            val streak = vm.stats.liveStreak
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(44.dp),
                        color = extended.keep,
                    )
                    Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (today >= goal) "Daily goal complete" else "Today's goal",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        "$today of $goal photos reviewed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (streak > 0) StreakBadge(streak)
            }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
        }
        Row(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Stat("Sessions", vm.stats.sessions.toString(), Modifier.weight(1f))
            VerticalDivider()
            Stat("Reviewed", vm.stats.reviewed.toString(), Modifier.weight(1f))
            VerticalDivider()
            Stat("Freed", formatBytes(vm.stats.bytesFreed), Modifier.weight(1f), extended.keep)
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier, color: Color = Color.Unspecified) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = color, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
    ) {
        Row(
            Modifier.padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, maxLines = 2, modifier = Modifier.weight(1f))
            TextButton(onClick = onClick) {
                Text(action, color = content)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(16.dp), tint = content)
            }
        }
    }
}
