package shmp.generator

import kotlin.random.Random


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
    throw IndexOutOfBoundsException("Empty array is passed in.")
}

fun <E> randomElementWithProbability(array: Array<E>, mapper: (E) -> Double, random: Random): E =
    randomElementWithProbability(array.toList(), mapper, random)

fun <E> randomElementWithProbability(iterable: Iterable<E>, mapper: (E) -> Double, random: Random): E =
    randomElementWithProbability(iterable.toList(), mapper, random)

fun <E> randomSublist(list: List<E>, random: Random, min: Int = 0, max: Int = list.size) =
    list.shuffled(random).subList(0, random.nextInt(min, max))

fun <E> randomElement(list: List<E>, random: Random) = list[random.nextInt(list.size)]

fun testProbability(probability: Double, random: Random) = random.nextDouble() <= probability