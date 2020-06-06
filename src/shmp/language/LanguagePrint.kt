package shmp.language

import shmp.language.category.paradigm.ParametrizedCategoryValue


fun getParadigmPrinted(language: Language, word: Word): String {
    return "Base - $word\n" +
            listCartesianProduct(//TODO no hardcoded genders for nouns!
                language.sentenceChangeParadigm.wordChangeParadigm
                    .getSpeechPartParadigm(word.semanticsCore.speechPart).exponenceClusters
                    .flatMap { it.categories }
                    .map { it.actualParametrizedValues }
            ).map { language.sentenceChangeParadigm.wordChangeParadigm.apply(word, it) to it }
                .joinToString("\n") { "${it.first} - " + it.second.joinToString() }
}

private fun <T> listCartesianProduct(l: List<Collection<T>>): List<List<T>> {
    if (l.isEmpty()) return emptyList()
    var result = l[0].map { mutableListOf(it) }
    for (cl in l.drop(1)) {
        result = cartesianProduct(result, cl)
            .map { setOf(it.second).union(it.first).toMutableList() }
            .toMutableList()
    }
    return result
}

private fun <T, U> cartesianProduct(c1: Collection<T>, c2: Collection<U>): List<Pair<T, U>> {
    return c1.flatMap { lhsElem -> c2.map { rhsElem -> lhsElem to rhsElem } }
}