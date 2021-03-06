package edu.unh.cs.ai.omab.domain

import edu.unh.cs.ai.omab.domain.Action.LEFT
import edu.unh.cs.ai.omab.domain.Action.RIGHT
import java.util.*

/**
 * @author Bence Cserna (bence@cserna.net)
 */

public data class BeliefState(val alphaLeft: Int, val betaLeft: Int, val alphaRight: Int, val betaRight: Int) {
    fun leftSum() = alphaLeft + betaLeft
    fun rightSum() = alphaRight + betaRight
    fun totalSum() = leftSum() + rightSum()
    fun leftMean() = alphaLeft.toDouble() / leftSum()
    fun rightMean() = alphaRight.toDouble() / rightSum()

    fun nextState(action: Action, success: Boolean): BeliefState {
        return when {
            LEFT == action && success -> BeliefState(alphaLeft + 1, betaLeft, alphaRight, betaRight)
            LEFT == action && !success -> BeliefState(alphaLeft, betaLeft + 1, alphaRight, betaRight)
            RIGHT == action && success -> BeliefState(alphaLeft, betaLeft, alphaRight + 1, betaRight)
            RIGHT == action && !success -> BeliefState(alphaLeft, betaLeft, alphaRight, betaRight + 1)
            else -> throw RuntimeException("Invalid state!")
        }
    }
}

enum class Action {
    LEFT, RIGHT
}

data class TransitionResult(val state: BeliefState, val reward: Int)

class MDP {
    private val states: Map<BeliefState, BeliefState> = HashMap()
    val startState = BeliefState(1, 1, 1, 1)
}

abstract class Simulator() {
    val random = Random()
    fun bernoulli(probability: Double): Boolean = random.nextDouble() <= probability
    abstract fun transition(state: BeliefState, action: Action): TransitionResult
}

class BanditSimulator() : Simulator() {
    override fun transition(state: BeliefState, action: Action): TransitionResult {
        return when (action) {
            LEFT -> {
                val success = bernoulli(state.leftMean())
                TransitionResult(state.nextState(LEFT, success), if (success) 1 else 0)
            }
            RIGHT -> {
                val success = bernoulli(state.rightMean())
                TransitionResult(state.nextState(RIGHT, success), if (success) 1 else 0)
            }
        }
    }
}

class BanditWorld(val leftProbability: Double, val rightProbability: Double) : Simulator() {
    override fun transition(state: BeliefState, action: Action): TransitionResult {
        return when (action) {
            LEFT -> {
                val success = bernoulli(leftProbability)
                TransitionResult(state.nextState(LEFT, success), if (success) 1 else 0)
            }
            RIGHT -> {
                val success = bernoulli(rightProbability)
                TransitionResult(state.nextState(RIGHT, success), if (success) 1 else 0)
            }
        }
    }
}

