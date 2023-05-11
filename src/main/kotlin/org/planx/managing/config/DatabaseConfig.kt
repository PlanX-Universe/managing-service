package org.planx.managing.config

import org.planx.managing.models.entities.FunctionalityEntity
import org.planx.managing.repositories.FunctionalityRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.planx.common.models.endpoint.solving.SolvingResultsBody

@Configuration
class DatabaseConfig {
    private val log = getLoggerFor<DatabaseConfig>()

    @Bean
    fun initDatabase(
        functionalityRepository: FunctionalityRepository,
        @Value("\${planx.queues.request.key}") routingKey: String,
        @Value("\${planx.messaging.topic}") topicExchange: String
    ): CommandLineRunner {
        return CommandLineRunner {
            val self = FunctionalityEntity().apply {
                this.routingKey = routingKey
                this.topic = topicExchange
                this.interfaces = listOf(
                    SolvingResultsBody::class.java.toString()
                )
            }
            log.info("Preloading " + functionalityRepository.save(self))
        }
    }
}