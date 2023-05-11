package org.planx.managing.config.http

import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

object ApplicationHeaders {
    const val APPLICATION_NAME = "PlanX_Managing_Service"
}

@Component
class AddResponseHeaderFilter : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return chain.filter(
            exchange.apply {
                response.headers.set("Application-Name", ApplicationHeaders.APPLICATION_NAME)
            }
        )
    }
}