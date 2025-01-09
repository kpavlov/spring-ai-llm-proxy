// (C) Copyright 2025 Konstantin Pavlov. Licensed under BSD-3-Clause License.
package me.kpavlov.llm.proxy.sample.client

import me.kpavlov.llm.proxy.grpc.client.LlmProxyClientAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
@ImportAutoConfiguration(
    LlmProxyClientAutoConfiguration::class,
)
open class SampleApplication
