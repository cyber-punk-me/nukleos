package me.cyber.nukleos.api

import java.util.*

enum class RunState {
    NEW, RUNNING, COMPLETE, ERROR
}

data class Meta(val scriptId: UUID?, val dataId: UUID?, val modelId: UUID? = null,
                val state: RunState? = null, val startTime: Long? = null, val endTime: Long? = null)

data class Model(val dataId: UUID, val scriptId: UUID)

data class PredictRequest(val instances : List<List<Float>>)

data class PredictResponse(val predictions : List<Prediction>)

data class Prediction(val output: Int, val distr: List<Float>, val midLayer: List<Float>? = null)

