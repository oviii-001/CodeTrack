package com.ovi.codetrack.shared.presentation.model

import com.ovi.codetrack.shared.domain.model.UserStats

data class UserStatsUiModel(
    val totalSolved: String,
    val easySolved: String,
    val mediumSolved: String,
    val hardSolved: String,
    val easyPercentage: Float,
    val mediumPercentage: Float,
    val hardPercentage: Float
)

fun UserStats.toUiModel(): UserStatsUiModel {
    val total = if (totalSolved > 0) totalSolved else 1
    return UserStatsUiModel(
        totalSolved = totalSolved.toString(),
        easySolved = easySolved.toString(),
        mediumSolved = mediumSolved.toString(),
        hardSolved = hardSolved.toString(),
        easyPercentage = easySolved.toFloat() / total,
        mediumPercentage = mediumSolved.toFloat() / total,
        hardPercentage = hardSolved.toFloat() / total
    )
}
