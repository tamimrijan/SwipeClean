@file:OptIn(ExperimentalMaterial3Api::class)

package com.swipeclean.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Screenshot
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.swipeclean.app.data.Access
import com.swipeclean.app.data.Permissions
import com.swipeclean.app.data.ThemeMode
import com.swipeclean.app.ui.AppViewModel
import com.swipeclean.app.ui.components.AppScaffold
import com.swipeclean.app.ui.components.PhotoImage
import com.swipeclean.app.ui.components.formatBytes
import kotlin.math.roundToInt

private enum class SettingsTab(val label: String, val icon: ImageVector) {
    Sessions("Sessions", Icons.Rounded.Swipe),
    Albums("Albums", Icons.Rounded.Collections),
    LookAndFeel("Look & feel", Icons.Rounded.Palette),
    About("About", Icons.Rounded.Info),
}

private object Developer {
    const val NAME = "Tamim Ahmed Rijan"
    const val GITHUB_URL = "https://github.com/tamimrijan"
    const val GITHUB_LABEL = "github.com/tamimrijan"
    const val INSTAGRAM_URL = "https://www.instagram.com/tamim_ahmed_rijan/"
    const val INSTAGRAM_LABEL = "@tamim_ahmed_rijan"
}

@Composable
fun SettingsScreen(vm: AppViewModel, onManageAccess: () -> Unit) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val tabs = SettingsTab.entries

    AppScaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = vm::back) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                    },
                )
                PrimaryTabRow(selectedTabIndex = selected) {
                    tabs.forEachIndexed { i, tab ->
                        Tab(
                            selected = selected == i,
                            onClick = { selected = i },
                            text = { Text(tab.label, maxLines = 1) },
                            icon = { Icon(tab.icon, null) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        AnimatedContent(
            targetState = tabs[selected],
            transitionSpec = { fadeIn(tween(180)).togetherWith(fadeOut(tween(120))) },
            modifier = Modifier.fillMaxSize(),
            label = "settingsTab",
        ) { tab ->
            LazyColumn(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 24.dp,
                ),
            ) {
                when (tab) {
                    SettingsTab.Sessions -> sessionsTab(vm)
                    SettingsTab.Albums -> albumsTab(vm)
                    SettingsTab.LookAndFeel -> lookAndFeelTab(vm)
                    SettingsTab.About -> aboutTab(vm, onManageAccess)
                }
            }
        }
    }
}

private fun LazyListScope.sessionsTab(vm: AppViewModel) {
    item {
        val s = vm.settings
        val favSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        ListItem(
            leadingContent = { Icon(Icons.Rounded.PhotoLibrary, null) },
            headlineContent = { Text("Eligible photos") },
            supportingContent = { Text("${vm.eligibleCount} of ${vm.photos.size} photos can appear in sessions") },
        )
        ListItem(
            leadingContent = { Icon(Icons.Rounded.Tune, null) },
            headlineContent = { Text("Photos per session") },
            supportingContent = {
                Column {
                    Text("${s.sessionMin}–${s.sessionMax} photos")
                    RangeSlider(
                        value = s.sessionMin.toFloat()..s.sessionMax.toFloat(),
                        onValueChange = { r ->
                            vm.updateSettings { it.copy(sessionMin = r.start.roundToInt(), sessionMax = r.endInclusive.roundToInt()) }
                        },
                        valueRange = 5f..30f,
                        steps = 24,
                    )
                }
            },
        )
        HorizontalDivider()
        SectionHeader("Skip in random sessions")
        SwitchItem(Icons.Rounded.Screenshot, "Screenshots", "Don't show screenshots", s.excludeScreenshots) { v ->
            vm.updateSettings { it.copy(excludeScreenshots = v) }
        }
        SwitchItem(Icons.Rounded.Download, "Downloaded images", "Don't show images from the Download folder", s.excludeDownloads) { v ->
            vm.updateSettings { it.copy(excludeDownloads = v) }
        }
        SwitchItem(
            Icons.Rounded.Star,
            "Favorites",
            if (favSupported) "Never show photos you've starred" else "Needs Android 11 or newer",
            s.excludeFavorites && favSupported,
            enabled = favSupported,
        ) { v -> vm.updateSettings { it.copy(excludeFavorites = v) } }
        HorizontalDivider()
        ListItem(
            leadingContent = { Icon(Icons.Rounded.Restore, null) },
            headlineContent = { Text("Reset “recently shown” history") },
            supportingContent = { Text("Let previously shown photos appear again right away") },
            modifier = Modifier.clickable(onClick = vm::resetHistory),
        )
    }
}

private fun LazyListScope.albumsTab(vm: AppViewModel) {
    item {
        Text(
            "Choose which albums can appear in random sessions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
    if (vm.albums.isEmpty()) {
        item { ListItem(headlineContent = { Text("No albums found") }) }
    }
    items(vm.albums, key = { it.id }) { album ->
        val cover = vm.photos.firstOrNull { it.bucketId == album.id }
        ListItem(
            leadingContent = {
                if (cover != null) PhotoImage(cover, Modifier.size(40.dp).clip(MaterialTheme.shapes.small))
            },
            headlineContent = { Text(album.name) },
            supportingContent = { Text("${album.count} photos") },
            trailingContent = {
                Switch(
                    checked = album.id !in vm.settings.excludedAlbums,
                    onCheckedChange = { include ->
                        vm.updateSettings {
                            it.copy(excludedAlbums = if (include) it.excludedAlbums - album.id else it.excludedAlbums + album.id)
                        }
                    },
                )
            },
        )
    }
}

private fun LazyListScope.lookAndFeelTab(vm: AppViewModel) {
    item {
        val s = vm.settings
        val dynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        SectionHeader("Appearance")
        ListItem(
            leadingContent = { Icon(Icons.Rounded.Brightness6, null) },
            headlineContent = { Text("Theme") },
            supportingContent = {
                val modes = listOf(ThemeMode.SYSTEM to "System", ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark")
                SingleChoiceSegmentedButtonRow(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    modes.forEachIndexed { i, (mode, label) ->
                        SegmentedButton(
                            selected = s.themeMode == mode,
                            onClick = { vm.updateSettings { it.copy(themeMode = mode) } },
                            shape = SegmentedButtonDefaults.itemShape(i, modes.size),
                        ) { Text(label) }
                    }
                }
            },
        )
        SwitchItem(
            Icons.Rounded.Palette,
            "Dynamic color",
            if (dynamicSupported) "Match colors to your wallpaper" else "Needs Android 12 or newer",
            s.dynamicColor && dynamicSupported,
            enabled = dynamicSupported,
        ) { v -> vm.updateSettings { it.copy(dynamicColor = v) } }
        HorizontalDivider()
        SectionHeader("Feedback & goals")
        SwitchItem(Icons.Rounded.Vibration, "Haptic feedback", "Little buzzes when you swipe", s.haptics) { v ->
            vm.updateSettings { it.copy(haptics = v) }
        }
        SwitchItem(Icons.Rounded.EmojiEvents, "Daily goal", "An optional, no-pressure target", s.dailyGoalEnabled) { v ->
            vm.updateSettings { it.copy(dailyGoalEnabled = v) }
        }
        if (s.dailyGoalEnabled) {
            ListItem(
                headlineContent = { Text("Review ${s.dailyGoal} photos a day") },
                supportingContent = {
                    Slider(
                        value = s.dailyGoal.toFloat(),
                        onValueChange = { v -> vm.updateSettings { it.copy(dailyGoal = (v / 5).roundToInt() * 5) } },
                        valueRange = 5f..100f,
                        steps = 18,
                    )
                },
            )
        }
    }
}

private fun LazyListScope.aboutTab(vm: AppViewModel, onManageAccess: () -> Unit) {
    item {
        val context = LocalContext.current
        val scheme = MaterialTheme.colorScheme

        // Developer card
        ElevatedCard(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ListItem(
                leadingContent = {
                    Surface(shape = CircleShape, color = scheme.primaryContainer, modifier = Modifier.size(56.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("TR", style = MaterialTheme.typography.titleLarge, color = scheme.onPrimaryContainer)
                        }
                    }
                },
                overlineContent = { Text("Designed & developed by") },
                headlineContent = { Text(Developer.NAME, style = MaterialTheme.typography.titleLarge) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            LinkItem(Icons.Rounded.Code, "GitHub", Developer.GITHUB_LABEL) { openLink(context, Developer.GITHUB_URL) }
            LinkItem(Icons.Rounded.PhotoCamera, "Instagram", Developer.INSTAGRAM_LABEL) { openLink(context, Developer.INSTAGRAM_URL) }
        }

        SectionHeader("App")
        ListItem(
            leadingContent = { Icon(Icons.Rounded.Info, null) },
            headlineContent = { Text("SwipeClean") },
            supportingContent = { Text("Version ${appVersion(context)}") },
        )
        ListItem(
            leadingContent = { Icon(Icons.Rounded.PrivacyTip, null) },
            headlineContent = { Text("Privacy") },
            supportingContent = {
                Text("Your photos are processed on this device only and never uploaded. Space freed so far: ${formatBytes(vm.stats.bytesFreed)}.")
            },
        )
        ListItem(
            leadingContent = { Icon(Icons.Rounded.VpnKey, null) },
            headlineContent = { Text("Photo access") },
            supportingContent = {
                Text(
                    when (vm.access) {
                        Access.Full -> "All photos · tap to manage"
                        Access.Partial -> "Selected photos only · tap to change"
                        else -> "Not granted"
                    }
                )
            },
            modifier = Modifier.clickable {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && vm.access == Access.Partial) onManageAccess()
                else Permissions.openAppSettings(context)
            },
        )
        Spacer(Modifier.height(8.dp))
        OutlinedCard(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Made with care for tidy galleries.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
private fun LinkItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        leadingContent = { Icon(icon, null) },
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = { Icon(Icons.AutoMirrored.Rounded.OpenInNew, "Open $title") },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable(onClick = onClick),
    )
}

/** Hands the link to the browser or the matching app; SwipeClean itself never goes online. */
private fun openLink(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No app found to open this link", Toast.LENGTH_SHORT).show()
    }
}

private fun appVersion(context: Context): String =
    runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
        .getOrNull() ?: "1.0.0"

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun SwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onChange: (Boolean) -> Unit,
) {
    ListItem(
        leadingContent = { Icon(icon, null) },
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onChange, enabled = enabled) },
    )
}
