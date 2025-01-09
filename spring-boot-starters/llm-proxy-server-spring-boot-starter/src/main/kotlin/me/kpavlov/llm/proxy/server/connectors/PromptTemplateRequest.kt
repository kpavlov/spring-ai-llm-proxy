// (C) Copyright 2025 Konstantin Pavlov. Licensed under BSD-3-Clause License.
package me.kpavlov.llm.proxy.server.connectors

/**
 * Represents a request for defining or transferring a prompt template's configuration.
 *
 * @constructor Creates an instance containing optional metadata and content for the prompt template.
 *
 * @property id Optional unique identifier for the prompt.
 * @property name Optional name or title of the prompt.
 * @property tags A map of categorical tags associated with the prompt template,
 *          where the keys and values define the tags.
 * @property labels Key-value pairs representing additional metadata about the prompt template.
 * @property template The content or body of the prompt template,
 *          used as the core structure for generating text or responses.
 */
data class PromptTemplateRequest(
    // Unique identifier for the prompt
    val id: String? = null,
    // Name/title of the prompt
    val name: String? = null,
    // List of categorical tags
    val tags: Map<String, String>? = null,
    // Key-value pairs for additional metadata
    val labels: Map<String, String>? = null,
    // The actual prompt template content
    val template: String? = null,
)
