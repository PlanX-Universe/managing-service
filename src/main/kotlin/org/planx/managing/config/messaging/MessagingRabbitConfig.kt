package org.planx.managing.config.messaging

import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Delivery
import org.planx.managing.config.getLoggerFor
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.amqp.RabbitProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.rabbitmq.*
import reactor.util.retry.Retry
import java.time.Duration
import javax.annotation.PreDestroy

@Configuration
class MessagingRabbitConfig {
    @Autowired
    private var connectionMono: Mono<Connection>? = null

    @Autowired
    private lateinit var planReplyQueue: Queue

    @Autowired
    private lateinit var errorQueue: Queue

    @Autowired
    private lateinit var deadLetterQueue: Queue

    @Autowired
    private lateinit var exchange: TopicExchange

    @Autowired
    private lateinit var amqpAdmin: AmqpAdmin

    private val logger = getLoggerFor<MessagingRabbitConfig>()

    /**
     * The mono for connection, it is cached to re-use the connection across sender and
     * receiver instances this should work properly in most cases
     */
    @Bean
    fun connectionMono(
        rabbitProperties: RabbitProperties,
        @Value("\${planx.messaging.topic}") mainTopicName: String,
        @Value("\${planx.queues.request.key}") mainRoutingKey: String
    ): Mono<Connection> {
        val connectionFactory = ConnectionFactory()
        connectionFactory.host = rabbitProperties.host
        connectionFactory.port = rabbitProperties.port
        connectionFactory.username = rabbitProperties.username
        connectionFactory.password = rabbitProperties.password

        return Mono.fromCallable {
            connectionFactory.newConnection("${mainTopicName}#${mainRoutingKey}")
        }.doOnError {
            logger.error("Failed to connect ({})", it?.message)
        }
            .retryWhen(Retry.backoff(100, Duration.ofSeconds(1)))
            .doOnNext { createBinding() }
            .cache()
    }

    @Bean
    fun reactiveSender(connectionMono: Mono<Connection>): Sender {
        return RabbitFlux.createSender(SenderOptions().connectionMono(connectionMono))
    }

    @Bean
    fun receiver(connectionMono: Mono<Connection>): Receiver {
        return RabbitFlux.createReceiver(ReceiverOptions().connectionMono(connectionMono))
    }

    @Bean
    fun deliveryFlux(receiver: Receiver): Flux<Delivery> {
        return receiver.consumeNoAck(planReplyQueue.name)
    }

    @Bean
    fun errorDeliveryFlux(receiver: Receiver): Flux<Delivery> {
        return receiver.consumeNoAck(errorQueue.name)
    }

    @PreDestroy
    @Throws(Exception::class)
    fun close() {
        connectionMono!!.block()!!.close()
        logger.info("Connection Message Broker connection is closed now.")
    }

    private fun createBinding() {
        try {
            amqpAdmin.declareExchange(exchange)
            amqpAdmin.declareQueue(planReplyQueue)
            amqpAdmin.declareQueue(errorQueue)
            amqpAdmin.declareQueue(deadLetterQueue)
        } catch (e: Exception) {
            logger.error(e.message)
        }
    }
}