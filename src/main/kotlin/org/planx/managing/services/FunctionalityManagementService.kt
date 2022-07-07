package org.planx.managing.services

import org.planx.managing.models.entities.FunctionalityEntity
import org.planx.managing.repositories.FunctionalityRepository
import org.springframework.stereotype.Service

@Service
class FunctionalityManagementService(
    private val functionalityRepository: FunctionalityRepository
) {

    fun getAllFunctionalities(): List<FunctionalityEntity> {
        return functionalityRepository.findAll()
    }

    //  TODO: add functionality
    fun addFunctionality() {
        val cap = FunctionalityEntity()
        functionalityRepository.save(cap)
    }
}