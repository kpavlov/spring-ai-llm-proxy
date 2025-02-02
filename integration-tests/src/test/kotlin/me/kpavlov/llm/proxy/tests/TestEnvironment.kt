package me.kpavlov.llm.proxy.tests

import me.kpavlov.aimocks.openai.MockOpenai

object TestEnvironment {
    internal val openaiMock = MockOpenai(verbose = true)
}
