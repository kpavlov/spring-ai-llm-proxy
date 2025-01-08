import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import me.kpavlov.llm.proxy.grpc.v1.ChatCompletionRequest
import me.kpavlov.llm.proxy.grpc.v1.ChatCompletionResponse
import me.kpavlov.llm.proxy.grpc.v1.LlmServiceGrpc
import me.kpavlov.llm.proxy.grpc.v1.Prompt
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.TimeUnit

private val logger = LoggerFactory.getLogger(LlmClient::class.java)

class LlmClient private constructor(
    private val channel: ManagedChannel,
    private val config: Config,
) : AutoCloseable {
    private val stub: LlmServiceGrpc.LlmServiceStub = LlmServiceGrpc.newStub(channel)

    data class Config(
        val host: String,
        val port: Int,
        val timeoutSeconds: Long = 30,
        val terminationTimeoutSeconds: Long = 5,
        val maxRetries: Int = 3,
        val useTls: Boolean = false,
    )

    companion object {
        @JvmStatic
        val DEFAULT_CONFIG: Config =
            Config(
                host = "localhost",
                port = 50051,
                timeoutSeconds = 30,
                maxRetries = 3,
                useTls = false,
            )

        fun create(
            host: String = "localhost",
            port: Int = 50051,
        ) = create(
            Config(host = host, port = port),
        )

        fun create(config: Config = DEFAULT_CONFIG): LlmClient {
            val channel =
                ManagedChannelBuilder
                    .forAddress(config.host, config.port)
                    .apply {
                        if (!config.useTls) usePlaintext()
                        enableRetry()
                        maxRetryAttempts(config.maxRetries)
                    }.build()
            return LlmClient(channel, config)
        }
    }

    fun chat(
        content: String,
        sessionId: String = UUID.randomUUID().toString(),
        parameters: Map<String, String> = emptyMap(),
        onError: (Throwable) -> Unit = { throw it },
    ): Flow<ChatCompletionResponse> =
        callbackFlow {
            var requestObserver: StreamObserver<ChatCompletionRequest>? = null

            @Suppress("TooGenericExceptionCaught")
            try {
                requestObserver =
                    stub
                        .withDeadlineAfter(config.timeoutSeconds, TimeUnit.SECONDS)
                        .chatCompletion(
                            object : StreamObserver<ChatCompletionResponse> {
                                override fun onNext(response: ChatCompletionResponse) {
                                    val result = trySend(response).isSuccess
                                    logger.info("Sent chat completion request: $result")
                                }

                                override fun onError(t: Throwable) {
                                    logger.error("Channel error", t)
                                    onError(t)
                                    close(t)
                                }

                                override fun onCompleted() {
                                    close()
                                    logger.info("Stream completed")
                                }
                            },
                        )

                val request = createChatRequest(content, sessionId, parameters)
                requestObserver.onNext(request)
            } catch (e: Exception) {
                requestObserver?.onError(e)
                onError(e)
                close(e)
            }

            awaitClose {
                requestObserver?.onCompleted()
            }
        }

    private fun createChatRequest(
        content: String,
        sessionId: String,
        parameters: Map<String, String>,
    ): ChatCompletionRequest =
        ChatCompletionRequest
            .newBuilder()
            .setRequestId(UUID.randomUUID().toString())
            .setSessionId(sessionId)
            .setPrompt(
                Prompt
                    .newBuilder()
                    .setId(UUID.randomUUID().toString())
                    .setContent(content)
                    .build(),
            ).putAllParameters(parameters)
            .build()

    override fun close() {
        channel.shutdown().awaitTermination(config.terminationTimeoutSeconds, TimeUnit.SECONDS)
    }
}

/*
// Example usage:
suspend fun main() {
    // Simple usage
    LlmClient.create("localhost", 50051).use { client ->
        client.chat("Tell me a joke").collect { response ->
            when {
                response.hasContent() -> {
                    print(response.content.text)
                    if (response.content.isFinal) println("\nFinal response received")
                }
                response.hasError() -> {
                    System.err.println("Error: ${response.error.message}")
                }
            }
        }
    }

    // Configurable usage
    val config =
        ConfigurableLlmClient.Config(
            host = "localhost",
            port = 50051,
            timeoutSeconds = 30,
            maxRetries = 3,
        )

    ConfigurableLlmClient.create(config).use { client ->
        client
            .chat(
                content = "What is the meaning of life?",
                parameters = mapOf("temperature" to "0.7"),
                onError = { println("Error occurred: ${it.message}") },
            ).collect { response ->
                when {
                    response.hasContent() -> {
                        print(response.content.text)
                        if (response.content.isFinal) println("\nFinal response received")
                    }
                    response.hasError() -> {
                        System.err.println("Error: ${response.error.message}")
                    }
                }
            }
    }
}
*/
