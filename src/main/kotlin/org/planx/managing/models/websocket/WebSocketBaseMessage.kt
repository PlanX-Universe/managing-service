package org.planx.managing.models.websocket

interface WebSocketBaseMessage {
    val requestId: String
    val content: Any?
    val error: WebSocketError?
}

data class WebSocketError(
    val origin: String = "",
    val errorMessage: String? = "",
    val stackTrace: List<String>? = null
)