package io.tashtabash.lang.language.derivation

import io.tashtabash.lang.containers.SemanticsCoreTemplate
import io.tashtabash.lang.containers.toSemanticsCore
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.phonology.PhonemeSequence
import io.tashtabash.lang.language.phonology.Syllable
import io.tashtabash.lang.language.phonology.Syllables
import io.tashtabash.random.*
import io.tashtabash.lang.utils.joinToList
import io.tashtabash.random.singleton.chanceOfNot
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.random.singleton.testProbability
import kotlin.random.Random


data class Compound(
    private val speechPart: TypedSpeechPart,
    val infix: PhonemeSequence,
    private val categoriesChanger: CategoryChanger,
    private val prosodyRule: CompoundProsodyRule
) {
    fun compose(words: List<Word>, resultCore: SemanticsCoreTemplate, random: Random): Word? {
        if (resultCore.speechPart != speechPart.type)
            return null

        val existingDoubles = words
            .map { getMeaningDistance(it.semanticsCore.meaningCluster, resultCore.word) }
            .foldRight(0.0, Double::plus)
        (1.0 / (existingDoubles + 1)).chanceOfNot {
            return null
        }

        val options = chooseOptions(words, resultCore.derivationClusterTemplate.possibleCompounds)

        val chosenCompound = options.randomElementOrNull()?.options
            ?: return null

        val chosenWords = chosenCompound.map { it.randomElement() }

        val newPhonemeList = chosenWords
            .map { w -> w.syllables.flatMap { it.phonemes.phonemes } }
            .joinToList(separator = infix.phonemes)
        val syllableTemplate = words[0].syllableTemplate
        val newCategories = categoriesChanger.makeStaticCategories(words.map { it.semanticsCore }, speechPart)
            ?: return null

        val syllables = syllableTemplate.splitOnSyllables(PhonemeSequence(newPhonemeList))
            ?: return null

        return Word(
            putProsodies(
                syllables,
                chosenWords,
                if (infix.size == 0 || syllableTemplate.splitOnSyllables(infix) == null) 0 else 1
            ),
            syllableTemplate,
            resultCore.toSemanticsCore(newCategories)
                .copy(changeHistory = CompoundHistory(this, chosenWords))
        )
    }

    private fun chooseOptions(words: List<Word>, templates: List<CompoundLink>): List<CompoundOptions> =
        templates.mapNotNull { pickOptionWords(words, it) } +
                CompoundOptions(null, noCompoundLink.probability)

    private fun pickOptionWords(
        words: List<Word>,
        template: CompoundLink
    ): CompoundOptions? = template.templates
        ?.map { t ->
            words.filter {//TODO generate probability test
                (1 / (it.semanticsCore.changeDepth + 1.0)).testProbability()
                        && it.semanticsCore.meaningCluster.contains(t)
            }
        }
        ?.takeIf { o -> o.all { it.isNotEmpty() } }
        ?.let { CompoundOptions(it, template.probability) }

    private fun putProsodies(syllables: Syllables, sourceWords: List<Word>, wordGap: Int): Syllables {
        val prosodySyllables = mutableListOf<Syllable>()
        var syllableInd = 0

        for ((wordInd, word) in sourceWords.withIndex()) {
            for (syllable in word.syllables) {
                prosodySyllables += syllables[syllableInd].copy(
                    prosodicEnums = prosodyRule.changeProsody(wordInd, syllable.prosodicEnums)
                )

                syllableInd++
            }

            if (syllables.size != syllableInd) {
                prosodySyllables += syllables.drop(syllableInd).take(wordGap)
                syllableInd += wordGap
            }
        }

        return prosodySyllables
    }

    override fun toString() =
        "Make a compound $speechPart, with infix '$infix'; $categoriesChanger; $prosodyRule"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Compound) return false

        if (toString() != other.toString()) return false

        return true
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }
}


private class CompoundOptions(
    val options: List<List<Word>>?,
    override val probability: Double
) : SampleSpaceObject
