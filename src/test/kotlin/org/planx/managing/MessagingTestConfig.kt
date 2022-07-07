package org.planx.managing

import com.rabbitmq.client.Connection
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import reactor.core.publisher.Mono

@TestConfiguration
class MessagingTestConfig {
    @MockBean
    lateinit var connectionMono: Mono<Connection>

    @MockBean
    lateinit var amqpAdmin: AmqpAdmin
}