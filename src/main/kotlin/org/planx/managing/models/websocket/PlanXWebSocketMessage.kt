package org.planx.managing.models.websocket

class PlanXWebSocketMessage(
    override val requestId: String = "",
    override val content: Any? = null,
    override val error: WebSocketError? = null
) : WebSocketBaseMessage