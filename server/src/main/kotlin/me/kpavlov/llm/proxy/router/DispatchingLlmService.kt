package me.kpavlov.llm.proxy.router

import kotlinx.coroutines.flow.Flow
import me.kpavlov.llm.proxy.grpc.v1.ChatCompletionRequest
import me.kpavlov.llm.proxy.server.grpc.LlmResult
import me.kpavlov.llm.proxy.server.grpc.LlmService
import java.util.concurrent.ConcurrentHashMap

class DispatchingLlmService : LlmService {
    private val routes: Map<String, LlmService> = ConcurrentHashMap()

    override suspend fun process(request: ChatCompletionRequest): Flow<LlmResult> {
        TODO("Not yet implemented")
    }
}
