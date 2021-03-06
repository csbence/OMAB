package edu.unh.cs.ai.omab.algorithms

import edu.unh.cs.ai.omab.domain.Action.LEFT
import edu.unh.cs.ai.omab.domain.Action.RIGHT
import edu.unh.cs.ai.omab.domain.MDP
import edu.unh.cs.ai.omab.domain.Simulator
import org.apache.commons.math3.distribution.BetaDistribution
import java.util.*
import java.util.stream.IntStream

/**
 * @author Bence Cserna (bence@cserna.net)
 */
fun thompsonSampling(mdp: MDP, horizon: Int, simulator: Simulator): Long {
    val random = Random()
    var currentState = mdp.startState

    return IntStream.iterate(0, {i -> i + 1}).limit(horizon.toLong()).mapToLong {
        val leftBetaDistribution = BetaDistribution(currentState.alphaLeft.toDouble(), currentState.betaLeft.toDouble())
        val rightBetaDistribution = BetaDistribution(currentState.alphaRight.toDouble(), currentState.betaRight.toDouble())

        val leftSample = leftBetaDistribution.inverseCumulativeProbability(random.nextDouble())
        val rightSample = rightBetaDistribution.inverseCumulativeProbability(random.nextDouble())

        val (nextState, reward) = if (leftSample > rightSample) {
            simulator.transition(currentState, LEFT)
        } else {
            simulator.transition(currentState, RIGHT)
        }

        currentState = nextState

        return@mapToLong reward.toLong()
    }.sum()
}