package io.tashtabash.lang.language.morphem.change

import io.tashtabash.lang.containers.NoPhonemeException
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.derivation.DerivationClass
import io.tashtabash.lang.language.diachronicity.*
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.morphem.change.substitution.DeletingPhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.EpenthesisSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.PhonemeSubstitution
import io.tashtabash.lang.language.phonology.PhonemeSequence
import io.tashtabash.lang.language.phonology.Syllable
import io.tashtabash.lang.language.phonology.Syllables
import io.tashtabash.lang.language.phonology.matcher.BorderPhonemeMatcher
import io.tashtabash.lang.language.phonology.matcher.PassingPhonemeMatcher


data class TemplateSingleChange(
    override val position: Position,
    val rule: PhonologicalRule
) : TemplateChange() {
    override fun change(
        word: Word,
        categoryValues: SourcedCategoryValues,
        derivationValues: List<DerivationClass>
    ): Word {
        try {
            val changingPhonemes = getChangingPhonemes(word, addStartBoundary = true, addEndBoundary = true)
            val changedPhonemes = changePhonemes(changingPhonemes)
                ?: return word.copy()
            val noProsodySyllables = word.syllableTemplate.splitOnSyllables(changedPhonemes)
                ?: throw ChangeException("Couldn't convert $word with change $this to a word")

            val prosodicSyllables = injectProsody(noProsodySyllables, word.syllables)
            val morphemes = constructMorphemes(word.morphemes, categoryValues, derivationValues)
            val additionalCategoryValues = categoryValues subtract word.categoryValues
            val newCategoryValues = word.categoryValues + additionalCategoryValues

            return word.copy(syllables = prosodicSyllables, morphemes = morphemes, categoryValues = newCategoryValues)
        } catch (e: NoPhonemeException) {
            throw ChangeException("Can't apply $this to $word: ${e.message}")
        }
    }

    private fun injectProsody(noProsodySyllables: Syllables, syllables: Syllables): List<Syllable> {
        if (position == Position.End)
            return mirror()
                .injectProsody(noProsodySyllables.reversed(), syllables.reversed())
                .reversed()

        val shift = noProsodySyllables.size - syllables.size
        return noProsodySyllables.mapIndexed { i, s ->
            s.copy(prosody = syllables.getOrNull(i - shift)?.prosody ?: listOf())
        }
    }

    private fun changePhonemes(phonemes: List<ChangingPhoneme>): PhonemeSequence? {
        return PhonologicalRuleApplicator().applyPhonologicalRule(phonemes, rule)
            .takeIf { it != phonemes }
            ?.let { clearChangingPhonemes(it) }
            ?.let { PhonemeSequence(it) }
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

        val affixSize = rule.substitutions.takeWhile { it is EpenthesisSubstitution }.size
        val newMorpheme = MorphemeData(affixSize, categoryValues, false, derivationValues)

        val updatedOriginalMorphemes = morphemes.toMutableList()
        var morphemeIdx = 0
        var phonemeIdx = 0
        for (substitution in rule.substitutions.dropWhile { it is EpenthesisSubstitution }) {
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
        rule.mirror()
    )

    override fun toString(): String =
        if (rule.precedingMatchers == listOf(BorderPhonemeMatcher))
            printStemMatcher() + printSubstitutions(rule.substitutions) +
                    printPassingFilling(rule.followingMatchers.size) +
                    "-"
        else if (rule.followingMatchers == listOf(BorderPhonemeMatcher))
            printStemMatcher() + "-" +
                    printPassingFilling(rule.followingMatchers.size) +
                    printSubstitutions(rule.substitutions)
        else
            rule.toString()

    private fun printStemMatcher(): String =
        if (rule.precedingMatchers == listOf(BorderPhonemeMatcher) && rule.matchers.size > 1)
            rule.matchers.drop(1).joinToString("") + "- -> "
        else if (rule.followingMatchers == listOf(BorderPhonemeMatcher) && rule.matchers.size > 1)
            "-" + rule.matchers.dropLast(1).joinToString("") + " -> "
        else
            ""

    /**
     * Strips brackets from epenthesis for clarity
     */
    private fun printSubstitutions(substitutions: List<PhonemeSubstitution>): String =
        substitutions.joinToString("")
            .replace(Regex("\\(.\\)")) { it.value.drop(1).dropLast(1) }

    private fun printPassingFilling(size: Int): String =
        (1..size).joinToString("") { PassingPhonemeMatcher.toString() }
}
