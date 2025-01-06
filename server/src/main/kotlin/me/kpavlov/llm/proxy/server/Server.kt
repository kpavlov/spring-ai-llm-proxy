package me.kpavlov.llm.proxy.server

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.Status
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import me.kpavlov.llm.proxy.grpc.v1.ChatCompletionRequest
import me.kpavlov.llm.proxy.grpc.v1.ChatCompletionResponse
import me.kpavlov.llm.proxy.grpc.v1.ChunkType
import me.kpavlov.llm.proxy.grpc.v1.ContentChunk
import me.kpavlov.llm.proxy.grpc.v1.ErrorChunk
import me.kpavlov.llm.proxy.grpc.v1.ErrorCode
import me.kpavlov.llm.proxy.grpc.v1.LlmServiceGrpc
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*

class LlmServiceImpl(
    private val llmService: LlmService
) : LlmServiceGrpc.LlmServiceImplBase() {

    private val logger = LoggerFactory.getLogger(LlmServiceImpl::class.java)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun chatCompletion(
        responseObserver: StreamObserver<ChatCompletionResponse>
    ): StreamObserver<ChatCompletionRequest> {

        return object : StreamObserver<ChatCompletionRequest> {
            override fun onNext(request: ChatCompletionRequest) {
                scope.launch {
                    try {
                        processChatRequest(request, responseObserver)
                    } catch (e: Exception) {
                        handleError(e, responseObserver)
                    }
                }
            }

            override fun onError(error: Throwable) {
                logger.error("Error in chat completion stream", error)
                handleError(error, responseObserver)
            }

            override fun onCompleted() {
                try {
                    responseObserver.onCompleted()
                } catch (e: Exception) {
                    logger.error("Error completing response stream", e)
                }
            }
        }
    }

    private suspend fun processChatRequest(
        request: ChatCompletionRequest,
        responseObserver: StreamObserver<ChatCompletionResponse>
    ) {
        logger.info("Processing chat request: ${request.requestId}")

        try {
            validateRequest(request)

            llmService.process(request).collect { result ->
                when (result) {
                    is LlmResult.Content -> sendContent(result.content, request, responseObserver)
                    is LlmResult.Error -> sendError(result.error, request, responseObserver)
                }
            }
        } catch (e: Exception) {
            handleError(e, responseObserver)
        }
    }

    private fun validateRequest(request: ChatCompletionRequest) {
        require(request.requestId.isNotBlank()) { "Request ID cannot be empty" }
        require(request.sessionId.isNotBlank()) { "Session ID cannot be empty" }
        require(request.prompt.content.isNotBlank()) { "Prompt content cannot be empty" }
    }

    private fun sendContent(
        content: String,
        request: ChatCompletionRequest,
        responseObserver: StreamObserver<ChatCompletionResponse>
    ) {
        val response = ChatCompletionResponse.newBuilder()
            .setChunkId(generateChunkId())
            .setRequestId(request.requestId)
            .setSessionId(request.sessionId)
            .setTimestamp(Instant.now().toEpochMilli())
            .setChunkType(ChunkType.CHUNK_TYPE_CONTENT)
            .setContent(
                ContentChunk.newBuilder()
                    .setText(content)
                    .setIsFinal(false)
                    .build()
            )
            .build()

        responseObserver.onNext(response)
    }

    private fun sendError(
        error: LlmError,
        request: ChatCompletionRequest,
        responseObserver: StreamObserver<ChatCompletionResponse>
    ) {
        val response = ChatCompletionResponse.newBuilder()
            .setChunkId(generateChunkId())
            .setRequestId(request.requestId)
            .setSessionId(request.sessionId)
            .setTimestamp(Instant.now().toEpochMilli())
            .setChunkType(ChunkType.CHUNK_TYPE_ERROR)
            .setError(
                ErrorChunk.newBuilder()
                    .setCode(mapErrorCode(error))
                    .setMessage(error.message ?: "Unknown error")
                    .build()
            )
            .build()

        responseObserver.onNext(response)
    }

    private fun handleError(
        error: Throwable,
        responseObserver: StreamObserver<ChatCompletionResponse>
    ) {
        logger.error("Error processing request", error)

        val status = when (error) {
            is IllegalArgumentException -> Status.INVALID_ARGUMENT
                .withDescription(error.message)

            is LlmServiceException -> Status.INTERNAL
                .withDescription("LLM service error: ${error.message}")

            else -> Status.INTERNAL
                .withDescription("Internal server error")
        }

        responseObserver.onError(status.asException())
    }

    private fun mapErrorCode(error: LlmError): ErrorCode = when (error) {
        is LlmError.InvalidRequest -> ErrorCode.ERROR_CODE_INVALID_REQUEST
        is LlmError.ModelError -> ErrorCode.ERROR_CODE_MODEL_ERROR
        is LlmError.RateLimited -> ErrorCode.ERROR_CODE_RATE_LIMITED
        is LlmError.ContextLengthExceeded -> ErrorCode.ERROR_CODE_CONTEXT_LENGTH_EXCEEDED
        is LlmError.ContentFiltered -> ErrorCode.ERROR_CODE_CONTENT_FILTERED
        is LlmError.Internal -> ErrorCode.ERROR_CODE_INTERNAL
    }

    private fun generateChunkId(): String = "chunk_${UUID.randomUUID()}"
}

// Result types
sealed class LlmResult {
    data class Content(val content: String) : LlmResult()
    data class Error(val error: LlmError) : LlmResult()
}

// Error types
sealed class LlmError(override val message: String) : Exception(message) {
    class InvalidRequest(message: String) : LlmError(message)
    class ModelError(message: String) : LlmError(message)
    class RateLimited(message: String) : LlmError(message)
    class ContextLengthExceeded(message: String) : LlmError(message)
    class ContentFiltered(message: String) : LlmError(message)
    class Internal(message: String) : LlmError(message)
}

class LlmServiceException(message: String, cause: Throwable? = null) : Exception(message, cause)

// Example server setup
class LlmServer(
    private val port: Int,
    llmService: LlmService
) {
    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(LlmServiceImpl(llmService))
        .build()

    private val logger = LoggerFactory.getLogger(LlmServer::class.java)

    fun start() {
        server.start()
        logger.info("Server started on port $port")
        Runtime.getRuntime().addShutdownHook(Thread {
            logger.info("Shutting down gRPC server")
            stop()
        })
    }

    fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}


// Example usage
fun main() {
    val service = object : LlmService {
        override suspend fun process(request: ChatCompletionRequest): Flow<LlmResult> = flow {
            emit(LlmResult.Content("Processing request: ${request.prompt.content}"))
            delay(1000) // Simulate processing
            emit(LlmResult.Content("Here's your response..."))
        }
    }

    val server = LlmServer(50051, service)
    server.start()
    server.blockUntilShutdown()
}
