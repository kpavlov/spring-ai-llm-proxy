package me.kpavlov.llm.proxy.server.grpc

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties

@AutoConfiguration
@EnableConfigurationProperties(LlmGrpcServerProperties::class)
open class LlmProxyGrpcServerAutoConfiguration
