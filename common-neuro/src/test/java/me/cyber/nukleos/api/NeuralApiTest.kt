package me.cyber.nukleos.api

import io.reactivex.internal.schedulers.ExecutorScheduler
import org.junit.Test
import java.util.concurrent.Executors

class NeuralApiTest {

    private val api = RetrofitApi("http://localhost:8080", ExecutorScheduler(Executors.newSingleThreadExecutor()))

    @Test
    fun testGetTime() {
        println(api.getServerTime().blockingGet())
    }

}