package me.cyber.nukleos


abstract class BasePresenter<V : BaseView>(open val view: V) {

    abstract fun create()

    abstract fun start()

    abstract fun destroy()

}