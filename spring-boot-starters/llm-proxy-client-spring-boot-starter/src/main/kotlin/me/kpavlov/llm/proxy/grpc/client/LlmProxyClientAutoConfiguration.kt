package me.kpavlov.llm.proxy.grpc.client

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@AutoConfiguration
@EnableConfigurationProperties(LlmClientProperties::class)
open class LlmProxyClientAutoConfiguration {
    @Bean
    open fun llmClient(properties: LlmClientProperties) = LlmClient.create(properties.options)
}
