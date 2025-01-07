package me.kpavlov.llm.proxy.server.grpc

import io.grpc.Server
import io.grpc.ServerBuilder
import org.slf4j.LoggerFactory

/**
 * Represents a gRPC server that manages the lifecycle of a gRPC service.
 *
 * The `GrpcServer` class provides methods to configure and manage the server instance for handling
 * gRPC requests. It allows starting, stopping, and awaiting termination of the server. The server
 * is configured to listen on a specified port and register a provided service implementation.
 *
 * The server integrates with the `LlmServiceImpl` implementation, which defines gRPC endpoints
 * used for communicating with an LLM processing service.
 *
 * @property port The port on which the server listens for incoming gRPC requests.
 * @constructor Creates a `GrpcServer` instance with the specified port and LLM service.
 */
class GrpcServer(
    private val port: Int,
    llmService: LlmService,
) {
    private val server: Server =
        ServerBuilder
            .forPort(port)
            .addService(LlmServiceGrpcImpl(llmService))
            .build()

    private val logger = LoggerFactory.getLogger(GrpcServer::class.java)

    fun start() {
        server.start()
        logger.info("GRPC Server started on port $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                logger.info("GRPC Shutting down gRPC server")
                stop()
            },
        )
    }

    fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}
