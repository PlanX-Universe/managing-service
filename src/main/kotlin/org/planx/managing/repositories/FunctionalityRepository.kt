package org.planx.managing.repositories

import org.planx.managing.models.entities.FunctionalityEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FunctionalityRepository : JpaRepository<FunctionalityEntity, String> {
}
