package org.planx.managing.config.http

import org.planx.managing.config.getLoggerFor
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.server.WebFilter


@Component
class LoggingInterceptor {

    private val logger = getLoggerFor<LoggingInterceptor>()

    @Bean
    fun loggingFilter(): WebFilter =
        WebFilter { exchange, chain ->
            val request = exchange.request

            logger.info(
                "Request recognized: [" +
                        "ip: ${request.remoteAddress!!.hostName}, " +
                        "id: ${request.id}, " +
                        "method=${request.method}, " +
                        "path=${request.path.pathWithinApplication()}, " +
                        "params=[${request.queryParams}" +
                        "] }"
            )
            return@WebFilter chain.filter(exchange)
        }
}