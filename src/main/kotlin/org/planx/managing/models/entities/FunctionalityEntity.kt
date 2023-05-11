package org.planx.managing.models.entities

import org.hibernate.annotations.GenericGenerator
import javax.persistence.*


@Entity
@Table(name = "FUNCTIONALITY_TABLE")
data class FunctionalityEntity(
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    var id: String = "",

    var topic: String = "",
    var routingKey: String = "",

    @ElementCollection(fetch = FetchType.EAGER)
    var interfaces: List<String> = mutableListOf()
) {
    override fun toString(): String {
        return "FunctionalityEntity(id='$id', topic='$topic', routingKey='$routingKey', interfaces=$interfaces)"
    }
}
