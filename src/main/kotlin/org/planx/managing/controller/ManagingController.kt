package org.planx.managing.controller

import org.planx.managing.config.getLoggerFor
import org.planx.managing.exceptions.ManagingError
import org.planx.managing.models.core.toFunctionalityResponseBody
import org.planx.managing.models.core.toResponseBody
import org.planx.managing.models.rest.HttpResponseBody
import org.planx.managing.models.rest.SolvingRequestBody
import org.planx.managing.services.ManagingService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import org.planx.common.models.endpoint.solving.Language
import org.planx.common.models.endpoint.solving.Planner
import org.planx.common.models.endpoint.solving.UniversalSolvingRequest

@RestController
@RequestMapping("/v1/managing")
@CrossOrigin(
    origins = [], allowCredentials = "false", allowedHeaders = ["*"],
    methods = [RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.DELETE]
)
class ManagingController(
    private val managingService: ManagingService
) {
    var logger = getLoggerFor<ManagingController>()

    /**
     * Get all [Functionality]s
     *
     * @return [Functionality] or not found if no functionalities are available
     */
    @GetMapping("/functionalities")
    fun getAllFunctionalities(): Mono<ResponseEntity<List<HttpResponseBody>>> {
        return managingService.getAllFunctionalities()
            .map { it.toFunctionalityResponseBody() }
            .collectList()
            .map {
                ResponseEntity.ok(it)
            }
    }

    /**
     * Rest controller for planning request
     */
    @PostMapping("/solving")
    fun requestPlanning(@RequestBody body: SolvingRequestBody): Mono<ResponseEntity<HttpResponseBody>> {
        val planner: Planner = when (body.planner) {
            Planner.HSP.value -> Planner.HSP
            Planner.FF.value -> Planner.FF
            Planner.FFAnytime.value -> Planner.FFAnytime
            Planner.HCAnytime.value -> Planner.HCAnytime
            else -> throw ManagingError(
                requestId = body.requestId,
                internalErrors = "Unsupported planner type ${body.planner}"
            )
        }

        val language = when (body.language) {
            Language.PDDL.name -> Language.PDDL
            else -> throw ManagingError(
                requestId = body.requestId,
                internalErrors = "Unsupported input language type ${body.language}"
            )
        }

        val request = UniversalSolvingRequest(
            problem = body.problem,
            domain = body.domain,
            planner = planner,
            language = language
        )

        return managingService.handleSolvingRequest(request, body.requestId)
            .map { it.toResponseBody() }
            .map {
                ResponseEntity.ok(it)
            }
    }

    /**
     * This function returns all possible interfaces, contained on the repository
     *
     * TODO: change from List of strings to a Model!
     */
    @GetMapping("/functionality/interfaces")
    fun getAllFunctionalityInterfaces(): Mono<ResponseEntity<List<String>>> {
        return managingService.getAllFunctionalityInterfaces()
            .collectList()
            .map {
                ResponseEntity.ok(it)
            }
    }
}
