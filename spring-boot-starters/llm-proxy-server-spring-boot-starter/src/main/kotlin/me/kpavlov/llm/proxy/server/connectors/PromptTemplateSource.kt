// (C) Copyright 2025 Konstantin Pavlov. Licensed under BSD-3-Clause License.
package me.kpavlov.llm.proxy.server.connectors

import org.springframework.ai.chat.prompt.PromptTemplate

/**
 * Represents a source for retrieving or generating prompt templates based on a given request.
 * This interface is invoked with a [PromptTemplateRequest], and it returns a [PromptTemplate].
 *
 * The implementation of this interface determines how the prompt templates are created or fetched
 * based on the properties defined in the provided request.
 */
interface PromptTemplateSource : (PromptTemplateRequest) -> PromptTemplate
