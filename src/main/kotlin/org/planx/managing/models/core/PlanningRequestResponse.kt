package org.planx.managing.models.core

import org.planx.managing.models.rest.HttpResponseBody

class PlanningRequestResponse(var requestId: String) {
}

fun PlanningRequestResponse.toResponseBody(): HttpResponseBody {
    return mapOf(
        "requestId" to requestId
    )
}