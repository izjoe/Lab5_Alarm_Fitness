package com.example.track.model

enum class Badge(
    val milestone: Int,
    val title: String,
    val description: String
) {
    BEGINNER(1_000, "Rookie Explorer", "First orbit reached"),
    EXPLORER(5_000, "Orbit Walker", "Deep space explorer"),
    CHAMPION(10_000, "Galaxy Runner", "System champion"),
    LEGEND(15_000, "Cosmic Legend", "Master of the universe");

    companion object {
        fun unlockedFor(steps: Int): List<Badge> = entries.filter { steps >= it.milestone }
    }
}
