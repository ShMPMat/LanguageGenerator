package shmp.language.derivation

import shmp.containers.SemanticsCoreTemplate
import shmp.containers.toSemanticsCore
import shmp.language.SpeechPart
import shmp.language.lexis.CompositionLink
import shmp.language.lexis.Word
import shmp.language.phonology.PhonemeSequence
import shmp.random.*
import shmp.utils.joinToList
import kotlin.random.Random


class Compound(
    private val speechPart: SpeechPart,
    private val infix: PhonemeSequence,
    private val categoriesChanger: CategoryChanger
) {
    fun compose(words: List<Word>, resultCore: SemanticsCoreTemplate, random: Random): Word? {
        if (resultCore.speechPart != speechPart)
            return null

        val options = chooseOptions(words, resultCore.derivationClusterTemplate.possibleCompositions)

        val chosenComposition = randomUnwrappedElementOrNull(options, random)
            ?: return null

        val chosenWords = chosenComposition.map { randomElement(it, random) }

        val newPhonemeList = chosenWords
            .map { w -> w.syllables.flatMap { it.phonemeSequence.phonemes } }
            .joinToList(separator = infix.phonemes)
        val syllableTemplate = words[0].syllableTemplate

        return syllableTemplate.createWord(
            PhonemeSequence(newPhonemeList),
            resultCore.toSemanticsCore(
                categoriesChanger.getNewStaticCategories(words.map { it.semanticsCore }),
                random
            )
        )
    }

    private fun chooseOptions(words: List<Word>, templates: List<CompositionLink>): List<CompoundOptions> =
        templates.mapNotNull { pickOptionWords(words, it) }

    private fun pickOptionWords(words: List<Word>, template: CompositionLink): CompoundOptions? = template.templates
        ?.map { t ->
            words.filter {
                it.semanticsCore.derivationHistory == null &&//TODO take words with history only with some probability
                        it.semanticsCore.words.contains(t.word)
            }
        }
        ?.takeIf { o -> o.all { it.isNotEmpty() } }
        ?.let { CompoundOptions(it, template.probability) }

    override fun toString() = "Make a compound $speechPart, with infix '$infix'; $categoriesChanger"
}


private class CompoundOptions(
    options: List<List<Word>>,
    override val probability: Double
) : UnwrappableSSO<List<List<Word>>>(options)
