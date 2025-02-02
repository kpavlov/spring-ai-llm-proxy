// (C) Copyright 2025 Konstantin Pavlov. Licensed under BSD-3-Clause License.
package me.kpavlov.llm.proxy.llm.spring.ai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.reactive.asFlow
import me.kpavlov.llm.proxy.grpc.v1.ChatCompletionRequest
import me.kpavlov.llm.proxy.server.grpc.LlmError
import me.kpavlov.llm.proxy.server.grpc.LlmResult
import me.kpavlov.llm.proxy.server.grpc.LlmService
import org.reactivestreams.Publisher
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.StreamingChatModel
import org.springframework.ai.chat.prompt.DefaultChatOptionsBuilder
import org.springframework.ai.chat.prompt.Prompt
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

open class StreamingChatModelLlmService(
    private val model: StreamingChatModel,
) : LlmService {
    override suspend fun process(request: ChatCompletionRequest): Flow<LlmResult> =
        model
            .stream(convertRequest(request))
            .log()
            .flatMap { convertLlmMessage(it) }
            .asFlow()

    private fun convertRequest(request: ChatCompletionRequest): Prompt =
        Prompt(
            request.prompt.content,
            DefaultChatOptionsBuilder()
                .build(),
        )

    private fun convertLlmMessage(message: ChatResponse): Publisher<LlmResult> =
        if (message.results.isNotEmpty()) {
            Flux
                .fromIterable(message.results)
                .map {
                    val metadata = it.metadata
                    if (metadata.finishReason.isNotBlank()) {
                        LlmResult.Completed(metadata.finishReason)
                    } else {
                        LlmResult.Content(it.output.text)
                    }
                }
        } else {
            Mono.just(LlmResult.Error(LlmError.ModelError("Unknown message")))
        }
}
