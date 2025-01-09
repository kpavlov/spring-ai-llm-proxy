// (C) Copyright 2025 Konstantin Pavlov. Licensed under BSD-3-Clause License.
package me.kpavlov.llm.proxy.grpc.client

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(prefix = "spring.ai.llm-proxy.client")
data class LlmClientProperties(
    val enabled: Boolean = true,
    @NestedConfigurationProperty
    val options: LlmClientConfig,
)
