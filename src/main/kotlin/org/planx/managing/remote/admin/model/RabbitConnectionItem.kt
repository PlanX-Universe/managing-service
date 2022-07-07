package org.planx.managing.remote.admin.model


data class RabbitConnectionItem(
    val state: String,
    val connected_at: Long,
    val user_provided_name: String,
    // unused
    val auth_mechanism: String?,
    val channel_max: Int?,
    val channels: Int?,
    val host: String?,
    val name: String?,
    val node: String?,
    val port: Int?,
    val protocol: String?,
    val timeout: Int?,
    val type: String?,
    val user: String?,
    val user_who_performed_action: String?,
    val vhost: String?
)