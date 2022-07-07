package org.planx.managing.models.rest

data class SolvingRequestBody(
        val requestId: String,
        val problem: String,
        val domain: String,
        // solver name
        val planner: String,
        val language: String
)