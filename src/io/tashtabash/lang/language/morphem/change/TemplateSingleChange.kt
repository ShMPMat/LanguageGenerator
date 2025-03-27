package io.tashtabash.lang.language.morphem.change

import io.tashtabash.lang.containers.NoPhonemeException
import io.tashtabash.lang.language.category.paradigm.SourcedCategoryValues
import io.tashtabash.lang.language.derivation.DerivationClass
import io.tashtabash.lang.language.diachronicity.*
import io.tashtabash.lang.language.lexis.Word
import io.tashtabash.lang.language.morphem.MorphemeData
import io.tashtabash.lang.language.morphem.change.substitution.DeletingPhonemeSubstitution
import io.tashtabash.lang.language.morphem.change.substitution.EpenthesisSubstitution
import io.tashtabash.lang.language.phonology.PhonemeSequence
import io.tashtabash.lang.language.phonology.Syllable
import io.tashtabash.lang.language.phonology.Syllables


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

    override fun toString(): String {
        return rule.toString()
    }
}
