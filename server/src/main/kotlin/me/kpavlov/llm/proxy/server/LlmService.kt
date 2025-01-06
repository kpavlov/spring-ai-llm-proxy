package me.kpavlov.llm.proxy.server

import kotlinx.coroutines.flow.Flow
import me.kpavlov.llm.proxy.grpc.v1.ChatCompletionRequest

// Service interface
interface LlmService {
    suspend fun process(request: ChatCompletionRequest): Flow<LlmResult>
}
