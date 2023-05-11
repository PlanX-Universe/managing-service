package org.planx.managing.remote.admin

import org.planx.managing.models.core.FunctionalityStatus
import org.springframework.boot.actuate.health.Health
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.Instant

@Service
class RabbitmqApiService(
    private var remoteClient: RabbitmqApiClient
) {

    fun getAllConnections(): Flux<FunctionalityStatus> {
        return remoteClient.retrieveAllConnections()
            .flatMapMany { Flux.fromIterable(it) }
            .map {
                FunctionalityStatus(
                    name = it.user_provided_name,
                    status = Health.status(it.state).build(),
                    connectedAt = Instant.ofEpochMilli(it.connected_at)
                )
            }
    }
}