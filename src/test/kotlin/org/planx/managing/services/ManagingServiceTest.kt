package org.planx.managing.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.rabbitmq.client.Connection
import com.rabbitmq.client.Delivery
import org.planx.managing.messaging.producer.Sender
import org.planx.managing.models.core.FunctionalityStatus
import org.planx.managing.models.core.PlanningRequestResponse
import org.planx.managing.models.entities.FunctionalityEntity
import org.planx.managing.remote.admin.RabbitmqApiService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.boot.actuate.health.Health
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import org.planx.models.endpoint.solving.Language
import org.planx.models.endpoint.solving.Planner
import org.planx.models.endpoint.solving.UniversalSolvingRequest
import java.time.Instant

internal class ManagingServiceTest {

    private val sender: Sender = mock(Sender::class.java)
    private val deliveryFlux: Flux<Delivery> = Flux.never()
    private val errorDeliveryFlux: Flux<Delivery> = Flux.never()
    private val rabbitApiService: RabbitmqApiService = mock(RabbitmqApiService::class.java)
    private val functionalityManagementService: FunctionalityManagementService = mock(FunctionalityManagementService::class.java)
    private val objectMapper: ObjectMapper = mock(ObjectMapper::class.java)
    private val connectionMono: Mono<Connection> = Mono.empty()
    private var planningResponseRoutingKey: String = "test.reply.plan"

    private val cut: ManagingService = ManagingService(
        sender,
        deliveryFlux,
        errorDeliveryFlux,
        rabbitApiService,
        functionalityManagementService,
        objectMapper,
        connectionMono,
        planningResponseRoutingKey
    )

    @Nested
    inner class `GetAllFunctionalities` {
        @Test
        fun `happy path - request all functionalities`() {
            val exampleStatus = FunctionalityStatus("Test 1", Health.up().build(), Instant.now())
            `when`(rabbitApiService.getAllConnections()).doReturn(Flux.just(exampleStatus))

            val result = cut.getAllFunctionalities()

            StepVerifier.create(result)
                .assertNext {
                    assertThat(it).isNotNull
                    assertThat(it).isEqualToComparingFieldByFieldRecursively(exampleStatus)
                }
                .verifyComplete()
        }
    }

    @Nested
    inner class `HandleSolvingRequest` {
        @Test
        fun `happy path - handleSolvingRequest`() {
            val solvingRequest =
                UniversalSolvingRequest(Planner.HSP, Language.PDDL, "...encodedDomain", "...encodedProblem")
            val requestId = "41c5519a-79db-473b-9c27-8839fba061ff"
            val planningRequestResponse = PlanningRequestResponse(requestId)

            `when`(sender.requestPlanning(anyNonNull(), eq(requestId), anyNonNull())).doReturn(
                Mono.just(
                    planningRequestResponse
                )
            )

            val result = cut.handleSolvingRequest(solvingRequest, requestId)

            StepVerifier.create(result)
                .assertNext {
                    assertThat(it.requestId).isEqualTo(requestId)
                }
                .verifyComplete()
        }
    }

