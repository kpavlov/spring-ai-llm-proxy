// (C) Copyright 2025 Konstantin Pavlov. Licensed under BSD-3-Clause License.
package me.kpavlov.llm.proxy.server.connectors

import org.springframework.ai.chat.model.StreamingChatModel
import org.springframework.core.style.ToStringCreator

open class StreamingChatModelConnector(
    protected val streamingChatModel: StreamingChatModel,
    protected val promptTemplateSource: PromptTemplateSource,
) {
    override fun toString(): String =
        ToStringCreator(this)
            .append("streamingChatModel", streamingChatModel)
            .append("promptTemplateSource", promptTemplateSource)
            .toString()
}
