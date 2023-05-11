package org.planx.managing.models.core

import org.planx.managing.models.rest.HttpResponseBody
import org.springframework.boot.actuate.health.Health
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class FunctionalityStatus(
    val name: String = "",
    val status: Health = Health.unknown().build(),
    val connectedAt: Instant? = null
)

fun FunctionalityStatus.toFunctionalityResponseBody(): HttpResponseBody {
    val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC))
    return mapOf(
        "name" to name,
        "health" to status.status,
        "connectedAt" to formatter.format(connectedAt)
    )
}
