package me.cyber.nukleos.control

import java.util.*

class TryControl {

    var buffer = LinkedList<Int>()

    fun guess(aGuess: Int): Int {
        buffer.addLast(aGuess)
        if (buffer.size > BUFFER_SIZE) {
            buffer.removeFirst()
        }
        if (buffer.size < BUFFER_SIZE) {
            return -1
        }
        return when {
            buffer.containsAtLeast(aGuess, THRESHOLD) -> aGuess
            else -> -1
        }
    }

    companion object {
        val COMMAND_SET = 4
        val BUFFER_SIZE = 6
        val THRESHOLD = 5
    }

    private fun <E> List<E>.containsAtLeast(aGuess: E, atLeast: Int): Boolean = fold(0) { acc, e ->
        if (e == aGuess) {
            acc + 1
        } else {
            acc
        }
    } > atLeast
}


val TOTAL = 100
val CORRECT_CHANCE = 90
val SAME_COMMAND_CHANCE = 90
val random = Random()

fun randomizeCommand(initial: Int, keepChance : Int): Int {
    val keep = random.nextInt(TOTAL) <= keepChance
    if (keep) {
        return initial
    } else {
        var different = random.nextInt(TryControl.COMMAND_SET)
        while (different == initial) {
            different = random.nextInt(TryControl.COMMAND_SET)
        }
        return different
    }
}

fun emitCommand(prev : Int) : Int {
    return randomizeCommand(prev, SAME_COMMAND_CHANCE)
}

fun main(args: Array<String>) {
    val experiments = 10000
    val control = TryControl()
    var correctControls = 0
    var incorrectControls = 0
    var desiredCommand = 0
    (1..experiments).forEach {
        desiredCommand = emitCommand(desiredCommand)
        val command = randomizeCommand(desiredCommand, CORRECT_CHANCE)
        val guess = control.guess(command)
        if (guess > -1) {
            if (desiredCommand == guess) {
                correctControls++
            } else {
                incorrectControls++
            }
        }
    }

    println("From $experiments experiments $correctControls were correct," +
            " $incorrectControls were incorrect and ${experiments - correctControls - incorrectControls} were dismissed.")
}