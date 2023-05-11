package org.planx.managing.config.messaging

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BindingConfig {

    @Autowired
    private lateinit var planReplyQueue: Queue

    @Autowired
    private lateinit var errorQueue: Queue

    @Autowired
    private lateinit var deadLetterQueue: Queue

    @Value("\${planx.messaging.topic}")
    private lateinit var topicExchangeName: String

    @Value("\${planx.queues.reply.plan.key}")
    private lateinit var planReplyRoutingKey: String

    @Value("\${planx.messaging.error.key}")
    private lateinit var errorRoutingKey: String

    @Value("\${planx.queues.dlq.key}")
    private lateinit var deadLetterQueueRoutingKey: String

    @Bean
    fun exchange(): TopicExchange {
        return TopicExchange(topicExchangeName)
    }

    @Bean
    fun replyQueueBinding(): Binding {
        return BindingBuilder.bind(planReplyQueue).to(exchange()).with(planReplyRoutingKey)
    }

    @Bean
    fun errorQueueBinding(): Binding {
        return BindingBuilder.bind(errorQueue).to(exchange()).with(errorRoutingKey)
    }

    @Bean
    fun deadLetterBinding(): Binding {
        return BindingBuilder.bind(deadLetterQueue).to(exchange()).with(deadLetterQueueRoutingKey)
    }
}