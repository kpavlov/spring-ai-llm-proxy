package me.kpavlov.llm.proxy.tests

import me.kpavlov.aimocks.openai.MockOpenai

object TestEnvironment {
    internal val openaiMock = MockOpenai(verbose = true)

    init {
        System.setProperty(
            "spring.ai.openai.base-url",
            "http://localhost:${openaiMock.port()}",
        )
    }
}
