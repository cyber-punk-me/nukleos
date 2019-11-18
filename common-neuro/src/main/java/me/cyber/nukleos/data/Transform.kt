package me.cyber.nukleos.data

const val DEFAULT_DATA_READS_PER_FEATURE = 8

//each FloatArray is a set of features for a channel
typealias FeaturesArraysByChannel = List<FloatArray>

val defaultFeatures: Array<(List<Float>) -> Float> = arrayOf(
        { channel -> meanAbsoluteValue(channel) },
        { channel -> waveformLength(channel) },
        { channel -> zeroCrossing(channel) },
        { channel -> slopeSignChanges(channel) })

fun mapNeuralDefault(data: List<FloatArray>): List<FeaturesArraysByChannel> {
    return mapNeuralChunked(data,
            DEFAULT_DATA_READS_PER_FEATURE,
            defaultFeatures
    )
}

/**
 * @param data - list where each element is data reading. Each data reading has multiple channels (FloatArray)
 * @param dataReadsPerFeature - length of data to accumulate a feature
 * @return list where each element is a chunk of all channel features. Each chunk was formed from data of length dataReadsPerFeature
 */
private fun mapNeuralChunked(data: List<FloatArray>,
                             dataReadsPerFeature: Int,
                             features: Array<(List<Float>) -> Float>): List<FeaturesArraysByChannel> {
    val chunked = data.chunked(dataReadsPerFeature) { mapNeural(it, features) }
    return if (chunked.last().size < dataReadsPerFeature) {
        //drop incomplete chunk
        chunked.dropLast(1)
    } else {
        chunked
    }
}

/**
 * @param data - list where each element is data reading. Each data reading has multiple channels (FloatArray)
 * @return list where each element is all features for a channel
 */
private fun mapNeural(data: List<FloatArray>,
                      features: Array<(List<Float>) -> Float>): FeaturesArraysByChannel {
    val result = ArrayList<FloatArray>()
    for (i in data[0].indices) {
        val channelData: List<Float> = data.map { it[i] }
        val channelFeatures: FloatArray = features.map { it(channelData) }.toFloatArray()
        result.add(channelFeatures)
    }
    return result
}

fun meanAbsoluteValue(segment: List<Float>): Float {
    var sum = 0.0F
    for (element in segment) {
        sum += kotlin.math.abs(element)
    }
    return sum / segment.size
}

fun waveformLength(segment: List<Float>): Float {
    var wl = 0.0F
    for (i in 1 until segment.size) {
        wl += kotlin.math.abs(segment[i] - segment[i - 1])
    }
    return wl
}

fun zeroCrossing(segment: List<Float>): Float {
    var zc = 0.0F
    for (i in 0 until segment.size - 1) {
        if (segment[i] * segment[i + 1] < 0) {
            zc += 1
        }
    }
    return zc
}

fun slopeSignChanges(segment: List<Float>): Float {
    var ssc = 0.0F
    for (i in 1 until segment.size - 1) {
        if ((segment[i - 1] < segment[i] && segment[i] > segment[i + 1]) || (segment[i - 1] > segment[i] && segment[i] < segment[i + 1])) {
            ssc += 1
        }
    }
    return ssc
}
