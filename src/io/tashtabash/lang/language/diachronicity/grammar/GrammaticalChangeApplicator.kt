package io.tashtabash.lang.language.diachronicity.grammar

import io.tashtabash.lang.language.Language
import io.tashtabash.lang.language.syntax.ChangeParadigm
import io.tashtabash.lang.language.syntax.transformer.ChangeOrderTransformer
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.randomElement
import kotlin.math.pow


class RandomGrammaticalChangeApplicator(
    val possibleRules: List<GrammaticalRule> = listOf(SimplifySandhi, UnifySvoOrder)
) {
    private val _messages = mutableListOf<String>()
    val messages: List<String>
        get() = _messages

    fun apply(language: Language): Language {
        val applicator = GrammaticalChangeApplicator()

        return applicator.apply(language, possibleRules.randomElement())
            .also { _messages += applicator.messages }
    }
}


class GrammaticalChangeApplicator {
    private val _messages = mutableListOf<String>()
    val messages: List<String>
        get() = _messages

    fun apply(language: Language, rule: GrammaticalRule): Language {
        val (changeParadigm, message) = rule.apply(language.changeParadigm)
        _messages += message

        return language.copy(
            changeParadigm = changeParadigm ?: language.changeParadigm
        )
    }
}

interface GrammaticalRule {
    fun apply(changeParadigm: ChangeParadigm): ChangeResult
}

data class ChangeResult(val changeParadigm: ChangeParadigm?, val message: String)

infix fun ChangeParadigm?.withMessage(message: String) = ChangeResult(this, message)

const val MAX_RULES = 50


object SimplifySandhi: GrammaticalRule {
    override fun apply(changeParadigm: ChangeParadigm): ChangeResult {
        val sandhiSize = changeParadigm.wordChangeParadigm.sandhiRules.size.toDouble()

        (sandhiSize / MAX_RULES).chanceOf {
            val index = changeParadigm.wordChangeParadigm.sandhiRules
                .indices
                .toList()
                .randomElement { 1.0 / it.toDouble().pow(.5) } // Drop sandhi rules w/ bias towards the older ones

            return changeParadigm.copy(
                wordChangeParadigm = changeParadigm.wordChangeParadigm.copy(
                    sandhiRules = changeParadigm.wordChangeParadigm.sandhiRules.filterIndexed { i, _ -> i != index }
                )
            ) withMessage "Drop sandhi rule ${changeParadigm.wordChangeParadigm.sandhiRules[index]} ($index)"
        }

        return null withMessage "No Sandhi change happened"
    }
}

object UnifySvoOrder: GrammaticalRule {
    override fun apply(changeParadigm: ChangeParadigm): ChangeResult {
        val transformers = changeParadigm.syntaxLogic.transformers
        val ids = transformers.indices.filter { transformers[it].second is ChangeOrderTransformer }
        if (ids.isEmpty())
            return null withMessage "The SOV word order is already unified"

        (ids.size / 30.0).chanceOf {
            val removedIdx = ids.randomElement()

            return changeParadigm.copy(
                syntaxLogic = changeParadigm.syntaxLogic.copy(
                    transformers = transformers.filterIndexed { i, _ -> i != removedIdx }
                )
            ) withMessage "Simplified SOV order: removed ${transformers[removedIdx]}"
        }

        return null withMessage "No SOV change happened"
    }
}
