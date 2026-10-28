package com.swipeclean.app.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.swipeclean.app.ui.components.LocalSnackbarHost
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swipeclean.app.data.Access
import com.swipeclean.app.data.Permissions
import com.swipeclean.app.ui.screens.ConfirmDeleteScreen
import com.swipeclean.app.ui.screens.DoneScreen
import com.swipeclean.app.ui.screens.GalleryScreen
import com.swipeclean.app.ui.screens.HomeScreen
import com.swipeclean.app.ui.screens.PermissionScreen
import com.swipeclean.app.ui.screens.ReviewScreen
import com.swipeclean.app.ui.screens.SettingsScreen
import com.swipeclean.app.ui.screens.SummaryScreen
import com.swipeclean.app.ui.screens.SwipeScreen
import com.swipeclean.app.ui.theme.SwipeCleanTheme

@Composable
fun SwipeCleanApp(vm: AppViewModel = viewModel()) {
    SwipeCleanTheme(vm.settings.themeMode, vm.settings.dynamicColor) {
        val context = LocalContext.current
        val snackbar = remember { SnackbarHostState() }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            vm.onPermissionRequested()
            vm.onAccessChecked(Permissions.access(context))
        }
        val requestAccess = { permissionLauncher.launch(Permissions.required()) }

        val consentLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result -> vm.onConsentResult(result.resultCode == Activity.RESULT_OK) }

        // Re-check access whenever we come back (the user may have changed it in Settings).
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
            vm.onAccessChecked(Permissions.access(context))
        }

        LaunchedEffect(Unit) {
            vm.events.collect { event ->
                when (event) {
                    is UiEvent.LaunchConsent ->
                        consentLauncher.launch(IntentSenderRequest.Builder(event.sender).build())
                    is UiEvent.Message -> snackbar.showSnackbar(event.text)
                }
            }
        }

        CompositionLocalProvider(LocalSnackbarHost provides snackbar) {
            when (vm.access) {
                Access.Unknown -> Unit
                Access.None -> PermissionScreen(
                    deniedBefore = vm.askedForPermission,
                    onGrant = requestAccess,
                    onOpenSettings = { Permissions.openAppSettings(context) },
                )
                else -> {
                    BackHandler(enabled = vm.backStack.size > 1 && vm.screen != Screen.Swipe) { vm.back() }
                    AnimatedContent(
                        targetState = vm.screen,
                        transitionSpec = {
                            (fadeIn(tween(220)) + scaleIn(tween(220), initialScale = 0.97f))
                                .togetherWith(fadeOut(tween(150)))
                        },
                        label = "screens",
                    ) { screen ->
                        when (screen) {
                            Screen.Home -> HomeScreen(vm, onManageAccess = requestAccess)
                            Screen.Swipe -> SwipeScreen(vm)
                            Screen.Summary -> SummaryScreen(vm)
                            Screen.Confirm -> ConfirmDeleteScreen(vm)
                            Screen.Review -> ReviewScreen(vm)
                            Screen.Done -> DoneScreen(vm)
                            Screen.Gallery -> GalleryScreen(vm)
                            Screen.Settings -> SettingsScreen(vm, onManageAccess = requestAccess)
                        }
                    }
                }
            }
        }
    }
}
