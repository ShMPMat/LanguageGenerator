package shmp.lang.language.derivation

import shmp.lang.containers.SemanticsCoreTemplate
import shmp.lang.containers.toSemanticsCore
import shmp.lang.language.SpeechPart
import shmp.lang.language.lexis.CompoundLink
import shmp.lang.language.lexis.Word
import shmp.lang.language.lexis.getMeaningDistance
import shmp.lang.language.lexis.noCompoundLink
import shmp.lang.language.phonology.PhonemeSequence
import shmp.lang.language.phonology.Syllable
import shmp.random.*
import shmp.lang.utils.joinToList
import kotlin.random.Random


class Compound(
    private val speechPart: SpeechPart,
    val infix: PhonemeSequence,
    private val categoriesChanger: CategoryChanger,
    private val prosodyRule: CompoundProsodyRule
) {
    fun compose(words: List<Word>, resultCore: SemanticsCoreTemplate, random: Random): Word? {
        if (resultCore.speechPart != speechPart)
            return null

        val existingDoubles = words
            .map { getMeaningDistance(it.semanticsCore.meaningCluster, resultCore.word) }
            .foldRight(0.0, Double::plus)
        if (!testProbability(1.0 / (existingDoubles + 1), random))
            return null

        val options = chooseOptions(words, resultCore.derivationClusterTemplate.possibleCompounds, random)

        val chosenCompound = randomElementOrNull(options, random)?.options
            ?: return null

        val chosenWords = chosenCompound.map { randomElement(it, random) }

        val newPhonemeList = chosenWords
            .map { w -> w.syllables.flatMap { it.phonemeSequence.phonemes } }
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
            resultCore.toSemanticsCore(newCategories, random)
                .copy(changeHistory = CompoundHistory(this, chosenWords))
        )
    }

    private fun chooseOptions(words: List<Word>, templates: List<CompoundLink>, random: Random): List<CompoundOptions> =
        templates.mapNotNull { pickOptionWords(words, it, random) } +
                CompoundOptions(null, noCompoundLink.probability)

    private fun pickOptionWords(
        words: List<Word>,
        template: CompoundLink,
        random: Random
    ): CompoundOptions? = template.templates
        ?.map { t ->
            words.filter {//TODO generate probability test
                testProbability(1 / (it.semanticsCore.changeDepth.toDouble() + 1), random) &&
                        it.semanticsCore.meaningCluster.contains(t.word)
            }
        }
        ?.takeIf { o -> o.all { it.isNotEmpty() } }
        ?.let { CompoundOptions(it, template.probability) }

    private fun putProsodies(syllables: List<Syllable>, sourceWords: List<Word>, wordGap: Int): List<Syllable> {
        val prosodySyllables = mutableListOf<Syllable>()
        var syllableInd = 0

        for ((wordInd, word) in sourceWords.withIndex()) {
            for (syllable in word.syllables) {
                prosodySyllables.add(syllables[syllableInd].copy(
                    prosodicEnums = prosodyRule.changeProsody(wordInd, syllable.prosodicEnums))
                )

                syllableInd++
            }

            if (syllables.size != syllableInd) {
                prosodySyllables.addAll(syllables.drop(syllableInd).take(wordGap))
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
