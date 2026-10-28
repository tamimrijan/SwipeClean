package com.swipeclean.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.swipeclean.app.ui.components.AppScaffold

@Composable
fun PermissionScreen(deniedBefore: Boolean, onGrant: () -> Unit, onOpenSettings: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    AppScaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(shape = CircleShape, color = scheme.primaryContainer, modifier = Modifier.size(96.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.PhotoLibrary, null, tint = scheme.onPrimaryContainer, modifier = Modifier.size(44.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Let's tidy up your gallery", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                "SwipeClean needs access to your photos so you can swipe through them.",
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))

            Feature(Icons.Rounded.CloudOff, "Stays on your phone", "No internet permission. Nothing is ever uploaded.")
            Feature(Icons.Rounded.Shield, "You approve every deletion", "Swiping only queues photos. Nothing is deleted until you confirm.")
            Feature(Icons.Rounded.Swipe, "Quick & fun", "10–15 photos per round. Left to delete, right to keep.")
            Spacer(Modifier.height(24.dp))

            if (deniedBefore) {
                Card(colors = CardDefaults.cardColors(containerColor = scheme.errorContainer, contentColor = scheme.onErrorContainer)) {
                    ListItem(
                        leadingContent = { Icon(Icons.Rounded.Warning, null) },
                        headlineContent = { Text("Photo access is off") },
                        supportingContent = {
                            Text("If no prompt appears, open Settings → Permissions → Photos and videos and choose Allow.")
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
            Button(onClick = onGrant, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Text("Allow photo access")
            }
            if (deniedBefore) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text("Open app settings")
                }
            }
        }
    }
}

@Composable
private fun Feature(icon: ImageVector, title: String, body: String) {
    val scheme = MaterialTheme.colorScheme
    ListItem(
        leadingContent = {
            Surface(shape = CircleShape, color = scheme.secondaryContainer, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = scheme.onSecondaryContainer, modifier = Modifier.size(22.dp))
                }
            }
        },
        headlineContent = { Text(title) },
        supportingContent = { Text(body) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
