package io.tashtabash.lang.language.morphem.change

import io.tashtabash.lang.containers.NoPhonemeException
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.derivation.DerivationClass
import io.tashtabash.lang.language.diachronicity.ChangingPhoneme
import io.tashtabash.lang.language.diachronicity.getChangingPhonemes
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
    fun findGoodIndex(word: Word): Int? =
        findGoodIndex(getChangingPhonemes(word, addStartBoundary = false, addEndBoundary = false))

    fun findGoodIndex(phonemes: List<ChangingPhoneme>): Int? = when (position) {
        Position.End -> mirror().findGoodIndex(phonemes.reversed())
            ?.let { phonemes.size - it }
        Position.Beginning -> {
            val isMatching = phonemeMatchers
                .zip(getBeginningTestedPhonemes(phonemes))
                .all { (matcher, phoneme) -> matcher.match(phoneme) }

            if (isMatching)
                phonemeMatchers.size
            else null
        }
    }

    private fun getBeginningTestedPhonemes(phonemes: List<ChangingPhoneme>): List<ChangingPhoneme> {
        val wordPhonemes = phonemes.take(phonemeMatchers.size)
        val missingPhonemes = (wordPhonemes.size until phonemeMatchers.size).map { ChangingPhoneme.Boundary }

        return wordPhonemes + missingPhonemes
    }

    override fun test(word: Word) = findGoodIndex(word) != null

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
            ?.prosody
            ?.toList()
            ?: listOf()

        val testResult = findGoodIndex(word)
            ?: return word.copy()

        try {
            val prosodicSyllables = when (position) {
                Position.End -> {
                    val change: List<Phoneme> = getFullChange()
                        .zip(testResult until testResult + matchedPhonemesSubstitution.size + affix.size)
                        .mapNotNull { (substitution, i) -> substitution.substitute(word.getOrNull(i)) }
                    val noProsodyWord = word.syllableTemplate.splitOnSyllables(
                        PhonemeSequence(
                            word.toPhonemes().dropLast(phonemeMatchers.size) + change
                        )
                    ) ?: throw ChangeException("Couldn't convert $word with change $this to word")

                    noProsodyWord.mapIndexed { i, s ->
                        s.copy(prosody = word.takeProsody(i))
                    }
                }
                Position.Beginning -> {
                    val change: List<Phoneme> = getFullChange()
                        .zip(testResult - matchedPhonemesSubstitution.size - affix.size until testResult)
                        .mapNotNull { (substitution, i) -> substitution.substitute(word.getOrNull(i)) }
                    val noProsodyWord = word.syllableTemplate.splitOnSyllables(
                        PhonemeSequence(
                            change + word.toPhonemes().drop(phonemeMatchers.size)
                        )
                    ) ?: throw ChangeException("Couldn't convert $word with change $this to a word")
                    val shift = noProsodyWord.size - word.syllables.size

                    noProsodyWord.mapIndexed { i, s ->
                        s.copy(prosody = word.takeProsody(i - shift))
                    }
                }
            }
            val morphemes = constructMorphemes(word.morphemes, categoryValues, derivationValues)
            val additionalCategoryValues = categoryValues subtract word.categoryValues
            val newCategoryValues = word.categoryValues + additionalCategoryValues

            return word.copy(syllables = prosodicSyllables, morphemes = morphemes, categoryValues = newCategoryValues)
        } catch (e: NoPhonemeException) {
            throw ChangeException("Can't apply $this to $word: ${e.message}")
        }
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
