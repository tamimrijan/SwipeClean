package com.swipeclean.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swipeclean.app.ui.AppViewModel
import com.swipeclean.app.ui.Messages
import com.swipeclean.app.ui.components.AppScaffold
import com.swipeclean.app.ui.components.Confetti
import com.swipeclean.app.ui.components.HeroIcon
import com.swipeclean.app.ui.components.formatBytes
import com.swipeclean.app.ui.theme.AppTheme

@Composable
fun DoneScreen(vm: AppViewModel) {
    val outcome = vm.outcome ?: run { LaunchedEffect(Unit) { vm.goHome() }; return }
    BackHandler { vm.goHome() }
    AppScaffold { padding ->
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                HeroIcon(Icons.Rounded.CheckCircle, AppTheme.extended.keepContainer, AppTheme.extended.onKeepContainer)
                Spacer(Modifier.height(16.dp))
                Text("Freed", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatBytes(outcome.bytes), style = MaterialTheme.typography.displayLarge, color = AppTheme.extended.keep)
                Spacer(Modifier.height(8.dp))
                Text(
                    "${outcome.count} photo${if (outcome.count == 1) "" else "s"} deleted. ${Messages.done(outcome.count)}",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = vm::startSession,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                ) {
                    Icon(Icons.Rounded.PlayArrow, null, Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Text("Another round")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = vm::goHome, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text("Back home")
                }
            }
            Confetti(key = outcome)
        }
    }
}
