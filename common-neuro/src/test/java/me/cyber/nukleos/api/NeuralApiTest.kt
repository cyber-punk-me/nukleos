package me.cyber.nukleos.api

import io.reactivex.internal.schedulers.ExecutorScheduler
import me.cyber.nukleos.data.mapNeuralDefault
import me.cyber.nukleos.sensors.SensorDataFeeder
import me.cyber.nukleos.sensors.SensorListener
import org.junit.Test
import java.lang.StringBuilder
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Executors

class NeuralApiTest {

    private val api = RetrofitApi("http://localhost:8080", ExecutorScheduler(Executors.newSingleThreadExecutor()))

    private val dataDir = "src/test/data"

    private val sensorsInEachRead = 8
    private val readsPerLine = 8


    private val trainDataUUID = UUID.fromString("f878049f-5ef3-4f59-a301-1359c36a355c")

    private val dataFiles: List<List<String>> = (0..4).map {
        val dataFiles = Paths.get(dataDir, "$it.csv").toAbsolutePath()
        Files.readAllLines(dataFiles)
    }

    private val testFiles: List<List<String>> = (0..4).map {
        val dataFiles = Paths.get(dataDir, "$it-1.csv").toAbsolutePath()
        Files.readAllLines(dataFiles)
    }

    /**
     * @param data - list of all reads. each read is an array from all sensors
     * @param dataClass - movement class
     * @param readsPerLine - how many reads to put on each line
     */
    private fun convertData(data: List<FloatArray>, dataClass: Int): String {
        val builder = StringBuilder()

        val start = System.currentTimeMillis()
        val grouped = mapNeuralDefault(data)
        println("Convert worked for ${System.currentTimeMillis() - start}")

        grouped.forEach { timeGroup ->
            timeGroup.forEach{ sensorFeatures ->
                sensorFeatures.forEach {
                    builder.append("$it,")
                }
            }
            builder.append("$dataClass\n")
        }

        val result = builder.toString()
        return result
    }

    @Test
    fun testGetTime() {
        println(api.getServerTime().blockingGet())
    }

    @Test
    fun testTrain() {

        dataFiles.forEach { fileLines: List<String> ->
            val dataClass = Integer.parseInt(fileLines.first().split(",").last())
            val feeder = SensorDataFeeder()
            feeder.listenOnce(object : SensorListener {
                override fun onSensorData(sensorName: String, data: List<FloatArray>) {
                    api.postData(trainDataUUID, convertData(data, dataClass), "csv").subscribe({
                        println(it)
                    }, {
                        println(it)
                    })
                }
            }, 235 * readsPerLine) //read 235 lines

            fileLines.map {
                it.split(",").map { s -> s.toFloat() }.dropLast(1).toFloatArray()
            }.forEach { line: FloatArray ->
                for (iStart in 0 until line.size step sensorsInEachRead) {
                    val reading = line.slice(iStart until iStart + sensorsInEachRead).toFloatArray()
                    feeder.onData("", listOf(reading))
                }
            }
            Thread.sleep(500)

        }

        Thread.sleep(5000)

    }


}