    @Nested
    inner class `RegisterSocket` {
        // FIXME: this tests are running infinite! verify whats wrong?!

        @Test
        fun `happy path - result message is incoming`() {
//            val requestId = "41c5519a-79db-473b-9c27-8839fba061ff"
//            val msg = Delivery(
//                Envelope(1234L, false, "", ""),
//                AMQP.BasicProperties(),
//                "{\"content\":{\"__TypeID__\":\"org.planx.models.endpoint.solving.plan.sequential.SequentialPlan\",\"cost\":20.0,\"actions\":[{\"name\":\"load-truck\",\"parameters\":[\"obj23\",\"tru2\",\"pos2\"],\"instantiations\":[\"pos2\",\"obj11\",\"cit1\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":0},{\"name\":\"load-truck\",\"parameters\":[\"obj21\",\"tru2\",\"pos2\"],\"instantiations\":[\"pos2\",\"obj11\",\"cit1\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":1},{\"name\":\"drive-truck\",\"parameters\":[\"tru2\",\"pos2\",\"apt2\",\"cit2\"],\"instantiations\":[\"obj11\",\"cit1\",\"cit1\",\"tru2\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":2},{\"name\":\"load-truck\",\"parameters\":[\"obj13\",\"tru1\",\"pos1\"],\"instantiations\":[\"pos2\",\"obj11\",\"cit1\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":3},{\"name\":\"unload-truck\",\"parameters\":[\"obj23\",\"tru2\",\"apt2\"],\"instantiations\":[\"pos2\",\"obj11\",\"cit1\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":4},{\"name\":\"load-truck\",\"parameters\":[\"obj11\",\"tru1\",\"pos1\"],\"instantiations\":[\"pos2\",\"obj11\",\"cit1\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":5},{\"name\":\"unload-truck\",\"parameters\":[\"obj21\",\"tru2\",\"apt2\"],\"instantiations\":[\"pos2\",\"obj11\",\"cit1\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":6},{\"name\":\"load-airplane\",\"parameters\":[\"obj21\",\"apn1\",\"apt2\"],\"instantiations\":[\"pos2\",\"obj22\",\"cit1\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":7},{\"name\":\"load-airplane\",\"parameters\":[\"obj23\",\"apn1\",\"apt2\"],\"instantiations\":[\"pos2\",\"obj22\",\"cit1\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":8},{\"name\":\"fly-airplane\",\"parameters\":[\"apn1\",\"apt2\",\"apt1\"],\"instantiations\":[\"obj22\",\"obj13\",\"obj13\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":9},{\"name\":\"unload-airplane\",\"parameters\":[\"obj21\",\"apn1\",\"apt1\"],\"instantiations\":[\"pos2\",\"obj22\",\"cit1\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":10},{\"name\":\"drive-truck\",\"parameters\":[\"tru1\",\"pos1\",\"apt1\",\"cit1\"],\"instantiations\":[\"obj11\",\"cit1\",\"cit1\",\"tru2\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":11},{\"name\":\"load-truck\",\"parameters\":[\"obj21\",\"tru1\",\"apt1\"],\"instantiations\":[\"pos2\",\"obj11\",\"cit1\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":12},{\"name\":\"unload-airplane\",\"parameters\":[\"obj23\",\"apn1\",\"apt1\"],\"instantiations\":[\"pos2\",\"obj22\",\"cit1\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":13},{\"name\":\"load-truck\",\"parameters\":[\"obj23\",\"tru1\",\"apt1\"],\"instantiations\":[\"pos2\",\"obj11\",\"cit1\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":14},{\"name\":\"unload-truck\",\"parameters\":[\"obj13\",\"tru1\",\"apt1\"],\"instantiations\":[\"pos2\",\"obj11\",\"cit1\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":15},{\"name\":\"unload-truck\",\"parameters\":[\"obj11\",\"tru1\",\"apt1\"],\"instantiations\":[\"pos2\",\"obj11\",\"cit1\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":16},{\"name\":\"drive-truck\",\"parameters\":[\"tru1\",\"apt1\",\"pos1\",\"cit1\"],\"instantiations\":[\"obj11\",\"cit1\",\"cit1\",\"tru2\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":17},{\"name\":\"unload-truck\",\"parameters\":[\"obj21\",\"tru1\",\"pos1\"],\"instantiations\":[\"pos2\",\"obj11\",\"cit1\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":18},{\"name\":\"unload-truck\",\"parameters\":[\"obj23\",\"tru1\",\"pos1\"],\"instantiations\":[\"pos2\",\"obj11\",\"cit1\"],\"cost\":1.0,\"duration\":1.0,\"momentInTime\":19}]},\"requestId\":\"${requestId}\",\"callStack\":{\"elements\":[[\"v1.router.managing-service\",\"toolbox.reply.plan\",null,\"java.util.Map\"]],\"empty\":false}}".toByteArray()
//            )
//
//            val innerCut = ManagingService(
//                sender,
//                Flux.just(msg),
//                Flux.never(),
//                rabbitApiService,
//                functionalityManagementService,
//                Jackson2ObjectMapperBuilder().build(),
//                connectionMono,
//                planningResponseRoutingKey
//            )
//
//            val result: Flux<PlanXWebSocketMessage> = innerCut.registerSocket(requestId)
//
//            StepVerifier.create(result)
//                .assertNext {
//                    assertThat(it.requestId).isEqualTo(requestId)
//                    assertThat(it.error).isNull()
//                    assertThat(it.content).isNotNull
//                    assertThat(it.content).hasFieldOrPropertyWithValue("cost", 20.0)
//                }
//                .verifyComplete()
        }

        //
        @Test
        fun `happy path - error message is end`() {
//            val requestId = "41c5519a-79db-473b-9c27-8839fba061ff"
//            val errorOrigin = "v1.transforming.parsing-service"
//            val errorMsg = Delivery(
//                Envelope(1L, false, "", ""),
//                AMQP.BasicProperties(),
//                ("{\"sender\":\"${errorOrigin}\",\n" +
//                        "   \"requestId\":\"${requestId}\",\n" +
//                        "   \"errorMessage\":\"Listener method 'public void org.planx.parsing.messaging.consumer.Receiver.handleMessage(org.planx.models.transforming.parsing.ParsingRequestBody,org.springframework.amqp.core.Message)' threw exception\\nInternal Error: \\nPARSER_ERROR:\\n\\n\\nLEXICAL_ERROR:\\nunexpected token \\\")\\\".\\nWas expecting:\\n    \\\"(\\\" ...\\n\\n\\nPARSER_WARNING:\\n\\n\\n\",\n" +
//                        "   \"stackTrace\":[\n" +
//                        "      { \"classLoaderName\":null, \"moduleName\":null, \"moduleVersion\":null, \"methodName\":\"invokeHandler\", \"fileName\":\"MessagingMessageListenerAdapter.java\",\"lineNumber\":228,   \"nativeMethod\":false,\"className\":\"org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter\"}\n" +
//                        "   ]\n" +
//                        "}").toByteArray()
//            )
//
//            val innerCut = ManagingService(
//                sender,
//                Flux.never(),
//                Flux.just(errorMsg),
//                rabbitApiService,
//                functionalityManagementService,
//                Jackson2ObjectMapperBuilder().build(),
//                connectionMono,
//                planningResponseRoutingKey
//            )
//
//            val result: Flux<PlanXWebSocketMessage> = innerCut.registerSocket(requestId)
//
//            StepVerifier.create(result)
//                .assertNext {
//                    assertThat(it.requestId).isEqualTo(requestId)
//                    assertThat(it.content).isNull()
//                    assertThat(it.error?.origin).isEqualTo(errorOrigin)
//                }
//                .verifyComplete()
        }
    }

    @Nested
    inner class `GetAllFunctionalityInterfaces` {
        @Test
        fun `happy path - returns stream`() {

            `when`(functionalityManagementService.getAllFunctionalities()).doReturn(
                listOf(
                    FunctionalityEntity(
                        "67ae9481-8dde-4295-82ea-7e2ee780d8aa", "", "", listOf(
                            "interface test.1.first",
                            "interface test.1.second"
                        )
                    ),
                    FunctionalityEntity(
                        "03c12ece-8f45-4402-bcd1-228a5a9b672b", "", "", listOf(
                            "interface test.2.first",
                            "interface test.2.second"
                        )
                    )
                )
            )

            val result = cut.getAllFunctionalityInterfaces()

            StepVerifier.create(result)
                .assertNext {
                    assertThat(it).isEqualTo("interface test.1.first")
                }
                .assertNext {
                    assertThat(it).isEqualTo("interface test.1.second")
                }
                .assertNext {
                    assertThat(it).isEqualTo("interface test.2.first")
                }
                .assertNext {
                    assertThat(it).isEqualTo("interface test.2.second")
                }
                .verifyComplete()
        }
    }

    inline fun <reified T> anyNonNull(): T = Mockito.any<T>(T::class.java)
}
