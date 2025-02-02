// (C) Copyright 2025 Konstantin Pavlov. Licensed under BSD-3-Clause License.
package me.kpavlov.spring.server.grpc

import io.grpc.Attributes
import io.grpc.BindableService
import io.grpc.ServerServiceDefinition
import io.grpc.ServerTransportFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import javax.annotation.PreDestroy

@Configuration
@ConditionalOnClass(BindableService::class)
open class GrpcServerConfiguration {
    private lateinit var server: GrpcServer

    @Bean
    open fun grpcServer(
        @Value("\${server.grpc.port}") port: Int,
        services: List<ServerServiceDefinition>,
        bindableServices: List<BindableService>,
    ): GrpcServer {
        server =
            GrpcServer(
                port = port,
                services = services,
                bindableServices = bindableServices,
            ) {
                addTransportFilter(
                    object : ServerTransportFilter() {
                        override fun transportReady(transportAttrs: Attributes): Attributes {
                            println("Transport Ready. transportAttrs = $transportAttrs")
                            return super.transportReady(transportAttrs)
                        }

                        override fun transportTerminated(transportAttrs: Attributes) {
                            println("Transport Terminated. transportAttrs = $transportAttrs")
                            super.transportTerminated(transportAttrs)
                        }
                    },
                )
            }
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
