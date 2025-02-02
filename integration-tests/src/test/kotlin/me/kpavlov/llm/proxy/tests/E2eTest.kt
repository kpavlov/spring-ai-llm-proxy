// (C) Copyright 2025 Konstantin Pavlov. Licensed under BSD-3-Clause License.
package me.kpavlov.llm.proxy.tests

import assertk.assertThat
import assertk.assertions.contains
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.test.runTest
import me.kpavlov.llm.proxy.grpc.client.LlmClient
import me.kpavlov.llm.proxy.sample.client.SampleApplication
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [SampleApplication::class])
class E2eTest {
    val openaiMock = TestEnvironment.openaiMock

    @Autowired
    lateinit var client: LlmClient

    @AfterEach
    fun afterEach() {
        openaiMock.verifyNoUnmatchedRequests()
    }

    @Test
    fun `Send and receive messages`() =
        runTest {
            openaiMock.completion {
                requestBodyContains("Tell me a joke")
            } respondsStream {
                responseFlow =
                    "OpenAI's servers are as stable as their CEOs' employment status"
                        .split(" ")
                        .asFlow()
            }

            val result = StringBuffer()
            client.use { client ->
                client.chat("Tell me a joke").collect { response ->
                    when {
                        response.hasContent() -> {
                            print(response)
                            result.append(response.content.text)
                            if (response.content.isFinal) {
                                println("\nFinal response received")
                            }
                        }

                        response.hasError() -> {
                            System.err.println("Error: ${response.error.message}")
                        }
                    }
                }
            }
            println("Result: $result")
            assertThat(result).contains("Here's your response...")
        }
}
