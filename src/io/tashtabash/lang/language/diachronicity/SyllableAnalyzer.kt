package io.tashtabash.lang.language.diachronicity

import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.lexis.Lexis
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.morphem.change.ChangeException
import io.tashtabash.lang.language.phonology.*
import io.tashtabash.lang.language.syntax.ChangeParadigm


fun fixSyllableStructure(lexis: Lexis, changeParadigm: ChangeParadigm, maxLength: Int = 15): Result<Lexis> {
    val fixedWords = lexis.words
        .map {
            fixSyllableStructure(it, changeParadigm, maxLength)
                .getOrElse { e -> return Result.failure(e) }
        }

    val fixedLexis = lexis.copy(words = fixedWords)

    return Result.success(unifySyllableStructure(fixedLexis))
}

fun unifySyllableStructure(lexis: Lexis): Lexis {
    var resultTemplate = lexis.words[0].syllableTemplate

    for (word in lexis.words.drop(1))
        resultTemplate = resultTemplate.merge(word.syllableTemplate)

    val newWords = lexis.words
        .map { it.copy(syllableTemplate = resultTemplate) }

    return lexis.copy(words = newWords)
}

fun fixSyllableStructure(word: Word, changeParadigm: ChangeParadigm, maxLength: Int = 15): Result<Word> {
    for (template in SyllableStructureIterator(word.syllableTemplate, maxLength))
        try {
            val fixedSyllables = template.splitOnSyllables(word.toPhonemes())
                ?: continue
            val fixedWord = word.copy(syllables = fixedSyllables, syllableTemplate = template)
            changeParadigm.wordChangeParadigm.getAllWordForms(fixedWord, true)

            return Result.success(fixedWord)
        } catch (e: ChangeException) {
        }

    return Result.failure(LanguageException("Can't create a syllable for all the changes of '$word'"))
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
