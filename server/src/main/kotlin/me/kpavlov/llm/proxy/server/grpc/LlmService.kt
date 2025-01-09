// (C) Copyright 2025 Konstantin Pavlov. Licensed under BSD-3-Clause License.
package me.kpavlov.llm.proxy.server.grpc

import kotlinx.coroutines.flow.Flow
import me.kpavlov.llm.proxy.grpc.v1.ChatCompletionRequest

/**
 * Represents a service for processing large language model (LLM) requests.
 *
 * This interface defines a single method, `process`, which asynchronously processes a given
 * `ChatCompletionRequest` and returns a stream of results as a `Flow` of `LlmResult`.
 *
 * The `process` function allows handling both content generation and potential errors
 * from the processing of the request. The results returned could either be:
 * - `LlmResult.Content` containing a generated textual response.
 * - `LlmResult.Error` encapsulating details of any error encountered during processing.
 *
 * Implementations of this interface are expected to define how the LLM requests are
 * processed and emit corresponding results via the returned `Flow`.
 */
interface LlmService {
    suspend fun process(request: ChatCompletionRequest): Flow<LlmResult>
}
