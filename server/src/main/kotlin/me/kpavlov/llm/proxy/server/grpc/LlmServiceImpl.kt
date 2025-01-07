package me.kpavlov.llm.proxy.server.grpc

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.kpavlov.llm.proxy.grpc.v1.ChatCompletionRequest
import org.springframework.stereotype.Service

@Service
object LlmService2 : LlmService {
    override suspend fun process(request: ChatCompletionRequest): Flow<LlmResult> =
        flow {
            emit(LlmResult.Content("Processing request: ${request.prompt.content}"))
            delay(1000) // Simulate processing
            emit(LlmResult.Content("Here's your response..."))
        }
}
