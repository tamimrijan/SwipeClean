package com.swipeclean.app.data

import kotlin.random.Random

object SessionPicker {

    fun isEligible(photo: Photo, settings: AppSettings, queue: Set<Long>): Boolean =
        photo.id !in queue &&
            photo.bucketId !in settings.excludedAlbums &&
            !(settings.excludeScreenshots && photo.isScreenshot) &&
            !(settings.excludeDownloads && photo.isDownload) &&
            !(settings.excludeFavorites && photo.isFavorite)

    /**
     * Picks a random batch, preferring photos that have never been shown, then the ones
     * shown longest ago — so consecutive sessions don't repeat photos when avoidable.
     */
    fun pick(
        all: List<Photo>,
        settings: AppSettings,
        queue: Set<Long>,
        history: Map<Long, Int>,
        random: Random = Random.Default,
    ): List<Photo> {
        val candidates = all.filter { isEligible(it, settings, queue) }
        if (candidates.isEmpty()) return emptyList()
        val min = settings.sessionMin.coerceAtLeast(1)
        val max = settings.sessionMax.coerceAtLeast(min)
        val count = random.nextInt(min, max + 1).coerceAtMost(candidates.size)
        // Shuffle first, then a stable sort by "last shown" keeps randomness within each tier.
        return candidates.shuffled(random)
            .sortedBy { history[it.id] ?: Int.MIN_VALUE }
            .take(count)
            .shuffled(random)
    }
}
