package org.planx.managing.config

import org.planx.managing.controller.PlanXWebSocketSessionHandler
import org.planx.managing.services.ManagingService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.WebSocketService
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy

@Configuration
class WebSocketConfig(val managingService: ManagingService) {

    @Bean
    fun sessionHandler(): PlanXWebSocketSessionHandler {
        return PlanXWebSocketSessionHandler(managingService)
    }

    @Bean
    fun handlerMapping(): HandlerMapping {
        val map: MutableMap<String, WebSocketHandler> = HashMap()
        map["/v1/websocket"] = sessionHandler()
        val mapping = SimpleUrlHandlerMapping()
        mapping.urlMap = map
        mapping.order = Ordered.HIGHEST_PRECEDENCE
        return mapping
    }

    @Bean
    fun handlerAdapter(): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter(webSocketService())
    }

    @Bean
    fun webSocketService(): WebSocketService {
        return HandshakeWebSocketService(ReactorNettyRequestUpgradeStrategy())
    }
}
