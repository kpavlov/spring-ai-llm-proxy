package me.kpavlov.llm.proxy.server.grpc

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.ai.llm-proxy.server.grpc")
data class LlmGrpcServerProperties(
    val enabled: Boolean = true,
)
