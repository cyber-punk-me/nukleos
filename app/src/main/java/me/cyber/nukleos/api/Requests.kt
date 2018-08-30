package me.cyber.nukleos.api

import io.mironov.smuggler.AutoParcelable
import me.cyber.nukleos.api.Data
// send Data
data class DataRequest(val data: Data) : AutoParcelable
