package org.planx.managing.remote.admin

import org.planx.managing.config.http.ApplicationHeaders
import org.planx.managing.remote.admin.model.RabbitConnectionModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.amqp.RabbitProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

@Component
class RabbitmqApiClient(
    private val webClientBuilder: WebClient.Builder,
    @Value("\${spring.rabbitmq.admin.api.url}") private val adminUrl: String,
    private val rabbitProperties: RabbitProperties
) {
    private val webClient by lazy {
        webClientBuilder
            .baseUrl("http://${adminUrl}")
            .defaultHeader("Application-Name", ApplicationHeaders.APPLICATION_NAME)
            .build()
    }

    fun retrieveAllConnections(): Mono<RabbitConnectionModel> {
        val userUriBuilder = UriComponentsBuilder
            .fromPath("connections")
        return webClient.get()
            .uri(userUriBuilder.toUriString())
            .headers {
                it.setBasicAuth(rabbitProperties.username, rabbitProperties.password)
            }
            .retrieve()
            .bodyToMono(RabbitConnectionModel::class.java)
    }
}