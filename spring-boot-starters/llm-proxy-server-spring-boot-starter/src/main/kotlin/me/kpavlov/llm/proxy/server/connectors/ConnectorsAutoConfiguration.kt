// (C) Copyright 2025 Konstantin Pavlov. Licensed under BSD-3-Clause License.
package me.kpavlov.llm.proxy.server.connectors

import org.springframework.ai.chat.model.StreamingChatModel
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnClass(StreamingChatModel::class)
open class ConnectorsAutoConfiguration {
    private val logger = org.slf4j.LoggerFactory.getLogger(javaClass)
    private lateinit var connectors: List<StreamingChatModelConnector>

    @Bean
    open fun promptTemplateSource(): PromptTemplateSource = PromptTemplateSourceImpl

    @Bean
    open fun oneStreamingConnector(
        models: List<StreamingChatModel>,
        promptTemplateSource: PromptTemplateSource,
    ): StreamingChatModelConnector {
        connectors =
            models
                .map {
                    StreamingChatModelConnector(it, promptTemplateSource)
                }.toList()

        logger.info("Created connectors: {}", connectors)
        return connectors.first() // todo support more
    }
}
