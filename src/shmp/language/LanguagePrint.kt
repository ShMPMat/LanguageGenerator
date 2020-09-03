package shmp.language

import shmp.generator.GeneratorException
import shmp.language.lexis.SemanticsCore
import shmp.language.lexis.Word
import shmp.language.syntax.Clause
import kotlin.math.max


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

fun getClauseAndInfoPrinted(clause: Clause): String {
    val (words, infos) = clause.words
        .map { it.toString() }
        .zip(getClauseInfoPrinted(clause).split(" "))
        .map { (s1, s2) -> lineUp(s1, s2) }
        .map { (s1, s2) -> s1 to s2 }
        .unzip()
    return words.joinToString(" ") + "\n" +
            infos.joinToString(" ")
}

fun lineUp(ss: List<String>): List<String> {
    val max = ss
        .map { it.length }
        .max()
        ?: throw GeneratorException("Cannot line up list of strings, because it's empty")
    return ss.map { it + " ".repeat(max - it.length) }
}

fun lineUp(vararg ss: String) = lineUp(ss.toList())

fun getClauseInfoPrinted(clause: Clause) = clause.words.joinToString(" ") { getWordInfoPrinted(it) }

fun getWordInfoPrinted(word: Word) = getSemanticsCorePrinted(word.semanticsCore) +
        word.categoryValues
            .joinToString("") { "-$it" }
            .replace(" ", "_")

private fun getSemanticsCorePrinted(semanticsCore: SemanticsCore) =
    if (semanticsCore.speechPart in listOf(SpeechPart.Particle, SpeechPart.Article)) ""
    else semanticsCore.toString()


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
