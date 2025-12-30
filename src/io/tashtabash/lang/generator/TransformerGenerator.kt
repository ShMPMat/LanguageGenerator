package io.tashtabash.lang.generator

import io.tashtabash.lang.language.category.*
import io.tashtabash.lang.language.category.paradigm.WordChangeParadigm
import io.tashtabash.lang.language.lexis.*
import io.tashtabash.lang.language.lexis.SpeechPart.*
import io.tashtabash.lang.language.lexis.SpeechPart.PersonalPronoun
import io.tashtabash.lang.language.syntax.SyntaxLogic
import io.tashtabash.lang.language.syntax.SyntaxRelation.*
import io.tashtabash.lang.language.syntax.clause.syntax.SyntaxNodeTag
import io.tashtabash.lang.language.syntax.transformer.*
import io.tashtabash.lang.utils.thenTake
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.testProbability


class TransformerGenerator(val changeParadigm: WordChangeParadigm, val syntaxLogic: SyntaxLogic) {
    fun generateTransformers(): List<Pair<SyntaxNodeMatcher, Transformer>> = listOfNotNull(
        // Word order
        .05.testProbability() thenTake {
            has(SemanticsTag("trans")) + Agent.matches(of(Noun)) + Patient.matches(of(PersonalPronoun)) then {
                RemapOrderTransformer(mapOf(Agent to Patient, Patient to Agent))
            }
        }
    ) +
            generateTopicMarking() +
            generateDeixisSimplifier() +
            generateDefinitivenessSimplifier() +
            generateDrop() +
            generateTopicMovement()

    private fun generateTopicMovement() = listOfNotNull(
        .9.testProbability() thenTake {
            has(SyntaxNodeTag.Topic) then {
                PutFirstTransformer(Predicate) // Assume all topics are children of Predicates
            }
        }
    )

    // The "subject is marked as a topic" rule is generated for all speech parts if at all
    // Since there are no relative clauses now, the rule doesn't check if the verb is of the main clause
    private fun generateTopicMarking(): List<Pair<SyntaxNodeMatcher, Transformer>> {
        // Simply take all nominals, no asymmetries
        val affectedNominals = changeParadigm.speechParts
            .filter(::isNominal)

        return listOfNotNull(
            // Create a rule only for simple governance systems where all nominals get the same case
            syntaxLogic.resolveVerbCase(Verb.toDefault(), Agent).contains(CaseValue.Nominative) thenTake {
                of(Verb) + Agent.matches(of(affectedNominals)) then {
                    transform(Agent) {
                        remove(caseName, adpositionName) + add(Topic) + add(SyntaxNodeTag.Topic)
                    }
                }
            },
            syntaxLogic.resolveVerbCase(Verb.toDefault(), Patient).contains(CaseValue.Absolutive) thenTake {
                of(Verb) + Patient.matches(of(affectedNominals)) then {
                    transform(Patient) {
                        remove(caseName, adpositionName) + add(Topic) + add(SyntaxNodeTag.Topic)
                    }
                }
            },
            of(Verb) + Argument.matches(of(affectedNominals)) then {
                transform(Argument) {
                    remove(caseName, adpositionName) + add(Topic) + add(SyntaxNodeTag.Topic)
                }
            }
        )
            .takeIf { .8.testProbability() }
            ?: listOf()
    }

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
                of(it) + has(deixisName) + has(Possessor) then {
                    RemoveCategoryTransformer(deixisName)
                }
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
                of(it) + has(definitenessName) + has(Possessor) then {
                    RemoveCategoryTransformer(definitenessName)
                }
            }
            .filter { .5.testProbability() }

    private fun generateDrop(): List<Pair<SyntaxNodeMatcher, Transformer>> {
        val transformers = mutableListOf<Pair<SyntaxNodeMatcher, Transformer>>()
        val pronounCategories = changeParadigm.getSpeechPartParadigm(PersonalPronoun.toDefault())
            .categories

        for (verbParadigm in changeParadigm.getSpeechPartParadigms(Verb)) {
            val verbalCategories = verbParadigm.categories

            for (relation in resolvePossibleArguments(verbParadigm.speechPart)) {
                val roleAgreementCategories = verbalCategories
                    .filter { it.source is CategorySource.Agreement && it.source.relation == relation }
                val unrepresentedCategoriesNumber = pronounCategories.size - roleAgreementCategories.size
                // .1 at 0, .9 at +Inf; the more categories are lost with the drop, the less is the probability of it
                val dropProb = .1 + .8 / (1.0 + unrepresentedCategoriesNumber)

                dropProb.chanceOf {
                    transformers += of(verbParadigm.speechPart) + relation.matches(of(PersonalPronoun)) then {
                        RelationTransformer(relation, DropTransformer)
                    }
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


private infix fun SyntaxNodeMatcher.then(expr: () -> Transformer): Pair<SyntaxNodeMatcher, Transformer> =
    this to expr()
