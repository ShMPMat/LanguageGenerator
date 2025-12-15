package io.tashtabash.lang.generator

import io.tashtabash.lang.language.category.CategorySource
import io.tashtabash.lang.language.category.definitenessName
import io.tashtabash.lang.language.category.deixisName
import io.tashtabash.lang.language.category.paradigm.WordChangeParadigm
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.lexis.SpeechPart.Noun
import io.tashtabash.lang.language.lexis.SpeechPart.PersonalPronoun
import io.tashtabash.lang.language.syntax.SyntaxLogic
import io.tashtabash.lang.language.syntax.SyntaxRelation.*
import io.tashtabash.lang.language.syntax.transformer.*
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.testProbability


class TransformerGenerator(val changeParadigm: WordChangeParadigm, val syntaxLogic: SyntaxLogic) {
    fun generateTransformers(): List<Pair<SyntaxNodeMatcher, Transformer>> = listOfNotNull(
        // Word order
        (has(SemanticsTag("trans")) + Agent.matches(of(Noun)) + Patient.matches(of(PersonalPronoun))
                to RemapOrderTransformer(mapOf(Agent to Patient, Patient to Agent)))
            .takeIf { .05.testProbability() }
        ) +
            generateDeixisSimplifier() +
            generateDefinitivenessSimplifier() +
            generateDrop()

    private fun generateDeixisSimplifier() =
        changeParadigm.speechParts
            .filter(::isNominal)
            .filter {
                changeParadigm.getSpeechPartParadigm(it)
                    .getCategoryOrNull(deixisName)
                    ?.compulsoryData
                    ?.isCompulsory == false
            }
            .map {
                of(it) + has(deixisName) + has(Possessor) to RemoveCategoryTransformer(deixisName)
            }
            .filter { .5.testProbability() }

    private fun generateDefinitivenessSimplifier() =
        changeParadigm.speechParts
            .filter(::isNominal)
            .filter {
                changeParadigm.getSpeechPartParadigm(it)
                    .getCategoryOrNull(definitenessName)
                    ?.compulsoryData
                    ?.isCompulsory == false
            }
            .map {
                of(it) + has(definitenessName) + has(Possessor) to RemoveCategoryTransformer(definitenessName)
            }
            .filter { .5.testProbability() }

    private fun generateDrop(): List<Pair<SyntaxNodeMatcher, Transformer>> {
        val transformers = mutableListOf<Pair<SyntaxNodeMatcher, Transformer>>()
        val pronounCategories = changeParadigm.getSpeechPartParadigm(PersonalPronoun.toDefault())
            .categories

        for (verbParadigm in changeParadigm.getSpeechPartParadigms(SpeechPart.Verb)) {
            val verbalCategories = verbParadigm.categories

            for (relation in resolvePossibleArguments(verbParadigm.speechPart)) {
                val roleAgreementCategories = verbalCategories
                    .filter { it.source is CategorySource.Agreement && it.source.relation == relation }
                val unrepresentedCategoriesNumber = pronounCategories.size - roleAgreementCategories.size
                // .1 at 0, .9 at +Inf; the more categories are lost with the drop, the less is the probability of it
                val dropProb = .1 + .8 / (1.0 + unrepresentedCategoriesNumber)

                dropProb.chanceOf {
                    transformers += of(verbParadigm.speechPart) + relation.matches(of(PersonalPronoun)) to
                            ChildTransformer(relation, DropTransformer)
                }
            }
        }

        return transformers
    }

    private fun resolvePossibleArguments(speechPart: TypedSpeechPart) =
        when (speechPart.subtype) {
            defaultSubtype -> listOf(Agent, Patient)
            intransitiveSubtype -> listOf(Argument)
            else -> syntaxLogic.resolvePossibleArguments(speechPart)
        }
}
