package org.planx.managing.messaging.producer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.rabbitmq.client.AMQP
import org.planx.managing.config.getLoggerFor
import org.planx.managing.exceptions.ManagingError
import org.planx.managing.models.core.PlanningRequestResponse
import org.springframework.amqp.AmqpException
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.rabbitmq.OutboundMessage
import org.planx.common.models.CallStack
import org.planx.common.models.FunctionalityType
import org.planx.common.models.MutableStack
import org.planx.common.models.endpoint.solving.SolvingRequest
import org.planx.common.models.endpoint.solving.UniversalSolvingBody
import org.planx.common.models.endpoint.solving.UniversalSolvingRequest

@Component
class Sender(var rabbitSender: reactor.rabbitmq.Sender) {
    var logger = getLoggerFor<Sender>()

    fun requestPlanning(
        solvingRequest: SolvingRequest,
        requestId: String,
        callStack: MutableStack<CallStack>
    ): Mono<PlanningRequestResponse> {
        val requestBody = UniversalSolvingBody(
            requestId = requestId,
            callStack = callStack,
            content = solvingRequest as UniversalSolvingRequest
        )
        val addressElement = callStack.peek()
        // BUILD message
        val props: AMQP.BasicProperties = AMQP.BasicProperties().builder()
            .contentType(MessageProperties.CONTENT_TYPE_JSON)
            .contentEncoding("utf8")
            .build()
        val message = OutboundMessage(
            addressElement.topic,
            FunctionalityType.Solving.routingKey,
            props,
            // is adding "__TypeId__" automatically
            jacksonObjectMapper().writeValueAsBytes(requestBody)
        )
        // Send message
        return rabbitSender
            .send(Mono.just(message))
            .onErrorMap {
                logger.error(it.message)
                ManagingError(requestId, it.message)
            }.doFinally {
                logger.info("Request is send to ${addressElement.topic} (RequestId: ${requestId})")
            }
            .thenReturn(PlanningRequestResponse(requestId))
    }
}