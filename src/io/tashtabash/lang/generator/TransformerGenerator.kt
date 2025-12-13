package io.tashtabash.lang.generator

import io.tashtabash.lang.language.category.CategorySource
import io.tashtabash.lang.language.category.paradigm.WordChangeParadigm
import io.tashtabash.lang.language.lexis.SpeechPart
import io.tashtabash.lang.language.lexis.SpeechPart.Noun
import io.tashtabash.lang.language.lexis.SpeechPart.PersonalPronoun
import io.tashtabash.lang.language.lexis.toDefault
import io.tashtabash.lang.language.syntax.SyntaxRelation.*
import io.tashtabash.lang.language.syntax.clause.description.MainObjectType
import io.tashtabash.lang.language.syntax.transformer.*
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.testProbability


class TransformerGenerator(val changeParadigm: WordChangeParadigm) {
    fun generateTransformers(): List<Pair<SyntaxNodeMatcher, Transformer>> = listOfNotNull(
        // Word order
        (has("trans") + Agent.matches(of(Noun)) + Patient.matches(of(PersonalPronoun))
                to RemapOrderTransformer(mapOf(Agent to Patient, Patient to Agent)))
            .takeIf { 0.05.testProbability() }
        ) +
            generateDropTransformers()

    private fun generateDropTransformers(): List<Pair<SyntaxNodeMatcher, Transformer>> {
        val pronounCategories = changeParadigm.getSpeechPartParadigm(PersonalPronoun.toDefault())
            .categories
        val transformers = mutableListOf<Pair<SyntaxNodeMatcher, Transformer>>()

        for (verbParadigm in changeParadigm.getSpeechPartParadigms(SpeechPart.Verb)) {
            val verbalCategories = verbParadigm.categories

            for (relation in MainObjectType.syntaxRelations) {
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
}
