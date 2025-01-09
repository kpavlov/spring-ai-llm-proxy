// (C) Copyright 2025 Konstantin Pavlov. Licensed under BSD-3-Clause License.
package me.kpavlov.llm.proxy.sample.client

import me.kpavlov.llm.proxy.grpc.client.LlmProxyClientAutoConfiguration
import me.kpavlov.llm.proxy.server.grpc.LlmProxyGrpcServerAutoConfiguration
import me.kpavlov.llm.proxy.server.grpc.LlmServicesConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(LlmServicesConfiguration::class)
@ImportAutoConfiguration(
    LlmProxyClientAutoConfiguration::class,
    LlmProxyGrpcServerAutoConfiguration::class,
)
open class SampleApplication
