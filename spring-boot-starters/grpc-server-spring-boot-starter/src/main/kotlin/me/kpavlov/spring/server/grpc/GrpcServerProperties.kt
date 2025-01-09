// (C) Copyright 2025 Konstantin Pavlov. Licensed under BSD-3-Clause License.
package me.kpavlov.spring.server.grpc

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Configuration properties for the gRPC server in the application.
 *
 * These properties control the behavior of the gRPC server, such as whether it is enabled and
 * the address to which the server binds.
 *
 * @property enabled Indicates whether the gRPC server is enabled. Defaults to `true`.
 * @property address Specifies the address that the gRPC server binds to. Defaults to `localhost`.
 * @property port gRPC server port. Defaults to `50051`.
 */
@ConfigurationProperties(prefix = "server.grpc")
data class GrpcServerProperties(
    val enabled: Boolean = true,
    val address: String = "localhost",
    val port: Int = 50051,
)
