// (C) Copyright 2025 Konstantin Pavlov. Licensed under BSD-3-Clause License.
package me.kpavlov.llm.proxy.server.grpc

import io.grpc.BindableService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class LlmServicesConfiguration {
    @Bean
    open fun llmService(): LlmService = LlmServiceImpl

    @Bean
    open fun llmGrpcService(service: LlmService): BindableService = LlmServiceGrpcImpl(service)
}
