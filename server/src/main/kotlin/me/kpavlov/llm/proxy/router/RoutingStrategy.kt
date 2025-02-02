package me.kpavlov.llm.proxy.router

import java.util.concurrent.atomic.AtomicInteger

interface RoutingStrategy<T, R> {
    fun routeRequest(
        request: T,
        activeStrategies: List<R>,
    ): R
}

class RoundRobinRoutingStrategy<T, R> : RoutingStrategy<T, R> {
    private var counter: AtomicInteger = AtomicInteger(0)

    override fun routeRequest(
        request: T,
        activeStrategies: List<R>,
    ): R {
        val route = activeStrategies[counter.incrementAndGet() % activeStrategies.size]
        counter.compareAndSet(activeStrategies.size, 0)
        return route
    }
}
