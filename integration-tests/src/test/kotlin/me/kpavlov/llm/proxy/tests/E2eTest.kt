// (C) Copyright 2025 Konstantin Pavlov. Licensed under BSD-3-Clause License.
package me.kpavlov.llm.proxy.tests

import assertk.assertThat
import assertk.assertions.contains
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import me.kpavlov.llm.proxy.grpc.client.LlmClient
import me.kpavlov.llm.proxy.sample.client.SampleApplication
import org.awaitility.kotlin.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

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
    fun `Send and receive messages`() {
        val openAiResponse = "OpenAI's servers are as stable as their CEOs' employment status"
        runTest {
            openaiMock.completion {
                requestBodyContains("Tell me a joke")
            } respondsStream {
                responseFlow =
                    openAiResponse
                        .split(" ")
                        .map { "$it " }
                        .asFlow()
            }

            val result = StringBuffer()
            client.use {
                it
                    .chat("Tell me a joke")
                    .onEach { response ->
                        when {
                            response.hasContent() -> {
                                print("❇️ Response:$response")
                                result.append(response.content.text)
                                if (response.content.isFinal) {
                                    println("\nFinal response received")
                                }
                            }

                            response.hasError() -> {
                                System.err.println("Error: ${response.error.message}")
                            }
                        }
                    }.count()
            }

            await
                .pollInterval(1.seconds.toJavaDuration())
                .untilAsserted {
                    println("Result: $result")
                    assertThat(result).contains(openAiResponse)
                }
        }
    }
}
