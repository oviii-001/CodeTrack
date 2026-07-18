package com.ovi.codetrack.shared.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object MainRoute

@Serializable
object DashboardRoute

@Serializable
object RoadmapRoute

@Serializable
object HistoryRoute

@Serializable
data class AddSubmissionRoute(
    val problemId: String? = null,
    val problemName: String? = null,
    val difficulty: String? = null,
    val tags: String? = null
)
