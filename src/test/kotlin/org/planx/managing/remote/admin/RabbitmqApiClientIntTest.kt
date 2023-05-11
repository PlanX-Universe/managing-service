package org.planx.managing.remote.admin

import com.github.tomakehurst.wiremock.client.WireMock
import org.planx.managing.BaseIntTest
import org.planx.managing.MessagingTestConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource
import reactor.test.StepVerifier

@AutoConfigureWireMock(port = 0)
@TestPropertySource(
    properties = [
        "spring.rabbitmq.admin.api.url=localhost:${'$'}{wiremock.server.port}"
    ]
)
@Import(MessagingTestConfig::class)
internal class RabbitmqApiClientIntTest : BaseIntTest() {

    @Autowired
    lateinit var cut: RabbitmqApiClient

    @BeforeEach
    fun setUp() {
        WireMock.resetAllRequests()
    }

    @Test
    fun `happy path - retrieveAllConnections`() {
        RabbitAdminApiWiremockTestCfg.setupConnectionApi()

        val result = cut.retrieveAllConnections()
        StepVerifier.create(result)
            .assertNext {
                assertThat(it.size).isEqualTo(2)
                assertThat(it.first().user_provided_name).isEqualTo("v1.transforming.parsing-service#toolbox.pddl4j")
                assertThat(it[1].user_provided_name).isEqualTo("v1.transforming.converting-service#toolbox.pddl4j-encoding")
            }
            .verifyComplete()
    }
}