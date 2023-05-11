package org.planx.managing.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.Connection
import com.rabbitmq.client.Delivery
import org.planx.managing.config.getLoggerFor
import org.planx.managing.messaging.producer.Sender
import org.planx.managing.models.core.FunctionalityStatus
import org.planx.managing.models.core.PlanningRequestResponse
import org.planx.managing.models.entities.FunctionalityEntity
import org.planx.managing.models.websocket.PlanXWebSocketMessage
import org.planx.managing.models.websocket.WebSocketError
import org.planx.managing.remote.admin.RabbitmqApiService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux
import org.planx.common.models.CallStack
import org.planx.common.models.CustomErrorMessage
import org.planx.common.models.MutableStack
import org.planx.common.models.endpoint.solving.SolvingRequest
import org.planx.common.models.endpoint.solving.SolvingResultsBody
import org.planx.common.models.endpoint.solving.plan.NoPlan
import org.planx.common.models.transforming.converting.encoding.pddl.PddlEncodingResponseBody
import javax.annotation.PostConstruct


@Service
class ManagingService(
    val sender: Sender,
    private val deliveryFlux: Flux<Delivery>,
    private val errorDeliveryFlux: Flux<Delivery>,
    private val rabbitApiService: RabbitmqApiService,
    private val functionalityManagementService: FunctionalityManagementService,
    private val objectMapper: ObjectMapper,
    private val connectionMono: Mono<Connection>,
    @Value("\${planx.queues.reply.plan.key}")
    private var planningResponseRoutingKey: String
) {
    private val logger = getLoggerFor<ManagingService>()

    fun getAllFunctionalities(): Flux<FunctionalityStatus> {
        return rabbitApiService.getAllConnections()
    }

    @Throws(NullPointerException::class)
    fun handleSolvingRequest(
        solvingRequest: SolvingRequest,
        requestId: String
    ): Mono<PlanningRequestResponse> {
        // create Callstack
        val callStack: MutableStack<CallStack> = MutableStack()
        // add self to receive any reply
        callStack.push(
            CallStack(
                topic = FunctionalityType.Managing.topic,
                routingKey = planningResponseRoutingKey
            )
        )
        // add remote service to solve the plan
        callStack.push(
            CallStack(
                topic = FunctionalityType.Solving.topic,
                routingKey = FunctionalityType.Solving.routingKey,
                replyClass = PddlEncodingResponseBody::class.java.name,
                state = mapOf(
                    "planner" to solvingRequest.planner?.name!!
                )
            )
        )

        return sender.requestPlanning(
            solvingRequest = solvingRequest,
            requestId = requestId,
            callStack = callStack
        )
    }

    /**
     * This Function returns a stream of [PlanXWebSocketMessage].
     * This message can contain an error or a normal plan content.
     *
     * @param requestId is the current requestID as [String]
     */
    fun registerSocket(requestId: String): Flux<PlanXWebSocketMessage> {
        logger.info("RegisterSocket, waiting for results of {}", requestId)
        // register subscription for response of plans and errors
        return Flux.merge(
            // TODO: SequentialPlanBody should be more generic
            getPlanResponseWebsocketMessageByRequestId(requestId),
            getErrorResponseWebsocketMessageByRequestId(requestId)
        ).map {
            logger.info("Send WebSocket response for RQ {})", it.requestId)
            it
        }
    }

    /**
     * Prototypical implementation of a functionality interface registry
     *
     * TODO: should return a Data model instead of string!
     */
    fun getAllFunctionalityInterfaces(): Flux<String> {
        return functionalityManagementService.getAllFunctionalities().toFlux()
            .map(FunctionalityEntity::interfaces)
            .flatMapIterable {
                it
            }
    }

    @PostConstruct
    private fun subscribeToMessagingConnection() {
        logger.info("start using messaging!")
        connectionMono
            .doOnNext {
                logger.info("connected to {}", it?.address?.hostAddress)
            }
            .subscribe()
    }

    private fun getPlanResponseWebsocketMessageByRequestId(
        requestId: String
    ): Flux<PlanXWebSocketMessage> {
        return deliveryFlux
            .map {
                logger.info("Plan message from functionality received {}", String(it.body))
                return@map try {
                    objectMapper.readValue(it.body, SolvingResultsBody::class.java)
                } catch (e: Exception) {
                    logger.error("Error while deserializing plan message! {}", e.message)
                    throw Exception("Error while Parsing Results!")
                }
            }
            .filter {
                // filter of the correct request id.
                requestId == it.requestId
            }
            .map {
                if (it.content == null) {
                    // TODO: handle this issue
                    throw Exception("empty content message!")
                }

                return@map when (it.content) {
                    is NoPlan -> PlanXWebSocketMessage(
                        requestId = it.requestId,
                        error = WebSocketError(
                            errorMessage = (it?.content as NoPlan).message
                        )
                    )
                    else -> PlanXWebSocketMessage(
                        requestId = it.requestId,
                        content = it.content
                    )
                }
            }
    }

    private fun getErrorResponseWebsocketMessageByRequestId(requestId: String): Flux<PlanXWebSocketMessage> {
        return errorDeliveryFlux
            .map {
                objectMapper.readValue(it.body, CustomErrorMessage::class.java)
            }
            .filter {
                // filter of the correct request id.
                requestId == it.requestId
            }
            .map {
                if (it.errorMessage.isNullOrBlank()) {
                    throw Exception("empty error message!")
                }
                val error = WebSocketError(
                    errorMessage = it.errorMessage,
                    origin = it.sender,
                    stackTrace = it.stackTrace?.map { stackTraceElement ->
                        stackTraceElement.toString()
                    }
                )
                return@map PlanXWebSocketMessage(
                    requestId = it.requestId,
                    error = error
                )
            }
    }
}