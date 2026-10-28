package com.swipeclean.app.ui

/** Friendly, low-pressure copy. Keeping photos is always celebrated too. */
object Messages {

    fun summary(reviewed: Int, kept: Int, toDelete: Int): String = when {
        toDelete == 0 -> listOf(
            "Every one a keeper.",
            "Nothing to toss — great memories!",
            "All kept. Your gallery has good taste.",
        ).random()
        kept == 0 -> listOf(
            "Clean sweep! Your gallery is getting lighter.",
            "Decisive! Those won't be missed.",
        ).random()
        else -> listOf(
            "Nice! You reviewed $reviewed memories today!",
            "Your gallery is getting lighter!",
            "Kept the best, let go of the rest.",
            "That felt good, right?",
        ).random()
    }

    fun swipeHint(index: Int, total: Int): String = when {
        index == 0 -> "Left to delete · Right to keep"
        index == total - 1 -> "Last one!"
        index == total / 2 -> "Halfway there"
        else -> listOf(
            "Trust your instinct",
            "Keep what makes you smile",
            "You're on a roll",
            "No pressure — keep anything you love",
            "Swipe, swipe, lighter",
        )[index % 5]
    }

    fun done(count: Int): String = listOf(
        "Your gallery is getting lighter!",
        "Fresh space, same great memories.",
        "A tidier gallery and more free space.",
    )[count % 3]
}
