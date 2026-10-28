package com.swipeclean.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.swipeclean.app.data.Photo
import com.swipeclean.app.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.getDefault(), "%.0f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format(Locale.getDefault(), "%.1f MB", mb)
    return String.format(Locale.getDefault(), "%.2f GB", mb / 1024.0)
}

fun formatDate(millis: Long): String =
    if (millis <= 0) "Unknown date"
    else SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))

/** One snackbar host shared by every screen's Scaffold. */
val LocalSnackbarHost = staticCompositionLocalOf { SnackbarHostState() }

@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        snackbarHost = { SnackbarHost(LocalSnackbarHost.current) },
        content = content,
    )
}

@Composable
fun PhotoImage(
    photo: Photo,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    AsyncImage(
        model = remember(photo.uri) {
            ImageRequest.Builder(context).data(photo.uri).crossfade(180).build()
        },
        contentDescription = photo.displayName,
        contentScale = contentScale,
        modifier = modifier,
    )
}

/** M3 filled card showing a single headline number. */
@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

/** Large icon in a tonal circle, used as the headline visual on result and empty screens. */
@Composable
fun HeroIcon(
    icon: ImageVector,
    container: Color = MaterialTheme.colorScheme.primaryContainer,
    content: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    Surface(shape = CircleShape, color = container, contentColor = content, modifier = Modifier.size(96.dp)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
fun StreakBadge(days: Int) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.LocalFireDepartment, contentDescription = "Streak", modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("$days ${if (days == 1) "day" else "days"}", style = MaterialTheme.typography.labelLarge)
        }
    }
}

private class Particle(
    val x: Float, val vx: Float, val vy: Float,
    val size: Float, val rotation: Float, val spin: Float, val colorIndex: Int,
)

/** A one-shot burst of confetti in the theme's colors. Disappears after ~3 seconds. */
@Composable
fun Confetti(modifier: Modifier = Modifier, key: Any = Unit) {
    val scheme = MaterialTheme.colorScheme
    val palette = listOf(scheme.primary, scheme.secondary, scheme.tertiary, AppTheme.extended.keep, scheme.error)
    val particles = remember(key) {
        List(110) {
            Particle(
                x = 0.5f + (Random.nextFloat() - 0.5f) * 0.3f,
                vx = (Random.nextFloat() - 0.5f) * 0.9f,
                vy = -(0.7f + Random.nextFloat() * 0.9f),
                size = 6f + Random.nextFloat() * 8f,
                rotation = Random.nextFloat() * 360f,
                spin = (Random.nextFloat() - 0.5f) * 900f,
                colorIndex = Random.nextInt(5),
            )
        }
    }
    val duration = 3.2f
    var t by remember(key) { mutableFloatStateOf(0f) }
    LaunchedEffect(key) {
        val start = withFrameMillis { it }
        while (t < duration) {
            withFrameMillis { now -> t = (now - start) / 1000f }
        }
    }
    if (t >= duration) return
    Canvas(modifier.fillMaxSize()) {
        val alpha = (1f - t / duration).coerceIn(0f, 1f)
        particles.forEach { p ->
            val x = (p.x + p.vx * t) * size.width
            val y = size.height * (0.4f + p.vy * t + 0.75f * t * t)
            val s = p.size * density
            rotate(p.rotation + p.spin * t, pivot = Offset(x, y)) {
                drawRect(
                    color = palette[p.colorIndex].copy(alpha = alpha),
                    topLeft = Offset(x - s / 2, y - s / 4),
                    size = Size(s, s / 2),
                )
            }
        }
    }
}
