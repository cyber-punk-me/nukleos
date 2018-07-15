package me.cyber.nukleos.api

import io.mironov.smuggler.AutoParcelable

data class Data(val arrayList: ArrayList<Int> = arrayListOf()): AutoParcelable