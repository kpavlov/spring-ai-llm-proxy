// (C) Copyright 2025 Konstantin Pavlov. Licensed under BSD-3-Clause License.
package me.kpavlov.llm.proxy.server.grpc

import io.grpc.BindableService
import me.kpavlov.llm.proxy.llm.spring.ai.StreamingChatModelLlmService
import me.kpavlov.llm.proxy.router.DispatchingLlmService
import org.springframework.ai.chat.model.StreamingChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class LlmServicesConfiguration {
    //    @Bean
//    open fun llmService(): LlmService = LlmServiceImpl
//
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    open fun openaiLlmService(model: StreamingChatModel): LlmService =
        StreamingChatModelLlmService(model)

    @Bean
    open fun llmServiceDispatcher(llmServices: Map<String, LlmService>): DispatchingLlmService =
        DispatchingLlmService(llmServices)

    @Bean
    open fun llmGrpcService(service: DispatchingLlmService): BindableService =
        LlmServiceGrpcImpl(service)
}
