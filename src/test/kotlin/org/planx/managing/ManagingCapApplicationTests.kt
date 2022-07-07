package org.planx.managing

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import


@SpringBootTest
@Import(MessagingTestConfig::class)
class ManagingCapApplicationTests {

    @Test
    fun contextLoads() {
    }
}
