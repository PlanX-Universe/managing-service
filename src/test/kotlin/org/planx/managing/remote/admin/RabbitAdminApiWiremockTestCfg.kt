package org.planx.managing.remote.admin

import com.github.tomakehurst.wiremock.client.WireMock

object RabbitAdminApiWiremockTestCfg {
    fun setupConnectionApi() {
        WireMock.stubFor(
                WireMock.get(
                        WireMock.urlEqualTo(
                                "/connections"
                        )
                ).withHeader("Application-Name", WireMock.equalTo("UIR_Planner_Managing_Cap"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(getWiremockConnectionApiResponse())
                        )
        )
    }

    private fun getWiremockConnectionApiResponse() = """
        [
    {
        "auth_mechanism": "PLAIN",
        "channel_max": 2047,
        "channels": 2,
        "client_properties": {
            "functionalities": {
                "authentication_failure_close": true,
                "basic.nack": true,
                "connection.blocked": true,
                "consumer_cancel_notify": true,
                "exchange_exchange_bindings": true,
                "publisher_confirms": true
            },
            "connection_name": "v1.transforming.parsing-service#toolbox.pddl4j",
            "copyright": "Copyright (c) 2007-2020 VMware, Inc. or its affiliates.",
            "information": "Licensed under the MPL. See https://www.rabbitmq.com/",
            "platform": "Java",
            "product": "RabbitMQ",
            "version": "5.9.0"
        },
        "connected_at": 1606208630992,
        "frame_max": 131072,
        "garbage_collection": {
            "fullsweep_after": 65535,
            "max_heap_size": 0,
            "min_bin_vheap_size": 46422,
            "min_heap_size": 233,
            "minor_gcs": 569
        },
        "host": "172.20.0.2",
        "name": "172.20.0.4:41682 -> 172.20.0.2:5672",
        "node": "rabbit@9130a48385d1",
        "peer_cert_issuer": null,
        "peer_cert_subject": null,
        "peer_cert_validity": null,
        "peer_host": "172.20.0.4",
        "peer_port": 41682,
        "port": 5672,
        "protocol": "AMQP 0-9-1",
        "recv_cnt": 547,
        "recv_oct": 88218,
        "recv_oct_details": {
            "rate": 0.0
        },
        "reductions": 2904052,
        "reductions_details": {
            "rate": 364.0
        },
        "send_cnt": 535,
        "send_oct": 25466,
        "send_oct_details": {
            "rate": 0.0
        },
        "send_pend": 0,
        "ssl": false,
        "ssl_cipher": null,
        "ssl_hash": null,
        "ssl_key_exchange": null,
        "ssl_protocol": null,
        "state": "running",
        "timeout": 60,
        "type": "network",
        "user": "planx_user",
        "user_provided_name": "v1.transforming.parsing-service#toolbox.pddl4j",
        "user_who_performed_action": "planx_user",
        "vhost": "/"
    },
    {
        "auth_mechanism": "PLAIN",
        "channel_max": 2047,
        "channels": 2,
        "client_properties": {
            "functionalities": {
                "authentication_failure_close": true,
                "basic.nack": true,
                "connection.blocked": true,
                "consumer_cancel_notify": true,
                "exchange_exchange_bindings": true,
                "publisher_confirms": true
            },
            "connection_name": "v1.transforming.converting-service#toolbox.pddl4j-encoding",
            "copyright": "Copyright (c) 2007-2020 VMware, Inc. or its affiliates.",
            "information": "Licensed under the MPL. See https://www.rabbitmq.com/",
            "platform": "Java",
            "product": "RabbitMQ",
            "version": "5.9.0"
        },
        "connected_at": 1606208631239,
        "frame_max": 131072,
        "garbage_collection": {
            "fullsweep_after": 65535,
            "max_heap_size": 0,
            "min_bin_vheap_size": 46422,
            "min_heap_size": 233,
            "minor_gcs": 197
        },
        "host": "172.20.0.2",
        "name": "172.20.0.6:48550 -> 172.20.0.2:5672",
        "node": "rabbit@9130a48385d1",
        "peer_cert_issuer": null,
        "peer_cert_subject": null,
        "peer_cert_validity": null,
        "peer_host": "172.20.0.6",
        "peer_port": 48550,
        "port": 5672,
        "protocol": "AMQP 0-9-1",
        "recv_cnt": 550,
        "recv_oct": 221122,
        "recv_oct_details": {
            "rate": 0.0
        },
        "reductions": 2899374,
        "reductions_details": {
            "rate": 359.6
        },
        "send_cnt": 535,
        "send_oct": 88013,
        "send_oct_details": {
            "rate": 0.0
        },
        "send_pend": 0,
        "ssl": false,
        "ssl_cipher": null,
        "ssl_hash": null,
        "ssl_key_exchange": null,
        "ssl_protocol": null,
        "state": "running",
        "timeout": 60,
        "type": "network",
        "user": "planx_user",
        "user_provided_name": "v1.transforming.converting-service#toolbox.pddl4j-encoding",
        "user_who_performed_action": "planx_user",
        "vhost": "/"
    }
]
    """.trimIndent()
}