package me.kpavlov.llm.proxy.router

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import me.kpavlov.llm.proxy.grpc.v1.ChatCompletionRequest
import me.kpavlov.llm.proxy.server.grpc.LlmResult
import me.kpavlov.llm.proxy.server.grpc.LlmService
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class DispatchingLlmService(
    routes: Map<String, LlmService>,
) : LlmService {
    private val logger = LoggerFactory.getLogger(DispatchingLlmService::class.java)
    private val routes = ConcurrentHashMap<String, LlmService>()
    private val strategy = RoundRobinRoutingStrategy<ChatCompletionRequest, LlmService>()

    init {
        for ((key, value) in routes) {
            this.routes[key] = value
        }
        logger.info("Initialized with routes: ${routes.keys.joinToString(", ")}")
    }

    override suspend fun process(request: ChatCompletionRequest): Flow<LlmResult> =
        getRoute(request)?.process(request) ?: emptyFlow()

    private fun getRoute(request: ChatCompletionRequest): LlmService? =
        strategy.routeRequest(request, routes.values.toList())
}
