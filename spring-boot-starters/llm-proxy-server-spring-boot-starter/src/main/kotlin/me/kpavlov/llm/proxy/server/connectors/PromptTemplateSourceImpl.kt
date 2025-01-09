// (C) Copyright 2025 Konstantin Pavlov. Licensed under BSD-3-Clause License.
package me.kpavlov.llm.proxy.server.connectors

import org.springframework.ai.chat.prompt.PromptTemplate

object PromptTemplateSourceImpl : PromptTemplateSource {
    override fun invoke(request: PromptTemplateRequest): PromptTemplate =
        request.template?.let { PromptTemplate(it) } ?: TODO("Not supported yet implemented")
}
