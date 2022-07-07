package org.planx.managing.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.planx.managing.config.getLoggerFor
import org.planx.managing.models.websocket.RequestConfirmation
import org.planx.managing.models.websocket.PlanXWebSocketMessage
import org.planx.managing.services.ManagingService
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class PlanXWebSocketSessionHandler(
    private val managingService: ManagingService
) : WebSocketHandler {

    var logger = getLoggerFor<PlanXWebSocketSessionHandler>()

    /**
     * Main handler for the websocket session.
     *
     * @return Mono<Void> of the outbound websocket stream
     */
    override fun handle(session: WebSocketSession): Mono<Void> {
        logger.info("Handle new websocket Session (ID: ${session.id})")
        val result: Flux<WebSocketMessage> = session.receive()
            .map {
                jacksonObjectMapper().readValue(it.payloadAsText, RequestConfirmation::class.java)
            }
            .flatMap { request: RequestConfirmation ->
                return@flatMap createMessage(request.requestId, session)
            }
        return session.send(result)
            .doOnError {
                logger.error(it.message)
            }
    }

    private fun createMessage(requestId: String, websocketSession: WebSocketSession): Flux<WebSocketMessage> {
        logger.debug("createMessage for RQ {}", requestId)
        return managingService.registerSocket(requestId)
            .map { planxMsg: PlanXWebSocketMessage ->
                val msg: String = jacksonObjectMapper().writeValueAsString(planxMsg)
                logger.debug("Socket response {}", msg)
                msg
            }.map { msg: String ->
                websocketSession.textMessage(msg)
            }
    }
}
