package org.planx.managing

import org.planx.managing.models.entities.FunctionalityEntity
import org.planx.managing.repositories.FunctionalityRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.planx.common.models.endpoint.solving.SolvingResultsBody
import java.util.*


/**
 * [MessagingRabbitConfig] should be MOCKED!
 * Otherwise the test will run infinite
 */
@Import(MessagingTestConfig::class)
internal class JpaIntegrationTest : BaseIntTest() {

    @Autowired
    private val functionalityRepository: FunctionalityRepository? = null

    @Test
    fun `repository Test`() {
        var functionalityEntity = FunctionalityEntity()
        functionalityEntity.apply {
            this.routingKey = "toolbox"
            this.topic = "v1.endpoint.solving-service"
            this.interfaces = listOf(
                SolvingResultsBody::class.java.toString()
            )
        }
        // returns id
        functionalityEntity = functionalityRepository!!.save(
            functionalityEntity
        )
        val foundEntity: Optional<FunctionalityEntity> = functionalityRepository
            .findById(functionalityEntity.id)
        assertThat(foundEntity.isPresent).isTrue()
        assertThat(functionalityEntity).isEqualToComparingFieldByFieldRecursively(foundEntity.get())
    }
}
