package me.kpavlov.llm.proxy.router

import java.util.concurrent.atomic.AtomicInteger

interface RoutingStrategy<T, R> {
    fun routeRequest(
        request: T,
        activeStrategies: List<R>,
    ): R?
}

class RoundRobinRoutingStrategy<T, R> : RoutingStrategy<T, R> {
    private val log = org.slf4j.LoggerFactory.getLogger(RoundRobinRoutingStrategy::class.java)
    private var counter: AtomicInteger = AtomicInteger(0)

    override fun routeRequest(
        request: T,
        activeStrategies: List<R>,
    ): R? {
        if (activeStrategies.isEmpty()) {
            log.debug("No active strategies provided, returning empty route")
            return null
        }
        val route = activeStrategies[counter.incrementAndGet() % activeStrategies.size]
        counter.compareAndSet(activeStrategies.size, 0)
        log.debug("Routing request {}} to {}", request, route)
        return route
    }
}
