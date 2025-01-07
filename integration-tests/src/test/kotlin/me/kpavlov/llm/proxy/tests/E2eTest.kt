package me.kpavlov.llm.proxy.tests

import LlmClient
import kotlinx.coroutines.test.runTest
import me.kpavlov.llm.proxy.Application
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [Application::class])
class E2eTest {
    @Test
    fun `Send and receive messages`() =
        runTest {
            LlmClient.create("localhost", 50051).use { client ->
                client.chat("Tell me a joke").collect { response ->
                    when {
                        response.hasContent() -> {
                            print(response)
                            if (response.content.isFinal) println("\nFinal response received")
                        }
                        response.hasError() -> {
                            System.err.println("Error: ${response.error.message}")
                        }
                    }
                }
            }
        }
}
