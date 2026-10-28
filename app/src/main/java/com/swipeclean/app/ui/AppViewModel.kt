package com.swipeclean.app.ui

import android.app.Application
import android.content.IntentSender
import android.os.Build
import android.os.SystemClock
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.swipeclean.app.data.Access
import com.swipeclean.app.data.Album
import com.swipeclean.app.data.AppSettings
import com.swipeclean.app.data.Photo
import com.swipeclean.app.data.PhotoRepository
import com.swipeclean.app.data.Prefs
import com.swipeclean.app.data.SessionPicker
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

enum class Screen { Home, Swipe, Summary, Confirm, Review, Done, Gallery, Settings }

data class Decision(val photo: Photo, val keep: Boolean)

data class SessionSummary(
    val sessionNumber: Int,
    val reviewed: Int,
    val kept: Int,
    val toDelete: Int,
    val bytes: Long,
    val message: String,
    val goalJustReached: Boolean,
)

data class DeletionOutcome(val count: Int, val bytes: Long)

sealed interface UiEvent {
    data class LaunchConsent(val sender: IntentSender) : UiEvent
    data class Message(val text: String) : UiEvent
}

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = PhotoRepository(app)
    private val prefs = Prefs(app)

    var access by mutableStateOf(Access.Unknown); private set
    var askedForPermission by mutableStateOf(prefs.askedForPermission); private set
    var loading by mutableStateOf(false); private set
    var loadError by mutableStateOf<String?>(null); private set
    var photos by mutableStateOf<List<Photo>>(emptyList()); private set
    var settings by mutableStateOf(prefs.loadSettings()); private set
    var stats by mutableStateOf(prefs.loadStats()); private set
    var queue by mutableStateOf(prefs.loadQueue()); private set

    private val history = prefs.loadHistory()
    private var lastLoadAt = 0L

    // ---- Navigation ----
    val backStack = mutableStateListOf(Screen.Home)
    val screen: Screen get() = backStack.last()

    fun navigate(to: Screen) { backStack.add(to) }
    fun back() { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
    fun goHome() { backStack.clear(); backStack.add(Screen.Home) }
    private fun replaceTop(to: Screen) { backStack[backStack.lastIndex] = to }

    // ---- Events (snackbars and system dialogs) ----
    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private fun message(text: String) { _events.trySend(UiEvent.Message(text)) }

    // ---- Derived data ----
    val queuedPhotos: List<Photo> by derivedStateOf {
        val byId = photos.associateBy { it.id }
        queue.mapNotNull { byId[it] }.sortedByDescending { it.date }
    }
    val queuedBytes: Long by derivedStateOf { queuedPhotos.sumOf { it.size } }
    val totalBytes: Long by derivedStateOf { photos.sumOf { it.size } }
    val albums: List<Album> by derivedStateOf {
        photos.groupBy { it.bucketId }
            .map { (id, list) -> Album(id, list.first().bucketName, list.size, list.first().uri) }
            .sortedByDescending { it.count }
    }
    val eligibleCount: Int by derivedStateOf {
        photos.count { SessionPicker.isEligible(it, settings, queue) }
    }

    // ---- Permissions & loading ----
    fun onAccessChecked(newAccess: Access) {
        val changed = newAccess != access
        access = newAccess
        if (newAccess == Access.Full || newAccess == Access.Partial) {
            // Reload when access changes (e.g. more photos selected) or the data is old.
            if (changed || photos.isEmpty() || isStale()) reload()
        } else {
            photos = emptyList()
        }
    }

    fun onPermissionRequested() {
        prefs.askedForPermission = true
        askedForPermission = true
    }

    private fun isStale() = SystemClock.elapsedRealtime() - lastLoadAt > 2 * 60_000

    fun reload() {
        viewModelScope.launch { loadNow() }
    }

    private suspend fun loadNow() {
        if (loading) return
        loading = true
        try {
            val loaded = repo.loadPhotos()
            photos = loaded
            loadError = null
            lastLoadAt = SystemClock.elapsedRealtime()
            // Drop queued ids for photos that no longer exist (deleted in another app).
            val ids = loaded.mapTo(HashSet()) { it.id }
            val pruned = queue.filterTo(HashSet()) { it in ids }
            if (pruned.size != queue.size) storeQueue(pruned)
        } catch (e: SecurityException) {
            loadError = "SwipeClean doesn't have permission to read your photos. Please allow photo access."
        } catch (e: Exception) {
            loadError = "Couldn't read your photos: ${e.message ?: "unknown error"}"
        } finally {
            loading = false
        }
    }

    // ---- Settings ----
    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        settings = transform(settings)
        prefs.saveSettings(settings)
    }

    fun resetHistory() {
        history.clear()
        prefs.saveHistory(history)
        message("Session history cleared — every photo is fair game again.")
    }

    private fun storeQueue(ids: Set<Long>) {
        queue = ids
        prefs.saveQueue(ids)
    }

    fun toggleQueued(photo: Photo) {
        storeQueue(if (photo.id in queue) queue - photo.id else queue + photo.id)
    }

    // ---- Swipe session ----
    var sessionPhotos by mutableStateOf<List<Photo>>(emptyList()); private set
    val decisions = mutableStateListOf<Decision>()
    /** Direction an undone card flies back in from: -1 left, +1 right, 0 none. */
    var enterFrom by mutableFloatStateOf(0f); private set
    var summary by mutableStateOf<SessionSummary?>(null); private set
    val sessionNumber: Int get() = stats.sessions + 1

    fun startSession() {
        viewModelScope.launch {
            if (photos.isEmpty() || isStale()) loadNow()
            val picked = SessionPicker.pick(photos, settings, queue, history)
            if (picked.isEmpty()) {
                message(
                    when {
                        loadError != null -> loadError!!
                        photos.isEmpty() -> "No photos found on this device yet."
                        else -> "All your photos are excluded or already queued. Check the filters in Settings."
                    }
                )
                return@launch
            }
            sessionPhotos = picked
            decisions.clear()
            enterFrom = 0f
            if (screen == Screen.Summary) replaceTop(Screen.Swipe) else navigate(Screen.Swipe)
        }
    }

    fun decide(keep: Boolean) {
        val photo = sessionPhotos.getOrNull(decisions.size) ?: return
        enterFrom = 0f
        decisions.add(Decision(photo, keep))
        storeQueue(if (keep) queue - photo.id else queue + photo.id)
        if (decisions.size == sessionPhotos.size) finishSession()
    }

    fun undo() {
        if (decisions.isEmpty()) return
        val last = decisions.last()
        enterFrom = if (last.keep) 1f else -1f
        decisions.removeAt(decisions.lastIndex)
        if (!last.keep) storeQueue(queue - last.photo.id)
    }

    fun finishSession() {
        if (screen != Screen.Swipe) return
        if (decisions.isEmpty()) { back(); return }

        val number = sessionNumber
        decisions.forEach { history[it.photo.id] = number }
        prefs.saveHistory(history)

        val before = stats.reviewedToday
        stats = stats.withSession(decisions.size)
        prefs.saveStats(stats)

        val kept = decisions.count { it.keep }
        val toDelete = decisions.size - kept
        val goalReached = settings.dailyGoalEnabled &&
            before < settings.dailyGoal && stats.reviewedToday >= settings.dailyGoal
        summary = SessionSummary(
            sessionNumber = number,
            reviewed = decisions.size,
            kept = kept,
            toDelete = toDelete,
            bytes = decisions.filter { !it.keep }.sumOf { it.photo.size },
            message = Messages.summary(decisions.size, kept, toDelete),
            goalJustReached = goalReached,
        )
        replaceTop(Screen.Summary)
    }

    // ---- Deletion ----
    var deleting by mutableStateOf(false); private set
    var outcome by mutableStateOf<DeletionOutcome?>(null); private set
    private var pending: List<Photo> = emptyList()
    private val runDeleted = ArrayList<Photo>()

    fun cancelDeletion() {
        storeQueue(emptySet())
        goHome()
        message("Deletion cancelled. All photos were kept.")
    }

    /** Called only from the explicit "Delete" button on the confirmation screen. */
    fun confirmDeletion() {
        val targets = queuedPhotos
        if (targets.isEmpty() || deleting) return
        runDeleted.clear()
        pending = targets
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                _events.trySend(UiEvent.LaunchConsent(repo.createDeleteRequest(targets.map { it.uri })))
            } catch (e: Exception) {
                message("Couldn't open the system delete dialog: ${e.message}")
            }
        } else {
            viewModelScope.launch { deleteLegacy() }
        }
    }

    /** Result of the Android system confirmation dialog. */
    fun onConsentResult(approved: Boolean) {
        if (!approved) {
            pending = emptyList()
            if (runDeleted.isNotEmpty()) finishDeletion()
            else message("Nothing was deleted.")
            return
        }
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                deleting = true
                // Confirm what actually disappeared rather than assuming.
                val gone = pending.filter { !repo.exists(it.uri) }
                applyDeleted(gone)
                pending = emptyList()
                finishDeletion()
            } else {
                deleteLegacy()
            }
        }
    }

    private suspend fun deleteLegacy() {
        deleting = true
        val remaining = pending.filter { p -> runDeleted.none { it.id == p.id } }
        val result = repo.deleteLegacy(remaining)
        applyDeleted(result.deleted)
        when {
            result.consent != null -> {
                deleting = false
                _events.trySend(UiEvent.LaunchConsent(result.consent))
            }
            else -> {
                result.error?.let { message(it) }
                pending = emptyList()
                finishDeletion()
            }
        }
    }

    private fun applyDeleted(list: List<Photo>) {
        if (list.isEmpty()) return
        runDeleted += list
        val ids = list.mapTo(HashSet()) { it.id }
        storeQueue(queue - ids)
        photos = photos.filter { it.id !in ids }
        stats = stats.copy(deleted = stats.deleted + list.size, bytesFreed = stats.bytesFreed + list.sumOf { it.size })
        prefs.saveStats(stats)
    }

    private fun finishDeletion() {
        deleting = false
        if (runDeleted.isEmpty()) {
            message("Nothing was deleted.")
            return
        }
        outcome = DeletionOutcome(runDeleted.size, runDeleted.sumOf { it.size })
        runDeleted.clear()
        backStack.clear()
        backStack.add(Screen.Home)
        backStack.add(Screen.Done)
    }
}
