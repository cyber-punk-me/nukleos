package me.cyber.nukleos.control

class ControlManager {

    val tryControl = TryControl()

    val controlListeners : HashMap<String, ControlListener> = HashMap()

    var lastUpdatedMotion = DATA_NOOP

    fun reset() {
        lastUpdatedMotion = DATA_NOOP
    }

    fun notifyDataArrived(dataClass: Int) {
        val guess = tryControl.guess(dataClass)
        if (guess != DATA_NOOP && lastUpdatedMotion != guess) {
            lastUpdatedMotion = guess
            notifyControlListeners()
        }
    }

    fun addControlListener(listenerName: String, listener: ControlListener) {
        synchronized(controlListeners) {
            controlListeners[listenerName] = listener
        }
    }

    fun removeControlListener(listenerName: String) {
        synchronized(controlListeners) {
            controlListeners.remove(listenerName)
        }
    }

    private fun notifyControlListeners() {
        val notifyUs = HashMap<String, ControlListener>()
        synchronized(controlListeners) {
            notifyUs.putAll(controlListeners)
        }
        notifyUs.forEach{ (_, l) -> l.onMotionUpdated(lastUpdatedMotion)}
    }

    interface ControlListener {
        fun onMotionUpdated(dataClass: Int)
    }

    companion object {
        val DATA_NOOP = -1
    }
}

