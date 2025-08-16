package kr.pe.freesearch.jhannanum.comm

import kotlin.random.Random

class HiddenMarkovModel(val numberOfStates: Int, val numberOfObservations: Int) {

    var A: TransitionProbabilities = TransitionProbabilities(numberOfStates)
    var B: ObservationProbabilities = ObservationProbabilities(numberOfStates, numberOfObservations)
    var Pi: InitialStateDistribution = InitialStateDistribution(numberOfStates)

    /* Randomly generates a tuple of (observations, states) */
    operator fun invoke(numTransitions: Int, startingState: Int? = null, random: Random): Pair<List<Int>, List<Int>> {
        val observations = mutableListOf<Int>()
        val states = mutableListOf<Int>()

        var state = startingState ?: Pi.getInitialState(random.nextDouble())
        states.add(state)
        observations.add(B.getObservation(state, random.nextDouble()))
        for (i in 1 until numTransitions) {
            state = A.getNextState(state, random.nextDouble())
            states.add(state)
            observations.add(B.getObservation(state, random.nextDouble()))
        }

        return Pair(observations, states)
    }

    fun normalize() {
        A.normalize()
        B.normalize()
        Pi.normalize()
    }

    fun prettyPrint(w: java.io.PrintStream) {
        for (row in 0 until numberOfStates) {
            if (row == 0) {
                w.print("A: [ ")
            } else {
                w.print("     ")
            }
            w.print(A.data[row].joinToString(prefix = "[", separator = " ", postfix = "]"))
            if (row == numberOfStates - 1) {
                w.print(" ]")
            }
            w.println()
        }
        for (row in 0 until numberOfStates) {
            if (row == 0) {
                w.print("B: [ ")
            } else {
                w.print("     ")
            }
            w.print(B.data[row].joinToString(prefix = "[", separator = " ", postfix = "]"))
            if (row == numberOfStates - 1) {
                w.print(" ]")
            }
            w.println()
        }
        w.print("Pi:  ")
        w.print(Pi.weights.joinToString(prefix = "[", separator = " ", postfix = "]"))
        w.println()
    }
}

/**
 * A = \{ a_{ij} \} State transition probability matrix.
 * a_{ij} = P(q_{t+1} = S_j | q_t = S_i), ie the probability of transitioning from state i to state j
 */
class TransitionProbabilities(val numberOfStates: Int) {
    internal val data = Array(numberOfStates) { DoubleArray(numberOfStates) }

    operator fun get(i: Int, j: Int): Double = data[i][j]
    operator fun set(i: Int, j: Int, value: Double) {
        data[i][j] = value
    }

    fun normalize() {
        val sums = data.map { it.sum() }

        val zeroIndex = sums.indexOf(0.0)
        if (zeroIndex >= 0) {
            throw IllegalStateException("Cannot normalize zero transition probabilities for index $zeroIndex")
        }

        for (i in 0 until numberOfStates) {
            for (j in 0 until numberOfStates) {
                data[i][j] /= sums[i]
            }
        }
    }

    fun getNextState(currentState: Int, random: Double): Int {
        if (random < 0.0 || random > 1.0) {
            throw IllegalArgumentException("Must supply a random double between 0.0 and 1.0 inclusive.")
        }
        val transitionDistribution = data[currentState]
        val weightedRandom = random * transitionDistribution.sum()

        var accumulated = 0.0
        var nextState = 0
        while (accumulated < weightedRandom) {
            accumulated += transitionDistribution[nextState]
            nextState += 1
        }
        return nextState - 1
    }
}

/**
 * B = \{ b_j(k) \} observation probabilities.
 * b_j(k) = P(v_k at t | q_t = S_j), ie the probabily of seeing observation k at state j
 */
class ObservationProbabilities(val numberOfStates: Int, val numberOfObservations: Int) {
    internal val data = Array(numberOfStates) { DoubleArray(numberOfObservations) }

    operator fun get(stateIndex: Int, observationIndex: Int): Double = data[stateIndex][observationIndex]
    operator fun set(stateIndex: Int, observationIndex: Int, value: Double) {
        data[stateIndex][observationIndex] = value
    }

    fun normalize() {
        val sums = data.map { it.sum() }

        val zeroIndex = sums.indexOf(0.0)
        if (zeroIndex >= 0) {
            throw IllegalStateException("Cannot normalize zero observation probabilities for index $zeroIndex")
        }

        for (i in 0 until numberOfStates) {
            for (j in 0 until numberOfObservations) {
                data[i][j] /= sums[i]
            }
        }
    }

    fun getObservation(currentState: Int, random: Double): Int {
        if (random < 0.0 || random > 1.0) {
            throw IllegalArgumentException("Must supply a random double between 0.0 and 1.0 inclusive.")
        }
        val observationDistribution = data[currentState]
        val weightedRandom = random * observationDistribution.sum()

        var accumulated = 0.0
        var observation = 0
        while (accumulated < weightedRandom) {
            accumulated += observationDistribution[observation]
            observation++
        }
        return observation - 1
    }
}

class InitialStateDistribution(val numberOfStates: Int) {
    internal val weights = DoubleArray(numberOfStates) { 0.0 }

    operator fun get(i: Int): Double = weights[i]
    operator fun set(i: Int, value: Double) {
        weights[i] = value
    }

    fun normalize() {
        val sum = weights.sum()

        if (sum == 0.0) {
            throw IllegalStateException("Cannot normalize with zero total weights.")
        }
        for (i in 0 until numberOfStates) {
            weights[i] /= sum
        }
    }

    fun getInitialState(random: Double): Int {
        if (random < 0.0 || random > 1.0) {
            throw IllegalArgumentException("Must supply a random double between 0.0 and 1.0 inclusive.")
        }
        val weightedRandom = random * weights.sum()

        var accumulated = 0.0
        var state = 0
        while (accumulated < weightedRandom) {
            accumulated += weights[state]
            state++
        }
        return state - 1
    }
}

class ViterbiAlgorithm(private val model: HiddenMarkovModel) {
    private val cache = hashMapOf<Pair<Int, Int>, Pair<Double, List<Int>>>()

    /** Computes (delta_t(i), psi_t(i)) */
    operator fun invoke(observations: List<Int>, t: Int, i: Int): Pair<Double, List<Int>> {
        val cmp = Comparator<Pair<Double, List<Int>>> { x, y ->
            x.first.compareTo(y.first)
        }

        return if (Pair(t, i) in cache) {
            cache[Pair(t, i)]!!
        } else {
            val result: Pair<Double, List<Int>> = if (t == 1) {
                Pair(model.Pi[i] * model.B[i, observations[0]], listOf(i))
            } else {
                (0 until model.numberOfStates).map { j ->
                    val previousResult = invoke(observations, t - 1, j)
                    // (이전 경로의 확률) * (상태 j에서 i로의 전이 확률) * (상태 i에서 현재 관측값이 나올 확률)
                    val probability = previousResult.first * model.A[j, i] * model.B[i, observations[t - 1]]
                    // 이전 최적 경로에 현재 상태 i를 추가
                    val path = previousResult.second + j
                    Pair(probability, path)
                }.maxWithOrNull(cmp)!!
            }
            cache[Pair(t, i)] = result
            return result
        }
    }
}