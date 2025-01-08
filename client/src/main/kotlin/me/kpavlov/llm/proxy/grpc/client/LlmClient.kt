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

/**
 * A client for interacting with a Language Learning Model (LLM) server through gRPC.
 *
 * @constructor This class cannot be directly instantiated. Use the `create` factory methods
 * to obtain an instance of this client.
 * @param channel Managed gRPC channel used for communication with the LLM server.
 * @param config Configuration object specifying connection parameters, timeout settings, and more.
 *
 * Example usage:
 * ```kotlin
 * val config =
 *         ConfigurableLlmClient.Config(
 *             host = "localhost",
 *             port = 50051,
 *             timeoutSeconds = 30,
 *             maxRetries = 3,
 *         )
 *
 *     ConfigurableLlmClient.create(config).use { client ->
 *         client
 *             .chat(
 *                 content = "What is the meaning of life?",
 *                 parameters = mapOf("temperature" to "0.7"),
 *                 onError = { println("Error occurred: ${it.message}") },
 *             ).collect { response ->
 *                 when {
 *                     response.hasContent() -> {
 *                         print(response.content.text)
 *                         if (response.content.isFinal) println("\nFinal response received")
 *                     }
 *                     response.hasError() -> {
 *                         System.err.println("Error: ${response.error.message}")
 *                     }
 *                 }
 *             }
 *     }
 *     ```
 */
class LlmClient private constructor(
    private val channel: ManagedChannel,
    private val config: Config,
) : AutoCloseable {
    private val stub: LlmServiceGrpc.LlmServiceStub = LlmServiceGrpc.newStub(channel)

    /**
     * Configuration data class used to define settings for establishing a connection in the LlmClient.
     *
     * @property host The hostname or IP address of the server to connect to.
     * @property port The port number of the server to connect to.
     * @property timeoutSeconds The timeout duration, in seconds, for inactive operations.
     * @property terminationTimeoutSeconds The timeout duration, in seconds, for ensuring graceful termination.
     * @property maxRetries The maximum number of retry attempts for failed requests.
     * @property useTls Indicates whether a secure TLS connection should be used. Default is `false`
     * @property closeOnError Determines whether the connection should close on encountering an error. Default is `true`
     */
    data class Config(
        val host: String,
        val port: Int,
        val timeoutSeconds: Long = 30,
        val terminationTimeoutSeconds: Long = 5,
        val maxRetries: Int = 3,
        val useTls: Boolean = false,
        val closeOnError: Boolean = true,
    )

    companion object {
        @JvmStatic
        val DEFAULT_CONFIG: Config =
            Config(
                host = "localhost",
                port = 50051,
            )

        /**
         * Creates and returns a new instance of the LlmClient with default configuration
         * based on the provided host and port.
         *
         * This method uses the supplied host and port parameters to construct a `Config` object and
         * subsequently initializes an instance of `LlmClient` through the primary create function.
         *
         * @param host The hostname or IP address of the server to connect to. Defaults to "localhost".
         * @param port The port number of the server to connect to. Defaults to 50051.
         * @return A new instance of the LlmClient configured with the specified host and port.
         * @see DEFAULT_CONFIG
         */
        fun create(
            host: String = "localhost",
            port: Int = 50051,
        ) = create(
            Config(host = host, port = port),
        )

        /**
         * Creates and returns a new instance of the LlmClient based on the provided configuration.
         * Establishes a connection to the specified server using gRPC with options like TLS and retry settings.
         *
         * @param config Configuration object containing the server connection details such as host, port,
         *               TLS usage, and retry settings. Defaults to `DEFAULT_CONFIG` if not provided.
         * @return A new instance of the LlmClient configured with the specified connection settings.
         */
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
                                    if (config.closeOnError) {
                                        close(t)
                                    }
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
