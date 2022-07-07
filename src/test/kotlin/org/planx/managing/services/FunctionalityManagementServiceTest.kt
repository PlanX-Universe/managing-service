package org.planx.managing.services

import org.planx.managing.repositories.FunctionalityRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito

internal class FunctionalityManagementServiceTest {

    private val functionalityRepoMock: FunctionalityRepository =
            Mockito.mock(FunctionalityRepository::class.java)
    private val cut: FunctionalityManagementService =
            FunctionalityManagementService(functionalityRepoMock)

    @Test
    fun getAllFunctionalities() {
        // given
        Mockito.`when`(functionalityRepoMock.findAll())
                .thenReturn(emptyList())

        val result = cut.getAllFunctionalities()
        assertThat(result).isEmpty()
    }
}