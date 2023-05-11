package org.planx.managing.controller

import com.github.tomakehurst.wiremock.common.Json
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import org.planx.managing.models.core.FunctionalityStatus
import org.planx.managing.models.core.PlanningRequestResponse
import org.planx.managing.models.rest.SolvingRequestBody
import org.planx.managing.services.ManagingService
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import org.planx.models.endpoint.solving.Language
import org.planx.models.endpoint.solving.Planner
import org.planx.models.endpoint.solving.SolvingResultsBody
import java.time.Instant

@ExtendWith(SpringExtension::class)
@WebFluxTest(ManagingController::class)
internal class ManagingControllerIntTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var managingService: ManagingService

    @Nested
    inner class GetAllFunctionalities {
        @Test
        fun `happy path - valid request, 1 element`() {
            // given
            val functionalityStatus = validFunctionality()
            `when`(managingService.getAllFunctionalities())
                .doReturn(Flux.just(functionalityStatus))

            // assert
            webTestClient.get().uri("/v1/managing/functionalities")
                .exchange()
                .expectStatus().isOk
                .expectHeader().valueEquals("Application-Name", "PlanX_Managing_Service")
                .expectBody()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("[0].name").isEqualTo(functionalityStatus.name)
                .jsonPath("[0].health.status").isEqualTo(functionalityStatus.status.status.code)
        }


        private fun validFunctionality(): FunctionalityStatus {
            return FunctionalityStatus(
                name = "v1.router.managing-service#toolbox",
                status = Health.up().build(),
                connectedAt = Instant.now()
            )
        }
    }

    @Nested
    inner class RequestPlanning {
        @Test
        fun `happy path - valid request`() {
            // given
            val validSolvingRequest = validSolvingRequest()
            val validPlanningRequestResponse = PlanningRequestResponse(validSolvingRequest.requestId)
            `when`(managingService.handleSolvingRequest(anyNonNull(), eq(validSolvingRequest.requestId)))
                .thenReturn(Mono.just(validPlanningRequestResponse))


            // assert
            webTestClient.post().uri("/v1/managing/solving")
                .body(Mono.just(validSolvingRequest), SolvingRequestBody::class.java)
                .exchange()
                .expectStatus().isOk
                .expectHeader().valueEquals("Application-Name", "UIR_Planner_Managing_Cap")
                .expectBody().json(Json.write(validPlanningRequestResponse))
        }

        @Test
        fun `unhappy path - invalid request - planner not found`() {
            // given
            val solvingRequestBody = invalidSolvingRequestPlannerNotFound()

            // assert
            webTestClient.post().uri("/v1/managing/solving")
                .body(Mono.just(solvingRequestBody), SolvingRequestBody::class.java)
                .exchange()
                .expectStatus().isBadRequest
                .expectHeader().valueEquals("Application-Name", "UIR_Planner_Managing_Cap")
                .expectBody()
                .jsonPath("$.message", "Unsupported planner type ${solvingRequestBody.planner}")
        }

        @Test
        fun `unhappy path - invalid request - language not found`() {
            // given
            val solvingRequestBody = invalidSolvingRequestLanguageNotFound()

            // assert
            webTestClient.post().uri("/v1/managing/solving")
                .body(Mono.just(solvingRequestBody), SolvingRequestBody::class.java)
                .exchange()
                .expectStatus().isBadRequest
                .expectHeader().valueEquals("Application-Name", "UIR_Planner_Managing_Cap")
                .expectBody()
                .jsonPath("$.message", "Unsupported input language type ${solvingRequestBody.language}")
        }

        private fun validSolvingRequest(): SolvingRequestBody {
            return SolvingRequestBody(
                requestId = "6092034d-5c7a-4de8-9179-0a57cd88fe8a",
                domain = PDDL_LOGISTICS_DOMAIN,
                problem = PDDL_LOGISTICS_PROBLEM_1,
                planner = Planner.HSP.value,
                language = Language.PDDL.name
            )
        }

        private fun invalidSolvingRequestPlannerNotFound(): SolvingRequestBody {
            return SolvingRequestBody(
                requestId = "6092034d-5c7a-4de8-9179-0a57cd88fe8a",
                domain = PDDL_LOGISTICS_DOMAIN,
                problem = PDDL_LOGISTICS_PROBLEM_1,
                planner = "invalid-planner-name",
                language = Language.PDDL.name
            )
        }

        private fun invalidSolvingRequestLanguageNotFound(): SolvingRequestBody {
            return SolvingRequestBody(
                requestId = "6092034d-5c7a-4de8-9179-0a57cd88fe8a",
                domain = PDDL_LOGISTICS_DOMAIN,
                problem = PDDL_LOGISTICS_PROBLEM_1,
                planner = Planner.FF.value,
                language = "invalid-lang"
            )
        }
    }

    @Nested
    inner class GetAllFunctionalityInterfaces {
        @Test
        fun `happy path - valid request`() {
            // given
            val interfaceName1 = SolvingResultsBody::class.java.toString()
            `when`(managingService.getAllFunctionalityInterfaces())
                .thenReturn(Flux.just(interfaceName1))

            val expectedJson = Json.write(arrayOf(interfaceName1))

            // assert
            webTestClient.get().uri("/v1/managing/functionality/interfaces")
                .exchange()
                .expectStatus().isOk
                .expectHeader().valueEquals("Application-Name", "PlanX_Managing_Service")
                .expectBody().json(expectedJson)
        }
    }


    // BASE 64 encoded
    val PDDL_LOGISTICS_DOMAIN = ""
    val PDDL_LOGISTICS_PROBLEM_1 = ""

    inline fun <reified T> anyNonNull(): T = Mockito.any<T>(T::class.java)
}