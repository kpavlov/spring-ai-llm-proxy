package me.kpavlov.llm.proxy.server.grpc

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener

@Configuration
@ConditionalOnBean(LlmService::class)
open class GrpcConfiguration {
    private lateinit var server: GrpcServer

    @Bean
    open fun grpcServer(
        @Value("\${llm.server.port:50051}") port: Int,
        llmService: LlmService,
    ): GrpcServer {
        server =
            GrpcServer(
                port = port,
                llmService = llmService,
            )
        return server
    }

    @EventListener
    fun onApplicationStarted(readyEvent: ApplicationStartedEvent) {
        server.start()
        server.blockUntilShutdown()
    }
}
