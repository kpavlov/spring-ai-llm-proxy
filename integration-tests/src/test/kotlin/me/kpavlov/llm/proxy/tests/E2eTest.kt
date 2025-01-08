package me.kpavlov.llm.proxy.tests

import kotlinx.coroutines.test.runTest
import me.kpavlov.llm.proxy.Application
import me.kpavlov.llm.proxy.grpc.client.LlmClient
import me.kpavlov.llm.proxy.sample.client.SampleApplication
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [Application::class, SampleApplication::class])
class E2eTest {
    @Autowired
    lateinit var client: LlmClient

    @Test
    fun `Send and receive messages`() =
        runTest {
            client.use { client ->
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
