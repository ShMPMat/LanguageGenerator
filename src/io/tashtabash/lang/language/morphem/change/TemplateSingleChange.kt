package io.tashtabash.lang.language.morphem.change

import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.derivation.DerivationClass
import io.tashtabash.lang.language.diachronicity.ChangingPhoneme
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.morphem.change.substitution.DeletingPhonemeSubstitution
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
                if (testFromPosition(word.toPhonemes()))
                    phonemeMatchers.size
                else null
            Position.End -> {
                val sublistStart = word.size - phonemeMatchers.size
                if (testFromPosition(word.toPhonemes())) sublistStart else null
            }
        }
    }

    override fun test(word: Word) = findGoodIndex(word) != null

    private fun getTestedPhonemes(phonemes: List<Phoneme>): List<ChangingPhoneme> = when (position) {
        Position.End -> mirror().getTestedPhonemes(phonemes.reversed())
        Position.Beginning -> {
            val wordPhonemes = phonemes.take(phonemeMatchers.size)
                .map { ChangingPhoneme.ExactPhoneme(it) }
            val missingPhonemes = (phonemes.size..phonemeMatchers.size).map { ChangingPhoneme.Boundary }

            missingPhonemes + wordPhonemes
        }
    }

    private fun testFromPosition(phonemes: List<Phoneme>) = phonemeMatchers
        .zip(getTestedPhonemes(phonemes))
        .all { (matcher, phoneme) -> matcher.match(phoneme) }

    private fun getFullChange() = when (position) {
        Position.Beginning -> affix + matchedPhonemesSubstitution
        Position.End -> matchedPhonemesSubstitution + affix
    }

    override fun change(
        word: Word,
        categoryValues: SourcedCategoryValues,
        derivationValues: List<DerivationClass>
    ): Word {
        fun Word.takeProsody(i: Int) = this.syllables
            .getOrNull(i)
            ?.prosodicEnums
            ?.toList()
            ?: listOf()

        val testResult = findGoodIndex(word)
            ?: return word.copy()

        val prosodicSyllables = when (position) {
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
                ) ?: throw ChangeException("Couldn't convert $word with change $this to word")

                noProsodyWord.mapIndexed { i, s ->
                    s.copy(prosodicEnums = word.takeProsody(i))
                }
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
                ) ?: throw ChangeException("Couldn't convert $word with change $this to a word")
                val shift = noProsodyWord.size - word.syllables.size

                noProsodyWord.mapIndexed { i, s ->
                    s.copy(prosodicEnums = word.takeProsody(i - shift))
                }
            }
        }
        val morphemes = constructMorphemes(word.morphemes, categoryValues, derivationValues)
        val additionalCategoryValues = categoryValues subtract word.categoryValues
        val newCategoryValues = word.categoryValues + additionalCategoryValues

        return word.copy(syllables = prosodicSyllables, morphemes = morphemes, categoryValues = newCategoryValues)
    }

    private fun constructMorphemes(
        morphemes: List<MorphemeData>,
        categoryValues: SourcedCategoryValues,
        derivationValues: List<DerivationClass>
    ): List<MorphemeData> {
        if (position == Position.End)
            return mirror()
                .constructMorphemes(morphemes.reversed(), categoryValues, derivationValues)
                .reversed()

        val newMorpheme = MorphemeData(affix.size, categoryValues, false, derivationValues)

        val updatedOriginalMorphemes = morphemes.toMutableList()
        var morphemeIdx = 0
        var phonemeIdx = 0
        for (substitution in matchedPhonemesSubstitution) {
            if (substitution == DeletingPhonemeSubstitution) {
                val curMorpheme = updatedOriginalMorphemes[morphemeIdx]
                if (curMorpheme.size == 0)
                    throw ChangeException("Can't decrease morpheme size for an empty morpheme")

                updatedOriginalMorphemes[morphemeIdx] = curMorpheme.copy(size = curMorpheme.size - 1)
            } else
                phonemeIdx++
            while (updatedOriginalMorphemes.getOrNull(morphemeIdx)?.size == phonemeIdx) {
                morphemeIdx++
                phonemeIdx = 0
            }
        }

        return listOf(newMorpheme) + updatedOriginalMorphemes
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
