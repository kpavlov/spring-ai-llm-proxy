package me.kpavlov.llm.proxy.grpc.client

/**
 * Configuration data class used to define settings for establishing a connection in the LlmClient.
 *
 * @property hostname The hostname or IP address of the server to connect to.
 * @property port The port number of the server to connect to.
 * @property timeoutSeconds The timeout duration, in seconds, for inactive operations.
 * @property terminationTimeoutSeconds The timeout duration, in seconds, for ensuring graceful termination.
 * @property maxRetries The maximum number of retry attempts for failed requests.
 * @property useTls Indicates whether a secure TLS connection should be used. Default is `false`
 * @property closeOnError Determines whether the connection should close on encountering an error. Default is `true`
 */
data class LlmClientConfig(
    val hostname: String,
    val port: Int,
    val timeoutSeconds: Long = 30,
    val terminationTimeoutSeconds: Long = 5,
    val maxRetries: Int = 3,
    val useTls: Boolean = false,
    val closeOnError: Boolean = true,
)
