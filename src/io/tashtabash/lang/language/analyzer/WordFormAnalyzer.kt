package io.tashtabash.lang.language.analyzer

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValue
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.syntax.sequence.WordSequence


// Returns all Word forms which are identical for different words.
//  If a word has two identical forms for different Cases/Tenses/etc,
//  those are not returned.
fun getIdenticalWordForms(language: Language): List<List<Pair<WordSequence, SourcedCategoryValues>>> =
    getUniqueWordForms(language)
        .groupBy { it.first.toString() }
        .filter { it.value.size > 1 }
        .entries
        .map { (_, sequences) -> sequences }

// Returns the fraction of identical word forms
fun getIdenticalWordFormFraction(language: Language): HomophoneStats {
    val uniqueWordForms = getUniqueWordForms(language)
    val homophoneFormsCount = uniqueWordForms.groupBy { it.first.toString() }
        .filter { it.value.size > 1 }
        .entries
        .sumOf { it.value.size }

    return HomophoneStats(uniqueWordForms.size, homophoneFormsCount)
}

private fun getUniqueWordForms(language: Language): List<Pair<WordSequence, List<SourcedCategoryValue>>> =
    language.lexis.words
        .flatMap { word ->
            language.changeParadigm.wordChangeParadigm
                .getAllWordForms(word, false)
                .distinctBy { it.first.toString() }
        }

data class HomophoneStats(val allWordFormsCount: Int, val homophoneFormsCount: Int) {
    val homophoneFraction = homophoneFormsCount.toDouble() / allWordFormsCount
}
