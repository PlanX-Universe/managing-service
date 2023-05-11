package org.planx.managing.config.messaging

import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.QueueBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


/**
 * This config sets up all queues for the given service topic
 *
 * @author Sebastian Graef
 */
@Configuration
class QueueConfigs {

    @Value("\${planx.queues.dlq.key}")
    private lateinit var deadLetterQueueRoutingKey: String

    @Bean
    fun planReplyQueue(
        @Value(value = "\${planx.queues.reply.plan.name}") replyQueueName: String
    ): Queue {
        return QueueBuilder.nonDurable(replyQueueName)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", deadLetterQueueRoutingKey)
            .build()
    }

    @Bean
    fun errorQueue(
        @Value(value = "\${planx.messaging.error.queue.name}") replyQueueName: String
    ): Queue {
        return QueueBuilder.durable(replyQueueName)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", deadLetterQueueRoutingKey)
            .build()
    }

    @Bean
    fun deadLetterQueue(
        @Value(value = "\${planx.queues.dlq.name}") deadLetterQueueName: String
    ): Queue {
        return QueueBuilder.durable(deadLetterQueueName).build()
    }
}