package com.swipeclean.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swipeclean.app.ui.AppViewModel
import com.swipeclean.app.ui.Screen
import com.swipeclean.app.ui.components.AppScaffold
import com.swipeclean.app.ui.components.Confetti
import com.swipeclean.app.ui.components.HeroIcon
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.TaskAlt
import com.swipeclean.app.ui.components.StatCard
import com.swipeclean.app.ui.components.formatBytes
import com.swipeclean.app.ui.theme.AppTheme

@Composable
fun SummaryScreen(vm: AppViewModel) {
    val summary = vm.summary ?: run { LaunchedEffect(Unit) { vm.goHome() }; return }
    val scheme = MaterialTheme.colorScheme
    val extended = AppTheme.extended
    BackHandler { vm.goHome() }

    AppScaffold { padding ->
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(16.dp))
                HeroIcon(if (summary.goalJustReached) Icons.Rounded.EmojiEvents else Icons.Rounded.TaskAlt)
                Spacer(Modifier.height(16.dp))
                Text(summary.message, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Session #${summary.sessionNumber} complete · ${summary.reviewed} photos reviewed",
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(
                        "Kept", summary.kept.toString(), Modifier.weight(1f),
                        containerColor = extended.keepContainer, contentColor = extended.onKeepContainer,
                    )
                    StatCard(
                        "To delete", summary.toDelete.toString(), Modifier.weight(1f),
                        containerColor = scheme.errorContainer, contentColor = scheme.onErrorContainer,
                    )
                    StatCard(
                        "Could free", formatBytes(summary.bytes), Modifier.weight(1f),
                        containerColor = scheme.primaryContainer, contentColor = scheme.onPrimaryContainer,
                    )
                }

                if (vm.settings.dailyGoalEnabled) {
                    Spacer(Modifier.height(12.dp))
                    val goal = vm.settings.dailyGoal
                    val today = vm.stats.reviewedToday
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                if (summary.goalJustReached) "Daily goal reached" else "Daily goal · $today / $goal",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { (today.toFloat() / goal).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth(),
                                color = extended.keep,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                val queued = vm.queuedPhotos.size
                if (queued > 0) {
                    Button(
                        onClick = { vm.navigate(Screen.Confirm) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = scheme.error, contentColor = scheme.onError),
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                    ) {
                        Icon(Icons.Rounded.DeleteSweep, null, Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text("Review & delete ($queued)")
                    }
                    Spacer(Modifier.height(8.dp))
                }
                FilledTonalButton(
                    onClick = vm::startSession,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                ) {
                    Icon(Icons.Rounded.Replay, null, Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text("Play another round")
                }
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = vm::goHome, modifier = Modifier.fillMaxWidth()) {
                    Text("Done for now")
                }
                if (queued > 0) {
                    Text(
                        "Your deletion queue is saved. Nothing is deleted until you confirm.",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Confetti(key = summary.sessionNumber)
        }
    }
}
