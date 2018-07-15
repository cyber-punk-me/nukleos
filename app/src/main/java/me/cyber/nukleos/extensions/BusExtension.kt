package me.cyber.nukleos.extensions

import org.greenrobot.eventbus.EventBus

interface IEvent
interface IBussed

fun IEvent.dispatch() = EventBus.getDefault().post(this)

/*
 * about sticky read here
 * http://greenrobot.org/eventbus/documentation/configuration/sticky-events/
 */
fun IEvent.dispatchSticky() = EventBus.getDefault().postSticky(this)

fun IBussed.connectBus() = EventBus.getDefault().register(this)
fun IBussed.disconnectBus() = EventBus.getDefault().unregister(this)


//todo add base models
//fun BaseViewModel.connectBus() = org.greenrobot.eventbus.EventBus.getDefault().register(this)
//fun BaseActivity.connectBus() = org.greenrobot.eventbus.EventBus.getDefault().register(this)
//fun BaseViewModel.disconnectBus() = org.greenrobot.eventbus.EventBus.getDefault().unregister(this)
//fun BaseActivity.disconnectBus() = org.greenrobot.eventbus.EventBus.getDefault().unregister(this)
