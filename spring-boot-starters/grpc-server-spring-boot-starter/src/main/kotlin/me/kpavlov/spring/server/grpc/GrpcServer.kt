// (C) Copyright 2025 Konstantin Pavlov. Licensed under BSD-3-Clause License.
package me.kpavlov.spring.server.grpc

import io.grpc.BindableService
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.ServerServiceDefinition
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
open class GrpcServer(
    private val port: Int,
    services: List<ServerServiceDefinition> = listOf(),
    bindableServices: List<BindableService> = listOf(),
) {
    private val logger = LoggerFactory.getLogger(GrpcServer::class.java)

    private val server: Server =
        ServerBuilder
            .forPort(port)
            .also { builder ->
                services.forEach {
                    logger.info("Registering ServerServiceDefinition: {}", it)
                    builder.addService(it)
                }
            }.also { builder ->
                bindableServices.forEach {
                    logger.info("Registering BindableService: {}", it)
                    builder.addService(it)
                }
            }.build()

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
