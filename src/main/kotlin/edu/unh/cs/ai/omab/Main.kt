package main.kotlin.edu.unh.cs.ai.omab

import edu.unh.cs.ai.omab.algorithms.thompsonSampling
import edu.unh.cs.ai.omab.algorithms.upperConfidenceBounds
import edu.unh.cs.ai.omab.domain.BanditWorld
import edu.unh.cs.ai.omab.domain.MDP
import edu.unh.cs.ai.omab.domain.Simulator
import java.util.stream.DoubleStream
import kotlin.system.measureTimeMillis

/**
 * @author Bence Cserna (bence@cserna.net)
 */

fun main(args: Array<String>) {
    System.out.print("OMAB!")

    val horizon = 1000

    var evaluateAlgorithm = 0.0
    var executionTime: Long

    executionTime = measureTimeMillis {
        evaluateAlgorithm = evaluateAlgorithm(::upperConfidenceBounds, horizon)
    }
    println("UCB  reward: $evaluateAlgorithm executionTime:$executionTime[ms]")

    executionTime = measureTimeMillis {
        evaluateAlgorithm = evaluateAlgorithm(::thompsonSampling, horizon)
    }
    println("Thompson sampling reward: $evaluateAlgorithm executionTime:$executionTime[ms]")

}

private fun evaluateAlgorithm(algorithm: (MDP, Int, Simulator) -> Long, horizon: Int): Double {
    val averageReward = DoubleStream
            .iterate(0.0, { i -> i + 0.04 })
            .limit(25)
            .parallel()
            .map { p1 ->
                DoubleStream
                        .iterate(0.0, { i -> i + 0.04 })
                        .limit(25)
                        .mapToLong { p2 ->
                            algorithm(MDP(), horizon, BanditWorld(p1 / 100.0, p2 / 100.0))
                        }.average()
                        .orElseThrow { throw RuntimeException() }
            }
            .average()
            .orElseThrow { throw RuntimeException() }
    return averageReward
}