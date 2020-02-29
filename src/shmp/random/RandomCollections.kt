package shmp.random

import kotlin.random.Random


fun <E> randomElement(collection: Collection<E>, random: Random) = collection.toList()[random.nextInt(collection.size)]

fun <E> randomElementWithProbability(collection: Collection<E>, mapper: (E) -> Double, random: Random): E {
    val list = collection.toList()
    val probabilities = list.map(mapper)
    var result = random.nextDouble() * probabilities.fold(0.toDouble(), Double::plus)
    for (i in probabilities.indices) {
        val probability = probabilities[i]
        if (result <= probability) {
            return list[i]
        }
        result -= probability
    }
    throw RandomException("Can't choose an element from an empty collection")
}

fun <E : SampleSpaceObject> randomElementWithProbability(array: Array<E>, random: Random): E =
    randomElementWithProbability(array, { it.probability }, random)

fun <E> randomElementWithProbability(array: Array<E>, mapper: (E) -> Double, random: Random): E =
    randomElementWithProbability(array.toList(), mapper, random)

fun <E> randomElementWithProbability(iterable: Iterable<E>, mapper: (E) -> Double, random: Random): E =
    randomElementWithProbability(iterable.toList(), mapper, random)

fun <E> randomSublist(list: List<E>, random: Random, min: Int = 0, max: Int = list.size) =
    list.shuffled(random).subList(0, random.nextInt(min, max))

fun <E> randomSublistWithProbability(
    array: Array<E>,
    mapper: (E) -> Double,
    random: Random,
    min: Int = 0,
    max: Int = array.map(mapper).count { it > 0.0 }
): List<E> =
    randomSublistWithProbability(array.toList(), mapper, random, min, max)

fun <E> randomSublistWithProbability(
    collection: Collection<E>,
    mapper: (E) -> Double,
    random: Random,
    min: Int = 0,
    max: Int = collection.map(mapper).count { it > 0.0 }
): List<E> {
    if (collection.isEmpty())
        throw RandomException("Can't choose an element from an empty collection")
    val resultList = ArrayList<E>()
    val size = random.nextInt(min, max)
    val list = collection.toList()
    val probabilities = list.map(mapper).toMutableList()
    val probabilitySum = probabilities.fold(0.toDouble(), Double::plus)
    var sum = random.nextDouble() * probabilitySum
    for (i in 1..size) {
        var currentSum = sum
        for (j in probabilities.indices) {
            val probability = probabilities[j]
            if (currentSum <= probability) {
                resultList.add(list[j])
                sum -= probabilities[j]
                probabilities[j] = 0.0
                break
            }
            currentSum -= probability
        }
    }
    return resultList
}
