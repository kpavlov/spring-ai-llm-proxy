package me.kpavlov.project

import kotlinx.coroutines.coroutineScope

class Sample {
    /**
     * Returns a greeting message with the given name.
     *
     * @param name The name of the person to greet.
     * @return A greeting message.
     */
    fun sayHi(name: String): String = "Hi, $name"

    /**
     * Asynchronously obtains a response from the provided supplier function.
     *
     * @param supplier A lambda function that supplies a string.
     * @return The string response provided by the supplier.
     */
    suspend fun getResponseAsync(supplier: () -> String): String =
        coroutineScope { supplier.invoke() }
}
