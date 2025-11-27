package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.category.realization.CategoryApplicator
import io.tashtabash.lang.language.category.realization.WordCategoryApplicator
import io.tashtabash.lang.language.lexis.Lexis
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.morphem.change.ChangeException
import io.tashtabash.lang.language.phonology.*
import io.tashtabash.lang.language.syntax.ChangeParadigm


fun fixSyllableStructure(
    lexis: Lexis,
    changeParadigm: ChangeParadigm,
    maxLength: Int = 15
): Result<Pair<Lexis, ChangeParadigm>> {
    var fixedChangeParadigm = changeParadigm
    val fixedWords = lexis.words
        .map {
            val (word, newChangeParadigm) = fixSyllableStructure(it, fixedChangeParadigm, maxLength)
                .getOrElse { e -> return Result.failure(e) }

            fixedChangeParadigm = newChangeParadigm
            word
        }

    val fixedLexis = lexis.shift(fixedWords)

    return Result.success(unifySyllableStructure(fixedLexis, fixedChangeParadigm))
}

fun unifySyllableStructure(lexis: Lexis, changeParadigm: ChangeParadigm): Pair<Lexis, ChangeParadigm> {
    val changeParadigmWords = changeParadigm.wordChangeParadigm
        .speechPartChangeParadigms
        .flatMap { (_, paradigm) -> paradigm.sources }
        .flatMap { it.originalMap.values.filterIsInstance<WordCategoryApplicator>() }
        .map { it.word }

    var resultTemplate = lexis.words[0].syllableTemplate
    for (word in lexis.words.drop(1) + changeParadigmWords)
        resultTemplate = resultTemplate.merge(word.syllableTemplate)

    val newWords = lexis.words
        .map { resultTemplate.apply(it) }
    val newChangeParadigm = changeParadigm.mapApplicators {
            when (it) {
                is WordCategoryApplicator -> it.copy(resultTemplate.apply(it.word))
                else -> it
            }
        }

    return lexis.shift(newWords) to newChangeParadigm
}

fun fixSyllableStructure(
    word: Word,
    changeParadigm: ChangeParadigm,
    maxLength: Int
): Result<Pair<Word, ChangeParadigm>> {
    var currentChangeParadigm = changeParadigm

    for (template in SyllableStructureIterator(word.syllableTemplate, maxLength))
        try {
            val fixedWord = template.applyOrNull(word)
                ?: continue
            currentChangeParadigm = mergeSyllableTemplate(changeParadigm, template)
            currentChangeParadigm.wordChangeParadigm
                .getUniqueWordForms(fixedWord)

            return Result.success(fixedWord to currentChangeParadigm)
        } catch (e: ChangeException) {
        }

    return Result.failure(LanguageException("Can't create a syllable for all the changes of '$word'"))
}

// Changes syllable structure for words in case it became more complex
private fun mergeSyllableTemplate(changeParadigm: ChangeParadigm, syllableTemplate: SyllableTemplate): ChangeParadigm =
    changeParadigm.mapApplicators { mergeSyllableTemplate(it, syllableTemplate) }

fun mergeSyllableTemplate(
    applicator: CategoryApplicator,
    syllableTemplate: SyllableTemplate
): CategoryApplicator = when (applicator) {
    is WordCategoryApplicator -> syllableTemplate.applyOrNull(applicator.word)
        ?.let { applicator.copy(it) }
        ?: applicator
    else -> applicator
}

fun analyzeSyllableStructure(
    phonemes: List<Phoneme>,
    startTemplate: SyllableTemplate = SyllableValenceTemplate(ValencyPlace(PhonemeType.Vowel, 1.0)),
    maxLength: Int = 15
): Result<SyllableTemplate> {
    for (template in SyllableStructureIterator(startTemplate, maxLength))
        if (template.splitOnSyllables(phonemes) != null)
            return Result.success(template)

    return Result.failure(LanguageException("Can't create a syllable splitting '${phonemes.joinToString("")}'"))
}


private class SyllableStructureIterator(
    val startTemplate: SyllableTemplate,
    val maxLength: Int
) : Iterator<SyllableTemplate> {
    private var additionalPlacesNumber = 0
    private var initialSize = 0

    override fun hasNext() =
        additionalPlacesNumber <= maxLength - startTemplate.maxSize

    override fun next(): SyllableTemplate {
        if (!hasNext())
            throw LanguageException("Can't create the next syllable")

        val finalSize = additionalPlacesNumber - initialSize
        var currentTemplate = startTemplate

        for (j in 1..initialSize)
            currentTemplate = currentTemplate.addInitial(PhonemeType.Consonant)
        for (j in 1..finalSize)
            currentTemplate = currentTemplate.addFinal(PhonemeType.Consonant)

        if (initialSize == additionalPlacesNumber) {
            additionalPlacesNumber++
            initialSize = 0
        }
        initialSize++

        return currentTemplate
    }
}
