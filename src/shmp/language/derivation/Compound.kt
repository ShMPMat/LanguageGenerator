package shmp.language.derivation

import shmp.containers.SemanticsCoreTemplate
import shmp.containers.toSemanticsCore
import shmp.language.SpeechPart
import shmp.language.lexis.CompoundLink
import shmp.language.lexis.Word
import shmp.language.lexis.noCompoundLink
import shmp.language.phonology.PhonemeSequence
import shmp.random.*
import shmp.utils.joinToList
import kotlin.random.Random


class Compound(
    private val speechPart: SpeechPart,
    val infix: PhonemeSequence,
    private val categoriesChanger: CategoryChanger
) {
    fun compose(words: List<Word>, resultCore: SemanticsCoreTemplate, random: Random): Word? {
        if (resultCore.speechPart != speechPart)
            return null

        val options = chooseOptions(words, resultCore.derivationClusterTemplate.possibleCompounds)

        val chosenCompound = randomElementOrNull(options, random)?.options
            ?: return null

        val chosenWords = chosenCompound.map { randomElement(it, random) }

        val newPhonemeList = chosenWords
            .map { w -> w.syllables.flatMap { it.phonemeSequence.phonemes } }
            .joinToList(separator = infix.phonemes)
        val syllableTemplate = words[0].syllableTemplate
        val newCategories = categoriesChanger.makeStaticCategories(words.map { it.semanticsCore }, speechPart)
            ?: return null

        return syllableTemplate.createWord(
            PhonemeSequence(newPhonemeList),
            resultCore.toSemanticsCore(
                newCategories,
                random
            ).copy(
                changeHistory = CompoundHistory(this, chosenWords)
            )
        )
    }

    private fun chooseOptions(words: List<Word>, templates: List<CompoundLink>): List<CompoundOptions> =
        templates.mapNotNull { pickOptionWords(words, it) } +
                CompoundOptions(null, noCompoundLink.probability)

    private fun pickOptionWords(words: List<Word>, template: CompoundLink): CompoundOptions? = template.templates
        ?.map { t ->
            words.filter {
                it.semanticsCore.changeHistory == null &&//TODO take words with history only with some probability
                        it.semanticsCore.words.contains(t.word)
            }
        }
        ?.takeIf { o -> o.all { it.isNotEmpty() } }
        ?.let { CompoundOptions(it, template.probability) }

    override fun toString() = "Make a compound $speechPart, with infix '$infix'; $categoriesChanger"
}


private class CompoundOptions(
    val options: List<List<Word>>?,
    override val probability: Double
) : SampleSpaceObject
