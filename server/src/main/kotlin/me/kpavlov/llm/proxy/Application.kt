package me.kpavlov.llm.proxy

import jakarta.annotation.PreDestroy
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.concurrent.CountDownLatch

private val latch = CountDownLatch(1)

@SpringBootApplication
open class Application {
    @PreDestroy
    fun onDestroy() {
        latch.countDown()
    }
}

fun main(args: Array<String>) {
    runApplication<Application>(args = args)
    latch.await()
}
