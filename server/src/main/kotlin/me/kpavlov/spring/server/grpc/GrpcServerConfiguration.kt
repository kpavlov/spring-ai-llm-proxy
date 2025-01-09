// (C) Copyright 2025 Konstantin Pavlov. Licensed under BSD-3-Clause License.
package me.kpavlov.spring.server.grpc

import io.grpc.BindableService
import jakarta.annotation.PreDestroy
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener

@Configuration
@ConditionalOnClass(BindableService::class)
open class GrpcServerConfiguration {
    private lateinit var server: GrpcServer

    @Bean
    open fun grpcServer(
        @Value("\${server.grpc.port}") port: Int,
        bindableServices: BindableService,
    ): GrpcServer {
        server =
            GrpcServer(
                port = port,
                bindableService = bindableServices,
            )
        return server
    }

    @EventListener
    @Suppress("UnusedParameter")
    fun onApplicationStarted(readyEvent: ApplicationStartedEvent) {
        Thread.startVirtualThread {
            server.start()
            server.blockUntilShutdown()
        }
    }

    @PreDestroy
    fun preDestroy() {
        server.stop()
    }
}
