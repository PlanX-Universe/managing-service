package org.planx.managing.services

import org.planx.managing.config.getLoggerFor
import org.springframework.stereotype.Service
import org.springframework.util.ErrorHandler

@Service
class RabbitErrorHandler : ErrorHandler {
    private val logger = getLoggerFor<RabbitErrorHandler>()

    override fun handleError(t: Throwable) {
        logger.warn("JMS error occurred!")
        logger.error("Error Message : {}", t.message)
        logger.error("Error Message stacktrace: {}", t.stackTrace)
    }
}