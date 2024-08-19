package io.tashtabash.lang.language.morphem.change

import io.tashtabash.lang.language.LanguageException
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.derivation.DerivationClass
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.morphem.change.substitution.ExactPhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.PhonemeSubstitution
import io.tashtabash.lang.language.phonology.Phoneme
import io.tashtabash.lang.language.phonology.PhonemeSequence
import io.tashtabash.lang.language.phonology.matcher.PhonemeMatcher


data class TemplateSingleChange(
    override val position: Position,
    val phonemeMatchers: List<PhonemeMatcher>,
    val matchedPhonemesSubstitution: List<PhonemeSubstitution>,
    val affix: List<ExactPhonemeSubstitution>
) : TemplateChange() {
    //TODO make an interface
    fun findGoodIndex(word: Word): Int? {
        return when (position) {
            Position.Beginning ->
                if (testFromPosition(word))
                    phonemeMatchers.size
                else null
            Position.End -> {
                val sublistStart = word.size - phonemeMatchers.size
                if (testFromPosition(word)) sublistStart else null
            }
        }
    }

    override fun test(word: Word) = findGoodIndex(word) != null

    private fun getTestedPhonemes(word: Word) = when (position) {
        Position.Beginning -> word.toPhonemes().take(phonemeMatchers.size)
        Position.End -> word.toPhonemes().takeLast(phonemeMatchers.size)
    }

    private fun testFromPosition(word: Word) = phonemeMatchers
        .zip(getTestedPhonemes(word))
        .all { (matcher, phoneme) -> matcher.match(phoneme) }

    fun getFullChange() = when (position) {
        Position.Beginning -> affix + matchedPhonemesSubstitution
        Position.End -> matchedPhonemesSubstitution + affix
    }

    override fun change(word: Word, categoryValues: SourcedCategoryValues, derivationValues: List<DerivationClass>): Word {
        fun Word.takeProsody(i: Int) = this.syllables
            .getOrNull(i)
            ?.prosodicEnums
            ?.toList()
            ?: listOf()

        val testResult = findGoodIndex(word)
        if (testResult != null) {
            val newMorpheme = MorphemeData(affix.size, categoryValues, false, derivationValues)
            val (prosodicSyllables, morphemes) = when (position) {
                Position.End -> {
                    val change: List<Phoneme?> = getFullChange()
                        .zip(testResult until testResult + matchedPhonemesSubstitution.size + affix.size)
                        .map { (substitution, i) -> substitution.substitute(word.getOrNull(i)) }
                    val noProsodyWord = word.syllableTemplate.splitOnSyllables(
                        PhonemeSequence(
                            word.toPhonemes().subList(
                                0,
                                word.size - phonemeMatchers.size
                            ) + change.filterNotNull()
                        )
                    ) ?: throw LanguageException("Couldn't convert $word with change $this to word")
                    val morphemes = word.morphemes + listOf(newMorpheme)

                    noProsodyWord.mapIndexed { i, s ->
                        s.copy(prosodicEnums = word.takeProsody(i))
                    } to morphemes
                }
                Position.Beginning -> {
                    val change: List<Phoneme?> = getFullChange()
                        .zip(testResult - matchedPhonemesSubstitution.size - affix.size until testResult)
                        .map { (substitution, i) -> substitution.substitute(word.getOrNull(i)) }
                    val noProsodyWord = word.syllableTemplate.splitOnSyllables(
                        PhonemeSequence(
                            change.filterNotNull() + word.toPhonemes().subList(
                                phonemeMatchers.size,
                                word.size
                            )
                        )
                    ) ?: throw LanguageException("Couldn't convert $word with change $this to word")
                    val shift = noProsodyWord.size - word.syllables.size
                    val morphemes = listOf(newMorpheme) + word.morphemes

                    noProsodyWord.mapIndexed { i, s ->
                        s.copy(prosodicEnums = word.takeProsody(i - shift))
                    } to morphemes
                }
            }
            val additionalCategoryValues = categoryValues subtract word.categoryValues
            val newCategoryValues = word.categoryValues + additionalCategoryValues

            return word.copy(syllables = prosodicSyllables, morphemes = morphemes, categoryValues = newCategoryValues)
        } else {
            return word.copy()
        }
    }

    override fun mirror() = TemplateSingleChange(
        if (position == Position.Beginning) Position.End else Position.Beginning,
        phonemeMatchers.reversed(),
        matchedPhonemesSubstitution.reversed(),
        affix.reversed()
    )

    override fun toString(): String {
        val matcherString =
            if (phonemeMatchers.isNotEmpty())
                phonemeMatchers.joinToString("")
            else
                "with any phonemes"

        return "$position $matcherString changes to ${getFullChange().joinToString("")}"
    }
}
